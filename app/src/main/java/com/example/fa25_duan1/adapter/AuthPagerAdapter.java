package com.example.fa25_duan1.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.fa25_duan1.LoginFragment;
import com.example.fa25_duan1.RegisterFragment;

public class AuthPagerAdapter extends FragmentStateAdapter {

    public AuthPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Trả về fragment tương ứng
        switch (position) {
            case 0:
                return new LoginFragment();
            case 1:
            default:
                return new RegisterFragment();
        }
    }

    @Override
    public int getItemCount() {
        // Có tổng cộng 2 trang (Login và Register)
        return 2;
    }
}