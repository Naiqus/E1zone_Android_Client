package com.naiqus.e1zone;

import android.app.Activity;
import android.os.Bundle;

import org.xwalk.core.XWalkPreferences;


public class MainActivity extends Activity{


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        XWalkPreferences.setValue(XWalkPreferences.ANIMATABLE_XWALK_VIEW, true);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null){
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new e1zoneFragment())
                    .commit();
        }

    }



}
