package com.jerson.hcdc_portal.domain.resolver

import com.jerson.hcdc_portal.domain.model.CanonicalProgram
import com.jerson.hcdc_portal.domain.model.ProspectusVersion
import com.jerson.hcdc_portal.util.Constants
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProspectusResolver @Inject constructor(
    private val programResolver: ProgramResolver
) {

    /**
     * Resolves the student course to the full prospectus URL and UUID.
     * e.g. "https://studentportal.hcdc.edu.ph/prospectus/{uuid}/1"
     */
    fun resolveProspectusUrl(rawStudentProgram: String?, preferredAy: String? = null): ProspectusUrlResolution? {
        val program = programResolver.resolve(rawStudentProgram) ?: return null
        val version = selectVersion(program, preferredAy) ?: return null

        val url = "${Constants.baseUrl}/prospectus/{${version.prospectusUuid}}/${version.versionNumber}"
        return ProspectusUrlResolution(
            program = program,
            selectedVersion = version,
            endpointUrl = url
        )
    }

    private fun selectVersion(program: CanonicalProgram, preferredAy: String?): ProspectusVersion? {
        if (program.versions.isEmpty()) return null
        if (!preferredAy.isNullOrBlank()) {
            val matchingAy = program.versions.firstOrNull { it.academicYear.equals(preferredAy, ignoreCase = true) }
            if (matchingAy != null) return matchingAy
        }
        // Fallback to default or first
        return program.versions.firstOrNull { it.isDefault } ?: program.versions.first()
    }
}

data class ProspectusUrlResolution(
    val program: CanonicalProgram,
    val selectedVersion: ProspectusVersion,
    val endpointUrl: String
)
