package com.jerson.hcdc_portal.util

import com.jerson.hcdc_portal.domain.model.Schedule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.Locale

class ScheduleHelperTest {

    private fun getCalendarFor(dayOfWeek: Int, hour: Int = 10, minute: Int = 0): Calendar {
        return Calendar.getInstance().apply {
            set(Calendar.YEAR, 2026)
            set(Calendar.MONTH, Calendar.OCTOBER)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.DAY_OF_WEEK, dayOfWeek)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }

    private fun createSchedule(
        days: String? = null,
        time: String? = null,
        subjectCode: String? = "SUBJ101",
        description: String = "Test Subject",
        room: String? = "RM 101"
    ): Schedule {
        return Schedule(
            id = 0,
            offeredNo = "12345",
            gClassCode = "GC101",
            subjectCode = subjectCode,
            description = description,
            unit = "3.0",
            days = days,
            time = time,
            room = room,
            lecLab = "LEC"
        )
    }

    @Test
    fun getTodayCode_returnsExpectedCodesAcrossDifferentLocales() {
        val originalLocale = Locale.getDefault()
        val testLocales = listOf(
            Locale.US,
            Locale.FRANCE,
            Locale.GERMAN,
            Locale("es", "ES"),
            Locale("tl", "PH"),
            Locale.JAPAN
        )

        val dayMappings = listOf(
            Calendar.MONDAY to "M",
            Calendar.TUESDAY to "T",
            Calendar.WEDNESDAY to "W",
            Calendar.THURSDAY to "Th",
            Calendar.FRIDAY to "F",
            Calendar.SATURDAY to "S",
            Calendar.SUNDAY to "Su"
        )

        try {
            for (locale in testLocales) {
                Locale.setDefault(locale)
                for ((dayOfWeek, expectedCode) in dayMappings) {
                    val cal = getCalendarFor(dayOfWeek)
                    val code = ScheduleHelper.getTodayCode(cal)
                    assertEquals("Failed for locale $locale on day $dayOfWeek", expectedCode, code)
                }
            }
        } finally {
            Locale.setDefault(originalLocale)
        }
    }

    @Test
    fun matchesDay_tuesdayDoesNotMatchThursdayOnly() {
        assertFalse(ScheduleHelper.matchesDay("Th", "T"))
        assertFalse(ScheduleHelper.matchesDay("TH", "T"))
        assertFalse(ScheduleHelper.matchesDay("th", "T"))
        assertFalse(ScheduleHelper.matchesDay("Thu", "T"))
        assertFalse(ScheduleHelper.matchesDay("THU", "T"))
    }

    @Test
    fun matchesDay_tuesdayMatchesCombinedDays() {
        assertTrue(ScheduleHelper.matchesDay("T", "T"))
        assertTrue(ScheduleHelper.matchesDay("TTH", "T"))
        assertTrue(ScheduleHelper.matchesDay("T-TH", "T"))
        assertTrue(ScheduleHelper.matchesDay("T/TH", "T"))
        assertTrue(ScheduleHelper.matchesDay("MTWTHF", "T"))
    }

    @Test
    fun matchesDay_thursdayMatchesCorrectly() {
        assertTrue(ScheduleHelper.matchesDay("Th", "Th"))
        assertTrue(ScheduleHelper.matchesDay("TH", "Th"))
        assertTrue(ScheduleHelper.matchesDay("T-TH", "Th"))
        assertTrue(ScheduleHelper.matchesDay("TTH", "Th"))
        assertTrue(ScheduleHelper.matchesDay("M-TH", "Th"))

        assertFalse(ScheduleHelper.matchesDay("T", "Th"))
        assertFalse(ScheduleHelper.matchesDay("MWF", "Th"))
    }

    @Test
    fun matchesDay_saturdayDoesNotMatchSundayOnly() {
        assertFalse(ScheduleHelper.matchesDay("Su", "S"))
        assertFalse(ScheduleHelper.matchesDay("SU", "S"))
        assertFalse(ScheduleHelper.matchesDay("Sun", "S"))
    }

    @Test
    fun matchesDay_saturdayMatchesSaturdayCombinations() {
        assertTrue(ScheduleHelper.matchesDay("S", "S"))
        assertTrue(ScheduleHelper.matchesDay("FS", "S"))
        assertTrue(ScheduleHelper.matchesDay("S-SU", "S"))
        assertTrue(ScheduleHelper.matchesDay("SSU", "S"))
    }

