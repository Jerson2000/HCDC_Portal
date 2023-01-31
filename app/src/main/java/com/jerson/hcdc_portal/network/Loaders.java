package com.jerson.hcdc_portal.network;

import androidx.lifecycle.MutableLiveData;

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
            System.out.println("Wrong Password!");
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

    public static void grades(MutableLiveData<List<GradeModel>> data, MutableLiveData<String> response,String link) throws IOException {
        List<GradeModel> gradeModelList = new ArrayList<>();
        Connection.Response homepage = Jsoup.connect(AppConstants.baseUrl+link)
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

            Element span = spans.first();
            Element span2 = spans.last();

            System.out.println(span.text());
            System.out.println(span2.text());

            int rows =tableData.size() - 3;

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

                System.out.println("Code "+code.text());

                if(i== rows){

                    break;
                }

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
                                teacher.text()
                        );

                gradeModelList.add(model);


            }
        }

        data.postValue(gradeModelList);
    }
}
