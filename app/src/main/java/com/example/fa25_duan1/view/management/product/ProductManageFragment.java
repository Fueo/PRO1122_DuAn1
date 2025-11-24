package com.example.fa25_duan1.view.management.product;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

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
import com.example.fa25_duan1.view.dialog.ConfirmDialogFragment;
import com.example.fa25_duan1.view.dialog.NotificationDialogFragment;
import com.example.fa25_duan1.view.management.UpdateActivity;
import com.example.fa25_duan1.viewmodel.AuthorViewModel;
import com.example.fa25_duan1.viewmodel.ProductViewModel;

import java.util.ArrayList;
import java.util.List;

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

        // 1. Nạp Fragment Filter vào Container (Container này KHÔNG được bị ẩn bởi layout_empty)
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
            // Gọi hàm kiểm tra rỗng
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

    // --- HÀM QUAN TRỌNG: XỬ LÝ ẨN/HIỆN ---
    private void checkEmptyState(List<Product> list) {
        if (list == null || list.isEmpty()) {
            // Nếu danh sách rỗng:
            // 1. Hiện layout thông báo rỗng
            layout_empty.setVisibility(View.VISIBLE);
            // 2. Ẩn RecyclerView
            rvData.setVisibility(View.GONE);

            // LƯU Ý: Chúng ta KHÔNG ẩn R.id.fragment_filter ở đây
            // Fragment Filter vẫn nằm trong Activity/Fragment cha và không bị ảnh hưởng
            // trừ khi layout_empty đè lên nó (Xem phần Lưu ý Layout bên dưới)
        } else {
            // Nếu có dữ liệu:
            layout_empty.setVisibility(View.GONE);
            rvData.setVisibility(View.VISIBLE);
        }
    }

    private void showRequireAuthorDialog() {
        NotificationDialogFragment dialogFragment = NotificationDialogFragment.newInstance(
                "Thiếu dữ liệu",
                "Hệ thống chưa có tác giả nào. Vui lòng tạo ít nhất 1 tác giả trước khi thêm sản phẩm.",
                "Đã hiểu",
                NotificationDialogFragment.TYPE_ERROR,
                () -> {}
        );
        dialogFragment.show(getParentFragmentManager(), "RequireAuthorDialog");
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
        ConfirmDialogFragment dialog = new ConfirmDialogFragment(
                "Xóa sản phẩm",
                "Xác nhận xoá sản phẩm " + product.getName(),
                new ConfirmDialogFragment.OnConfirmListener() {
                    @Override
                    public void onConfirmed() {
                        viewModel.deleteProduct(product.getId()).observe(getViewLifecycleOwner(), success -> {
                            if (success != null && success) {
                                viewModel.refreshData();
                                NotificationDialogFragment dialogFragment = NotificationDialogFragment.newInstance(
                                        "Thành công",
                                        "Bạn đã xóa sản phẩm thành công",
                                        "Đóng",
                                        NotificationDialogFragment.TYPE_SUCCESS,
                                        () -> {}
                                );
                                dialogFragment.show(getParentFragmentManager(), "SuccessDialog");
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
}