package com.example.fa25_duan1.view.home;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.fa25_duan1.R;
import com.example.fa25_duan1.view.home.HomeActivity; // Import HomeActivity
import com.example.fa25_duan1.view.detail.DetailActivity;
import com.example.fa25_duan1.view.home.ProductFragment; // Import ProductFragment

public class HeaderHomeFragment extends Fragment {

    private FrameLayout flCart;
    private EditText etSearch; // Khai báo biến EditText

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_header, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        flCart = view.findViewById(R.id.fl_cart_container);
        etSearch = view.findViewById(R.id.et_search); // Ánh xạ

        // 1. Xử lý click giỏ hàng
        flCart.setOnClickListener(v -> {
            Intent intent = new Intent(view.getContext(), DetailActivity.class);
            intent.putExtra(DetailActivity.EXTRA_HEADER_TITLE, "Giỏ hàng");
            intent.putExtra(DetailActivity.EXTRA_CONTENT_FRAGMENT, "cart");
            startActivity(intent);
        });

        // 2. Xử lý tìm kiếm khi ấn Enter/Search trên bàn phím
        etSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                        actionId == EditorInfo.IME_ACTION_DONE ||
                        (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {

                    String query = etSearch.getText().toString().trim();
                    if (!query.isEmpty()) {
                        performSearch(query);
                    }
                    return true;
                }
                return false;
            }
        });
    }

    private void performSearch(String query) {
        // Ẩn bàn phím sau khi ấn search
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        // Tạo ProductFragment và truyền keyword
        ProductFragment productFragment = new ProductFragment();
        Bundle args = new Bundle();
        args.putString("search_query", query); // Key quan trọng
        productFragment.setArguments(args);

        // Chuyển Fragment (Giả sử HomeActivity có hàm loadFragment)
        if (getActivity() instanceof HomeActivity) {
            ((HomeActivity) getActivity()).loadFragment(productFragment, true);
        }
    }
}