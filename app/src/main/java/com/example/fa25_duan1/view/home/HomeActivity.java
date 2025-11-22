package com.example.fa25_duan1.view.home;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider; // Cần import này

import com.example.fa25_duan1.R;
import com.example.fa25_duan1.model.User;
import com.example.fa25_duan1.view.detail.ProductFragment;
import com.example.fa25_duan1.view.home.UserFragment;
import com.example.fa25_duan1.viewmodel.AuthViewModel; // Cần import này
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private AuthViewModel authViewModel; // 1. Khai báo ViewModel

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // 2. Khởi tạo ViewModel
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // 3. Gọi hàm kiểm tra Role ngay khi vào màn hình
        checkUserRole();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_header, new HeaderHomeFragment())
                .commit();

        loadFragment(new HomeFragment(), true);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                loadFragment(new HomeFragment(), true);
            } else if (id == R.id.nav_favorite) {
                loadFragment(new FavoriteFragment(), true);
            } else if (id == R.id.nav_profile) {
                loadFragment(new UserFragment(), true);
            } else if (id == R.id.nav_admin) {
                // Kiểm tra lại lần nữa để chắc chắn (bảo mật logic)
                loadFragment(new AdminFragment(), true);
            }

            return true;
        });
    }

    /**
     * Hàm lấy thông tin user và ẩn/hiện tab Admin
     */
    private void checkUserRole() {
        authViewModel.getMyInfo().observe(this, response -> {
            // Kiểm tra response thành công và có dữ liệu
            if (response != null && response.getData() != null) {
                User user = response.getData();

                // Lấy Menu từ BottomNavigationView
                Menu menu = bottomNavigationView.getMenu();
                MenuItem adminItem = menu.findItem(R.id.nav_admin);

                if (adminItem != null) {
                    // Giả định model User có getter getRole() trả về int
                    // Nếu role == 0 (User thường) -> Ẩn tab Admin
                    if (user.getRole() == 0) {
                        adminItem.setVisible(false);
                    } else {
                        // Role 1, 2... (Admin/Staff) -> Hiện tab Admin
                        adminItem.setVisible(true);
                    }
                }
            }
        });
    }

    public void loadFragment(@NonNull Fragment fragment, boolean showBottomNav) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_content, fragment)
                .addToBackStack(null)
                .commit();

        bottomNavigationView.setVisibility(showBottomNav ? BottomNavigationView.VISIBLE : BottomNavigationView.GONE);
    }
}