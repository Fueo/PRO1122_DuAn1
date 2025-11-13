package com.example.fa25_duan1.view.management;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fa25_duan1.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AccountActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_header, new HeaderManagementFragment())
                .commit();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_content, new AccountManageFragment())
                .commit();
    }
}