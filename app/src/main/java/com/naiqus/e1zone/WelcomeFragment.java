package com.naiqus.e1zone;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * A simple {@link Fragment} subclass.
 */
public class WelcomeFragment extends Fragment {


    public WelcomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_welcome, container, false);

//        Button mLoginBtn = (Button) v.findViewById(R.id.welcome_login_btn);
//        Button mRegisterBtn = (Button) v.findViewById(R.id.welcome_register_btn);
//        // Inflate the layout for this fragment
//        mLoginBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.v("welcomeF","clicked");
//                getFragmentManager().beginTransaction()
//                        .replace(R.id.welcome_placeholder,new LoginFragment())
//                        .commit();
//            }
//        });
//
//        mLoginBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });
        return v;
    }





}
