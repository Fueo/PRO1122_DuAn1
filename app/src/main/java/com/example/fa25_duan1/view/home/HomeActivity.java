package com.example.fa25_duan1.view.home;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View; // Import View

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.fa25_duan1.R;
import com.example.fa25_duan1.model.User;
import com.example.fa25_duan1.view.home.ProductFragment;
import com.example.fa25_duan1.viewmodel.AuthViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private AuthViewModel authViewModel;
    private View layoutLoading; // Khai báo biến Layout Loading

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        layoutLoading = findViewById(R.id.layout_loading); // Ánh xạ Layout Loading

        // 1. Nạp Header
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_header, new HeaderHomeFragment())
                .commit();

        // 2. Xử lý Intent
        handleIntent(getIntent());

        // 3. Setup ViewModel
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        checkUserRole();

        // 4. Click Menu
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            // Logic chọn Fragment
            Fragment selectedFragment = null;
            if (id == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (id == R.id.nav_favorite) {
                selectedFragment = new FavoriteFragment();
            } else if (id == R.id.nav_profile) {
                selectedFragment = new UserFragment();
            } else if (id == R.id.nav_admin) {
                selectedFragment = new AdminFragment();
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment, true);
            }
            return true;
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent != null && "product".equals(intent.getStringExtra("target_fragment"))) {
            String query = intent.getStringExtra("search_query");
            ProductFragment productFragment = new ProductFragment();
            Bundle args = new Bundle();
            args.putString("search_query", query);
            productFragment.setArguments(args);
            loadFragment(productFragment, true);
        } else {
            loadFragment(new HomeFragment(), true);
        }
    }

    private void checkUserRole() {
        authViewModel.getMyInfo().observe(this, response -> {
            if (response != null && response.getData() != null) {
                User user = response.getData();
                Menu menu = bottomNavigationView.getMenu();
                MenuItem adminItem = menu.findItem(R.id.nav_admin);
                if (adminItem != null) {
                    adminItem.setVisible(user.getRole() != 0);
                }
            }
        });
    }

    // --- CẬP NHẬT HÀM LOAD FRAGMENT ---
    public void loadFragment(@NonNull Fragment fragment, boolean showBottomNav) {
        // 1. Hiện Loading ngay lập tức
        showLoading();

        // 2. Thực hiện chuyển Fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_content, fragment)
                .addToBackStack(null)
                .commit();

        bottomNavigationView.setVisibility(showBottomNav ? BottomNavigationView.VISIBLE : BottomNavigationView.GONE);

        // 3. Ẩn Loading sau 1 khoảng thời gian (Ví dụ 800ms) để tạo hiệu ứng mượt
        // Đây là cách đơn giản nhất: Giả lập thời gian load
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            hideLoading();
        }, 800);
    }

    // Hàm hiển thị Loading (Public để Fragment có thể gọi nếu cần)
    public void showLoading() {
        if (layoutLoading != null) {
            layoutLoading.setVisibility(View.VISIBLE);
        }
    }

    // Hàm ẩn Loading (Public để Fragment gọi khi data thật đã về)
    public void hideLoading() {
        if (layoutLoading != null) {
            layoutLoading.setVisibility(View.GONE);
        }
    }
}