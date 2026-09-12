package com.jerson.hcdc_portal.data.parser

import org.jsoup.Jsoup
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GradeParserTest {

    private lateinit var parser: GradeParserImpl

    @Before
    fun setUp() {
        parser = GradeParserImpl()
    }

    @Test
    fun parseGrades_standardTable_returnsGradesAndSummary() {
        val html = """
            <html>
            <body>
                <select class="select2-multiple form-control">
                    <option value="/grade_hed/101" selected>1st Semester 2023-2024</option>
                    <option value="/grade_hed/100">2nd Semester 2022-2023</option>
                </select>
                <div class="col-md-12">
                    <table>
                        <tbody>
                            <tr>
                                <td>10101</td>
                                <td>CS 311</td>
                                <td>Algorithms</td>
                                <td>3.0</td>
                                <td>1.5</td>
                                <td>Passed</td>
                                <td>1.3</td>
                                <td>Passed</td>
                                <td>Dr. Smith</td>
                            </tr>
                            <tr>
                                <td>10102</td>
                                <td>CS 312</td>
                                <td>Database Systems</td>
                                <td>3.0</td>
                                <td>1.7</td>
                                <td>Passed</td>
                                <td>1.5</td>
                                <td>Passed</td>
                                <td>Prof. Doe</td>
                            </tr>
                            <tr>
                                <td>Total Earned Units</td>
                                <td>6.0</td>
                                <td>GWA</td>
                                <td>1.40</td>
                                <td></td><td></td><td></td><td></td><td></td>
                            </tr>
                            <tr>
                                <td colspan="9">Grading System: 1.0 - 5.0</td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </body>
            </html>
        """.trimIndent()

        val doc = Jsoup.parse(html)
        val grades = parser.parseGrades(doc, 101)

        assertEquals(2, grades.size)
        assertEquals("CS 311", grades[0].subjectCode)
        assertEquals("Algorithms", grades[0].description)
        assertEquals("1.5", grades[0].midGrade)
        assertEquals("1.3", grades[0].finalGrade)
        assertEquals("1.40", grades[0].weightedAve)
        assertEquals("6.0", grades[0].earnedUnits)
        assertEquals("1st Semester 2023-2024", grades[0].term)
        assertEquals(101, grades[0].termId)

        assertEquals("CS 312", grades[1].subjectCode)
    }

    @Test
    fun parseGrades_singleRowTable_doesNotDropGrade() {
        val html = """
            <html>
            <body>
                <select class="select2-multiple form-control">
                    <option value="/grade_hed/50" selected>Summer 2023</option>
                </select>
                <div class="col-md-12">
                    <table>
                        <tbody>
                            <tr>
                                <td>99999</td>
                                <td>OJT 101</td>
                                <td>Practicum</td>
                                <td>3.0</td>
                                <td>1.0</td>
                                <td>Passed</td>
                                <td>1.0</td>
                                <td>Passed</td>
                                <td>Coordinator</td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </body>
            </html>
        """.trimIndent()

        val doc = Jsoup.parse(html)
        val grades = parser.parseGrades(doc, 50)

        // Under old code with rows.subList(0, rows.size - 3), this was returning empty!
        assertEquals(1, grades.size)
        assertEquals("OJT 101", grades[0].subjectCode)
        assertEquals("Practicum", grades[0].description)
    }

    @Test
    fun parseGrades_emptyTable_returnsEmptyList() {
        val html = """
            <html>
            <body>
                <div class="col-md-12">
                    <table>
                        <tbody>
                        </tbody>
                    </table>
                </div>
            </body>
            </html>
        """.trimIndent()

        val doc = Jsoup.parse(html)
        val grades = parser.parseGrades(doc, 1)
        assertTrue(grades.isEmpty())
    }

    @Test
    fun parseTerms_validOptions_returnsDescendingList() {
        val html = """
            <html>
            <body>
                <main class="app-content">
                    <select class="select2-multiple form-control">
                        <option value="/grade_hed/1">1st Semester 2021-2022</option>
                        <option value="/grade_hed/2">2nd Semester 2022-2023</option>
                        <option value="/grade_hed/3">1st Semester 2023-2024</option>
                    </select>
                </main>
            </body>
            </html>
        """.trimIndent()

        val doc = Jsoup.parse(html)
        val terms = parser.parseTerms(doc)

        assertEquals(3, terms.size)
        assertEquals(1, terms[0].isGrade)
        // Descending order check
        assertEquals("2nd Semester 2022-2023", terms[0].term)
    }
}
