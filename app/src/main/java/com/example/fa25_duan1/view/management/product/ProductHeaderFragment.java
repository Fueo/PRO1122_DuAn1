package com.example.fa25_duan1.view.management.product;

import android.os.Bundle;
import android.text.Editable; // Import cần thiết
import android.text.TextWatcher; // Import cần thiết
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.fa25_duan1.R;
// Không cần import AuthorViewModel
import com.example.fa25_duan1.viewmodel.ProductViewModel;

import org.angmarch.views.NiceSpinner;
import org.angmarch.views.OnSpinnerItemSelectedListener; // Import cần thiết

import java.util.Arrays;
import java.util.LinkedList;

public class ProductHeaderFragment extends Fragment {
    TextView tv_title;
    ImageView ivBack;
    EditText etSearch;
    NiceSpinner spSearch;
    ProductViewModel viewModel;

    // Biến lưu trữ loại tìm kiếm hiện tại (name/author)
    private String currentSearchType = "name";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_headermanagement, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ivBack = view.findViewById(R.id.ivBack);
        etSearch = view.findViewById(R.id.etSearch);
        spSearch = view.findViewById(R.id.spSearch);
        tv_title = view.findViewById(R.id.tv_title);

        tv_title.setText("Quản lí sản phẩm");
        etSearch.setHint("Tìm kiếm sản phẩm");

        viewModel = new ViewModelProvider(requireActivity()).get(ProductViewModel.class);

        ivBack.setOnClickListener(v -> requireActivity().finish()); // Sử dụng requireActivity().finish()

        // 1. Dữ liệu Spinner
        LinkedList<String> data = new LinkedList<>(Arrays.asList(
                "Theo tên", "Theo tác giả"));
        spSearch.attachDataSource(data);

        // Thiết lập loại tìm kiếm mặc định
        currentSearchType = "name";

        // 2. Lắng nghe sự kiện thay đổi Spinner
        spSearch.setOnSpinnerItemSelectedListener(new OnSpinnerItemSelectedListener() {
            @Override
            public void onItemSelected(NiceSpinner parent, View view, int position, long id) {
                // 0: Theo tên -> "name"
                // 1: Theo tác giả -> "author"
                if (position == 0) {
                    currentSearchType = "name";
                } else {
                    currentSearchType = "author";
                }

                // Kích hoạt tìm kiếm lại ngay lập tức với loại mới
                searchProductsFromHeader(etSearch.getText().toString());
            }
        });

        // 3. Lắng nghe sự kiện thay đổi Text trong EditText
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchProductsFromHeader(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    /**
     * Hàm gọi tìm kiếm sản phẩm trong ProductViewModel, sử dụng loại tìm kiếm hiện tại.
     */
    private void searchProductsFromHeader(String query) {
        if (viewModel == null) return;
        viewModel.searchProducts(query, currentSearchType);
    }
}