package com.example.fa25_duan1.view.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.fa25_duan1.R;
import com.example.fa25_duan1.view.detail.DetailActivity;

public class HeaderHomeFragment extends Fragment {
    FrameLayout flCart;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_header, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        flCart = view.findViewById(R.id.fl_cart_container);

        flCart.setOnClickListener(v -> {
            Intent intent = new Intent(view.getContext(), DetailActivity.class);
            intent.putExtra(DetailActivity.EXTRA_HEADER_TITLE, "Giỏ hàng");
            intent.putExtra(DetailActivity.EXTRA_CONTENT_FRAGMENT, "cart");
            startActivity(intent);
        });
    }
}
