package com.naiqus.e1zone;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.xwalk.core.XWalkPreferences;
import org.xwalk.core.XWalkView;
import org.xwalk.core.internal.XWalkCookieManager;


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

        xWalkCookieManager =new XWalkCookieManager();
        xWalkCookieManager.setAcceptCookie(true);
        //load extra from intent and set cookie to xwalkview
        xWalkCookieManager.removeAllCookie();
        String[] cookieArray = getActivity().getIntent().getStringArrayExtra("cookieArray");
        for (String aCookieArray : cookieArray)
            xWalkCookieManager.setCookie(getString(R.string.host_url), aCookieArray);
        //start
        mXWalkView.load(mUrl,null);
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


}
