package com.naiqus.e1zone;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;


/**
 * A login screen that offers login via email/password and via Google+ sign in.
 * <p/>
 * ************ IMPORTANT SETUP NOTES: ************
 * In order for Google+ sign in to work with your app, you must first go to:
 * https://developers.google.com/+/mobile/android/getting-started#step_1_enable_the_google_api
 * and follow the steps in "Step 1" to create an OAuth 2.0 client for your package.
 */
public class LoginActivity extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //check if already logged in
        SharedPreferences sharedPreferences = getSharedPreferences("preference", Context.MODE_PRIVATE);
        if (sharedPreferences.getBoolean("logged_in",false)){
            //yes start main activity
            Log.v("loginActivity_Logged_in",Boolean.toString(sharedPreferences.getBoolean("logged_in",false)));
            Intent mainActivityIntent = new Intent(this,MainActivity.class);
            startActivity(mainActivityIntent);
        }else{
            //not yet. start welcome screen
            setContentView(R.layout.activity_login);
            WelcomeFragment mWelcomeFragment = new WelcomeFragment();
            if (savedInstanceState == null){
                getFragmentManager().beginTransaction()
                        .add(R.id.welcome_placeholder,mWelcomeFragment)
                        .commit();
            }
        }
    }

    public void WelcomeLoginBtn(View v){
        getFragmentManager().beginTransaction()
                .replace(R.id.welcome_placeholder,new LoginFragment())
                .commit();
    }

    public void WelcomeRegisterBtn(View v){
        getFragmentManager().beginTransaction()
                .replace(R.id.welcome_placeholder,new RegisterFragment())
                .commit();
    }


}



