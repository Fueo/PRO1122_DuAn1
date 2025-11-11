// WelcomePagerAdapter.java
package com.example.fa25_duan1.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.fa25_duan1.R;
import com.example.fa25_duan1.WelcomeFragment;

public class WelcomePagerAdapter extends FragmentStateAdapter {

    // Danh sách các layout
    private static final int[] LAYOUTS = {
            R.layout.fragment_welcome1,
            R.layout.fragment_welcome2,
            R.layout.fragment_welcome3
    };

    public WelcomePagerAdapter(FragmentActivity fa) {
        super(fa);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Tạo WelcomeFragment với layoutId tương ứng
        return WelcomeFragment.newInstance(LAYOUTS[position]);
    }

    @Override
    public int getItemCount() {
        // Trả về số lượng trang
        return LAYOUTS.length;
    }
}