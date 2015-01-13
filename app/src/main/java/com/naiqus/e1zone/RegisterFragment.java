package com.naiqus.e1zone;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.wareninja.opensource.discourse.DiscourseApiClient;
import com.wareninja.opensource.discourse.utils.ResponseModel;

import org.apache.http.client.CookieStore;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class RegisterFragment extends Fragment  implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Keep track of the register task to ensure we can cancel it if requested.
     */
    private UserRegisterTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private AutoCompleteTextView mUsernameView;
    private TextView mNameView;
    private EditText mPasswordView;
    private EditText mPasswordConfirmView;
    private View mProgressView;
    private View mRegisterFormView;
    private View mEmailRegisterFormView;
    private CookieStore mCookieStore;
    private JSONObject mRegisterError;
    private String mGenerateAPIError;



    public RegisterFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_register, container, false);
        // Inflate the layout for this fragment
        // Set up the register form.

        mUsernameView = (AutoCompleteTextView) v.findViewById(R.id.register_username);
        mEmailView = (AutoCompleteTextView) v.findViewById(R.id.register_email);
        mNameView = (TextView) v.findViewById(R.id.register_name);
        populateAutoComplete();

        mPasswordView = (EditText) v.findViewById(R.id.register_password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.register || id == EditorInfo.IME_NULL) {
//                    attemptRegister();
                    return true;
                }
                return false;
            }
        });

        mPasswordConfirmView = (EditText) v.findViewById(R.id.register_password_confirm);
        mPasswordConfirmView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (mPasswordConfirmView.getText().toString().equals(mPasswordView.getText().toString())){
                    if (id == R.id.register_confirm || id == EditorInfo.IME_NULL) {
                        attemptRegister();
                        return true;
                    }
                    return false;
                }else{
                    mPasswordConfirmView.setError(getString(R.string.error_password_confirm));
                    return false;
                }
            }
        });

        Button mRegisterButton = (Button) v.findViewById(R.id.register_btn);
        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v("Register", "clicked once");
                attemptRegister();
            }
        });

        mRegisterFormView = v.findViewById(R.id.register_form);
        mProgressView = v.findViewById(R.id.register_progress);
        mEmailRegisterFormView = v.findViewById(R.id.email_register_form);

        return v;
    }

    private void populateAutoComplete() {
        getLoaderManager().initLoader(0, null, this);
    }


    /**
     * Attempts to sign in or register the account specified by the register form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual register attempt is made.
     */
    public void attemptRegister() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);
        mPasswordConfirmView.setError(null);
        mUsernameView.setError(null);
        mNameView.setError(null);

        // Store values at the time of the register attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        String username = mUsernameView.getText().toString();
        String passwordConfirm = mPasswordConfirmView.getText().toString();
        String name = mNameView.getText().toString();
;

        boolean cancel = false;
        View focusView = null;


        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for password confirmation, if the user entered one.
        if (!TextUtils.isEmpty(passwordConfirm) && !isPasswordValid(passwordConfirm)) {
            mPasswordConfirmView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordConfirmView;
            cancel = true;
        }
        //Check whether both password are same
        if (!isPasswordConfirmed(password,passwordConfirm)){
            mPasswordConfirmView.setError(getString(R.string.error_password_confirm));
            focusView = mPasswordConfirmView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }
        // Check for a valid username.
        if (TextUtils.isEmpty(username)) {
            mUsernameView.setError(getString(R.string.error_field_required));
            focusView = mUsernameView;
            cancel = true;
//        } else if (!isEmailValid(email)) {
//            mUsernameView.setError(getString(R.string.error_invalid_username));
//            focusView = mUsernameView;
//            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt register and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user register attempt.
            showProgress(true);
            mAuthTask = new UserRegisterTask(email, password,username,name);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
//        return true;
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 7;
    }

    private boolean isPasswordConfirmed(String password, String passwordConfirm) {
        //TODO: Replace this with your own logic
        return password.equals(passwordConfirm);
    }
    /**
     * Shows the progress UI and hides the register form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mRegisterFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mRegisterFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mRegisterFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mRegisterFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }





    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(getActivity(),
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<String>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }


    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(getActivity(),
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }

    public class UserRegisterTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mUsername;
        private final String mName;
        private final String mPassword;
        private JSONObject mResponse;

        UserRegisterTask(String email, String password, String username, String name) {
            mEmail = email;
            mPassword = password;
            mUsername = username;
            mName = name;
        }

        @Override
        protected Boolean doInBackground(Void... params){
            //test Discourse API
            final DiscourseApiClient mDiscourseClient = new DiscourseApiClient(
                    getString(R.string.host_url),
                    getString(R.string.api_key),
                    getString(R.string.api_user)
            );
//            showProgress(true);

            ResponseModel responseModel;
            Map<String,String> parameters = new HashMap<String, String>();
            parameters.put("name", mName+"");
            parameters.put("email", mEmail);
            parameters.put("username", mUsername);
            parameters.put("password", mPassword);
//            parameters.put("active", "true");  //comment this is activation needed
            responseModel = mDiscourseClient.createUser(parameters);//send register request
            mCookieStore = mDiscourseClient.getCookieStore();       //get cookies
            Log.v("DisourseAPI", responseModel.toString());
            try {
                mResponse = new JSONObject(responseModel.data.toString());
                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("preference", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                if (!mResponse.getBoolean("success")){ //signed in failed
                    mRegisterError = mResponse;
                    editor.putBoolean("logged_in",false);
                    return false;

                }else {  //signed in successfully
                    /***activate user ***/
                    //there is a bug, is no activation email send, then activation in backend doesn't work
//                    Map<String,String> activateParam = new HashMap<String, String>();
//                    activateParam.put("userid",Integer.toString(mResponse.getInt("user_id")));
//                    activateParam.put("username",mUsername);
//                    Log.v("activate",mDiscourseClient.activateUser(activateParam).toString());
                    return true;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return true;
        }

        @Override
        protected void onPostExecute (Boolean result){
            mAuthTask = null;
            if (result) {
                //show success msg
                try {
                    new AlertDialog.Builder(getActivity())
                            .setMessage(Html.fromHtml(mResponse.getString("message")))
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // continue with delete
                                }
                            })
                            .show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //go back to login page
                getActivity().getFragmentManager()
                        .beginTransaction()
                        .replace(R.id.welcome_placeholder,new LoginFragment())
                        .commit();
                //record pass and username ***in case activation required, comment this
                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("preference",Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("user_name",mUsername)
                      .putString("password",mPassword)
                      .apply();
                showProgress(false);
            }else {          //signed in failed
                showProgress(false);
                try {
                    JSONObject registerError = mRegisterError.getJSONObject("errors");

                    if (registerError.has("username")) {
                        mUsernameView.setError(registerError.getString("username"));
                        mUsernameView.requestFocus();
                    } else if (registerError.has("email")) {
                        mEmailView.setError(registerError.getString("email"));
                        mEmailView.requestFocus();
                    } else if (registerError.has("password")) {
                        mPasswordView.setError(registerError.getString("password"));
                        mPasswordView.requestFocus();
                    } else {
                        Context context = getActivity().getApplicationContext();
                        Toast errorToast = Toast.makeText(context, mRegisterError.getString("message"), Toast.LENGTH_LONG);
                        errorToast.show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}


