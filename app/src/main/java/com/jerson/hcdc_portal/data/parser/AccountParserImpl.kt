package com.jerson.hcdc_portal.data.parser

import com.jerson.hcdc_portal.domain.model.Account
import com.jerson.hcdc_portal.domain.model.Term
import org.jsoup.nodes.Document
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountParserImpl @Inject constructor() : AccountParser {

    override fun parseAccounts(doc: Document, termId: Int): List<Account> {
        val list = mutableListOf<Account>()
        val table = doc.select("div.col-md-12 section.invoice table > tbody")
        val term = doc.select("select.select2-multiple.form-control option[selected]").first()?.text()?.trim() ?: ""
        val rows = table.select("tr")
        var explicitDueText = ""
        var explicitDueAmount = ""

        for (row in rows) {
            val rowText = row.text().lowercase()
            if (rowText.contains("due") || rowText.contains("total balance") || rowText.contains("remaining balance")) {
                val td0 = row.select("td:eq(0)").text().trim()
                val td1 = row.select("td:eq(1)").text().trim()
                if (td0.isNotBlank()) explicitDueText = td0
                if (td1.isNotBlank()) explicitDueAmount = td1
            }
        }

        var dueText = explicitDueText.ifBlank { table.select("tr:nth-last-child(2) > td:eq(0)").text().trim() }
        var dueAmount = explicitDueAmount.ifBlank { table.select("tr:nth-last-child(2) > td:eq(1)").text().trim() }
        if (dueText.isBlank()) {
            dueText = doc.select("tbody tr:nth-last-child(2) > td:eq(0)").text().trim()
        }
        if (dueAmount.isBlank()) {
            dueAmount = doc.select("tbody tr:nth-last-child(2) > td:eq(1)").text().trim()
        }

        for (row in rows) {
            val rowText = row.text().lowercase()
            if (rowText.contains("due") || rowText.contains("total balance") || rowText.contains("remaining balance")) {
                continue
            }

            val cells = row.select("td")
            if (cells.size < 3) continue

            val date = row.select("td:eq(0)").text().trim()
            val ref = row.select("td:eq(1)").text().trim()
            val desc = row.select("td:eq(2)").text().trim()
            val period = row.select("td:eq(3)").text().trim()
            val added = row.select("td:eq(4)").text().trim()
            val deducted = row.select("td:eq(5)").text().trim()
            val runBal = row.select("td:eq(6)").text().trim()

            if (desc.isNotBlank() || ref.isNotBlank() || date.isNotBlank()) {
                list.add(
                    Account(
                        0,
                        termId,
                        term,
                        date,
                        ref,
                        desc,
                        period,
                        added,
                        deducted,
                        runBal,
                        dueText,
                        dueAmount
                    )
                )
            }
        }

        // Account with no ledger rows yet but valid active account page
        if (list.isEmpty() && doc.select("li.nav-item a").hasClass("active")) {
            list.add(
                Account(
                    0,
                    termId,
                    term,
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    dueText,
                    dueAmount
                )
            )
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
                list.add(Term(0, link, term, 2))
            }
        }
        return list.sortedByDescending { it.term }
    }
}
