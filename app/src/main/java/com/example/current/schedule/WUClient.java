package com.example.current.schedule;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.WindowManager;

import com.example.current.schedule.Login.LoginActivity;
import com.example.current.schedule.Schedule.Lesson;

import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Current on 08.01.2015.
 */
public class WUClient extends AsyncTask<String, String, Void> {

    private String loginURL = "https://wu.wsiz.rzeszow.pl/wunet/Logowanie2.aspx";
    private String scheduleUrl = "https://wu.wsiz.rzeszow.pl/wunet/PodzGodzin.aspx";

    //-----------------Schedule options-----------------
    public static String WEEK = "Tygodniowo";
    public static String SEMESTER = "Semestralnie";

    static String SCHEDULE_TABLE_ID = "ctl00_ctl00_ContentPlaceHolder_RightContentPlaceHolder_dgDane";
    //--------------------------------------------------
    private ProgressDialog progressDialog;

    LoginActivity funcDonor;
    private Context loginActivityContext;

    public WUClient(LoginActivity activity, Context context){
        funcDonor = activity;
        loginActivityContext = context;
    }

    @Override
    protected void onPreExecute() {
        //Create a new progress dialog
        progressDialog = new ProgressDialog(loginActivityContext);
        //Enabling to show in BRUTAL WAY
        progressDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        //Set the progress dialog to display a horizontal progress bar
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        //Set the dialog title to 'Loading...'
        progressDialog.setTitle(funcDonor.getString(R.string.connection_dialog_title));
        //Set the dialog message to 'Loading application View, please wait...'
        progressDialog.setMessage(funcDonor.getString(R.string.connection_dialog_text1));
        //This dialog can't be canceled by pressing the back key
        progressDialog.setCancelable(true);
        //This dialog isn't indeterminate
        progressDialog.setIndeterminate(false);
        //The maximum number of items is 100
        progressDialog.setMax(100);
        //Set the current progress to zero
        progressDialog.setProgress(0);
        //Display the progress dialog
        progressDialog.show();
    }

