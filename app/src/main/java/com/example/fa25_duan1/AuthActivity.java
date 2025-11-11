package com.example.fa25_duan1;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.fa25_duan1.adapter.AuthPagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class AuthActivity extends AppCompatActivity {

    TabLayout tabLayout;
    ViewPager2 viewPager;
    AuthPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        tabLayout = findViewById(R.id.tabLayout_auth);
        viewPager = findViewById(R.id.view_pager_auth);

        // Khởi tạo Adapter
        adapter = new AuthPagerAdapter(this);
        viewPager.setAdapter(adapter);
        RecyclerView recyclerView = (RecyclerView) viewPager.getChildAt(0);

        int space = 40;

        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                                       @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                outRect.left = space / 2;
                outRect.right = space / 2;
            }
        });
        // Kết nối TabLayout với ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position == 0) {
                tab.setText("Đăng nhập");
            } else {
                tab.setText("Đăng ký");
            }
        }).attach();

        // Nhận cờ (flag) từ Welcome3Fragment để biết nên mở tab nào
        int defaultTab = getIntent().getIntExtra("DEFAULT_TAB", 0);
        viewPager.setCurrentItem(defaultTab);
    }
}