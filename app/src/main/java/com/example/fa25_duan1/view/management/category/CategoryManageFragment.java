package com.example.fa25_duan1.view.management.category;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fa25_duan1.R;
import com.example.fa25_duan1.adapter.CategoryAdapter; // Dùng Adapter mới
import com.example.fa25_duan1.adapter.CategoryManageAdapter;
import com.example.fa25_duan1.model.Author;
import com.example.fa25_duan1.model.Category;
import com.example.fa25_duan1.view.dialog.ConfirmDialogFragment;
import com.example.fa25_duan1.view.dialog.NotificationDialogFragment;
import com.example.fa25_duan1.view.management.UpdateActivity;
import com.example.fa25_duan1.viewmodel.CategoryViewModel; // Dùng ViewModel Category

import java.util.ArrayList;
import java.util.List;

public class CategoryManageFragment extends Fragment {
    private View layout_empty;
    private RecyclerView rvData;
    private Button btnAdd;
    private CategoryManageAdapter categoryAdapter; // Sửa tên biến
    private CategoryViewModel viewModel;    // Sửa ViewModel

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Layout này phải chứa RecyclerView id=rvData và Button id=btnAdd
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
                checkEmptyState(categories); // Gọi hàm kiểm tra
            }
        });

        btnAdd.setOnClickListener(v -> openUpdateActivity(null));
    }

    private void openUpdateActivity(Category category) {
        Intent intent = new Intent(getContext(), UpdateActivity.class);
        intent.putExtra(UpdateActivity.EXTRA_HEADER_TITLE, "Thêm mới danh mục");

        if (category != null) {
            intent.putExtra(UpdateActivity.EXTRA_HEADER_TITLE, "Chỉnh sửa danh mục");
            intent.putExtra("Id", category.get_id()); // Lưu ý: Kiểm tra model Category dùng id hay _id
        }
        // Key này để UpdateActivity biết cần load Fragment nào
        intent.putExtra(UpdateActivity.EXTRA_CONTENT_FRAGMENT, "category");

        startActivityForResult(intent, 1001);
    }

    private void deleteCategory(Category category) {
        if (category == null) return;

        ConfirmDialogFragment dialog = new ConfirmDialogFragment(
                "Xóa danh mục",
                "Xác nhận xoá danh mục: " + category.getName(),
                new ConfirmDialogFragment.OnConfirmListener() {
                    @Override
                    public void onConfirmed() {
                        viewModel.deleteCategory(category.get_id()).observe(getViewLifecycleOwner(), success -> {
                            if (success != null && success) {
                                viewModel.refreshData();
                                Toast.makeText(getContext(), "Đã xóa thành công", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "Xóa thất bại", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
        );
        dialog.show(getParentFragmentManager(), "ConfirmDialog");
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
            layout_empty.setVisibility(View.VISIBLE); // Hiện ảnh rỗng
            rvData.setVisibility(View.GONE);         // Ẩn danh sách
        } else {
            layout_empty.setVisibility(View.GONE);    // Ẩn ảnh rỗng
            rvData.setVisibility(View.VISIBLE);      // Hiện danh sách
        }
    }
}