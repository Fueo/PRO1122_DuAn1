package com.example.fa25_duan1.view.management.category;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.fa25_duan1.R;
import com.example.fa25_duan1.viewmodel.CategoryViewModel;

// --- IMPORT THƯ VIỆN MỚI ---
import com.shashank.sony.fancytoastlib.FancyToast;
import io.github.cutelibs.cutedialog.CuteDialog;

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

        // Lấy ID từ Intent
        if (getActivity().getIntent() != null) {
            categoryId = getActivity().getIntent().getStringExtra("Id");
        }

        // Nếu có ID -> Chế độ Edit -> Load dữ liệu cũ
        if (categoryId != null) {
            loadCategoryData(categoryId);
            btnSave.setText("Cập nhật");
        } else {
            btnSave.setText("Thêm mới");
        }

        btnSave.setOnClickListener(v -> {
            String name = edtCategoryName.getText().toString().trim();

            // Validate input
            if (name.isEmpty()) {
                edtCategoryName.setError("Tên không được để trống");
                FancyToast.makeText(getContext(), "Vui lòng nhập tên danh mục", FancyToast.LENGTH_SHORT, FancyToast.WARNING, true).show();
                return;
            }

            if (categoryId == null) {
                // Thêm mới
                viewModel.addCategory(name).observe(getViewLifecycleOwner(), category -> {
                    if (category != null) {
                        showSuccessDialog("Thêm danh mục thành công!");
                    } else {
                        FancyToast.makeText(getContext(), "Thêm thất bại", FancyToast.LENGTH_SHORT, FancyToast.ERROR, true).show();
                    }
                });
            } else {
                // Cập nhật
                viewModel.updateCategory(categoryId, name).observe(getViewLifecycleOwner(), category -> {
                    if (category != null) {
                        showSuccessDialog("Cập nhật danh mục thành công!");
                    } else {
                        FancyToast.makeText(getContext(), "Cập nhật thất bại", FancyToast.LENGTH_SHORT, FancyToast.ERROR, true).show();
                    }
                });
            }
        });
    }

    private void loadCategoryData(String id) {
        viewModel.getCategoryByID(id).observe(getViewLifecycleOwner(), category -> {
            if (category != null) {
                edtCategoryName.setText(category.getName());
            } else {
                FancyToast.makeText(getContext(), "Không tìm thấy danh mục", FancyToast.LENGTH_SHORT, FancyToast.ERROR, true).show();
            }
        });
    }

    // --- HÀM HIỂN THỊ DIALOG THÀNH CÔNG ---
    private void showSuccessDialog(String message) {
        new CuteDialog.withIcon(requireActivity())
                .setIcon(R.drawable.ic_dialog_success)
                .setTitle("Thành công")
                .setDescription(message)

                // Cấu hình màu sắc đồng bộ Blue
                .setPrimaryColor(R.color.blue)
                .setPositiveButtonColor(R.color.blue)
                .setTitleTextColor(R.color.black)
                .setDescriptionTextColor(R.color.gray_text)

                .setPositiveButtonText("Đóng", v -> {
                    finishActivity(); // Đóng Activity sau khi bấm nút trên Dialog
                })
                .hideNegativeButton(true)
                .show();
    }

    private void finishActivity() {
        if (getActivity() != null) {
            getActivity().setResult(Activity.RESULT_OK);
            getActivity().finish();
        }
    }
}