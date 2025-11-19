package com.example.fa25_duan1.view.management.category;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.fa25_duan1.R;
import com.example.fa25_duan1.viewmodel.CategoryViewModel; // Dùng CategoryViewModel

import org.angmarch.views.NiceSpinner;

import java.util.Arrays;
import java.util.LinkedList;

public class CategoryFilterFragment extends Fragment {

    private NiceSpinner spSort;
    private CategoryViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Lưu ý: Đảm bảo layout fragment_catetfilter.xml của bạn có id: spSort
        return inflater.inflate(R.layout.fragment_catetfilter, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        spSort = view.findViewById(R.id.spSort);

        // Kết nối với ViewModel của Activity cha (để chia sẻ dữ liệu với ManageFragment)
        viewModel = new ViewModelProvider(requireActivity()).get(CategoryViewModel.class);

        setupSpinner();
    }

    private void setupSpinner() {
        LinkedList<String> data = new LinkedList<>(Arrays.asList(
                "Sắp xếp A - Z",
                "Sắp xếp Z - A"
        ));
        spSort.attachDataSource(data);

        spSort.setOnSpinnerItemSelectedListener((parent, v, position, id) -> {
            // Bạn cần thêm hàm sortByName vào CategoryViewModel nếu muốn dùng chức năng này
            // Hiện tại tôi để logic tìm kiếm tạm thời
            if (position == 0) {
                // Logic sort A-Z
                viewModel.searchCategories(""); // Reset về mặc định (thường là A-Z)
            } else {
                // Logic sort Z-A (Cần implement thêm trong ViewModel nếu cần)
            }
        });
    }
}