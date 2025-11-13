// MainActivity.java
package com.example.fa25_duan1.view.welcome;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;

import com.example.fa25_duan1.R;
import com.example.fa25_duan1.adapter.WelcomePagerAdapter;
import com.example.fa25_duan1.view.auth.AuthActivity;

public class WelcomeActivity extends AppCompatActivity implements WelcomeFragment.OnWelcomeActionListener {

    private ViewPager2 viewPager;
    private FragmentStateAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // (Layout của activity chứa ViewPager2)

        viewPager = findViewById(R.id.view_pager); // (ID của ViewPager2)
        pagerAdapter = new WelcomePagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setUserInputEnabled(false);
        // ...
    }

    // --- Implement các phương thức từ OnWelcomeActionListener ---

    @Override
    public void onSkipClicked() {
        // Chuyển đến màn hình chính hoặc đăng nhập
        // Ví dụ: startActivity(new Intent(this, MainActivity.class));
        viewPager.setCurrentItem(pagerAdapter.getItemCount());
    }

    @Override
    public void onNextClicked() {
        // Di chuyển ViewPager sang trang tiếp theo
        int currentItem = viewPager.getCurrentItem();
        if (currentItem < (pagerAdapter.getItemCount() - 1)) {
            viewPager.setCurrentItem(currentItem + 1);
        }
    }

    @Override
    public void onRegisterClicked() {
        startAuthActivity(1);
        // Chuyển đến màn hình đăng ký
    }

    @Override
    public void onLoginClicked() {
        startAuthActivity(0);
        // Chuyển đến màn hình đăng nhập
    }

    @Override
    public void onGuestClicked() {
        startAuthActivity(1);
        // Chuyển đến màn hình chính (dưới dạng khách)
    }

    private void startAuthActivity(int tab) {
        Intent intent = new Intent(WelcomeActivity.this, AuthActivity.class);
        // Gửi cờ "DEFAULT_TAB" sang cho AuthActivity
        intent.putExtra("DEFAULT_TAB", tab);
        startActivity(intent);
    }
}

