package com.naiqus.e1zone;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import org.xwalk.core.XWalkPreferences;


public class MainActivity extends ActionBarActivity {

    private XWalkViewFragment mxWalkViewFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        XWalkPreferences.setValue(XWalkPreferences.ANIMATABLE_XWALK_VIEW, true);
        setContentView(R.layout.activity_main);
        //PreferenceManager.setDefaultValues(this,R.xml.preference,false); //reade preference first


        mxWalkViewFragment = new XWalkViewFragment();

        //set up drawer
        NavigationDrawerFragment mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);

        //Start the Fragments
        if (savedInstanceState == null){

                getFragmentManager().beginTransaction()
                        .add(R.id.main_container, mxWalkViewFragment)
                        .commit();
                mxWalkViewFragment.setUrl(getString(R.string.host_url));

        }

    }

//    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
//        FragmentManager fragmentManager = getSupportFragmentManager();
//        fragmentManager.beginTransaction()
//                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
//                .commit();
    }





}