    @Test
    fun matchesDay_sundayMatchesCorrectly() {
        assertTrue(ScheduleHelper.matchesDay("Su", "Su"))
        assertTrue(ScheduleHelper.matchesDay("SU", "Su"))
        assertTrue(ScheduleHelper.matchesDay("S-SU", "Su"))
        assertTrue(ScheduleHelper.matchesDay("SSU", "Su"))

        assertFalse(ScheduleHelper.matchesDay("S", "Su"))
        assertFalse(ScheduleHelper.matchesDay("FS", "Su"))
    }

    @Test
    fun matchesDay_otherDaysMatch() {
        assertTrue(ScheduleHelper.matchesDay("MWF", "M"))
        assertTrue(ScheduleHelper.matchesDay("MWF", "W"))
        assertTrue(ScheduleHelper.matchesDay("MWF", "F"))
        assertFalse(ScheduleHelper.matchesDay("MWF", "T"))
        assertFalse(ScheduleHelper.matchesDay("MWF", "Th"))
        assertFalse(ScheduleHelper.matchesDay("MWF", "S"))
    }

    @Test
    fun matchesDay_allFilterAlwaysReturnsTrue() {
        assertTrue(ScheduleHelper.matchesDay("MWF", "All"))
        assertTrue(ScheduleHelper.matchesDay("T-TH", "All"))
        assertTrue(ScheduleHelper.matchesDay(null, "All"))
        assertTrue(ScheduleHelper.matchesDay("", "All"))
    }

    @Test
    fun matchesDay_nullOrBlankDaysReturnsFalseUnlessAll() {
        assertFalse(ScheduleHelper.matchesDay(null, "M"))
        assertFalse(ScheduleHelper.matchesDay("", "M"))
        assertFalse(ScheduleHelper.matchesDay("   ", "M"))
    }

    @Test
    fun isToday_correctlyIdentifiesTuesdayVsThursday() {
        val tuesdayCal = getCalendarFor(Calendar.TUESDAY)
        val thursdayCal = getCalendarFor(Calendar.THURSDAY)

        val schedTh = createSchedule(days = "TH", time = "08:00 AM - 09:30 AM", description = "Class A")
        val schedT = createSchedule(days = "T", time = "08:00 AM - 09:30 AM", description = "Class B")
        val schedTTH = createSchedule(days = "T-TH", time = "10:00 AM - 11:30 AM", description = "Class C")

        assertFalse(ScheduleHelper.isToday(schedTh, tuesdayCal))
        assertTrue(ScheduleHelper.isToday(schedT, tuesdayCal))
        assertTrue(ScheduleHelper.isToday(schedTTH, tuesdayCal))

        assertTrue(ScheduleHelper.isToday(schedTh, thursdayCal))
        assertFalse(ScheduleHelper.isToday(schedT, thursdayCal))
        assertTrue(ScheduleHelper.isToday(schedTTH, thursdayCal))
    }

    @Test
    fun getNextClass_picksClassForTodayOnly() {
        val tuesdayCal = getCalendarFor(Calendar.TUESDAY, hour = 7, minute = 30)

        val thOnly = createSchedule(
            subjectCode = "IT101",
            days = "TH",
            time = "08:00 AM - 09:30 AM",
            description = "Thursday Class"
        )
        val tOnly = createSchedule(
            subjectCode = "IT102",
            days = "T",
            time = "09:00 AM - 10:30 AM",
            description = "Tuesday Class"
        )

        val next = ScheduleHelper.getNextClass(listOf(thOnly, tOnly), tuesdayCal)
        assertNotNull(next)
        assertEquals("IT102", next?.schedule?.subjectCode)
        assertEquals(ClassState.UPCOMING, next?.state)
    }

    @Test
    fun getClassState_ongoingAndCompleted() {
        val cal = getCalendarFor(Calendar.MONDAY, hour = 9, minute = 0)
        val ongoingSched = createSchedule(days = "M", time = "08:30 AM - 10:00 AM")
        val upcomingSched = createSchedule(days = "M", time = "10:30 AM - 12:00 PM")
        val completedSched = createSchedule(days = "M", time = "07:00 AM - 08:30 AM")

        assertEquals(ClassState.CURRENT, ScheduleHelper.getClassState(ongoingSched, cal))
        assertEquals(ClassState.UPCOMING, ScheduleHelper.getClassState(upcomingSched, cal))
        assertEquals(ClassState.COMPLETED, ScheduleHelper.getClassState(completedSched, cal))
    }
}
