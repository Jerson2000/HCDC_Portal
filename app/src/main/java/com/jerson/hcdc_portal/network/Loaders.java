package com.jerson.hcdc_portal.network;

import androidx.lifecycle.MutableLiveData;

import com.jerson.hcdc_portal.model.AccountLinksModel;
import com.jerson.hcdc_portal.model.AccountModel;
import com.jerson.hcdc_portal.model.DashboardModel;
import com.jerson.hcdc_portal.model.GradeLinksModel;
import com.jerson.hcdc_portal.model.GradeModel;
import com.jerson.hcdc_portal.util.AppConstants;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Loaders {
    private static final String TAG = "Loaders";
    private static Map<String, String> cookies;
    private static int timeout = 3 * 10000;
    static Map<String, String> headers = new HashMap<>();

    public static void login(MutableLiveData<String> response, String email, String password) throws IOException {
        headers.put("accept-encoding", "gzip, deflate");

        Connection.Response loginForm = Jsoup.connect(AppConstants.baseUrl + AppConstants.loginUrl)
                .timeout(timeout)
                .userAgent(AppConstants.userAgent)
                .headers(headers)
                .execute();

        cookies = loginForm.cookies();

        Document html = loginForm.parse();

        String authToken = html.select("input[name=_token]").first().attr("value");

        Map<String, String> formData = new HashMap<>();
        formData.put("_token", authToken);
        formData.put("email", email);
        formData.put("password", password);
        headers.put("Content-Type", "application/x-www-form-urlencoded");

        Connection.Response afterLoginPage = Jsoup.connect(AppConstants.baseUrl + AppConstants.loginPostUrl)
                .timeout(timeout)
                .cookies(cookies)
                .headers(headers)
                .userAgent(AppConstants.userAgent)
                .data(formData)
                .method(Connection.Method.POST)
                .referrer(AppConstants.baseUrl + AppConstants.loginUrl)
                .execute();

        // update cookies
        cookies = afterLoginPage.cookies();

        Connection.Response homePage = Jsoup.connect(AppConstants.baseUrl)
                .timeout(10000)
                .cookies(cookies).method(Connection.Method.GET)
                .userAgent(AppConstants.userAgent)
                .followRedirects(true)
                .referrer(AppConstants.baseUrl + AppConstants.loginPostUrl)
                .headers(headers)
                .execute();

        Document doc = homePage.parse();

        // if false the password is correct
        boolean wrongPass = doc.body().text().contains("CROSSIAN LOG-IN");

        if (wrongPass) {
            System.out.println("Incorrect Credentials!");
            response.postValue("Incorrect Credentials!");
        }
        if (!wrongPass) {
            System.out.println("Logged In!");
            response.postValue("Logged In!");
        }


    }

    public static void dashboard(MutableLiveData<List<DashboardModel>> data, MutableLiveData<String> response) throws IOException {


        List<DashboardModel> dashboardModelList = new ArrayList<>();
        Connection.Response homepage = Jsoup.connect(AppConstants.baseUrl)
                .timeout(timeout)
                .userAgent(AppConstants.userAgent)
                .headers(headers)
                .cookies(cookies)
                .method(Connection.Method.GET)
                .execute();
        Document doc = homepage.parse();
        Elements dashboardTable = doc.select("div.col-sm-9 tbody");


        int i = 0;
        for (Element list : dashboardTable) {
//            System.out.println(list.select("a[class=app-menu__item]").attr("href"));
            Elements tableData = list.select("tr");

            for (Element rowData : tableData) {
                //  System.out.println("----------- ROW "+i+"--------------"); // Test purposes
                i++;

                Elements offerNo = rowData.select("td:eq(0)");
                Elements gClass = rowData.select("td:eq(1)");
                Elements subjCode = rowData.select("td:eq(2)");
                Elements desc = rowData.select("td:eq(3)");
                Elements unit = rowData.select("td:eq(4)");
                Elements days = rowData.select("td:eq(5)");
                Elements time = rowData.select("td:eq(6)");
                Elements room = rowData.select("td:eq(7)");
                Elements lec_lab = rowData.select("td:eq(8)");

                DashboardModel model = new DashboardModel
                        (
                                offerNo.text(),
                                gClass.text(),
                                subjCode.text(),
                                desc.text(),
                                unit.text(),
                                days.text(),
                                time.text(),
                                room.text(),
                                lec_lab.text()
                        );

                dashboardModelList.add(model);


            }
        }

        data.postValue(dashboardModelList);

    }

    public static void gradesLink(MutableLiveData<List<GradeLinksModel>> gradeSemLinks, MutableLiveData<String> response) throws IOException {
        List<GradeLinksModel> gradesLinks = new ArrayList<>();
        Connection.Response gradePage = Jsoup.connect(AppConstants.baseUrl + AppConstants.gradesUrl)
                .timeout(timeout)
                .userAgent(AppConstants.userAgent)
                .headers(headers)
                .cookies(cookies)
                .method(Connection.Method.GET)
                .execute();
        Document doc = gradePage.parse();

        Elements semList = doc.select("main.app-content ul li.nav-item");

        for (Element list : semList) {
            String link = list.select("a.nav-link").attr("href");
            String text = list.select("a.nav-link").text();
            GradeLinksModel model = new GradeLinksModel(link, text);
            gradesLinks.add(model);

        }

        gradeSemLinks.postValue(gradesLinks);


    }

    public static void grades(MutableLiveData<List<GradeModel>> data, MutableLiveData<String> response, String link) throws IOException {
        List<GradeModel> gradeModelList = new ArrayList<>();
        Connection.Response homepage = Jsoup.connect(AppConstants.baseUrl + link)
                .timeout(timeout)
                .userAgent(AppConstants.userAgent)
                .headers(headers)
                .cookies(cookies)
                .method(Connection.Method.GET)
                .execute();
        Document doc = homepage.parse();
        Elements table = doc.select("div.col-md-9 tbody");


        int i = 0;
        for (Element list : table) {
            Elements tableData = list.select("tr");
            Elements spans = list.select("span");


            int rows = tableData.size() - 3;
            int rows2 = tableData.size() - 1;
            int i2 = 0;
            String earnText, earn = null, aveText, ave = null;


            // GET AVERAGE and EARNED UNITS
            for (Element rowData : tableData) {

                Elements earnUnitsText = rowData.select("td:eq(0)");
                Elements earnUnits = rowData.select("td:eq(1)");
                Elements averageText = rowData.select("td:eq(2)");
                Elements average = rowData.select("td:eq(3)");

                earnText = earnUnitsText.text();
                earn = earnUnits.text();
                aveText = averageText.text();
                ave = average.text();

                i2++;

                if (i2 == rows2) {
                    System.out.println(earnText);
                    System.out.println(earn);
                    System.out.println(aveText);
                    System.out.println(ave);

//                    GradeModel model = new GradeModel();
//                    gradeModelList.add(model);
                    break;
                }

            }


            // GET ALL SUBJECTS GRADES
            for (Element rowData : tableData) {
                //  System.out.println("----------- ROW "+i+"--------------"); // Test purposes
                i++;

                Elements code = rowData.select("td:eq(0)");
                Elements subject = rowData.select("td:eq(1)");
                Elements desc = rowData.select("td:eq(2)");
                Elements unit = rowData.select("td:eq(3)");
                Elements midgrade = rowData.select("td:eq(4)");
                Elements mideremark = rowData.select("td:eq(5)");
                Elements finalgrade = rowData.select("td:eq(6)");
                Elements finalremark = rowData.select("td:eq(7)");
                Elements teacher = rowData.select("td:eq(8)");

//                System.out.println("Code "+code.text());

                GradeModel model = new GradeModel
                        (
                                code.text(),
                                subject.text(),
                                desc.text(),
                                unit.text(),
                                midgrade.text(),
                                mideremark.text(),
                                finalgrade.text(),
                                finalremark.text(),
                                teacher.text(),
                                earn,
                                ave
                        );
                gradeModelList.add(model);

                if (i == rows) {

                    break;
                }

            }

        }

        data.postValue(gradeModelList);
    }

    public static void accountLink(MutableLiveData<List<AccountLinksModel>> data, MutableLiveData<String> response) throws IOException {
        List<AccountLinksModel> links = new ArrayList<>();
        Connection.Response gradePage = Jsoup.connect(AppConstants.baseUrl + AppConstants.accountUrl)
                .timeout(timeout)
                .userAgent(AppConstants.userAgent)
                .headers(headers)
                .cookies(cookies)
                .method(Connection.Method.GET)
                .execute();
        Document doc = gradePage.parse();

        Elements semList = doc.select("main.app-content ul li.nav-item");

        for (Element list : semList) {
            String link = list.select("a.nav-link").attr("href");
            String text = list.select("a.nav-link").text();
            AccountLinksModel model = new AccountLinksModel(link, text);
            links.add(model);

        }

        data.postValue(links);
    }

    public static void account(MutableLiveData<List<AccountModel>> data, MutableLiveData<String> response, String link) throws IOException {
        List<AccountModel> accounts = new ArrayList<>();
        Connection.Response gradePage = Jsoup.connect(AppConstants.baseUrl + link)
                .timeout(timeout)
                .userAgent(AppConstants.userAgent)
                .headers(headers)
                .cookies(cookies)
                .method(Connection.Method.GET)
                .execute();
        Document doc = gradePage.parse();

        Elements table = doc.select("div.col-md-9 section.invoice tbody");

//        System.out.println(table);

        for (Element tabData : table) {
            Elements rowsData = tabData.select("tr");

//            System.out.println("ROWDATA::  "+rowsData);

            int iDue = rowsData.size() - 1;
            int indexDue = 0;
            int iData = rowsData.size() - 2;
            int indexData = 0;
            String dueTex = null;
            String dueDat = null;

            System.out.println(rowsData.text());

            // Get Due Payment
            for (Element row : rowsData) {
                indexDue++;
                Elements dueText = row.select("td:eq(0)");
                Elements dueData = row.select("td:eq(1)");
                // System.out.println("ROWS IN FOR LOOP::"+ indexDue+" :: "+row);
                if (indexDue == iDue) {

                    dueTex = dueText.text();
                    dueDat = dueData.text();
                    break;
                }
            }

            for (Element row : rowsData) {
                indexData++;
                Elements date = row.select("td:eq(0)");
                Elements ref = row.select("td:eq(1)");
                Elements desc = row.select("td:eq(2)");
                Elements period = row.select("td:eq(3)");
                Elements added = row.select("td:eq(4)");
                Elements deducted = row.select("td:eq(5)");
                Elements runBal = row.select("td:eq(6)");

                AccountModel model = new AccountModel
                        (
                                date.text(),
                                ref.text(),
                                desc.text(),
                                period.text(),
                                added.text(),
                                deducted.text(),
                                runBal.text(),
                                dueTex,
                                dueDat

                        );
                accounts.add(model);

                if (indexData == iData) {
                    break;
                }

            }

        }

        data.postValue(accounts);
    }

}