    @Override
    protected Void doInBackground(String... params) {



        switch (params[0]){

            //region case "checkLogin"
            case "Connect":

                // if connected to internet the check login/password, else show error message
                if (checkConnection()){

                    //updating info
                    publishProgress(funcDonor.getString(R.string.connection_dialog_text2));

                    // if login/password is correct, them show main activity else show error message
                    if(checkLogin(params[1], params[2])) {
                        // start main activity from ui thread
                        funcDonor.startMainActivity();
                        //getSchedule(SEMESTER);
                        //simpleScheduleGet();

                    } else funcDonor.showLoginError();

                } else funcDonor.showConnetionError();
                break;
            //endregion

            case "getSchedule":

                break;


        }


        return null;
    }


    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        progressDialog.setMessage(values[0]);
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        progressDialog.dismiss();
    }


    private boolean checkLogin(String login, String password){
        HttpClient httpClient = new DefaultHttpClient();
        // replace with your url
        HttpPost postRequest = new HttpPost(loginURL);

//        //Post headers -----------------------------------------------<< Insert headers and check of works
//        postRequest.setHeader("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
//        postRequest.setHeader("Origin","https://wu.wsiz.rzeszow.pl");
//        postRequest.setHeader("User-Agent","Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.95 Safari/537.36");
//        postRequest.setHeader("Content-Type","application/x-www-form-urlencoded");
//        postRequest.setHeader("Referer","https://wu.wsiz.rzeszow.pl/wunet/Logowanie2.aspx?ReturnUrl=%2fwunet%2fLogoutLiveComRedirect.aspx");
//        postRequest.setHeader("Accept-Encoding","gzip, deflate");
//        postRequest.setHeader("Accept-Language","uk,pl;q=0.8,ru;q=0.6,en-US;q=0.4,en;q=0.2");
//        postRequest.setHeader("","");


        // SET CONNECTION TYPE ! ! !

        //Post Keys
        List<NameValuePair> nameValuePair = new ArrayList<>(2);
        nameValuePair.add(new BasicNameValuePair("ctl00_ctl00_ScriptManager1_HiddenField",""));
        nameValuePair.add(new BasicNameValuePair("__EVENTTARGET", ""));
        nameValuePair.add(new BasicNameValuePair("__EVENTARGUMENT", ""));
        nameValuePair.add(new BasicNameValuePair("__VIEWSTATE", ""));
        nameValuePair.add(new BasicNameValuePair("ctl00_ctl00_TopMenuPlaceHolder_TopMenuContentPlaceHolder_MenuTop3_menuTop3_ClientState", ""));
        nameValuePair.add(new BasicNameValuePair("ctl00$ctl00$ContentPlaceHolder$MiddleContentPlaceHolder$txtIdent", login));
        nameValuePair.add(new BasicNameValuePair("ctl00$ctl00$ContentPlaceHolder$MiddleContentPlaceHolder$txtHaslo", password));
        nameValuePair.add(new BasicNameValuePair("ctl00$ctl00$ContentPlaceHolder$MiddleContentPlaceHolder$butLoguj", "Zaloguj"));

        //Encoding POST data
        try {
            postRequest.setEntity(new UrlEncodedFormEntity(nameValuePair));
        } catch (UnsupportedEncodingException e) {
            // log exception
            e.printStackTrace();
        }

        //making POST request.
        try {
            HttpResponse response = httpClient.execute(postRequest);

            // write response to log
            Log.d("Http Post Response:", response.toString());



            String html = EntityUtils.toString(response.getEntity());
            //try to find some random element on page:
            Document d = Jsoup.parse(html);
            // if it finds that element - connection is fine
            d.getElementById("ctl00_ctl00_ContentPlaceHolder_MiddleContentPlaceHolder_spnLogowanie");

            Element nameEl = d.getElementById("ctl00_ctl00_ContentPlaceHolder_wumasterWhoIsLoggedIn");;
            String userName = nameEl.ownText();

            funcDonor.showToast("Logged as: " + userName);

            return true;

        } catch (Exception e) {
            // Log exception
            e.printStackTrace();
            return false;
        }



    }

    ArrayList<Lesson> getSchedule(String how) {
        HttpClient httpClient = new DefaultHttpClient();
        // replace with your url
        HttpPost httpPost = new HttpPost(scheduleUrl);


        //Post Data
        List<NameValuePair> nameValuePair = new ArrayList<>(2);
        //auth?
        nameValuePair.add(new BasicNameValuePair("ctl00$ctl00$ContentPlaceHolder$MiddleContentPlaceHolder$txtIdent","w47186"));
        nameValuePair.add(new BasicNameValuePair("ctl00$ctl00$ContentPlaceHolder$MiddleContentPlaceHolder$txtHaslo","35130070700"));
        nameValuePair.add(new BasicNameValuePair("ctl00$ctl00$ContentPlaceHolder$MiddleContentPlaceHolder$butLoguj","Zaloguj"));
        //body
        nameValuePair.add(new BasicNameValuePair("__EVENTARGUMENT",""));
        nameValuePair.add(new BasicNameValuePair("__EVENTTARGET","ctl00$ctl00$ContentPlaceHolder$RightContentPlaceHolder$rbJak$1"));
        nameValuePair.add(new BasicNameValuePair("__EVENTVALIDATION","/wEWCwL/o48sAoGrw68HAqOi9q4EAqfoyc4GAti0wcwKAuemvPkFAv/7iKwEAtWxoogIArD/osELApz3uMsEApbL89oHLZ3OfZaoKcaNKnbf5ST+kLJZIcc="));
        nameValuePair.add(new BasicNameValuePair("__LASTFOCUS",""));
        nameValuePair.add(new BasicNameValuePair("__VIEWSTATE",""));
        nameValuePair.add(new BasicNameValuePair("ctl00$ctl00$ContentPlaceHolder$RightContentPlaceHolder$hid_Temp",""));
        nameValuePair.add(new BasicNameValuePair("ctl00$ctl00$ContentPlaceHolder$RightContentPlaceHolder$rbJak",how));
        nameValuePair.add(new BasicNameValuePair("ctl00_ctl00_ContentPlaceHolder_wumasterMenuLeft_radMenu_ClientState",""));
        nameValuePair.add(new BasicNameValuePair("ctl00_ctl00_ScriptManager1_HiddenField",""));
        nameValuePair.add(new BasicNameValuePair("ctl00_ctl00_TopMenuPlaceHolder_MenuTop2_menuTop2_ClientState",""));
        nameValuePair.add(new BasicNameValuePair("ctl00_ctl00_TopMenuPlaceHolder_wumasterMenuTop_menuTop_ClientState",""));


        //Encoding POST data
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair));
        } catch (UnsupportedEncodingException e) {
            // log exception
            e.printStackTrace();
        }

        //making POST request.
        try {
            HttpResponse response = httpClient.execute(httpPost);
            // write response to log
            Log.d("Http Post Response:", response.toString());

            // get String html of schedule page and parse it
            String scheduleHTML = EntityUtils.toString(response.getEntity());
            Document d = Jsoup.parse(scheduleHTML);

            // find schedule table
            Element table = d.getElementById(SCHEDULE_TABLE_ID);
            // get all lessons
            Elements rows = table.getElementsByTag("tr");

            // new lesson array list
            ArrayList<Lesson> lessons = new ArrayList<>();
            // extracting lesson attributes into array list
            for (Element cell : rows) {

                String lessonName = cell.getElementsByTag("td").get(0).text();
                String lessonType = cell.getElementsByTag("td").get(6).text();
                String lessonImportance = cell.getElementsByTag("td").get(7).text();
                String room = cell.getElementsByTag("td").get(5).text();
                String date = cell.getElementsByTag("td").get(2).text();
                String fromHour = cell.getElementsByTag("td").get(3).text();
                String toHour = cell.getElementsByTag("td").get(4).text();

                lessons.add(new Lesson(lessonName, lessonType, lessonImportance, room, date, fromHour, toHour));
            }



            return null;

        } catch (Exception e) {
            // Log exception
            e.printStackTrace();
            return null;
        }
    }

    private boolean checkConnection(){
        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet(loginURL);
        // replace with your url

        HttpResponse response;
        try {
            response = client.execute(request);

            Log.d("Response of GET request", response.toString());

            //try to find some random element on page:
            Document d = Jsoup.parse(response.toString());
            // if it finds that element - connection is fine
            d.getElementById("ctl00_ctl00_ContentPlaceHolder_MiddleContentPlaceHolder_spnLogowanie");
            return true;

        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Performs an HTTPS POST with the given data.
     * @param urlString The URL to POST to.
     * @param variables key/value pairs for all parameters to be POSTed.
     * @return The data passed back from the server, as a String.
     * @throws IOException if the network connection failed.
     * @throws HttpException if the HTTP transaction failed
     */
    public String postHttpsContent(String urlString,Map<String,String> variables) throws IOException, HttpException {
        String response="";
        URL url=new URL(urlString);
        HttpsURLConnection httpsConnection=(HttpsURLConnection)url.openConnection();
//        if (mHostnameVerifier != null) {
//            httpsConnection.setHostnameVerifier(mHostnameVerifier);
//        }
        httpsConnection.setDoInput(true);
        httpsConnection.setDoOutput(true);
        httpsConnection.setUseCaches(false);
        httpsConnection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
        String postData="";
        for (  String key : variables.keySet()) {
            postData+="&" + key + "="+ variables.get(key);
        }
        postData=postData.substring(1);
        DataOutputStream postOut=new DataOutputStream(httpsConnection.getOutputStream());
        postOut.writeBytes(postData);
        postOut.flush();
        postOut.close();
        int responseCode=httpsConnection.getResponseCode();
        if (responseCode == HttpsURLConnection.HTTP_OK) {
            String line;
            BufferedReader br=new BufferedReader(new InputStreamReader(httpsConnection.getInputStream()));
            while ((line=br.readLine()) != null) {
                response+=line;
            }
        }
        else {
            response="";
            Log.e("TAG","HTTPs request failed on: " + urlString + " With error code: "+ responseCode);
            throw new HttpException(responseCode + "");
        }
        return response;
    }


}