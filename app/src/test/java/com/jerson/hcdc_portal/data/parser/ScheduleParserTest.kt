package com.jerson.hcdc_portal.data.parser

import org.jsoup.Jsoup
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ScheduleParserTest {

    private lateinit var parser: ScheduleParserImpl

    @Before
    fun setUp() {
        parser = ScheduleParserImpl()
    }

    @Test
    fun parseSchedules_validHtml_returnsScheduleList() {
        val html = """
            <html>
            <body>
                <div class="col-sm-9">
                    <table>
                        <tbody>
                            <tr>
                                <td>10101</td>
                                <td>BSIT-4A</td>
                                <td>IT 411</td>
                                <td>Capstone Project 2</td>
                                <td>3.0</td>
                                <td>M-W-F</td>
                                <td>8:00 AM - 9:00 AM</td>
                                <td>NB 401</td>
                                <td>LEC</td>
                            </tr>
                            <tr>
                                <td>10102</td>
                                <td>BSIT-4A</td>
                                <td>IT 412</td>
                                <td>Systems Administration</td>
                                <td>3.0</td>
                                <td>T-TH</td>
                                <td>1:00 PM - 2:30 PM</td>
                                <td>NB 402</td>
                                <td>LAB</td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </body>
            </html>
        """.trimIndent()

        val doc = Jsoup.parse(html)
        val schedules = parser.parseSchedules(doc)

        assertEquals(2, schedules.size)
        assertEquals("10101", schedules[0].offeredNo)
        assertEquals("BSIT-4A", schedules[0].gClassCode)
        assertEquals("IT 411", schedules[0].subjectCode)
        assertEquals("Capstone Project 2", schedules[0].description)
        assertEquals("3.0", schedules[0].unit)
        assertEquals("M-W-F", schedules[0].days)
        assertEquals("8:00 AM - 9:00 AM", schedules[0].time)
        assertEquals("NB 401", schedules[0].room)
        assertEquals("LEC", schedules[0].lecLab)

        assertEquals("10102", schedules[1].offeredNo)
        assertEquals("IT 412", schedules[1].subjectCode)
    }

    @Test
    fun parseSchedules_emptyTable_returnsEmptyList() {
        val html = """
            <html>
            <body>
                <div class="col-sm-9">
                    <table>
                        <tbody>
                            <tr>
                                <td></td>
                                <td></td>
                                <td></td>
                                <td></td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </body>
            </html>
        """.trimIndent()

        val doc = Jsoup.parse(html)
        val schedules = parser.parseSchedules(doc)
        assertTrue(schedules.isEmpty())
    }

    @Test
    fun parseUserProfile_standardDesignation_parsesCourseAndId() {
        val html = """
            <html>
            <body>
                <div class="col-sm-9">
                    <b>Total Units: 21.0</b>
                </div>
                <div class="app-sidebar__user-name">JUAN DELA CRUZ</div>
                <div class="app-sidebar__user-designation">BSIT 2020-00123</div>
                <div class="mybox-body"><center><h5>Enrollment is ongoing</h5></center></div>
                <img class="app-sidebar__user-avatar" src="https://example.com/avatar.jpg" />
                <p>Officially Enrolled</p>
            </body>
            </html>
        """.trimIndent()

        val doc = Jsoup.parse(html)
        val profile = parser.parseUserProfile(doc)

        assertEquals("Juan Dela Cruz", profile.name)
        assertEquals("21.0", profile.units)
        assertEquals("BSIT", profile.course)
        assertEquals("2020-00123", profile.studentId)
        assertEquals("Enrollment is ongoing", profile.enrollAnnounce)
        assertEquals("Officially Enrolled.", profile.isEnrolled)
        assertEquals("https://example.com/avatar.jpg", profile.avatarUrl)
    }

    @Test
    fun parseUserProfile_multiWordCourse_correctlySeparatesCourseAndId() {
        val html = """
            <html>
            <body>
                <div class="col-sm-9"><b>Total Units: 18.0</b></div>
                <div class="app-sidebar__user-name">MARIA CLARA</div>
                <div class="app-sidebar__user-designation">Bachelor of Science in Information Technology (1900456)</div>
                <p>Not officially enrolled</p>
            </body>
            </html>
        """.trimIndent()

        val doc = Jsoup.parse(html)
        val profile = parser.parseUserProfile(doc)

        assertEquals("Maria Clara", profile.name)
        assertEquals("Bachelor of Science in Information Technology", profile.course)
        assertEquals("1900456", profile.studentId)
        assertEquals("Not enrolled this sem.", profile.isEnrolled)
    }

    @Test
    fun parseUserProfile_multiWordCourseSpaceSeparated_parsesProperly() {
        val html = """
            <html>
            <body>
                <div class="app-sidebar__user-name">PEDRO PENDUKO</div>
                <div class="app-sidebar__user-designation">BS Computer Science 2021-9999</div>
            </body>
            </html>
        """.trimIndent()

        val doc = Jsoup.parse(html)
        val profile = parser.parseUserProfile(doc)

        assertEquals("Pedro Penduko", profile.name)
        assertEquals("BS Computer Science", profile.course)
        assertEquals("2021-9999", profile.studentId)
    }

    @Test
    fun parseDesignation_edgeCases() {
        val (c1, id1) = ScheduleParserImpl.parseDesignation("BSIT (12345)")
        assertEquals("BSIT", c1)
        assertEquals("12345", id1)

        val (c2, id2) = ScheduleParserImpl.parseDesignation("BS Information Technology 12345")
        assertEquals("BS Information Technology", c2)
        assertEquals("12345", id2)

        val (c3, id3) = ScheduleParserImpl.parseDesignation("BSIT")
        assertEquals("BSIT", c3)
        assertEquals("", id3)

        val (c4, id4) = ScheduleParserImpl.parseDesignation("")
        assertEquals("", c4)
        assertEquals("", id4)
    }
}
