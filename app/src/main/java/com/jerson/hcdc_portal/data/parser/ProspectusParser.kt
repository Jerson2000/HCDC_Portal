package com.jerson.hcdc_portal.data.parser

import com.jerson.hcdc_portal.domain.model.CanonicalProgram
import com.jerson.hcdc_portal.domain.model.Prospectus
import com.jerson.hcdc_portal.domain.model.ProspectusMetadata
import com.jerson.hcdc_portal.domain.model.ProspectusSubject
import com.jerson.hcdc_portal.domain.model.ProspectusVersion
import com.jerson.hcdc_portal.domain.model.SemesterCurriculum
import com.jerson.hcdc_portal.domain.model.YearLevelCurriculum
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProspectusParser @Inject constructor() {

    /**
     * Parses the /courses page HTML to extract all programs and their prospectus links.
     * Maps table rows containing [Program, Prospectus Link] into a list of [CanonicalProgram].
     */
    fun parseCoursesCatalog(html: String): List<CanonicalProgram> {
        val doc: Document = Jsoup.parse(html)
        val programs = mutableListOf<CanonicalProgram>()

        // Look for all tables on the page
        val rows = doc.select("table tbody tr")
        for (row in rows) {
            val cols = row.select("td")
            if (cols.isEmpty()) continue

            // The link to prospectus is typically in an <a> tag with href like 'prospectus/{UUID}/1'
            val linkElem = row.select("a[href*='prospectus']").first() ?: continue
            val href = linkElem.attr("href")

            // Extract UUID and version from href: e.g. "https://studentportal.hcdc.edu.ph/prospectus/{03000000-B01B-0157-24F6-DD5C30F25AF2}/1"
            val uuidRegex = Regex("""prospectus/\{?([0-9a-fA-F\-]+)\}?/(\d+)""")
            val match = uuidRegex.find(href)
            val uuid = match?.groupValues?.get(1) ?: ""
            val versionNumber = match?.groupValues?.get(2)?.toIntOrNull() ?: 1

            // Program name is usually in the first column or link text
            val rawProgramName = if (cols.size > 1) cols[0].text().trim() else linkElem.text().trim()
            if (rawProgramName.isBlank() || uuid.isBlank()) continue

            // Extract code and clean name
            val programCode = extractProgramCode(rawProgramName)

            val version = ProspectusVersion(
                academicYear = "",
                curriculumVersion = "",
                prospectusUuid = uuid,
                versionNumber = versionNumber,
                isDefault = true
            )

            programs.add(
                CanonicalProgram(
                    code = programCode,
                    canonicalName = rawProgramName,
                    aliases = listOf(programCode, rawProgramName),
                    versions = listOf(version)
                )
            )
        }

        return programs
    }

    /**
     * Parses the individual /prospectus/{uuid}/1 page HTML into structured [Prospectus].
     */
    fun parseProspectusHtml(html: String, fallbackProgramCode: String = ""): Prospectus {
        val doc: Document = Jsoup.parse(html)

        // Metadata extraction
        // Usually:
        // <h4>Bachelor of Science in Information Technology</h4>
        // <h5>Revision: Rev. 2</h5>
        // <h5>Effective AY: 2023-2024</h5>
        val h4Text = doc.select("h4").text().trim()
        val h5List = doc.select("h5").map { it.text().trim() }

        var programName = h4Text
        var revision = ""
        var effectiveAy = ""

        for (h5 in h5List) {
            when {
                h5.startsWith("Revision", ignoreCase = true) -> {
                    revision = h5.substringAfter(":").trim()
                }
                h5.startsWith("Effective AY", ignoreCase = true) -> {
                    effectiveAy = h5.substringAfter(":").trim()
                }
            }
        }

        val programCode = if (programName.isNotBlank()) extractProgramCode(programName) else fallbackProgramCode

        // Parse curriculum tables
        // The page contains multiple tables or one table with headers:
        // Rows with <th colspan="...">First Year - 1st Semester</th> or similar header rows
        val yearLevelsMap = linkedMapOf<String, LinkedHashMap<String, MutableList<ProspectusSubject>>>()
        val semesterTotalsMap = mutableMapOf<String, String>()

        var currentYear = "First Year"
        var currentSem = "1st Semester"

        val tables = doc.select("table")
        for (table in tables) {
            val trList = table.select("tr")
            for (tr in trList) {
                val headerCell = tr.select("th[colspan], td[colspan]").firstOrNull()
                val headerText = headerCell?.text()?.trim() ?: ""

                if (headerText.isNotBlank() && (headerText.contains("Year", ignoreCase = true) || headerText.contains("Semester", ignoreCase = true) || headerText.contains("Summer", ignoreCase = true))) {
                    val parsed = parseYearAndSemHeader(headerText)
                    currentYear = parsed.first
                    currentSem = parsed.second
                    continue
                }

                // Check if this row is a Total Units row
                val rowText = tr.text().trim()
                if (rowText.startsWith("Total Units", ignoreCase = true) || rowText.contains("Total Units:", ignoreCase = true)) {
                    val unitsDisplay = tr.select("td, th").last()?.text()?.trim() ?: ""
                    semesterTotalsMap["$currentYear-$currentSem"] = unitsDisplay
                    continue
                }

                // Check if this is a subject row (td cells)
                val tds = tr.select("td")
                if (tds.size >= 3) {
                    // Typical columns:
                    // [0]: Subject Code
                    // [1]: Subject Title / Description
                    // [2]: Units
                    // [3]: Pre-Requisites (optional)
                    val code = tds[0].text().trim()
                    val title = tds[1].text().trim()
                    val units = tds[2].text().trim()
                    val prereq = if (tds.size > 3) tds[3].text().trim() else "None"

                    // Ignore table header row if it used td instead of th
                    if (code.equals("SUBJECT CODE", ignoreCase = true) || title.equals("SUBJECT", ignoreCase = true)) {
                        continue
                    }

                    if (code.isNotBlank()) {
                        val isLab = title.contains("Lab", ignoreCase = true) || code.endsWith("L", ignoreCase = true)
                        val isReligious = code.startsWith("REED", ignoreCase = true) || code.startsWith("THEO", ignoreCase = true)

                        val subject = ProspectusSubject(
                            code = code,
                            name = title,
                            units = units,
                            preRequisites = if (prereq.isBlank()) "None" else prereq,
                            isLab = isLab,
                            isReligious = isReligious
                        )

                        val yearMap = yearLevelsMap.getOrPut(currentYear) { linkedMapOf() }
                        val semList = yearMap.getOrPut(currentSem) { mutableListOf() }
                        semList.add(subject)
                    }
                }
            }
        }

        // Build domain models
        var grandTotalUnits = 0.0
        val yearLevelCurricula = yearLevelsMap.map { (yearName, semsMap) ->
            val semesterCurricula = semsMap.map { (semName, subjects) ->
                val semTotalDisplay = semesterTotalsMap["$yearName-$semName"]
                    ?: "${subjects.sumOf { it.units.toDoubleOrNull() ?: 0.0 }}"
                
                // Add to grand total
                val semTotalDouble = subjects.sumOf { it.units.toDoubleOrNull() ?: 0.0 }
                grandTotalUnits += semTotalDouble

                SemesterCurriculum(
                    semesterName = semName,
                    subjects = subjects,
                    totalUnitsDisplay = semTotalDisplay
                )
            }
            YearLevelCurriculum(
                yearLevelName = yearName,
                semesters = semesterCurricula
            )
        }

        val metadata = ProspectusMetadata(
            programCode = programCode,
            programName = programName,
            revision = revision,
            effectiveAy = effectiveAy,
            isOfflineCached = true,
            lastSyncedTimestamp = System.currentTimeMillis()
        )

        return Prospectus(
            metadata = metadata,
            yearLevels = yearLevelCurricula,
            totalCurriculumUnits = grandTotalUnits
        )
    }

    private fun parseYearAndSemHeader(header: String): Pair<String, String> {
        // Examples:
        // "First Year - 1st Semester"
        // "First Year -1st Semester"
        // "Second Year - 2nd Semester"
        // "Third Year - Summer"
        val parts = header.split("-", "–").map { it.trim() }
        if (parts.size >= 2) {
            return Pair(parts[0], parts[1])
        }
        return when {
            header.contains("First Year", ignoreCase = true) -> Pair("First Year", header)
            header.contains("Second Year", ignoreCase = true) -> Pair("Second Year", header)
            header.contains("Third Year", ignoreCase = true) -> Pair("Third Year", header)
            header.contains("Fourth Year", ignoreCase = true) -> Pair("Fourth Year", header)
            header.contains("Fifth Year", ignoreCase = true) -> Pair("Fifth Year", header)
            else -> Pair(header, "")
        }
    }

    private fun extractProgramCode(rawName: String): String {
        val upper = rawName.uppercase()
        return when {
            upper.contains("INFORMATION TECHNOLOGY") || upper.contains("BSIT") -> "BSIT"
            upper.contains("COMPUTER SCIENCE") || upper.contains("BSCS") -> "BSCS"
            upper.contains("COMPUTER ENGINEERING") || upper.contains("BSCPE") -> "BSCpE"
            upper.contains("ELECTRONICS ENGINEERING") || upper.contains("BSECE") -> "BSECE"
            upper.contains("CIVIL ENGINEERING") || upper.contains("BSCE") -> "BSCE"
            upper.contains("CRIMINOLOGY") || upper.contains("BSCRIM") -> "BSCrim"
            upper.contains("HOSPITALITY") || upper.contains("BSHM") -> "BSHM"
            upper.contains("TOURISM") || upper.contains("BSTM") -> "BSTM"
            upper.contains("LIBRARY") || upper.contains("BLIS") -> "BLIS"
            upper.contains("ACCOUNTANCY") || upper.contains("BSA") -> "BSA"
            upper.contains("FINANCIAL MANAGEMENT") || upper.contains("BSBA-FM") -> "BSBA-FM"
            upper.contains("MARKETING MANAGEMENT") || upper.contains("BSBA-MM") -> "BSBA-MM"
            upper.contains("HUMAN RESOURCE") || upper.contains("BSBA-HR") -> "BSBA-HRM"
            upper.contains("SECONDARY EDUCATION") || upper.contains("BSED") -> "BSEd"
            upper.contains("ELEMENTARY EDUCATION") || upper.contains("BEED") -> "BEEd"
            upper.contains("SPECIAL NEEDS EDUCATION") || upper.contains("BSNED") -> "BSNEd"
            upper.contains("PHYSICAL EDUCATION") || upper.contains("BPED") -> "BPEd"
            upper.contains("PSYCHOLOGY") || upper.contains("BSPSYCH") -> "BSPsych"
            upper.contains("SOCIAL WORK") || upper.contains("BSSW") -> "BSSW"
            upper.contains("POLITICAL SCIENCE") || upper.contains("ABPOLSCI") -> "ABPolSci"
            upper.contains("COMMUNICATION") || upper.contains("ABCOMM") -> "ABComm"
            upper.contains("ENGLISH LANGUAGE") || upper.contains("ABENG") -> "ABELS"
            upper.contains("MEDICAL TECHNOLOGY") || upper.contains("BSMT") -> "BSMT"
            else -> {
                // Generate acronym: e.g. "Bachelor of Science in..." -> BS...
                rawName.split(" ")
                    .filter { it.isNotBlank() && it[0].isUpperCase() && it.lowercase() !in listOf("in", "of", "and") }
                    .map { it[0] }
                    .joinToString("")
            }
        }
    }
}
