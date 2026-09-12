package com.jerson.hcdc_portal.util

import com.jerson.hcdc_portal.domain.model.Schedule
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class ClassState {
    CURRENT,
    UPCOMING,
    COMPLETED,
    UNKNOWN
}

data class NextClassInfo(
    val schedule: Schedule,
    val state: ClassState,
    val statusText: String
)

object ScheduleHelper {

    /**
     * Maps Calendar.DAY_OF_WEEK integer to standard schedule day code:
     * Monday -> "M", Tuesday -> "T", Wednesday -> "W", Thursday -> "Th",
     * Friday -> "F", Saturday -> "S", Sunday -> "Su".
     * Integer-based lookup ensures 100% immunity to device locale settings.
     */
    fun getTodayCode(cal: Calendar = Calendar.getInstance()): String {
        return when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "M"
            Calendar.TUESDAY -> "T"
            Calendar.WEDNESDAY -> "W"
            Calendar.THURSDAY -> "Th"
            Calendar.FRIDAY -> "F"
            Calendar.SATURDAY -> "S"
            Calendar.SUNDAY -> "Su"
            else -> ""
        }
    }

    /**
     * Checks if a schedule's days string matches the specified day code (or "All").
     * Properly disambiguates "T" (Tuesday) vs "Th" (Thursday) and "S" (Saturday) vs "Su" (Sunday).
     */
    fun matchesDay(days: String?, dayCode: String): Boolean {
        if (dayCode.equals("All", ignoreCase = true)) return true
        if (days.isNullOrBlank()) return false

        return when (dayCode) {
            "Th", "TH", "th" -> days.contains("th", ignoreCase = true)
            "T", "t" -> {
                // Remove "th" occurrences first to avoid false positive match for Tuesday in "Th" or "T-TH"
                val withoutTh = days.replace(Regex("(?i)th"), "")
                withoutTh.contains("t", ignoreCase = true)
            }
            "Su", "SU", "su" -> days.contains("su", ignoreCase = true)
            "S", "s" -> {
                // Remove "su" occurrences first to avoid false positive match for Saturday in "Su" or "S-SU"
                val withoutSu = days.replace(Regex("(?i)su"), "")
                withoutSu.contains("s", ignoreCase = true)
            }
            else -> days.contains(dayCode, ignoreCase = true)
        }
    }

    /**
     * Checks if a schedule falls on today (or on the given calendar date).
     */
    fun isToday(schedule: Schedule, cal: Calendar = Calendar.getInstance()): Boolean {
        return matchesDay(schedule.days, getTodayCode(cal))
    }

    /**
     * Parses the start and end time of a class schedule.
     * Example input: "07:30 AM - 09:00 AM" or "1:00 PM-3:00 PM"
     * Anchors the parsed times to [referenceCal]'s year, month, and day.
     */
    fun parseClassTimes(timeString: String?, referenceCal: Calendar = Calendar.getInstance()): Pair<Calendar, Calendar>? {
        if (timeString.isNullOrBlank()) return null
        val parts = timeString.split(Regex("[-–—]")).map { it.trim() }
        if (parts.size < 2) return null

        val startCal = parseSingleTime(parts[0], referenceCal) ?: return null
        val endCal = parseSingleTime(parts[1], referenceCal) ?: return null

        return Pair(startCal, endCal)
    }

    private fun parseSingleTime(rawTime: String, referenceCal: Calendar): Calendar? {
        val formats = arrayOf("hh:mm a", "h:mm a", "hh:mma", "h:mma", "HH:mm", "H:mm")
        for (pattern in formats) {
            try {
                val sdf = SimpleDateFormat(pattern, Locale.US)
                val date = sdf.parse(rawTime.trim())
                if (date != null) {
                    val parsedCal = Calendar.getInstance().apply { time = date }
                    val cal = referenceCal.clone() as Calendar
                    cal.set(Calendar.HOUR_OF_DAY, parsedCal.get(Calendar.HOUR_OF_DAY))
                    cal.set(Calendar.MINUTE, parsedCal.get(Calendar.MINUTE))
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                    return cal
                }
            } catch (_: Exception) { }
        }
        return null
    }

    /**
     * Returns the state of a class right now (or at the given [referenceCal]).
     */
    fun getClassState(schedule: Schedule, referenceCal: Calendar = Calendar.getInstance()): ClassState {
        val times = parseClassTimes(schedule.time, referenceCal) ?: return ClassState.UNKNOWN
        val (start, end) = times

        return when {
            referenceCal.after(end) -> ClassState.COMPLETED
            referenceCal.before(start) -> ClassState.UPCOMING
            else -> ClassState.CURRENT
        }
    }

    /**
     * Finds the next class (either currently ongoing or next upcoming) for today (or at the given [referenceCal]).
     */
    fun getNextClass(schedules: List<Schedule>, referenceCal: Calendar = Calendar.getInstance()): NextClassInfo? {
        val todayClasses = schedules.filter { isToday(it, referenceCal) }
        if (todayClasses.isEmpty()) return null

        val now = referenceCal

        // 1. First check if a class is currently ongoing
        val ongoing = todayClasses.firstOrNull {
            val times = parseClassTimes(it.time, now) ?: return@firstOrNull false
            now.timeInMillis >= times.first.timeInMillis && now.timeInMillis <= times.second.timeInMillis
        }
        if (ongoing != null) {
            return NextClassInfo(
                schedule = ongoing,
                state = ClassState.CURRENT,
                statusText = "Class in progress"
            )
        }

        // 2. Otherwise find the earliest upcoming class today
        val upcomingWithDiff = todayClasses.mapNotNull { sched ->
            val times = parseClassTimes(sched.time, now) ?: return@mapNotNull null
            val diffMs = times.first.timeInMillis - now.timeInMillis
            if (diffMs > 0) Pair(sched, diffMs) else null
        }.sortedBy { it.second }

        val nextUpcoming = upcomingWithDiff.firstOrNull()
        if (nextUpcoming != null) {
            val minutes = (nextUpcoming.second / (1000 * 60)).toInt()
            val text = if (minutes in 1..90) {
                "Starts in $minutes minutes"
            } else if (minutes < 1) {
                "Starting now"
            } else {
                "Starts at ${nextUpcoming.first.time?.split("-")?.firstOrNull()?.trim() ?: ""}"
            }
            return NextClassInfo(
                schedule = nextUpcoming.first,
                state = ClassState.UPCOMING,
                statusText = text
            )
        }

        return null
    }

    /**
     * Extracts readable room and building.
     */
    fun formatRoomBuilding(room: String?): String {
        if (room.isNullOrBlank()) return "TBA"
        return room.trim()
    }
}
