package com.naiqus.e1zone;

import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.wareninja.opensource.discourse.DiscourseApiClient;
import com.wareninja.opensource.discourse.utils.ResponseModel;

import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import org.xwalk.core.XWalkPreferences;
import org.xwalk.core.XWalkView;
import org.xwalk.core.internal.XWalkCookieManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class XWalkViewFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private XWalkView mXWalkView;
    private static String mUrl;
    private XWalkCookieManager xWalkCookieManager;


    public XWalkViewFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
//        View rootView = inflater.inflate(R.layout.fragment_e1zone, container, false);
//        return rootView;
        View v = inflater.inflate(R.layout.fragment_e1zone, container, false);
        mXWalkView = (XWalkView) v.findViewById(R.id.xwalkview);
        //enable remote debugging
        XWalkPreferences.setValue(XWalkPreferences.REMOTE_DEBUGGING, true);
        mXWalkView.load(mUrl,null);

        xWalkCookieManager =new XWalkCookieManager();
        xWalkCookieManager.setAcceptCookie(true);
        Button loginButton = (Button) getActivity().findViewById(R.id.login_btn);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DiscourseAPIRequest mAPIRequest = new DiscourseAPIRequest();
                mAPIRequest.execute();
            }
        });
        if (xWalkCookieManager.hasCookies())
            Log.v("Cookie Existed", xWalkCookieManager.getCookie("http://www.e1zone.de"));
        else
            Log.v("Cookie Existed", "None");
        return v;
    }

    public void setUrl(String url){
        mUrl = url;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mXWalkView != null) {
            mXWalkView.pauseTimers();
            mXWalkView.onHide();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mXWalkView != null) {
            mXWalkView.resumeTimers();
            mXWalkView.onShow();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mXWalkView != null) {
            mXWalkView.onDestroy();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mXWalkView != null) {
            mXWalkView.onActivityResult(requestCode, resultCode, data);
        }
    }


    public void onNewIntent(Intent intent) {
        if (mXWalkView != null) {
            mXWalkView.onNewIntent(intent);
        }
    }

    public class DiscourseAPIRequest extends AsyncTask<Void, Void, CookieStore> {
        @Override
        protected CookieStore doInBackground(Void... params){
            //test Discourse API
            final DiscourseApiClient mDiscourseClient = new DiscourseApiClient(
                    "http://www.e1zone.de",
                    "972f732c3152ab0af0f9eea6bad731e3872f98984fae2f1d738349157d1ba20f",
                    "naiqus"
            );



            String test_username = "panda";
            String test_password = "iquie1y9oob";
            ResponseModel responseModel;

            Map<String,String> param = new HashMap<String, String>();
            param.put("login",test_username);
            param.put("password",test_password);
            //do the method
            responseModel = mDiscourseClient.loginUser(param);
//            Map<String,String> parameters = new HashMap<String, String>();
//            parameters.put("name", test_username);
//            parameters.put("email", test_username+"@dummy.com");
//            parameters.put("username", test_username);
//            parameters.put("password", test_username+"_pwd");
//            responseModel = mDiscourseClient.createUser(parameters);
            Log.v("DisourseAPI", responseModel.toString());
            return mDiscourseClient.getCookieStore(); //return cookie
        }

        @Override
        protected void onPostExecute (CookieStore result){
            List<Cookie> mCookies = result.getCookies();
            Log.v("app get Cookie", result.getCookies().toString());
            xWalkCookieManager.removeAllCookie();
            for (Cookie cookie : mCookies) {
                String setCookie = new StringBuilder(cookie.getName())
                        .append("=")
                        .append(cookie.getValue())
                        .append("; domain=").append(cookie.getDomain())
                        .append("; path=").append(cookie.getPath())
                        .toString();
                Log.v("NewCookieContent", setCookie);
                xWalkCookieManager.setCookie(getString(R.string.host_url), setCookie);
                Log.v("New Cookie", xWalkCookieManager.getCookie("http://www.e1zone.de"));
            }
        }

    }

}
