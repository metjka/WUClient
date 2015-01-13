package com.example.current.schedule;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.current.schedule.Login.LoginActivity;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class WUClient extends AsyncTask<String, String, Void>{
    private List<String> cookies;
    private HttpsURLConnection connection;

    private String loginUrl = "https://wu.wsiz.rzeszow.pl/wunet/Logowanie2.aspx";

    LoginActivity funcDonor;
    private Context loginActivityContext;

    ProgressDialog progressDialog;

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

        String login = params[1];
        String password = params[2];

        switch (params[0]){

            case "Connect":
                try { // to set connection
                    setConnection();
                    //updating info
                    publishProgress(funcDonor.getString(R.string.connection_dialog_text2));
                    try {
                        authorize(login, password);
                    } catch (Exception e) { // if couldn't log in with passed login and password
                        e.printStackTrace();
                        funcDonor.showLoginError();
                    }
                } catch (Exception e) { // if couldn't connect - show error in main thread
                    funcDonor.showConnetionError();
                }
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


    //region Additional Methods
    void setConnection() throws Exception{
        getPageContent(loginUrl);
    }

    private String getPageContent(String url) throws Exception {

        URL obj = new URL(url);
        connection = (HttpsURLConnection) obj.openConnection();

        // default is GET
        connection.setRequestMethod("GET");

        connection.setUseCaches(false);

        // act like a browser
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");
        connection.setRequestProperty("Accept",
                "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        connection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        if (cookies != null) {
            for (String cookie : this.cookies) {
                connection.addRequestProperty("Cookie", cookie.split(";", 1)[0]);
            }
        }
        int responseCode = connection.getResponseCode();
        System.out.println("\nSending 'GET' request to URL : " + url);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in =
                new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        // Get the response cookies
        setCookies(connection.getHeaderFields().get("Set-Cookie"));

        // Parsing HTML for getting title
        String html = response.toString();
        //If didn't found site title, then there is connection problem
//        if ( html.contains("<title>Wirtualna Uczelnia</title>")) funcDonor.showToast("Connected, baby!");

        return response.toString();
    }

    void authorize(String login, String password) throws Exception{

        URL url = new URL(loginUrl);
        connection = (HttpsURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setUseCaches(false);


        //region Setting headers
        connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        connection.setRequestProperty("Accept-Encoding", "gzip, deflate");
        connection.setRequestProperty("Accept-Language", "uk,pl;q=0.8,ru;q=0.6,en-US;q=0.4,en;q=0.2");
        connection.setRequestProperty("Cache-Control", "max-age=0");
        connection.setRequestProperty("Connection", "keep-alive");
        connection.setRequestProperty("Content-Length", "1104");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setRequestProperty("Host", "wu.wsiz.rzeszow.pl");
        connection.setRequestProperty("Origin", "https://wu.wsiz.rzeszow.pl");
        connection.setRequestProperty("Referer", "https://wu.wsiz.rzeszow.pl/wunet/Logowanie2.aspx");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.95 Safari/537.36");
        //endregion

        connection.setDoOutput(true);
        connection.setDoInput(true);

        //region Setting keys
        List<String> paramList = new ArrayList<String>();
        paramList.add("ctl00_ctl00_ScriptManager1_HiddenField=");
        paramList.add("__EVENTTARGET=");
        paramList.add("__EVENTARGUMENT=");
        paramList.add("__VIEWSTATE=");
        paramList.add("ctl00_ctl00_TopMenuPlaceHolder_TopMenuContentPlaceHolder_MenuTop3_menuTop3_ClientState=");
        paramList.add("ctl00$ctl00$ContentPlaceHolder$MiddleContentPlaceHolder$txtIdent=" + login);
        paramList.add("ctl00$ctl00$ContentPlaceHolder$MiddleContentPlaceHolder$txtHaslo=" + password);
        paramList.add("ctl00$ctl00$ContentPlaceHolder$MiddleContentPlaceHolder$butLoguj=Zaloguj");
        //endregion

        // build parameters list
        StringBuilder paramsBuilder = new StringBuilder();
        for (String param : paramList) {
            if (paramsBuilder.length() == 0) {
                paramsBuilder.append(param);
            } else {
                paramsBuilder.append("&" + param);
            }
        }
        String postParams = paramsBuilder.toString();

        //send the POST out
        PrintWriter out = new PrintWriter(connection.getOutputStream());
        out.print(postParams);
        out.close();

        int responseCode = connection.getResponseCode();
        System.out.println("POST Response Code:" + responseCode);

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
    }


    //endregion

    //region Cookies
    public List<String> getCookies() {
        return cookies;
    }
    public void setCookies(List<String> cookies) {
        this.cookies = cookies;
    }
    //endregion

}