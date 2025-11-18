package com.example.fa25_duan1.view.welcome;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.example.fa25_duan1.R;
import com.example.fa25_duan1.adapter.WelcomePagerAdapter;
import com.example.fa25_duan1.view.auth.AuthActivity;

public class WelcomeActivity extends AppCompatActivity implements WelcomeFragment.OnWelcomeActionListener {
    private ViewPager2 viewPager;
    private FragmentStateAdapter pagerAdapter;

    // --- Thêm hằng số cho SharedPreferences ---
    public static final String APP_PREFS = "AppPrefs";
    public static final String KEY_IS_FIRST_RUN = "isFirstRun";
    // ------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewPager = findViewById(R.id.view_pager);
        pagerAdapter = new WelcomePagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setUserInputEnabled(false);

        SharedPreferences prefs = getSharedPreferences(APP_PREFS, MODE_PRIVATE);
        boolean isFirstRun = prefs.getBoolean(KEY_IS_FIRST_RUN, true);

        if (isFirstRun) {
            prefs.edit().putBoolean(KEY_IS_FIRST_RUN, false).apply();
        } else {
            viewPager.post(() -> {
                int lastItemIndex = pagerAdapter.getItemCount() - 1;

                if (lastItemIndex >= 0) {
                    // Chuyển item và 'false' để không có animation
                    viewPager.setCurrentItem(lastItemIndex, false);
                }
            });
        }
    }

    @Override
    public void onSkipClicked() {
        int lastItemIndex = pagerAdapter.getItemCount() - 1;
        viewPager.setCurrentItem(lastItemIndex);
    }

    @Override
    public void onNextClicked() {
        int currentItem = viewPager.getCurrentItem();
        if (currentItem < (pagerAdapter.getItemCount() - 1)) {
            viewPager.setCurrentItem(currentItem + 1);
        }
    }

    @Override
    public void onRegisterClicked() {
        startAuthActivity(1);
    }

    @Override
    public void onLoginClicked() {
        startAuthActivity(0);
    }

    @Override
    public void onGuestClicked() {
        startAuthActivity(1);
    }

    private void startAuthActivity(int tab) {
        Intent intent = new Intent(WelcomeActivity.this, AuthActivity.class);
        intent.putExtra("DEFAULT_TAB", tab);
        startActivity(intent);
    }
}

