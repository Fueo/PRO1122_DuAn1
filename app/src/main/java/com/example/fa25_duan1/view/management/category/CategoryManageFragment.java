package com.example.fa25_duan1.view.management.category;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fa25_duan1.R;
import com.example.fa25_duan1.adapter.CategoryManageAdapter;
import com.example.fa25_duan1.model.Category;
import com.example.fa25_duan1.view.management.UpdateActivity;
import com.example.fa25_duan1.viewmodel.CategoryViewModel;

import java.util.ArrayList;
import java.util.List;

// --- IMPORT THƯ VIỆN MỚI ---
import com.shashank.sony.fancytoastlib.FancyToast;
import io.github.cutelibs.cutedialog.CuteDialog;

public class CategoryManageFragment extends Fragment {
    private View layout_empty;
    private RecyclerView rvData;
    private Button btnAdd;
    private CategoryManageAdapter categoryAdapter;
    private CategoryViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_category_management, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_filter, new CategoryFilterFragment())
                .commit();

        rvData = view.findViewById(R.id.rvData);
        btnAdd = view.findViewById(R.id.btnAdd);
        layout_empty = view.findViewById(R.id.layout_empty);

        // Khởi tạo Adapter
        categoryAdapter = new CategoryManageAdapter(getContext(), new ArrayList<>(), new CategoryManageAdapter.OnCategoryActionListener() {
            @Override
            public void onEditClick(Category category) {
                openUpdateActivity(category);
            }

            @Override
            public void onDeleteClick(Category category) {
                deleteCategory(category);
            }
        });

        rvData.setLayoutManager(new LinearLayoutManager(getContext()));
        rvData.setAdapter(categoryAdapter);

        // Khởi tạo ViewModel
        viewModel = new ViewModelProvider(requireActivity()).get(CategoryViewModel.class);

        // Observe dữ liệu
        viewModel.getDisplayedCategories().observe(getViewLifecycleOwner(), categories -> {
            if (categories != null) {
                categoryAdapter.setData(categories);
                checkEmptyState(categories);
            }
        });

        btnAdd.setOnClickListener(v -> openUpdateActivity(null));
    }

    private void openUpdateActivity(Category category) {
        Intent intent = new Intent(getContext(), UpdateActivity.class);
        intent.putExtra(UpdateActivity.EXTRA_HEADER_TITLE, "Thêm mới danh mục");

        if (category != null) {
            intent.putExtra(UpdateActivity.EXTRA_HEADER_TITLE, "Chỉnh sửa danh mục");
            intent.putExtra("Id", category.get_id());
        }
        intent.putExtra(UpdateActivity.EXTRA_CONTENT_FRAGMENT, "category");

        startActivityForResult(intent, 1001);
    }

    private void deleteCategory(Category category) {
        if (category == null) return;

        // --- SỬ DỤNG CUTEDIALOG (XÁC NHẬN) ---
        new CuteDialog.withIcon(requireActivity())
                .setIcon(R.drawable.ic_dialog_confirm)
                .setTitle("Xóa danh mục")
                .setDescription("Bạn có chắc chắn muốn xóa danh mục: " + category.getName() + "?")

                // Cấu hình màu sắc đồng bộ Blue
                .setPrimaryColor(R.color.blue)
                .setPositiveButtonColor(R.color.blue)
                .setTitleTextColor(R.color.black)
                .setDescriptionTextColor(R.color.gray_text)

                .setPositiveButtonText("Xóa", v -> {
                    performDelete(category);
                })
                .setNegativeButtonText("Hủy", v -> {
                    // Tự động đóng dialog
                })
                .show();
    }

    private void performDelete(Category category) {
        viewModel.deleteCategory(category.get_id()).observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                viewModel.refreshData();

                // --- SỬ DỤNG CUTEDIALOG (THÀNH CÔNG) ---
                new CuteDialog.withIcon(requireActivity())
                        .setIcon(R.drawable.ic_dialog_success)
                        .setTitle("Thành công")
                        .setDescription("Đã xóa danh mục thành công!")

                        .setPrimaryColor(R.color.blue)
                        .setPositiveButtonColor(R.color.blue)
                        .setTitleTextColor(R.color.black)
                        .setDescriptionTextColor(R.color.gray_text)

                        .setPositiveButtonText("Đóng", v -> {})
                        .hideNegativeButton(true)
                        .show();
            } else {
                // --- SỬ DỤNG FANCY TOAST (LỖI) ---
                FancyToast.makeText(getContext(),
                        "Xóa thất bại! Vui lòng thử lại.",
                        FancyToast.LENGTH_SHORT,
                        FancyToast.ERROR,
                        true).show();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == Activity.RESULT_OK) {
            viewModel.refreshData();
        }
    }

    private void checkEmptyState(List<Category> list) {
        if (list == null || list.isEmpty()) {
            layout_empty.setVisibility(View.VISIBLE);
            rvData.setVisibility(View.GONE);
        } else {
            layout_empty.setVisibility(View.GONE);
            rvData.setVisibility(View.VISIBLE);
        }
    }
}