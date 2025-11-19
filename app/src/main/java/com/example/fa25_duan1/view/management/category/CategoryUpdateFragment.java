package com.example.fa25_duan1.view.management.category;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.fa25_duan1.R;
import com.example.fa25_duan1.model.Category;
import com.example.fa25_duan1.viewmodel.CategoryViewModel;

public class CategoryUpdateFragment extends Fragment {

    private EditText edtCategoryName;
    private Button btnSave;
    private CategoryViewModel viewModel;
    private String categoryId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_categoryupdate, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        edtCategoryName = view.findViewById(R.id.edtCategoryName);
        btnSave = view.findViewById(R.id.btnSave);
        viewModel = new ViewModelProvider(this).get(CategoryViewModel.class);

        // Lấy ID từ Intent (truyền từ Activity mẹ)
        if (getActivity().getIntent() != null) {
            categoryId = getActivity().getIntent().getStringExtra("Id");
        }

        // Nếu có ID -> Chế độ Edit -> Load dữ liệu cũ
        if (categoryId != null) {
            loadCategoryData(categoryId);
        }

        btnSave.setOnClickListener(v -> {
            String name = edtCategoryName.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập tên danh mục", Toast.LENGTH_SHORT).show();
                return;
            }

            if (categoryId == null) {
                // Thêm mới
                viewModel.addCategory(name).observe(getViewLifecycleOwner(), category -> {
                    if (category != null) {
                        Toast.makeText(getContext(), "Thêm thành công", Toast.LENGTH_SHORT).show();
                        finishActivity();
                    } else {
                        Toast.makeText(getContext(), "Thêm thất bại", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                // Cập nhật
                viewModel.updateCategory(categoryId, name).observe(getViewLifecycleOwner(), category -> {
                    if (category != null) {
                        Toast.makeText(getContext(), "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                        finishActivity();
                    } else {
                        Toast.makeText(getContext(), "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void loadCategoryData(String id) {
        viewModel.getCategoryByID(id).observe(getViewLifecycleOwner(), category -> {
            if (category != null) {
                edtCategoryName.setText(category.getName());
            }
        });
    }

    private void finishActivity() {
        if (getActivity() != null) {
            getActivity().setResult(Activity.RESULT_OK);
            getActivity().finish();
        }
    }
}