package com.example.fa25_duan1.view.management.product;

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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fa25_duan1.R;
import com.example.fa25_duan1.adapter.ProductManageAdapter;
import com.example.fa25_duan1.model.Author;
import com.example.fa25_duan1.model.Product;
import com.example.fa25_duan1.view.management.UpdateActivity;
import com.example.fa25_duan1.viewmodel.AuthorViewModel;
import com.example.fa25_duan1.viewmodel.ProductViewModel;

import java.util.ArrayList;
import java.util.List;

// --- IMPORT THƯ VIỆN MỚI ---
import com.shashank.sony.fancytoastlib.FancyToast;
import io.github.cutelibs.cutedialog.CuteDialog;

public class ProductManageFragment extends Fragment {
    private View layout_empty;
    private RecyclerView rvData;
    private Button btnAdd;
    private ProductManageAdapter productManageAdapter;
    private ProductViewModel viewModel;
    private AuthorViewModel authorViewModel;
    private List<Author> currentAuthorList = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_productmanagement, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Nạp Fragment Filter
        if (savedInstanceState == null) {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_filter, new ProductFilterFragment())
                    .commit();
        }

        rvData = view.findViewById(R.id.rvData);
        btnAdd = view.findViewById(R.id.btnAdd);
        layout_empty = view.findViewById(R.id.layout_empty);

        productManageAdapter = new ProductManageAdapter(requireActivity(), new ArrayList<>(), new ProductManageAdapter.OnProductActionListener() {
            @Override
            public void onEditClick(Product product) {
                openUpdateActivity(product);
            }

            @Override
            public void onDeleteClick(Product product) {
                deleteProduct(product);
            }
        });

        rvData.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rvData.setAdapter(productManageAdapter);

        // Khởi tạo ViewModels
        viewModel = new ViewModelProvider(requireActivity()).get(ProductViewModel.class);
        authorViewModel = new ViewModelProvider(requireActivity()).get(AuthorViewModel.class);

        // 2. Quan sát danh sách sản phẩm
        viewModel.getDisplayedProducts().observe(getViewLifecycleOwner(), products -> {
            productManageAdapter.setData(products);
            checkEmptyState(products);
        });

        authorViewModel.getDisplayedAuthors().observe(getViewLifecycleOwner(), authors -> {
            currentAuthorList = authors != null ? authors : new ArrayList<>();
        });

        btnAdd.setOnClickListener(v -> {
            if (currentAuthorList == null || currentAuthorList.isEmpty()) {
                showRequireAuthorDialog();
            } else {
                openUpdateActivity(null);
            }
        });

        viewModel.refreshData();
    }

    private void checkEmptyState(List<Product> list) {
        if (list == null || list.isEmpty()) {
            layout_empty.setVisibility(View.VISIBLE);
            rvData.setVisibility(View.GONE);
        } else {
            layout_empty.setVisibility(View.GONE);
            rvData.setVisibility(View.VISIBLE);
        }
    }

    // --- SỬ DỤNG CUTEDIALOG (CẢNH BÁO THIẾU TÁC GIẢ) ---
    private void showRequireAuthorDialog() {
        new CuteDialog.withIcon(requireActivity())
                .setIcon(R.drawable.ic_dialog_info) // Hoặc icon warning
                .setTitle("Thiếu dữ liệu")
                .setDescription("Hệ thống chưa có tác giả nào. Vui lòng tạo ít nhất 1 tác giả trước khi thêm sản phẩm.")

                // Cấu hình màu sắc đồng bộ Blue
                .setPrimaryColor(R.color.blue)
                .setPositiveButtonColor(R.color.blue)
                .setTitleTextColor(R.color.black)
                .setDescriptionTextColor(R.color.gray_text)

                .setPositiveButtonText("Đã hiểu", v -> {
                    // Đóng dialog
                })
                .hideNegativeButton(true)
                .show();
    }

    private void openUpdateActivity(Product product) {
        Intent intent = new Intent(getContext(), UpdateActivity.class);
        intent.putExtra(UpdateActivity.EXTRA_HEADER_TITLE, "Thêm mới sản phẩm");
        if (product != null) {
            intent.putExtra(UpdateActivity.EXTRA_HEADER_TITLE, "Chỉnh sửa sản phẩm");
            intent.putExtra("Id", product.getId());
        }
        intent.putExtra(UpdateActivity.EXTRA_CONTENT_FRAGMENT, "product");
        startActivityForResult(intent, 1001);
    }

    private void deleteProduct(Product product) {
        if (product == null) return;

        // --- SỬ DỤNG CUTEDIALOG (XÁC NHẬN XÓA) ---
        new CuteDialog.withIcon(requireActivity())
                .setIcon(R.drawable.ic_dialog_confirm)
                .setTitle("Xóa sản phẩm")
                .setDescription("Xác nhận xoá sản phẩm: " + product.getName() + "?")

                // Cấu hình màu sắc
                .setPrimaryColor(R.color.blue)
                .setPositiveButtonColor(R.color.blue)
                .setTitleTextColor(R.color.black)
                .setDescriptionTextColor(R.color.gray_text)

                .setPositiveButtonText("Xóa", v -> {
                    performDelete(product);
                })
                .setNegativeButtonText("Hủy", v -> {
                    // Tự động đóng
                })
                .show();
    }

    private void performDelete(Product product) {
        viewModel.deleteProduct(product.getId()).observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                viewModel.refreshData();

                // --- SỬ DỤNG CUTEDIALOG (THÀNH CÔNG) ---
                new CuteDialog.withIcon(requireActivity())
                        .setIcon(R.drawable.ic_dialog_success)
                        .setTitle("Thành công")
                        .setDescription("Bạn đã xóa sản phẩm thành công")

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


}