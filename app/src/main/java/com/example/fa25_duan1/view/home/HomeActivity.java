package com.example.fa25_duan1.view.home;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.fa25_duan1.R;
import com.example.fa25_duan1.model.User;
import com.example.fa25_duan1.view.auth.AuthActivity; // Import AuthActivity
import com.example.fa25_duan1.view.detail.DetailActivity;
import com.example.fa25_duan1.viewmodel.AuthViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import io.github.cutelibs.cutedialog.CuteDialog; // Import CuteDialog

public class HomeActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private AuthViewModel authViewModel;
    private View layoutLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        layoutLoading = findViewById(R.id.layout_loading);

        // 1. Nạp Header
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_header, new HeaderHomeFragment())
                .commit();

        // 2. Xử lý Intent
        handleIntent(getIntent());

        // 3. Setup ViewModel & Check Role
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        checkUserRole();

        // 4. Click Menu (CẬP NHẬT LOGIC GUEST)
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            // --- [MỚI] KIỂM TRA QUYỀN TRUY CẬP CỦA GUEST ---
            // Nếu là Guest mà bấm vào Profile, Favorite hoặc Admin -> Chặn lại bắt đăng nhập
            if (isGuestUser()) {
                if (id == R.id.nav_profile || id == R.id.nav_favorite || id == R.id.nav_admin) {
                    showLoginRequiredDialog();
                    return false; // Không cho chuyển tab
                }
            }
            // -------------------------------------------------

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
        if (intent == null) return;

        String target = intent.getStringExtra("target_fragment");

        if ("product".equals(target)) {
            String query = intent.getStringExtra("search_query");
            ProductFragment productFragment = new ProductFragment();
            Bundle args = new Bundle();
            args.putString("search_query", query);
            productFragment.setArguments(args);
            loadFragment(productFragment, true);

        } else if ("payment_qr".equals(target)) {
            String orderId = intent.getStringExtra("ORDER_ID");
            long totalAmount = intent.getLongExtra("TOTAL_AMOUNT", 0);
            String transCode = intent.getStringExtra("TRANS_CODE");

            Intent paymentIntent = new Intent(this, DetailActivity.class);
            paymentIntent.putExtra(DetailActivity.EXTRA_HEADER_TITLE, "Thanh toán QR");
            paymentIntent.putExtra(DetailActivity.EXTRA_CONTENT_FRAGMENT, "payment");
            paymentIntent.putExtra("ORDER_ID", orderId);
            paymentIntent.putExtra("TOTAL_AMOUNT", totalAmount);
            paymentIntent.putExtra("TRANS_CODE", transCode);

            startActivity(paymentIntent);
            loadFragment(new HomeFragment(), true);

        } else {
            loadFragment(new HomeFragment(), true);
        }
    }

    // --- [MỚI] CẬP NHẬT LOGIC CHECK USER ROLE ---
    private void checkUserRole() {
        Menu menu = bottomNavigationView.getMenu();
        MenuItem adminItem = menu.findItem(R.id.nav_admin);

        if (isGuestUser()) {
            // Nếu là Guest -> Ẩn menu Admin ngay lập tức, không cần gọi API
            if (adminItem != null) adminItem.setVisible(false);
        } else {
            // Nếu là User đăng nhập -> Gọi API lấy Role để hiện/ẩn Admin
            authViewModel.getMyInfo().observe(this, response -> {
                if (response != null && response.getData() != null) {
                    User user = response.getData();
                    if (adminItem != null) {
                        adminItem.setVisible(user.getRole() != 0); // 0: User thường -> Ẩn
                    }
                }
            });
        }
    }

    // --- [MỚI] HÀM KIỂM TRA GUEST ---
    private boolean isGuestUser() {
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String token = prefs.getString("accessToken", null);
        return token == null; // Không có token -> Là Guest
    }

    // --- [MỚI] DIALOG YÊU CẦU ĐĂNG NHẬP ---
    private void showLoginRequiredDialog() {
        new CuteDialog.withIcon(this)
                .setIcon(R.drawable.ic_dialog_info) // Đảm bảo có icon này
                .setTitle("Yêu cầu đăng nhập")
                .setDescription("Bạn cần đăng nhập để sử dụng tính năng này.")
                .setPositiveButtonText("Đăng nhập", v -> {
                    // Chuyển sang màn hình Auth
                    Intent intent = new Intent(HomeActivity.this, AuthActivity.class);
                    // Clear task để user không bấm back về lại Home được (tuỳ chọn)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButtonText("Để sau", v -> {})
                .show();
    }

    public void loadFragment(@NonNull Fragment fragment, boolean showBottomNav) {
        showLoading();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_content, fragment)
                .addToBackStack(null)
                .commit();
        bottomNavigationView.setVisibility(showBottomNav ? BottomNavigationView.VISIBLE : BottomNavigationView.GONE);
        new Handler(Looper.getMainLooper()).postDelayed(this::hideLoading, 800);
    }

    public void showLoading() {
        if (layoutLoading != null) layoutLoading.setVisibility(View.VISIBLE);
    }

    public void hideLoading() {
        if (layoutLoading != null) layoutLoading.setVisibility(View.GONE);
    }
}