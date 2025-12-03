package com.example.fa25_duan1.view.management.order;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
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
import com.example.fa25_duan1.viewmodel.OrderViewModel;

import org.angmarch.views.NiceSpinner;

import java.util.Arrays;
import java.util.LinkedList;

public class OrderHeaderFragment extends Fragment {

    private TextView tv_title;
    private ImageView ivBack;
    private EditText etSearch;
    private NiceSpinner spSearch;
    private OrderViewModel viewModel;

    // Định nghĩa các loại tìm kiếm (Khớp với OrderViewModel)
    private static final int SEARCH_BY_NAME = 0;
    private static final int SEARCH_BY_PHONE = 1;
    private static final int SEARCH_BY_DATE = 2;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Đảm bảo file layout XML đúng tên (fragment_headermanagement)
        return inflater.inflate(R.layout.fragment_headermanagement, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Ánh xạ View
        ivBack = view.findViewById(R.id.ivBack);
        etSearch = view.findViewById(R.id.etSearch);
        spSearch = view.findViewById(R.id.spSearch);
        tv_title = view.findViewById(R.id.tv_title);

        // 2. Thiết lập tiêu đề
        tv_title.setText("Quản lí hóa đơn");

        // 3. Kết nối ViewModel (Dùng requireActivity để share dữ liệu với OrderManageFragment)
        viewModel = new ViewModelProvider(requireActivity()).get(OrderViewModel.class);

        // 4. Xử lý nút Back
        ivBack.setOnClickListener(v -> {
            if (getActivity() != null) getActivity().finish();
        });

        // 5. Cấu hình Spinner (Loại tìm kiếm)
        setupSearchSpinner();

        // 6. Xử lý sự kiện nhập liệu tìm kiếm
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Gọi hàm tìm kiếm mỗi khi text thay đổi
                performSearch(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupSearchSpinner() {
        LinkedList<String> data = new LinkedList<>(Arrays.asList(
                "Tên người nhận", // Index 0
                "Số điện thoại",  // Index 1
                "Ngày đặt"        // Index 2
        ));
        spSearch.attachDataSource(data);

        // Mặc định chọn cái đầu tiên
        spSearch.setSelectedIndex(SEARCH_BY_NAME);
        etSearch.setHint("Nhập tên người nhận...");
        etSearch.setInputType(InputType.TYPE_CLASS_TEXT);

        // Bắt sự kiện chọn item trong spinner
        spSearch.setOnSpinnerItemSelectedListener((parent, view, position, id) -> {
            // Xóa text cũ để tránh search nhầm kiểu dữ liệu
            etSearch.setText("");

            switch (position) {
                case SEARCH_BY_NAME:
                    etSearch.setHint("Nhập tên người nhận...");
                    etSearch.setInputType(InputType.TYPE_CLASS_TEXT);
                    break;

                case SEARCH_BY_PHONE:
                    etSearch.setHint("Nhập số điện thoại...");
                    etSearch.setInputType(InputType.TYPE_CLASS_PHONE);
                    break;

                case SEARCH_BY_DATE:
                    // Gợi ý định dạng ngày tháng rõ ràng
                    etSearch.setHint("VD: 2025-12-02 (Năm-Tháng-Ngày)");
                    // Dùng TYPE_CLASS_TEXT để dễ gõ dấu gạch ngang "-"
                    // TYPE_CLASS_DATETIME đôi khi hiển thị bàn phím số khó gõ ký tự đặc biệt
                    etSearch.setInputType(InputType.TYPE_CLASS_TEXT);
                    break;
            }

            // Reset lại danh sách hiển thị về ban đầu (vì text search đã bị xóa rỗng)
            performSearch("");
        });
    }

    private void performSearch(String query) {
        if (viewModel == null) return;

        // Lấy loại tìm kiếm hiện tại từ Spinner
        int searchType = spSearch.getSelectedIndex();

        // Gửi sang ViewModel xử lý (Hàm searchOrders bạn đã thêm ở bước trước)
        viewModel.searchOrders(query, searchType);
    }
}