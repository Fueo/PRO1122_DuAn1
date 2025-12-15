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
import com.example.fa25_duan1.model.Category;
import com.example.fa25_duan1.viewmodel.CategoryViewModel;

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

        if (getActivity().getIntent() != null) {
            categoryId = getActivity().getIntent().getStringExtra("Id");
        }

        if (categoryId != null) {
            loadCategoryData(categoryId);
            btnSave.setText("Cập nhật");
        } else {
            btnSave.setText("Thêm mới");
        }

        btnSave.setOnClickListener(v -> {
            String name = edtCategoryName.getText().toString().trim();

            if (name.isEmpty()) {
                edtCategoryName.setError("Tên không được để trống");
                FancyToast.makeText(getContext(), "Vui lòng nhập tên danh mục", FancyToast.LENGTH_SHORT, FancyToast.WARNING, true).show();
                return;
            }

            if (categoryId == null) {
                // Thêm mới (Xử lý ApiResponse<Category>)
                viewModel.addCategory(name).observe(getViewLifecycleOwner(), apiResponse -> {
                    if (apiResponse != null && apiResponse.isStatus()) {
                        showSuccessDialog("Thêm danh mục thành công!");
                    } else {
                        String msg = (apiResponse != null) ? apiResponse.getMessage() : "Thêm thất bại";
                        FancyToast.makeText(getContext(), msg, FancyToast.LENGTH_SHORT, FancyToast.ERROR, true).show();
                    }
                });
            } else {
                // Cập nhật (Xử lý ApiResponse<Category>)
                viewModel.updateCategory(categoryId, name).observe(getViewLifecycleOwner(), apiResponse -> {
                    if (apiResponse != null && apiResponse.isStatus()) {
                        showSuccessDialog("Cập nhật danh mục thành công!");
                    } else {
                        String msg = (apiResponse != null) ? apiResponse.getMessage() : "Cập nhật thất bại";
                        FancyToast.makeText(getContext(), msg, FancyToast.LENGTH_SHORT, FancyToast.ERROR, true).show();
                    }
                });
            }
        });
    }

    private void loadCategoryData(String id) {
        // [SỬA] Xử lý ApiResponse<Category>
        viewModel.getCategoryByID(id).observe(getViewLifecycleOwner(), apiResponse -> {
            if (apiResponse != null && apiResponse.isStatus()) {
                Category category = apiResponse.getData();
                if (category != null) {
                    edtCategoryName.setText(category.getName());
                }
            } else {
                String msg = (apiResponse != null) ? apiResponse.getMessage() : "Không tìm thấy danh mục";
                FancyToast.makeText(getContext(), msg, FancyToast.LENGTH_SHORT, FancyToast.ERROR, true).show();
            }
        });
    }

    private void showSuccessDialog(String message) {
        new CuteDialog.withIcon(requireActivity())
                .setIcon(R.drawable.ic_dialog_success)
                .setTitle("Thành công")
                .setDescription(message)
                .setPrimaryColor(R.color.blue)
                .setPositiveButtonColor(R.color.blue)
                .setTitleTextColor(R.color.black)
                .setDescriptionTextColor(R.color.gray_text)
                .setPositiveButtonText("Đóng", v -> finishActivity())
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