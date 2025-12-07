package com.example.fa25_duan1.view.home;

import android.content.Intent;
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
import com.example.fa25_duan1.view.detail.DetailActivity; // Import DetailActivity
import com.example.fa25_duan1.view.home.ProductFragment;
import com.example.fa25_duan1.viewmodel.AuthViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

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

        // 2. Xử lý Intent (Quan trọng)
        handleIntent(getIntent());

        // 3. Setup ViewModel
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        checkUserRole();

        // 4. Click Menu
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
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
            // Case 1: Mở màn hình tìm kiếm sản phẩm
            String query = intent.getStringExtra("search_query");
            ProductFragment productFragment = new ProductFragment();
            Bundle args = new Bundle();
            args.putString("search_query", query);
            productFragment.setArguments(args);
            loadFragment(productFragment, true);

        } else if ("payment_qr".equals(target)) {
            // Case 2: [MỚI] Mở màn hình thanh toán QR từ Checkout
            String orderId = intent.getStringExtra("ORDER_ID");
            long totalAmount = intent.getLongExtra("TOTAL_AMOUNT", 0);
            String transCode = intent.getStringExtra("TRANS_CODE");

            // Mở DetailActivity chứa PaymentFragment đè lên trên Home
            Intent paymentIntent = new Intent(this, DetailActivity.class);
            paymentIntent.putExtra(DetailActivity.EXTRA_HEADER_TITLE, "Thanh toán QR");
            paymentIntent.putExtra(DetailActivity.EXTRA_CONTENT_FRAGMENT, "payment");
            paymentIntent.putExtra("ORDER_ID", orderId);
            paymentIntent.putExtra("TOTAL_AMOUNT", totalAmount);
            paymentIntent.putExtra("TRANS_CODE", transCode);

            startActivity(paymentIntent);

            // Lưu ý: KHÔNG loadFragment gì cả ở Home, để Home giữ nguyên trạng thái cũ (hoặc load HomeFragment mặc định)
            // Nếu bạn muốn Home ở dưới load lại trang chủ cho mới thì:
            loadFragment(new HomeFragment(), true);

        } else {
            // Mặc định load Home
            loadFragment(new HomeFragment(), true);
        }
    }

    // ... (Rest of the class methods: checkUserRole, loadFragment, showLoading, hideLoading... remain unchanged) ...

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