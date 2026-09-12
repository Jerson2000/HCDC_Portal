package com.jerson.hcdc_portal.resolver

import com.jerson.hcdc_portal.data.parser.ProspectusParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ProspectusParserTest {

    private lateinit var parser: ProspectusParser

    @Before
    fun setup() {
        parser = ProspectusParser()
    }

    @Test
    fun parseCoursesCatalog_extractsPrograms() {
        val sampleCatalogHtml = """
            <table>
                <tbody>
                    <tr>
                        <td>Bachelor of Science in Information Technology</td>
                        <td><a href="https://studentportal.hcdc.edu.ph/prospectus/{03000000-B01B-0157-24F6-DD5C30F25AF2}/1">View Prospectus</a></td>
                    </tr>
                    <tr>
                        <td>Bachelor of Science in Computer Science</td>
                        <td><a href="https://studentportal.hcdc.edu.ph/prospectus/{03000000-B42F-0183-9C8C-A0BAAE798383}/1">View Prospectus</a></td>
                    </tr>
                </tbody>
            </table>
        """.trimIndent()

        val programs = parser.parseCoursesCatalog(sampleCatalogHtml)
        assertEquals(2, programs.size)
        assertEquals("BSIT", programs[0].code)
        assertEquals("03000000-B01B-0157-24F6-DD5C30F25AF2", programs[0].versions[0].prospectusUuid)
        assertEquals("BSCS", programs[1].code)
        assertEquals("03000000-B42F-0183-9C8C-A0BAAE798383", programs[1].versions[0].prospectusUuid)
    }

    @Test
    fun parseProspectusHtml_extractsMetadataAndCurriculum() {
        val sampleProspectusHtml = """
            <h4>Bachelor of Science in Information Technology</h4>
            <h5>Revision: Rev. 2</h5>
            <h5>Effective AY: 2023-2024</h5>
            <table class="table table-bordered">
                <tr>
                    <th colspan="4">First Year - 1st Semester</th>
                </tr>
                <tr>
                    <td>SUBJECT CODE</td>
                    <td>SUBJECT</td>
                    <td>UNITS</td>
                    <td>PRE-REQUISITES</td>
                </tr>
                <tr>
                    <td>IT 111</td>
                    <td>Introduction to Computing</td>
                    <td>3.0</td>
                    <td>None</td>
                </tr>
                <tr>
                    <td>IT 112</td>
                    <td>Computer Programming 1 (Lab)</td>
                    <td>3.0</td>
                    <td>None</td>
                </tr>
                <tr>
                    <td colspan="2">Total Units:</td>
                    <td>6.0</td>
                    <td></td>
                </tr>
            </table>
        """.trimIndent()

        val prospectus = parser.parseProspectusHtml(sampleProspectusHtml)
        assertEquals("BSIT", prospectus.metadata.programCode)
        assertEquals("Bachelor of Science in Information Technology", prospectus.metadata.programName)
        assertEquals("Rev. 2", prospectus.metadata.revision)
        assertEquals("2023-2024", prospectus.metadata.effectiveAy)

        assertEquals(1, prospectus.yearLevels.size)
        val firstYear = prospectus.yearLevels[0]
        assertEquals("First Year", firstYear.yearLevelName)
        assertEquals(1, firstYear.semesters.size)

        val firstSem = firstYear.semesters[0]
        assertEquals("1st Semester", firstSem.semesterName)
        assertEquals(2, firstSem.subjects.size)

        val subj1 = firstSem.subjects[0]
        assertEquals("IT 111", subj1.code)
        assertEquals("Introduction to Computing", subj1.name)
        assertEquals("3.0", subj1.units)
        assertFalse(subj1.isLab)

        val subj2 = firstSem.subjects[1]
        assertEquals("IT 112", subj2.code)
        assertEquals("Computer Programming 1 (Lab)", subj2.name)
        assertEquals("3.0", subj2.units)
        assertTrue(subj2.isLab)

        assertEquals(6.0, prospectus.totalCurriculumUnits, 0.01)
    }
}
