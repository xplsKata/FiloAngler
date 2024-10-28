package com.example.filoangler.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.filoangler.R;
import com.example.filoangler.Utils;
import com.example.filoangler.activities.LoginActivity;

public class NoInternetFragment extends Fragment {

    private Button btnLogin;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_no_internet, container, false);

        btnLogin = view.findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.ChangeIntent(getActivity(), LoginActivity.class);
                getActivity().finish();
            }
        });

        return view;
    }

}