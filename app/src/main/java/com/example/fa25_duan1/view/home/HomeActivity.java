package com.example.fa25_duan1.view.home;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.fa25_duan1.R;
import com.example.fa25_duan1.view.detail.DetailFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Khởi tạo BottomNavigationView
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Load Header và Fragment chính lúc đầu
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_header, new HeaderHomeFragment())
                .commit();

        loadFragment(new HomeFragment(), true); // Hiện BottomNavigationView cho fragment chính

        // Listener cho BottomNavigationView
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                loadFragment(new HomeFragment(), true);
            } else if (id == R.id.nav_favorite) {
                loadFragment(new FavoriteFragment(), true);
            } else if (id == R.id.nav_profile) {
                loadFragment(new UserFragment(), true);
            } else if (id == R.id.nav_admin) {
                loadFragment(new AdminFragment(), true);
            }

            return true;
        });
    }

    /**
     * Load fragment vào fragment_content
     * @param fragment Fragment muốn load
     * @param showBottomNav true = hiện BottomNavigationView, false = ẩn
     */
    public void loadFragment(@NonNull Fragment fragment, boolean showBottomNav) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_content, fragment)
                .addToBackStack(null) // để quay lại fragment trước khi nhấn back
                .commit();

        bottomNavigationView.setVisibility(showBottomNav ? BottomNavigationView.VISIBLE : BottomNavigationView.GONE);
    }

}
