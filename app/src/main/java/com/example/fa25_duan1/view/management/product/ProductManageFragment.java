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
import com.shashank.sony.fancytoastlib.FancyToast;

import java.util.ArrayList;
import java.util.List;

import io.github.cutelibs.cutedialog.CuteDialog;

public class ProductManageFragment extends Fragment {

    private View layoutEmpty;
    private RecyclerView rvData;
    private Button btnAdd;

    private ProductManageAdapter adapter;
    private ProductViewModel productViewModel;
    private AuthorViewModel authorViewModel;

    private List<Author> currentAuthorList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_productmanagement, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        initViewModels();
        setupRecyclerView();
        setupObservers();
        setupEvents();

        // 👉 CHỈ fetch lần đầu (tránh reset filter)
        if (savedInstanceState == null) {
            productViewModel.refreshData();
            attachFilterFragmentOnce();
        }
    }

    private void initViews(View view) {
        rvData = view.findViewById(R.id.rvData);
        btnAdd = view.findViewById(R.id.btnAdd);
        layoutEmpty = view.findViewById(R.id.layout_empty);
    }

    private void initViewModels() {
        productViewModel = new ViewModelProvider(requireActivity()).get(ProductViewModel.class);
        authorViewModel = new ViewModelProvider(requireActivity()).get(AuthorViewModel.class);
    }

    private void setupRecyclerView() {
        adapter = new ProductManageAdapter(
                requireActivity(),
                new ArrayList<>(),
                new ProductManageAdapter.OnProductActionListener() {
                    @Override
                    public void onEditClick(Product product) {
                        openUpdateActivity(product);
                    }

                    @Override
                    public void onDeleteClick(Product product) {
                        confirmDelete(product);
                    }
                }
        );

        rvData.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        rvData.setAdapter(adapter);
    }

    private void setupObservers() {
        productViewModel.getDisplayedProducts().observe(
                getViewLifecycleOwner(),
                products -> {
                    List<Product> safeList =
                            products != null ? products : new ArrayList<>();
                    adapter.setData(safeList);
                    updateEmptyState(safeList);
                }
        );

        productViewModel.getMessage().observe(
                getViewLifecycleOwner(),
                msg -> {
                    if (msg != null && !msg.isEmpty()) {
                        FancyToast.makeText(
                                requireContext(),
                                msg,
                                FancyToast.LENGTH_SHORT,
                                FancyToast.ERROR,
                                false
                        ).show();
                    }
                }
        );

        authorViewModel.getDisplayedAuthors().observe(
                getViewLifecycleOwner(),
                authors -> currentAuthorList =
                        authors != null ? authors : new ArrayList<>()
        );
    }

    private void setupEvents() {
        btnAdd.setOnClickListener(v -> {
            if (currentAuthorList.isEmpty()) {
                showRequireAuthorDialog();
            } else {
                openUpdateActivity(null);
            }
        });
    }

    private void attachFilterFragmentOnce() {
        if (!isAdded()) return;

        Fragment existing =
                getChildFragmentManager().findFragmentById(R.id.fragment_filter);

        if (existing == null) {
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_filter, new ProductFilterFragment())
                    .commit();
        }
    }

    private void updateEmptyState(List<Product> list) {
        boolean isEmpty = list == null || list.isEmpty();
        layoutEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rvData.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void openUpdateActivity(@Nullable Product product) {
        Intent intent = new Intent(requireContext(), UpdateActivity.class);
        intent.putExtra(UpdateActivity.EXTRA_CONTENT_FRAGMENT, "product");

        if (product != null) {
            intent.putExtra(UpdateActivity.EXTRA_HEADER_TITLE, "Chỉnh sửa sản phẩm");
            intent.putExtra("Id", product.getId());
        } else {
            intent.putExtra(UpdateActivity.EXTRA_HEADER_TITLE, "Thêm mới sản phẩm");
        }

        startActivityForResult(intent, 1001);
    }

    private void confirmDelete(Product product) {
        if (product == null) return;

        new CuteDialog.withIcon(requireActivity())
                .setIcon(R.drawable.ic_dialog_confirm)
                .setTitle("Xóa sản phẩm")
                .setDescription("Xác nhận xoá sản phẩm: " + product.getName() + "?")
                .setPrimaryColor(R.color.blue)
                .setPositiveButtonText("Xóa", v -> performDelete(product))
                .setNegativeButtonText("Hủy", v -> {})
                .show();
    }

    private void performDelete(Product product) {
        productViewModel.deleteProduct(product.getId())
                .observe(getViewLifecycleOwner(), res -> {
                    if (res != null && res.isStatus()) {
                        productViewModel.refreshData();

                        new CuteDialog.withIcon(requireActivity())
                                .setIcon(R.drawable.ic_dialog_success)
                                .setTitle("Thành công")
                                .setDescription("Đã xóa sản phẩm")
                                .setPositiveButtonText("Đóng", v -> {})
                                .hideNegativeButton(true)
                                .show();
                    } else {
                        FancyToast.makeText(
                                requireContext(),
                                res != null ? res.getMessage() : "Xóa thất bại",
                                FancyToast.LENGTH_SHORT,
                                FancyToast.ERROR,
                                true
                        ).show();
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == Activity.RESULT_OK) {
            productViewModel.refreshData();
        }
    }

    private void showRequireAuthorDialog() {
        new CuteDialog.withIcon(requireActivity())
                .setIcon(R.drawable.ic_dialog_info)
                .setTitle("Thiếu dữ liệu")
                .setDescription("Hệ thống chưa có tác giả nào. Vui lòng tạo ít nhất 1 tác giả trước khi thêm sản phẩm.")
                .setPrimaryColor(R.color.blue)
                .setPositiveButtonColor(R.color.blue)
                .setTitleTextColor(R.color.black)
                .setDescriptionTextColor(R.color.gray_text)
                .setPositiveButtonText("Đã hiểu", v -> {})
                .hideNegativeButton(true)
                .show();
    }
}
