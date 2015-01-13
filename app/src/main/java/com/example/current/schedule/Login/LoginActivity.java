package com.example.current.schedule.Login;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.current.schedule.R;
import com.example.current.schedule.ScheduleActivity;
import com.example.current.schedule.WUClient;

import java.util.Locale;


public class LoginActivity extends Activity {



    // top image
    ImageView logoAndText;

    // log in button
    Button logInButton;

    // Polish, Ukrainian and English language buttons
    Button[] langButtons = new Button[3];
    // Current language
    int activeLang;

    Locale myLocale;


    //Inputs
    EditText loginField, passwordField;

    // WU Callback ----------------------------------
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //region Setting shrinking background layout
        ((ShrinkingLinearLayout)findViewById(R.id.loginLinearLayout)).setOnSoftKeyboardListener(new ShrinkingLinearLayout.OnSoftKeyboardListener() {
            // hides ui elements when keyboard shows
            @Override
            public void onShown() {
                findViewById(R.id.errorTextView).setVisibility(View.INVISIBLE);

                findViewById(R.id.logoAndText).setVisibility(View.GONE);
                findViewById(R.id.second_space).setVisibility(View.GONE);
                findViewById(R.id.language_panel).setVisibility(View.GONE);
            }
            // shows hidden elements when keyboard hides
            @Override
            public void onHidden() {
                findViewById(R.id.logoAndText).setVisibility(View.VISIBLE);
                findViewById(R.id.second_space).setVisibility(View.VISIBLE);
                findViewById(R.id.language_panel).setVisibility(View.VISIBLE);
            }

        });
        //endregion
        /**/
        //region Setting UI elements
        setUIElements();
        //endregion
        /**/
        //region Defying login button
        logInButton = (Button) findViewById(R.id.loginBtn);
        logInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.errorTextView).setVisibility(View.GONE);

                // hide keyboard
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

                String login = loginField.getText() + "";
                String password = passwordField.getText() + "";


                WUClient https = new WUClient(LoginActivity.this, LoginActivity.this.getBaseContext());
                https.execute("Connect", login, password);

                //startMainActivity();

            }
        });
        //endregion

    }

    //region Setting other stuff
    private void setUIElements() {
        // defying login button
        logInButton = (Button) findViewById(R.id.loginBtn);

        // defying language buttons
        langButtons[0] = (Button) findViewById(R.id.btnPL);
        langButtons[1] = (Button) findViewById(R.id.btnUA);
        langButtons[2] = (Button) findViewById(R.id.btnEN);

        // setting them onClickListener
        for (Button b : langButtons) b.setOnClickListener(langTouch);

        // defying imageView and checking active language
        logoAndText = (ImageView) findViewById(R.id.logoAndText);
        // set image depending on system language
        switch (getResources().getConfiguration().locale.getLanguage()){
            case "en":
                logoAndText.setBackgroundResource(R.drawable.logoandtext_en);
                activeLang = 2;
                break;
            case "uk":
                logoAndText.setBackgroundResource(R.drawable.logoandtext_ua);
                activeLang = 1;
                break;
            default:
                logoAndText.setBackgroundResource(R.drawable.logoandtext_pl);
                activeLang = 0;
                break;
        }
        // highlighting active language button
        langButtons[activeLang].setBackgroundResource(R.drawable.active_language_btn);

        // defying text fields
        loginField = (EditText) findViewById(R.id.loginField);
        passwordField = (EditText) findViewById(R.id.passwordField);
    }

    public void showToast(final String toast) {
        runOnUiThread(new Runnable() {
            public void run()
            {
                Toast.makeText(LoginActivity.this, toast, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void showConnetionError() {
        runOnUiThread(new Runnable() {
            public void run()
            {
                TextView errorTextView = (TextView) findViewById(R.id.errorTextView);
                errorTextView.setText(getResources().getString(R.string.connection_error_text));
                errorTextView.setVisibility(View.VISIBLE);
            }
        });
    }

    public void startMainActivity() {
        runOnUiThread(new Runnable() {
            public void run()
            {
                Intent Main = new Intent(LoginActivity.this, ScheduleActivity.class);
                startActivity(Main);
            }
        });
    }


    public void showLoginError() {
        runOnUiThread(new Runnable() {
            public void run()
            {
                //setting error text view
                TextView errorTextView = (TextView) findViewById(R.id.errorTextView);
                errorTextView.setText(getResources().getString(R.string.login_error_text));
                errorTextView.setVisibility(View.VISIBLE);

                //clearing inputs
                loginField.setText("");
                passwordField.setText("");
            }
        });
    }

    //endregion

    //region Changing language

    public void changeLang(int i) {

        String[] langs = {"pl", "uk", "en"};
        String lang = langs[i];

        myLocale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);

        Intent refresh = new Intent(this, LoginActivity.class);
        finish();
        startActivity(refresh);
        overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);
    }

    View.OnClickListener langTouch = new View.OnClickListener() {

        @Override
        public void onClick(final View v) {
            langButtons[activeLang].setBackgroundResource(R.drawable.language_btn);

            switch (v.getId()){

                case R.id.btnPL:
                    activeLang = 0; break;

                case R.id.btnUA:
                    activeLang = 1; break;

                case R.id.btnEN:
                    activeLang = 2; break;
            }

            //change language . . .
            langButtons[activeLang].setBackgroundResource(R.drawable.active_language_btn);
            changeLang(activeLang);


        }


    };

    //endregion

    //region WU Callback

//    WUClient.WUCallback callback = new WUClient.WUCallback() {
//        @Override
//        public void showProgressDialog() {
//            //Create a new progress dialog
//            progressDialog = new ProgressDialog(Login.this.getBaseContext());
//            //Set the progress dialog to display a horizontal progress bar
//            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//            //Set the dialog title to 'Loading...'
//            progressDialog.setTitle(Login.this.getString(R.string.connection_dialog_title));
//            //Set the dialog message to 'Loading application View, please wait...'
//            progressDialog.setMessage(Login.this.getString(R.string.connection_dialog_text1));
//            //This dialog can't be canceled by pressing the back key
//            progressDialog.setCancelable(true);
//            //This dialog isn't indeterminate
//            progressDialog.setIndeterminate(false);
//            //The maximum number of items is 100
//            progressDialog.setMax(100);
//            //Set the current progress to zero
//            progressDialog.setProgress(0);
//            //Display the progress dialog
//            progressDialog.show();
//        }
//    };



    //endregion



}
