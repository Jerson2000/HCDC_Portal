package com.jerson.hcdc_portal.data.parser

import com.jerson.hcdc_portal.domain.model.Schedule
import org.jsoup.nodes.Document
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScheduleParserImpl @Inject constructor() : ScheduleParser {

    override fun parseSchedules(doc: Document): List<Schedule> {
        val list = mutableListOf<Schedule>()
        val tBody = doc.select("div.col-sm-9 tbody")
        for (body in tBody) {
            val tableData = body.select("tr")
            for (rowData in tableData) {
                val offerNo = rowData.select("td:eq(0)").text().trim()
                val gClass = rowData.select("td:eq(1)").text().trim()
                val subjCode = rowData.select("td:eq(2)").text().trim()
                val desc = rowData.select("td:eq(3)").text().trim()
                val unit = rowData.select("td:eq(4)").text().trim()
                val days = rowData.select("td:eq(5)").text().trim()
                val time = rowData.select("td:eq(6)").text().trim()
                val room = rowData.select("td:eq(7)").text().trim()
                val lecLab = rowData.select("td:eq(8)").text().trim()

                // Skip header or invalid empty row
                if (subjCode.isNotBlank() || offerNo.isNotBlank()) {
                    list.add(
                        Schedule(
                            0,
                            offerNo,
                            gClass,
                            subjCode,
                            desc,
                            unit,
                            days,
                            time,
                            room,
                            lecLab
                        )
                    )
                }
            }
        }
        return list
    }

    override fun parseUserProfile(doc: Document): UserProfile {
        val units = doc.select("div.col-sm-9 > b").text().replace("Total Units: ", "").trim()
        val rawName = doc.select(".app-sidebar__user-name").text().trim()
        val name = if (rawName.isNotBlank()) {
            rawName.lowercase().split(Regex("\\s+")).joinToString(" ") { token ->
                token.replaceFirstChar { x -> x.uppercase() }
            }
        } else {
            ""
        }
        val rawDesignation = doc.select(".app-sidebar__user-designation").text().trim()
        val (course, studentId) = parseDesignation(rawDesignation)

        val enrollAnnounce = doc.select(".mybox-body > center > h5").text().trim()
        val isEnrolledText = doc.body().text()
        val isEnrolled = if (isEnrolledText.contains("Officially Enrolled")) {
            "Officially Enrolled."
        } else {
            "Not enrolled this sem."
        }
        val avatarUrl = doc.body().select(".app-sidebar__user-avatar").attr("src").ifBlank { null }

        return UserProfile(
            name = name,
            units = units,
            course = course,
            studentId = studentId,
            enrollAnnounce = enrollAnnounce,
            isEnrolled = isEnrolled,
            avatarUrl = avatarUrl
        )
    }

    companion object {
        fun parseDesignation(raw: String): Pair<String, String> {
            val trimmed = raw.trim()
            if (trimmed.isBlank()) return Pair("", "")

            // Case 1: Student ID in parentheses e.g. "BS Information Technology (1900123)"
            val parenMatch = Regex("""\(([^)]+)\)""").find(trimmed)
            if (parenMatch != null) {
                val studentId = parenMatch.groupValues[1].trim()
                val course = trimmed.substring(0, parenMatch.range.first).trim()
                return Pair(course, studentId)
            }

            // Case 2: Space-delimited e.g. "BSIT 1900123" or "BS Information Technology 1900123"
            val parts = trimmed.split(Regex("\\s+")).filter { it.isNotBlank() }
            return when {
                parts.isEmpty() -> Pair("", "")
                parts.size == 1 -> Pair(parts[0], "")
                else -> {
                    // Check if the last part looks like an ID (digits/hyphen) or just the last token
                    val last = parts.last()
                    val courseParts = parts.dropLast(1).joinToString(" ")
                    Pair(courseParts, last)
                }
            }
        }
    }
}
