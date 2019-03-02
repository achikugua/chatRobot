package com.practice.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BlankOtherFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BlankOtherFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_SHOW_TEXT = "text";

    //private String mContentText;


    public BlankOtherFragment() {
        // Required empty public constructor
    }


    public static BlankOtherFragment newInstance() {
        BlankOtherFragment fragment = new BlankOtherFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_SHOW_TEXT, param1);
//        fragment.setArguments(args);
        return fragment;
    }

//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
////        if (getArguments() != null) {
////            mContentText = getArguments().getString(ARG_SHOW_TEXT);
////        }
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_other, container, false);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Button help = (Button)getActivity().findViewById(R.id.help);
        Button response = (Button)getActivity().findViewById(R.id.response);
        Button aboutus = (Button)getActivity().findViewById(R.id.aboutus);
        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(),Help.class);
                startActivity(intent);
            }
        });
        response.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(),Response.class);
                startActivity(intent);
            }
        });
        aboutus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(),Aboutus.class);
                startActivity(intent);
            }
        });

    }
}
