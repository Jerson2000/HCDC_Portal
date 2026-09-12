package com.jerson.hcdc_portal.data.repository

import com.jerson.hcdc_portal.data.local.dao.ProspectusDao
import com.jerson.hcdc_portal.data.parser.ProspectusParser
import com.jerson.hcdc_portal.domain.model.CanonicalProgram
import com.jerson.hcdc_portal.domain.model.Prospectus
import com.jerson.hcdc_portal.domain.model.ProspectusEntity
import com.jerson.hcdc_portal.domain.model.ProspectusMetadata
import com.jerson.hcdc_portal.domain.model.ProspectusSubject
import com.jerson.hcdc_portal.domain.model.ProspectusSubjectEntity
import com.jerson.hcdc_portal.domain.model.SemesterCurriculum
import com.jerson.hcdc_portal.domain.model.YearLevelCurriculum
import com.jerson.hcdc_portal.domain.repository.ProspectusRepository
import com.jerson.hcdc_portal.domain.resolver.ProgramResolver
import com.jerson.hcdc_portal.domain.resolver.ProspectusResolver
import com.jerson.hcdc_portal.util.Constants
import com.jerson.hcdc_portal.util.Resource
import com.jerson.hcdc_portal.util.await
import com.jerson.hcdc_portal.util.getRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProspectusRepositoryImpl @Inject constructor(
    private val client: OkHttpClient,
    private val dao: ProspectusDao,
    private val programResolver: ProgramResolver,
    private val prospectusResolver: ProspectusResolver,
    private val parser: ProspectusParser
) : ProspectusRepository {

    override fun getCanonicalPrograms(): List<CanonicalProgram> {
        // Returns the currently resolved program catalog
        return listOf()
    }

    override fun syncCoursesCatalog(): Flow<Resource<List<CanonicalProgram>>> = channelFlow {
        try {
            withContext(Dispatchers.IO) {
                send(Resource.Loading())
                val url = "${Constants.baseUrl}/courses"
                val response = client.newCall(getRequest(url)).await()

                response.use { res ->
                    if (res.isSuccessful) {
                        val html = res.body?.string().orEmpty()
                        val parsed = parser.parseCoursesCatalog(html)
                        if (parsed.isNotEmpty()) {
                            programResolver.updateCatalog(parsed)
                        }
                        send(Resource.Success(parsed))
                    } else {
                        send(Resource.Error("Failed to fetch /courses: ${res.code}"))
                    }
                }
            }
        } catch (e: Exception) {
            send(Resource.Error(e.message ?: "Failed to sync courses catalog"))
        }
    }

    override fun getProspectus(rawStudentProgram: String?, forceRefresh: Boolean): Flow<Resource<Prospectus>> = channelFlow {
        try {
            withContext(Dispatchers.IO) {
                send(Resource.Loading())

                // 1. Resolve program
                val resolution = prospectusResolver.resolveProspectusUrl(rawStudentProgram)
                if (resolution == null) {
                    send(Resource.Error("Could not resolve program: '$rawStudentProgram'. Please verify program designation."))
                    return@withContext
                }

                val programCode = resolution.program.code
                val prospectusId = "${programCode}_${resolution.selectedVersion.academicYear.ifBlank { "LATEST" }}"

                // 2. Check local database cache
                val cachedMeta = dao.getProspectusMetaByProgram(programCode).firstOrNull()
                var cachedProspectus: Prospectus? = null

                if (cachedMeta != null) {
                    val cachedSubjectEntities = dao.getProspectusSubjects(cachedMeta.id).firstOrNull().orEmpty()
                    if (cachedSubjectEntities.isNotEmpty()) {
                        cachedProspectus = buildProspectusFromEntities(cachedMeta, cachedSubjectEntities)
                        send(Resource.Success(cachedProspectus))
                    }
                }

                // If not forcing refresh and cached exists, we are done
                if (!forceRefresh && cachedProspectus != null) {
                    return@withContext
                }

                // 3. Network fetch online
                try {
                    val response = client.newCall(getRequest(resolution.endpointUrl)).await()
                    response.use { res ->
                        if (res.isSuccessful) {
                            val html = res.body?.string().orEmpty()
                            val parsed = parser.parseProspectusHtml(html, fallbackProgramCode = programCode)

                            // Save to Room DB
                            val metaEntity = ProspectusEntity(
                                id = prospectusId,
                                programCode = parsed.metadata.programCode.ifBlank { programCode },
                                programName = parsed.metadata.programName.ifBlank { resolution.program.canonicalName },
                                revision = parsed.metadata.revision,
                                effectiveAy = parsed.metadata.effectiveAy.ifBlank { resolution.selectedVersion.academicYear },
                                totalUnits = parsed.totalCurriculumUnits,
                                lastSyncedTimestamp = System.currentTimeMillis()
                            )

                            val subjectEntities = mutableListOf<ProspectusSubjectEntity>()
                            parsed.yearLevels.forEachIndexed { yIndex, year ->
                                year.semesters.forEachIndexed { sIndex, sem ->
                                    sem.subjects.forEach { subj ->
                                        subjectEntities.add(
                                            ProspectusSubjectEntity(
                                                prospectusId = prospectusId,
                                                yearLevelName = year.yearLevelName,
                                                yearLevelOrder = yIndex,
                                                semesterName = sem.semesterName,
                                                semesterOrder = sIndex,
                                                subjectCode = subj.code,
                                                subjectName = subj.name,
                                                units = subj.units,
                                                preRequisites = subj.preRequisites,
                                                totalUnitsDisplay = sem.totalUnitsDisplay
                                            )
                                        )
                                    }
                                }
                            }

                            dao.saveFullProspectus(metaEntity, subjectEntities)

                            // Emit updated prospectus with isOfflineCached = false (freshly synced)
                            val freshProspectus = parsed.copy(
                                metadata = parsed.metadata.copy(
                                    isOfflineCached = false,
                                    lastSyncedTimestamp = System.currentTimeMillis()
                                )
                            )
                            send(Resource.Success(freshProspectus))
                        } else {
                            if (cachedProspectus == null) {
                                send(Resource.Error("Failed to fetch prospectus (${res.code}): ${res.message}"))
                            }
                        }
                    }
                } catch (netEx: Exception) {
                    // If network fails but we had cached data, the cached data was already emitted above.
                    if (cachedProspectus == null) {
                        send(Resource.Error(netEx.message ?: "Network error loading prospectus"))
                    }
                }
            }
        } catch (e: Exception) {
            send(Resource.Error(e.message ?: "Unexpected error loading prospectus"))
        }
    }

    private fun buildProspectusFromEntities(
        meta: ProspectusEntity,
        subjectEntities: List<ProspectusSubjectEntity>
    ): Prospectus {
        val yearMap = linkedMapOf<String, LinkedHashMap<String, MutableList<ProspectusSubject>>>()
        val totalsMap = mutableMapOf<String, String>()

        for (e in subjectEntities) {
            val semMap = yearMap.getOrPut(e.yearLevelName) { linkedMapOf() }
            val list = semMap.getOrPut(e.semesterName) { mutableListOf() }
            list.add(
                ProspectusSubject(
                    code = e.subjectCode,
                    name = e.subjectName,
                    units = e.units,
                    preRequisites = e.preRequisites,
                    isLab = e.subjectName.contains("Lab", ignoreCase = true) || e.subjectCode.endsWith("L", ignoreCase = true),
                    isReligious = e.subjectCode.startsWith("REED", ignoreCase = true) || e.subjectCode.startsWith("THEO", ignoreCase = true)
                )
            )
            totalsMap["${e.yearLevelName}-${e.semesterName}"] = e.totalUnitsDisplay
        }

        val yearLevels = yearMap.map { (yName, semMap) ->
            val sems = semMap.map { (sName, subjects) ->
                SemesterCurriculum(
                    semesterName = sName,
                    subjects = subjects,
                    totalUnitsDisplay = totalsMap["$yName-$sName"] ?: "${subjects.sumOf { it.units.toDoubleOrNull() ?: 0.0 }}"
                )
            }
            YearLevelCurriculum(
                yearLevelName = yName,
                semesters = sems
            )
        }

        return Prospectus(
            metadata = ProspectusMetadata(
                programCode = meta.programCode,
                programName = meta.programName,
                revision = meta.revision,
                effectiveAy = meta.effectiveAy,
                isOfflineCached = true,
                lastSyncedTimestamp = meta.lastSyncedTimestamp
            ),
            yearLevels = yearLevels,
            totalCurriculumUnits = meta.totalUnits
        )
    }
}
