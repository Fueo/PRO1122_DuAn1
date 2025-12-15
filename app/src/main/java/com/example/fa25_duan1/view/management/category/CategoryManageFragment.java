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

        viewModel = new ViewModelProvider(requireActivity()).get(CategoryViewModel.class);

        if (savedInstanceState == null) {
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.fragment_filter, new CategoryFilterFragment())
                    .commit();
        }

        rvData = view.findViewById(R.id.rvData);
        btnAdd = view.findViewById(R.id.btnAdd);
        layout_empty = view.findViewById(R.id.layout_empty);

        rvData.setLayoutManager(new LinearLayoutManager(getContext()));
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
        rvData.setAdapter(categoryAdapter);

        // **Thay đổi ở đây:** quan sát allCategoriesForAdmin để hiển thị tất cả danh mục
        viewModel.getAllCategoriesForAdmin().observe(getViewLifecycleOwner(), categories -> {
            categoryAdapter.setData(categories);
            checkEmptyState(categories);
        });

        viewModel.getMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                FancyToast.makeText(requireContext(), msg, FancyToast.LENGTH_SHORT, FancyToast.ERROR, false).show();
            }
        });

        btnAdd.setOnClickListener(v -> openUpdateActivity(null));
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.refreshAllForAdmin();
        }

    }

    private void openUpdateActivity(Category category) {
        Intent intent = new Intent(getContext(), UpdateActivity.class);
        intent.putExtra(UpdateActivity.EXTRA_HEADER_TITLE, category == null ? "Thêm mới danh mục" : "Chỉnh sửa danh mục");
        if (category != null) intent.putExtra("Id", category.get_id());
        intent.putExtra(UpdateActivity.EXTRA_CONTENT_FRAGMENT, "category");
        startActivityForResult(intent, 1001);
    }

    private void deleteCategory(Category category) {
        if (category == null) return;

        new CuteDialog.withIcon(requireActivity())
                .setIcon(R.drawable.ic_dialog_confirm)
                .setTitle("Xóa danh mục")
                .setDescription("Bạn có chắc chắn muốn xóa danh mục: " + category.getName() + "?")
                .setPrimaryColor(R.color.blue)
                .setPositiveButtonColor(R.color.blue)
                .setTitleTextColor(R.color.black)
                .setDescriptionTextColor(R.color.gray_text)
                .setPositiveButtonText("Xóa", v -> performDelete(category))
                .setNegativeButtonText("Hủy", v -> {})
                .show();
    }

    private void performDelete(Category category) {
        viewModel.deleteCategory(category.get_id()).observe(getViewLifecycleOwner(), apiResponse -> {
            if (apiResponse != null && apiResponse.isStatus()) {
                viewModel.refreshAllForAdmin();
                new CuteDialog.withIcon(requireActivity())
                        .setIcon(R.drawable.ic_dialog_success)
                        .setTitle("Thành công")
                        .setDescription("Đã xóa danh mục thành công!")
                        .setPrimaryColor(R.color.blue)
                        .setPositiveButtonColor(R.color.blue)
                        .setPositiveButtonText("Đóng", v -> {})
                        .hideNegativeButton(true)
                        .show();
            } else {
                String msg = (apiResponse != null) ? apiResponse.getMessage() : "Xóa thất bại";
                FancyToast.makeText(getContext(), msg, FancyToast.LENGTH_SHORT, FancyToast.ERROR, true).show();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == Activity.RESULT_OK && viewModel != null) viewModel.refreshAllForAdmin();;
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
