package com.jerson.hcdc_portal.domain.resolver

import com.jerson.hcdc_portal.domain.model.CanonicalProgram
import com.jerson.hcdc_portal.domain.model.ProspectusVersion
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProgramResolver @Inject constructor() {

    // Default canonical catalog seeded directly from HCDC /courses
    private val defaultCatalog: List<CanonicalProgram> = listOf(
        CanonicalProgram(
            code = "BSIT",
            canonicalName = "Bachelor of Science in Information Technology",
            aliases = listOf(
                "BSIT", "BS-IT", "INFORMATION TECHNOLOGY", "INFO TECH",
                "BACHELOR OF SCIENCE IN INFORMATION TECHNOLOGY"
            ),
            department = "College of Information Technology",
            versions = listOf(
                ProspectusVersion(
                    academicYear = "2023-2024",
                    curriculumVersion = "Rev. 2",
                    prospectusUuid = "03000000-B01B-0157-24F6-DD5C30F25AF2",
                    versionNumber = 1,
                    isDefault = true
                )
            )
        ),
        CanonicalProgram(
            code = "BSCS",
            canonicalName = "Bachelor of Science in Computer Science",
            aliases = listOf(
                "BSCS", "BS-CS", "COMPUTER SCIENCE", "COMP SCI",
                "BACHELOR OF SCIENCE IN COMPUTER SCIENCE"
            ),
            department = "College of Information Technology",
            versions = listOf(
                ProspectusVersion(
                    academicYear = "2023-2024",
                    curriculumVersion = "Rev. 2",
                    prospectusUuid = "03000000-B42F-0183-9C8C-A0BAAE798383",
                    versionNumber = 1,
                    isDefault = true
                )
            )
        ),
        CanonicalProgram(
            code = "BSCpE",
            canonicalName = "Bachelor of Science in Computer Engineering",
            aliases = listOf(
                "BSCPE", "BS-CPE", "COMPUTER ENGINEERING", "COMP ENG",
                "BACHELOR OF SCIENCE IN COMPUTER ENGINEERING"
            ),
            department = "College of Engineering",
            versions = listOf(
                ProspectusVersion(
                    academicYear = "2023-2024",
                    curriculumVersion = "Rev. 1",
                    prospectusUuid = "03000000-B2CA-01DF-8CDF-D0F722D45ED8",
                    versionNumber = 1,
                    isDefault = true
                )
            )
        ),
        CanonicalProgram(
            code = "BSECE",
            canonicalName = "Bachelor of Science in Electronics Engineering",
            aliases = listOf(
                "BSECE", "BS-ECE", "ELECTRONICS ENGINEERING",
                "BACHELOR OF SCIENCE IN ELECTRONICS ENGINEERING"
            ),
            department = "College of Engineering",
            versions = listOf(
                ProspectusVersion(
                    academicYear = "2023-2024",
                    curriculumVersion = "Rev. 1",
                    prospectusUuid = "03000000-B147-017E-970B-CB431BA36A15",
                    versionNumber = 1,
                    isDefault = true
                )
            )
        ),
        CanonicalProgram(
            code = "BSCE",
            canonicalName = "Bachelor of Science in Civil Engineering",
            aliases = listOf(
                "BSCE", "BS-CE", "CIVIL ENGINEERING",
                "BACHELOR OF SCIENCE IN CIVIL ENGINEERING"
            ),
            department = "College of Engineering",
            versions = listOf(
                ProspectusVersion(
                    academicYear = "2023-2024",
                    curriculumVersion = "Rev. 1",
                    prospectusUuid = "03000000-BD25-0105-C36C-C3DE64B78D89",
                    versionNumber = 1,
                    isDefault = true
                )
            )
        ),
        CanonicalProgram(
            code = "BSCrim",
            canonicalName = "Bachelor of Science in Criminology",
            aliases = listOf(
                "BSCRIM", "BS-CRIM", "CRIMINOLOGY",
                "BACHELOR OF SCIENCE IN CRIMINOLOGY"
            ),
            department = "College of Criminal Justice Education",
            versions = listOf(
                ProspectusVersion(
                    academicYear = "2023-2024",
                    curriculumVersion = "Rev. 1",
                    prospectusUuid = "03000000-B671-016E-1EB1-C8DF78726F82",
                    versionNumber = 1,
                    isDefault = true
                )
            )
        ),
        CanonicalProgram(
            code = "BSHM",
            canonicalName = "Bachelor of Science in Hospitality Management",
            aliases = listOf(
                "BSHM", "BS-HM", "HOSPITALITY MANAGEMENT", "HOTEL AND RESTAURANT MANAGEMENT",
                "BACHELOR OF SCIENCE IN HOSPITALITY MANAGEMENT"
            ),
            department = "College of Hospitality and Tourism",
            versions = listOf(
                ProspectusVersion(
                    academicYear = "2023-2024",
                    curriculumVersion = "Rev. 1",
                    prospectusUuid = "03000000-B40E-014E-048E-53BE5E170276",
                    versionNumber = 1,
                    isDefault = true
                )
            )
        ),
        CanonicalProgram(
            code = "BSTM",
            canonicalName = "Bachelor of Science in Tourism Management",
            aliases = listOf(
                "BSTM", "BS-TM", "TOURISM MANAGEMENT",
                "BACHELOR OF SCIENCE IN TOURISM MANAGEMENT"
            ),
            department = "College of Hospitality and Tourism",
            versions = listOf(
                ProspectusVersion(
                    academicYear = "2023-2024",
                    curriculumVersion = "Rev. 1",
                    prospectusUuid = "03000000-B0F3-0177-380F-A9FDEBBD5FA9",
                    versionNumber = 1,
                    isDefault = true
                )
            )
        ),
        CanonicalProgram(
            code = "BLIS",
            canonicalName = "Bachelor of Library and Information Science",
            aliases = listOf(
                "BLIS", "LIBRARY SCIENCE", "LIBRARY AND INFORMATION SCIENCE",
                "BACHELOR OF LIBRARY AND INFORMATION SCIENCE"
            ),
            department = "College of Humanities, Education and Social Sciences",
            versions = listOf(
                ProspectusVersion(
                    academicYear = "2023-2024",
                    curriculumVersion = "Rev. 1",
                    prospectusUuid = "03000000-BE7E-01B9-BC60-B147F755FBA9",
                    versionNumber = 1,
                    isDefault = true
                )
            )
        ),
        CanonicalProgram(
            code = "BSA",
            canonicalName = "Bachelor of Science in Accountancy",
            aliases = listOf(
                "BSA", "BS-A", "ACCOUNTANCY",
                "BACHELOR OF SCIENCE IN ACCOUNTANCY"
            ),
            department = "College of Business and Management",
            versions = listOf(
                ProspectusVersion(
                    academicYear = "2023-2024",
                    curriculumVersion = "Rev. 1",
                    prospectusUuid = "03000000-BAEC-0142-8DE4-C567A04D36FE",
                    versionNumber = 1,
                    isDefault = true
                )
            )
        ),
        CanonicalProgram(
            code = "BSBA-FM",
            canonicalName = "BSBA Major in Financial Management",
            aliases = listOf(
                "BSBA-FM", "BSBA FM", "FINANCIAL MANAGEMENT",
                "BACHELOR OF SCIENCE IN BUSINESS ADMINISTRATION MAJOR IN FINANCIAL MANAGEMENT"
            ),
            department = "College of Business and Management",
            versions = listOf(
                ProspectusVersion(
                    academicYear = "2023-2024",
                    curriculumVersion = "Rev. 1",
                    prospectusUuid = "03000000-BAEC-0142-8DE4-C567A04D36FE",
                    versionNumber = 1,
                    isDefault = true
                )
            )
        ),
        CanonicalProgram(
            code = "BSBA-MM",
            canonicalName = "BSBA Major in Marketing Management",
            aliases = listOf(
                "BSBA-MM", "BSBA MM", "MARKETING MANAGEMENT",
                "BACHELOR OF SCIENCE IN BUSINESS ADMINISTRATION MAJOR IN MARKETING MANAGEMENT"
            ),
            department = "College of Business and Management",
            versions = listOf(
                ProspectusVersion(
                    academicYear = "2023-2024",
                    curriculumVersion = "Rev. 1",
                    prospectusUuid = "03000000-B214-0130-C32E-AF54784CF95F",
                    versionNumber = 1,
                    isDefault = true
                )
            )
        ),
        CanonicalProgram(
            code = "BSEd",
            canonicalName = "Bachelor of Secondary Education",
            aliases = listOf(
                "BSED", "BS-ED", "SECONDARY EDUCATION",
                "BACHELOR OF SECONDARY EDUCATION"
            ),
            department = "College of Education",
            versions = listOf(
                ProspectusVersion(
                    academicYear = "2023-2024",
                    curriculumVersion = "Rev. 1",
                    prospectusUuid = "03000000-B3A9-011F-95BC-A253456C8C70",
                    versionNumber = 1,
                    isDefault = true
                )
            )
        ),
        CanonicalProgram(
            code = "BEEd",
            canonicalName = "Bachelor of Elementary Education",
            aliases = listOf(
                "BEED", "BE-ED", "ELEMENTARY EDUCATION",
                "BACHELOR OF ELEMENTARY EDUCATION"
            ),
            department = "College of Education",
            versions = listOf(
                ProspectusVersion(
                    academicYear = "2023-2024",
                    curriculumVersion = "Rev. 1",
                    prospectusUuid = "03000000-BF9E-0136-2396-D1B950FDDC00",
                    versionNumber = 1,
                    isDefault = true
                )
            )
        ),
        CanonicalProgram(
            code = "BSPsych",
            canonicalName = "Bachelor of Science in Psychology",
            aliases = listOf(
                "BSPSYCH", "BS-PSYCH", "PSYCHOLOGY",
                "BACHELOR OF SCIENCE IN PSYCHOLOGY"
            ),
            department = "College of Humanities and Social Sciences",
            versions = listOf(
                ProspectusVersion(
                    academicYear = "2023-2024",
                    curriculumVersion = "Rev. 1",
                    prospectusUuid = "03000000-B570-0199-8DF0-D91398BC8BA4",
                    versionNumber = 1,
                    isDefault = true
                )
            )
        ),
        CanonicalProgram(
            code = "ABPolSci",
            canonicalName = "Bachelor of Arts in Political Science",
            aliases = listOf(
                "ABPOLSCI", "AB-POLSCI", "POLITICAL SCIENCE",
                "BACHELOR OF ARTS IN POLITICAL SCIENCE"
            ),
            department = "College of Humanities and Social Sciences",
            versions = listOf(
                ProspectusVersion(
                    academicYear = "2023-2024",
                    curriculumVersion = "Rev. 1",
                    prospectusUuid = "03000000-B95C-0110-B147-DE2C7EB1885D",
                    versionNumber = 1,
                    isDefault = true
                )
            )
        ),
        CanonicalProgram(
            code = "ABComm",
            canonicalName = "Bachelor of Arts in Communication",
            aliases = listOf(
                "ABCOMM", "AB-COMM", "COMMUNICATION", "COMMUNICATION ARTS",
                "BACHELOR OF ARTS IN COMMUNICATION"
            ),
            department = "College of Humanities and Social Sciences",
            versions = listOf(
                ProspectusVersion(
                    academicYear = "2023-2024",
                    curriculumVersion = "Rev. 1",
                    prospectusUuid = "03000000-B8E5-0158-692B-AE2BD3302634",
                    versionNumber = 1,
                    isDefault = true
                )
            )
        ),
        CanonicalProgram(
            code = "BSMT",
            canonicalName = "Bachelor of Science in Medical Technology",
            aliases = listOf(
                "BSMT", "BS-MT", "MEDICAL TECHNOLOGY", "MED TECH",
                "BACHELOR OF SCIENCE IN MEDICAL TECHNOLOGY"
            ),
            department = "College of Allied Health Sciences",
            versions = listOf(
                ProspectusVersion(
                    academicYear = "2023-2024",
                    curriculumVersion = "Rev. 1",
                    prospectusUuid = "03000000-B0C4-01EE-96BE-E99464673B88",
                    versionNumber = 1,
                    isDefault = true
                )
            )
        )
    )

    @Volatile
    private var dynamicCatalog: MutableList<CanonicalProgram> = defaultCatalog.toMutableList()

    fun updateCatalog(parsedPrograms: List<CanonicalProgram>) {
        if (parsedPrograms.isEmpty()) return
        synchronized(this) {
            val map = dynamicCatalog.associateBy { it.code }.toMutableMap()
            for (p in parsedPrograms) {
                val existing = map[p.code]
                if (existing != null) {
                    val mergedAliases = (existing.aliases + p.aliases).distinct()
                    val mergedVersions = if (p.versions.isNotEmpty()) p.versions else existing.versions
                    map[p.code] = existing.copy(aliases = mergedAliases, versions = mergedVersions)
                } else {
                    map[p.code] = p
                }
            }
            dynamicCatalog = map.values.toMutableList()
        }
    }

    /**
     * Resolves a raw student course string into the matching [CanonicalProgram].
     * Normalizes punctuation, casing, acronyms, and aliases.
     */
    fun resolve(rawInput: String?): CanonicalProgram? {
        if (rawInput.isNullOrBlank()) return null
        val normalizedInput = normalize(rawInput)

        // 1. Check exact canonical code match
        for (prog in dynamicCatalog) {
            if (prog.code.equals(rawInput.trim(), ignoreCase = true)) {
                return prog
            }
        }

        // 2. Check normalized alias matches
        for (prog in dynamicCatalog) {
            for (alias in prog.aliases) {
                if (normalize(alias) == normalizedInput) {
                    return prog
                }
            }
        }

        // 3. Check contains matching (fuzzy / partial)
        for (prog in dynamicCatalog) {
            val progNormalized = normalize(prog.canonicalName)
            if (normalizedInput.contains(progNormalized) || progNormalized.contains(normalizedInput)) {
                return prog
            }
            val codeNormalized = normalize(prog.code)
            if (normalizedInput.contains(codeNormalized)) {
                return prog
            }
        }

        // 4. Fallback: match by key keywords
        for (prog in dynamicCatalog) {
            for (alias in prog.aliases) {
                val normAlias = normalize(alias)
                if (normAlias.length > 4 && normalizedInput.contains(normAlias)) {
                    return prog
                }
            }
        }

        return null
    }

    private fun normalize(str: String): String {
        return str.uppercase(Locale.ROOT)
            .replace("-", " ")
            .replace(".", "")
            .replace(",", "")
            .replace("  ", " ")
            .trim()
    }
}
