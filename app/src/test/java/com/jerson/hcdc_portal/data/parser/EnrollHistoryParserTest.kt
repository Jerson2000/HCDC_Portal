package com.jerson.hcdc_portal.data.parser

import org.jsoup.Jsoup
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class EnrollHistoryParserTest {

    private lateinit var parser: EnrollHistoryParserImpl

    @Before
    fun setUp() {
        parser = EnrollHistoryParserImpl()
    }

    @Test
    fun parseEnrollHistory_standardTable_returnsEnrollHistoryList() {
        val html = """
            <html>
            <body>
                <select class="select2-multiple form-control">
                    <option value="/enrollmentHistory/1" selected>1st Semester 2022-2023</option>
                </select>
                <div class="tiles">
                    <table>
                        <tbody>
                            <tr>
                                <td>20001</td>
                                <td>MATH 101</td>
                                <td>College Algebra</td>
                                <td>3.0</td>
                            </tr>
                            <tr>
                                <td>20002</td>
                                <td>ENG 101</td>
                                <td>Purposive Communication</td>
                                <td>3.0</td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </body>
            </html>
        """.trimIndent()

        val doc = Jsoup.parse(html)
        val history = parser.parseEnrollHistory(doc, 1)

        assertEquals(2, history.size)
        assertEquals("20001", history[0].offeredNo)
        assertEquals("MATH 101", history[0].subjectCode)
        assertEquals("College Algebra", history[0].description)
        assertEquals("3.0", history[0].unit)
        assertEquals("1st Semester 2022-2023", history[0].term)
        assertEquals(1, history[0].termId)
    }

    @Test
    fun parseEnrollHistory_emptyTable_returnsEmptyList() {
        val html = """
            <html>
            <body>
                <div class="tiles">
                    <table>
                        <tbody>
                        </tbody>
                    </table>
                </div>
            </body>
            </html>
        """.trimIndent()

        val doc = Jsoup.parse(html)
        val history = parser.parseEnrollHistory(doc, 1)
        assertTrue(history.isEmpty())
    }

    @Test
    fun parseTerms_enrollHistoryCategory() {
        val html = """
            <html>
            <body>
                <main class="app-content">
                    <select class="select2-multiple form-control">
                        <option value="/enrollmentHistory/1">1st Semester 2020</option>
                        <option value="/enrollmentHistory/2">2nd Semester 2020</option>
                    </select>
                </main>
            </body>
            </html>
        """.trimIndent()

        val doc = Jsoup.parse(html)
        val terms = parser.parseTerms(doc)

        assertEquals(2, terms.size)
        assertEquals(0, terms[0].isGrade)
    }
}
