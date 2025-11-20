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
import com.example.fa25_duan1.model.Product;
import com.example.fa25_duan1.view.dialog.ConfirmDialogFragment;
import com.example.fa25_duan1.view.dialog.NotificationDialogFragment;
import com.example.fa25_duan1.view.management.UpdateActivity;
import com.example.fa25_duan1.viewmodel.ProductViewModel;

import java.util.ArrayList;
import java.util.List;

public class ProductManageFragment extends Fragment {
    private View layout_empty;
    private RecyclerView rvData;
    private Button btnAdd;
    private ProductManageAdapter productManageAdapter;
    private ProductViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_productmanagement, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_filter, new ProductFilterFragment())
                .commit();

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

        // Sử dụng ViewModel scoped to Activity nếu muốn chia sẻ dữ liệu giữa Fragment
// Trong Fragment
        viewModel = new ViewModelProvider(requireActivity()).get(ProductViewModel.class);

        viewModel.getDisplayedProducts().observe(getViewLifecycleOwner(), products -> {
            productManageAdapter.setData(products);
//            checkEmptyState(products); // Gọi hàm kiểm tra
        });


        btnAdd.setOnClickListener(v -> openUpdateActivity(null));
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
                        // Gọi API và observe kết quả
                        viewModel.deleteProduct(product.getId()).observe(getViewLifecycleOwner(), success -> {
                            if (success != null && success) {
                                // Nếu API báo thành công, tải lại toàn bộ danh sách
                                viewModel.refreshData();

                                // Hiển thị thông báo thành công
                                NotificationDialogFragment dialogFragment = NotificationDialogFragment.newInstance(
                                        "Thành công",
                                        "Bạn đã xóa sản phẩm thành công",
                                        "Đóng",
                                        NotificationDialogFragment.TYPE_SUCCESS,
                                        () -> {}
                                );
                                dialogFragment.show(getParentFragmentManager(), "SuccessDialog");
                            } else {
                                // Xử lý khi xóa thất bại (optional)
                                Toast.makeText(getContext(), "Xóa thất bại", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
        );
        dialog.show(getParentFragmentManager(), "ConfirmDialog");
    }

    // Không cần gọi loadUsers() ở onActivityResult vì LiveData tự cập nhật
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == Activity.RESULT_OK) {
            viewModel.refreshData();
        }
    }

    // Viết tách ra một hàm riêng cho gọn (Optional)
    private void checkEmptyState(List<Product> list) {
        boolean isEmpty = (list == null || list.isEmpty());
        Log.d("ProductManageFragment", "checkEmptyState called. List is empty: " + isEmpty + ", Size: " + (list != null ? list.size() : "null"));
        if (list == null || list.isEmpty()) {
            layout_empty.setVisibility(View.VISIBLE); // Hiện ảnh rỗng
            rvData.setVisibility(View.GONE);         // Ẩn danh sách
        } else {
            layout_empty.setVisibility(View.GONE);    // Ẩn ảnh rỗng
            rvData.setVisibility(View.VISIBLE);      // Hiện danh sách
        }
    }
}
