package com.jerson.hcdc_portal.data.parser

import com.jerson.hcdc_portal.domain.model.EnrollHistory
import com.jerson.hcdc_portal.domain.model.Term
import org.jsoup.nodes.Document
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EnrollHistoryParserImpl @Inject constructor() : EnrollHistoryParser {

    override fun parseEnrollHistory(doc: Document, termId: Int): List<EnrollHistory> {
        val list = mutableListOf<EnrollHistory>()
        val tableBody = doc.select("div.tiles table > tbody")
        val term = doc.select("select.select2-multiple.form-control option[selected]").first()?.text()?.trim() ?: ""

        for (row in tableBody.select("tr")) {
            val offeredNo = row.select("td:eq(0)").text().trim()
            val subjectCode = row.select("td:eq(1)").text().trim()
            val description = row.select("td:eq(2)").text().trim()
            val unit = row.select("td:eq(3)").text().trim()

            if (subjectCode.isNotBlank() || offeredNo.isNotBlank()) {
                list.add(
                    EnrollHistory(
                        id = 0,
                        termId = termId,
                        term = term,
                        offeredNo = offeredNo,
                        subjectCode = subjectCode,
                        description = description,
                        unit = unit
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
                list.add(Term(0, link, term, 0))
            }
        }
        return list.sortedByDescending { it.term }
    }
}
