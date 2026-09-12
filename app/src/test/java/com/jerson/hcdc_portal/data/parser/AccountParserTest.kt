package com.jerson.hcdc_portal.data.parser

import org.jsoup.Jsoup
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AccountParserTest {

    private lateinit var parser: AccountParserImpl

    @Before
    fun setUp() {
        parser = AccountParserImpl()
    }

    @Test
    fun parseAccounts_standardStatement_returnsLedgerAndDue() {
        val html = """
            <html>
            <body>
                <select class="select2-multiple form-control">
                    <option value="/account_hed/1" selected>1st Semester 2023-2024</option>
                </select>
                <div class="col-md-12">
                    <section class="invoice">
                        <table>
                            <tbody>
                                <tr>
                                    <td>2023-08-15</td>
                                    <td>OR-1001</td>
                                    <td>Downpayment</td>
                                    <td>Prelim</td>
                                    <td>0.00</td>
                                    <td>5,000.00</td>
                                    <td>15,000.00</td>
                                </tr>
                                <tr>
                                    <td>2023-09-20</td>
                                    <td>OR-1045</td>
                                    <td>Midterm Assessment</td>
                                    <td>Midterm</td>
                                    <td>1,200.00</td>
                                    <td>0.00</td>
                                    <td>16,200.00</td>
                                </tr>
                                <tr>
                                    <td>Total Due: Prelim</td>
                                    <td>Php 3,500.00</td>
                                    <td></td><td></td><td></td><td></td><td></td>
                                </tr>
                            </tbody>
                        </table>
                    </section>
                </div>
            </body>
            </html>
        """.trimIndent()

        val doc = Jsoup.parse(html)
        val accounts = parser.parseAccounts(doc, 1)

        assertEquals(2, accounts.size)
        assertEquals("2023-08-15", accounts[0].date)
        assertEquals("OR-1001", accounts[0].reference)
        assertEquals("Downpayment", accounts[0].description)
        assertEquals("Prelim", accounts[0].period)
        assertEquals("5,000.00", accounts[0].deducted)
        assertEquals("15,000.00", accounts[0].runningBal)
        assertEquals("Total Due: Prelim", accounts[0].dueTextPeriod)
        assertEquals("Php 3,500.00", accounts[0].dueAmount)
        assertEquals("1st Semester 2023-2024", accounts[0].term)
        assertEquals(1, accounts[0].termId)
    }

    @Test
    fun parseAccounts_singleLedgerRow_doesNotDropRow() {
        val html = """
            <html>
            <body>
                <select class="select2-multiple form-control">
                    <option value="/account_hed/2" selected>Summer 2023</option>
                </select>
                <div class="col-md-12">
                    <section class="invoice">
                        <table>
                            <tbody>
                                <tr>
                                    <td>2023-06-01</td>
                                    <td>OR-5000</td>
                                    <td>Tuition Summer</td>
                                    <td>Final</td>
                                    <td>4,500.00</td>
                                    <td>4,500.00</td>
                                    <td>0.00</td>
                                </tr>
                            </tbody>
                        </table>
                    </section>
                </div>
            </body>
            </html>
        """.trimIndent()

        val doc = Jsoup.parse(html)
        val accounts = parser.parseAccounts(doc, 2)

        // Under old code, rows.size < 2 caused emptyList()
        assertEquals(1, accounts.size)
        assertEquals("Tuition Summer", accounts[0].description)
        assertEquals("0.00", accounts[0].runningBal)
    }

    @Test
    fun parseAccounts_activeTabNoLedger_returnsPlaceholderAccount() {
        val html = """
            <html>
            <body>
                <li class="nav-item"><a class="active">Account</a></li>
                <select class="select2-multiple form-control">
                    <option value="/account_hed/3" selected>2nd Semester 2023-2024</option>
                </select>
                <div class="col-md-12">
                    <section class="invoice">
                        <table>
                            <tbody>
                            </tbody>
                        </table>
                    </section>
                </div>
            </body>
            </html>
        """.trimIndent()

        val doc = Jsoup.parse(html)
        val accounts = parser.parseAccounts(doc, 3)

        assertEquals(1, accounts.size)
        assertEquals(3, accounts[0].termId)
        assertEquals("2nd Semester 2023-2024", accounts[0].term)
        assertTrue(accounts[0].description.isNullOrEmpty())
    }

    @Test
    fun parseTerms_accountCategory() {
        val html = """
            <html>
            <body>
                <main class="app-content">
                    <select class="select2-multiple form-control">
                        <option value="/account_hed/1">Term 1</option>
                        <option value="/account_hed/2">Term 2</option>
                    </select>
                </main>
            </body>
            </html>
        """.trimIndent()

        val doc = Jsoup.parse(html)
        val terms = parser.parseTerms(doc)

        assertEquals(2, terms.size)
        assertEquals(2, terms[0].isGrade)
    }
}
