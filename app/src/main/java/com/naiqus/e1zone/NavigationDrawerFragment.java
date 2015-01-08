package com.naiqus.e1zone;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;


public class NavigationDrawerFragment extends Fragment {

private View rootView;
private ListView mNavigationDrawerList;
private final String[] testCategories = {
        "Pron",
        "Meta",
        "18+"
};
private ArrayAdapter<String> mCategoryAdapter;

    public NavigationDrawerFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_navigation_drawer, container, false);
        mCategoryAdapter = new ArrayAdapter<String>(
                getActivity(),
                R.layout.list_item_catergories,
                R.id.list_item_categories,
                new ArrayList<>(Arrays.asList(testCategories))
        );
        mNavigationDrawerList = (ListView) rootView.findViewById(R.id.category_list);
        mNavigationDrawerList.setAdapter(mCategoryAdapter);

        return rootView;
    }





}
