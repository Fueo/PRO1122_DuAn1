package com.example.fa25_duan1.view.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.fa25_duan1.R;
import com.example.fa25_duan1.model.User;
import com.example.fa25_duan1.view.home.ProductFragment; // Import ProductFragment
import com.example.fa25_duan1.viewmodel.AuthViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // 1. Luôn nạp Header mặc định
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_header, new HeaderHomeFragment())
                .commit();

        // 2. Xử lý Logic hiển thị Fragment nội dung (Search hoặc Home)
        handleIntent(getIntent());

        // 3. Setup ViewModel và phân quyền
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        checkUserRole();

        // 4. Bắt sự kiện click menu dưới đáy
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
     * Hàm quan trọng: Giúp nhận Intent mới khi Activity đang chạy (SingleTop)
     * Ví dụ: Đang ở Home -> vào Detail -> Search -> quay lại Home để hiện kết quả
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent); // Cập nhật intent mới
        handleIntent(intent); // Xử lý lại logic hiển thị
    }

    /**
     * Tách logic xử lý Intent ra hàm riêng để dùng chung cho onCreate và onNewIntent
     */
    private void handleIntent(Intent intent) {
        if (intent != null && "product".equals(intent.getStringExtra("target_fragment"))) {
            // --- TRƯỜNG HỢP 1: Có yêu cầu TÌM KIẾM ---
            String query = intent.getStringExtra("search_query");

            ProductFragment productFragment = new ProductFragment();
            Bundle args = new Bundle();
            args.putString("search_query", query);
            productFragment.setArguments(args);

            // Load Fragment tìm kiếm
            loadFragment(productFragment, true);

            // (Tùy chọn) Bỏ chọn item trên BottomNav để người dùng biết đang ở màn hình khác
            // bottomNavigationView.getMenu().setGroupCheckable(0, true, false);
        } else {
            // --- TRƯỜNG HỢP 2: Mặc định vào HOME ---
            // Chỉ load HomeFragment nếu đây là lần đầu tạo Activity (savedInstanceState == null)
            // Hoặc nếu gọi từ onNewIntent mà không có cờ search
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
                    // Nếu role == 0 (User thường) -> Ẩn tab Admin
                    // Nếu role == 1 (Admin) -> Hiện tab Admin
                    adminItem.setVisible(user.getRole() != 0);
                }
            }
        });
    }

    public void loadFragment(@NonNull Fragment fragment, boolean showBottomNav) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_content, fragment)
                .addToBackStack(null) // Lưu vào backstack để nút Back hoạt động
                .commit();

        bottomNavigationView.setVisibility(showBottomNav ? BottomNavigationView.VISIBLE : BottomNavigationView.GONE);
    }
}