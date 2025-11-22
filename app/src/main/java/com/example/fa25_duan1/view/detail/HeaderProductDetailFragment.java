package com.example.fa25_duan1.view.detail;

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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.fa25_duan1.R;
import com.example.fa25_duan1.view.home.HomeActivity; // Import Activity chứa danh sách sản phẩm của bạn

public class HeaderProductDetailFragment extends Fragment {

    private ImageView iv_back;
    private EditText etSearch; // 1. Khai báo EditText

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_header_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        iv_back = view.findViewById(R.id.iv_back);
        etSearch = view.findViewById(R.id.et_search); // 2. Ánh xạ

        iv_back.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().finish();
            }
        });

        // 3. Xử lý sự kiện tìm kiếm
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
        // A. Ẩn bàn phím
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        // B. Điều hướng sang màn hình danh sách sản phẩm
        // Tùy thuộc vào cấu trúc App của bạn, bạn có thể chọn 1 trong 2 cách sau:

        // CÁCH 1: Nếu bạn muốn quay về HomeActivity và hiển thị ProductFragment với kết quả tìm kiếm
        Intent intent = new Intent(getActivity(), HomeActivity.class);
        intent.putExtra("target_fragment", "product"); // Cờ để HomeActivity biết cần mở fragment nào
        intent.putExtra("search_query", query); // Truyền từ khóa
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);

    }
}