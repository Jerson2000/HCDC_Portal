package com.jerson.hcdc_portal.data.parser

import com.jerson.hcdc_portal.domain.model.Grade
import com.jerson.hcdc_portal.domain.model.Term
import org.jsoup.nodes.Document
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GradeParserImpl @Inject constructor() : GradeParser {

    override fun parseGrades(doc: Document, termId: Int): List<Grade> {
        val list = mutableListOf<Grade>()
        val table = doc.select("div.col-md-12 tbody")
        var weightedAve = table.select("tbody tr:nth-last-child(2) > td:eq(3)").text().trim()
        var earnedUnits = table.select("tbody tr:nth-last-child(2) > td:eq(1)").text().trim()
        val term = doc.select("select.select2-multiple.form-control option[selected]").first()?.text()?.trim() ?: ""

        val rows = table.select("tr")
        if (weightedAve.isBlank() || earnedUnits.isBlank()) {
            for (row in rows) {
                val rowText = row.text().lowercase()
                if (rowText.contains("earned units") || rowText.contains("weighted") || rowText.contains("gwa")) {
                    val cells = row.select("td")
                    for (i in 0 until cells.size) {
                        val cellText = cells[i].text().trim()
                        if (cellText.contains("Earned", ignoreCase = true) && i + 1 < cells.size && earnedUnits.isBlank()) {
                            earnedUnits = cells[i + 1].text().trim()
                        }
                        if ((cellText.contains("Average", ignoreCase = true) || cellText.contains("GWA", ignoreCase = true)) && i + 1 < cells.size && weightedAve.isBlank()) {
                            weightedAve = cells[i + 1].text().trim()
                        }
                    }
                }
            }
        }

        for (x in rows) {
            val rowText = x.text().lowercase()
            if (rowText.contains("total earned units") ||
                rowText.contains("weighted average") ||
                rowText.contains("general weighted") ||
                rowText.contains("gwa") ||
                rowText.contains("grading system")
            ) {
                continue
            }

            val cells = x.select("td")
            if (cells.size < 2) continue

            val offeredNo = x.select("td:eq(0)").text().trim()
            val subjectCode = x.select("td:eq(1)").text().trim()
            val description = x.select("td:eq(2)").text().trim()
            val unit = x.select("td:eq(3)").text().trim()
            val midtermGrade = x.select("td:eq(4)").text().trim()
            val midtermRemark = x.select("td:eq(5)").text().trim()
            val finalGrade = x.select("td:eq(6)").text().trim()
            val finalRemark = x.select("td:eq(7)").text().trim()
            val teacher = x.select("td:eq(8)").text().trim()

            if (subjectCode.isNotBlank() || offeredNo.isNotBlank()) {
                list.add(
                    Grade(
                        0,
                        termId,
                        term,
                        offeredNo,
                        subjectCode,
                        description,
                        unit,
                        midtermGrade,
                        midtermRemark,
                        finalGrade,
                        finalRemark,
                        earnedUnits,
                        weightedAve,
                        teacher
                    )
                )
            }
        }
        return list
    }

    override fun parseTerms(doc: Document): List<Term> {
        val list = mutableListOf<Term>()
        val items = doc.select("main.app-content select.select2-multiple.form-control option")

        for (item in items) {
            val term = item.text().trim()
            val link = item.attr("value").trim()
            if (term.isNotBlank()) {
                list.add(Term(0, link, term, 1))
            }
        }
        return list.sortedByDescending { it.term }
    }
}
