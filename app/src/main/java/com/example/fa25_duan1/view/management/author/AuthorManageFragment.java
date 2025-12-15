package com.example.fa25_duan1.view.management.author;

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
import com.example.fa25_duan1.adapter.AuthorManageAdapter;
import com.example.fa25_duan1.model.Author;
import com.example.fa25_duan1.model.Product;
import com.example.fa25_duan1.view.management.UpdateActivity;
import com.example.fa25_duan1.viewmodel.AuthorViewModel;
import com.example.fa25_duan1.viewmodel.ProductViewModel;

import java.util.ArrayList;
import java.util.List;

import com.shashank.sony.fancytoastlib.FancyToast;
import io.github.cutelibs.cutedialog.CuteDialog;

public class AuthorManageFragment extends Fragment {

    private View layout_empty;
    private RecyclerView rvData;
    private Button btnAdd;
    private AuthorManageAdapter authorManageAdapter;
    private ProductViewModel productViewModel;
    private AuthorViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_authormanagement, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Khởi tạo ViewModel
        viewModel = new ViewModelProvider(requireActivity()).get(AuthorViewModel.class);
        productViewModel = new ViewModelProvider(requireActivity()).get(ProductViewModel.class);

        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_filter, new AuthorFilterFragment())
                .commit();

        rvData = view.findViewById(R.id.rvData);
        btnAdd = view.findViewById(R.id.btnAdd);
        layout_empty = view.findViewById(R.id.layout_empty);

        // 2. Setup Adapter
        authorManageAdapter = new AuthorManageAdapter(requireActivity(), new ArrayList<>(), new AuthorManageAdapter.OnItemClickListener() {
            @Override
            public void onEditClick(Author author) {
                openUpdateActivity(author);
            }

            @Override
            public void onDeleteClick(Author author) {
                checkAndDeleteAuthor(author);
            }

            @Override
            public void onItemClick(Author author) {
                FancyToast.makeText(requireContext(), author.getName(), FancyToast.LENGTH_SHORT, FancyToast.INFO, false).show();
            }
        }, productViewModel, getViewLifecycleOwner());

        rvData.setLayoutManager(new LinearLayoutManager(getContext()));
        rvData.setAdapter(authorManageAdapter);

        viewModel.getDisplayedAuthors().observe(getViewLifecycleOwner(), authors -> {
            authorManageAdapter.setData(authors);
            checkEmptyState(authors);
        });

        // Observe Error Message
        viewModel.getMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                FancyToast.makeText(requireContext(), msg, FancyToast.LENGTH_SHORT, FancyToast.ERROR, false).show();
            }
        });

        btnAdd.setOnClickListener(v -> openUpdateActivity(null));
    }

    private void openUpdateActivity(Author author) {
        Intent intent = new Intent(getContext(), UpdateActivity.class);
        intent.putExtra(UpdateActivity.EXTRA_HEADER_TITLE, "Thêm mới tác giả");

        if (author != null) {
            intent.putExtra(UpdateActivity.EXTRA_HEADER_TITLE, "Chỉnh sửa tác giả");
            intent.putExtra("Id", author.getAuthorID());
        }
        intent.putExtra(UpdateActivity.EXTRA_CONTENT_FRAGMENT, "author");
        startActivityForResult(intent, 1001);
    }

    private void checkAndDeleteAuthor(Author author) {
        if (author == null) return;

        // [QUAN TRỌNG] Quan sát ApiResponse<List<Product>>
        productViewModel.getProductsByAuthor(author.getAuthorID()).observe(getViewLifecycleOwner(), apiResponse -> {
            if (apiResponse != null && apiResponse.isStatus()) {
                List<Product> products = apiResponse.getData();
                if (products != null && !products.isEmpty()) {
                    // TRƯỜNG HỢP 1: Tác giả CÓ sách -> Chặn xóa
                    showCannotDeleteDialog(author.getName(), products.size());
                } else {
                    // TRƯỜNG HỢP 2: Tác giả KHÔNG có sách -> Hiện popup xác nhận xóa
                    showConfirmDeleteDialog(author);
                }
            } else {
                // Lỗi khi kiểm tra sách
                String msg = (apiResponse != null) ? apiResponse.getMessage() : "Lỗi kết nối";
                FancyToast.makeText(getContext(), "Không thể kiểm tra sách: " + msg, FancyToast.LENGTH_SHORT, FancyToast.ERROR, false).show();
            }
        });
    }

    private void showCannotDeleteDialog(String authorName, int bookCount) {
        new CuteDialog.withIcon(requireActivity())
                .setIcon(R.drawable.ic_dialog_info)
                .setTitle("Không thể xóa")
                .setDescription("Tác giả " + authorName + " đang có " + bookCount + " đầu sách. Vui lòng xóa hết sách trước.")
                .setPrimaryColor(R.color.blue)
                .setPositiveButtonColor(R.color.blue)
                .setPositiveButtonText("Đã hiểu", v -> {})
                .hideNegativeButton(true)
                .show();
    }

    private void showConfirmDeleteDialog(Author author) {
        new CuteDialog.withIcon(requireActivity())
                .setIcon(R.drawable.ic_dialog_confirm)
                .setTitle("Xóa tác giả")
                .setDescription("Bạn có chắc chắn muốn xóa tác giả " + author.getName() + "?")
                .setPrimaryColor(R.color.blue)
                .setPositiveButtonColor(R.color.blue)
                .setPositiveButtonText("Xóa", v -> performDelete(author))
                .setNegativeButtonText("Hủy", v -> {})
                .show();
    }

    private void performDelete(Author author) {
        // [SỬA] Xử lý ApiResponse<Void>
        viewModel.deleteAuthor(author.getAuthorID()).observe(getViewLifecycleOwner(), apiResponse -> {
            if (apiResponse != null && apiResponse.isStatus()) {
                viewModel.refreshData(); // Refresh list

                new CuteDialog.withIcon(requireActivity())
                        .setIcon(R.drawable.ic_dialog_success)
                        .setTitle("Thành công")
                        .setDescription("Đã xóa tác giả thành công.")
                        .setPrimaryColor(R.color.blue)
                        .setPositiveButtonColor(R.color.blue)
                        .setPositiveButtonText("Đóng", v -> {})
                        .hideNegativeButton(true)
                        .show();
            } else {
                String msg = (apiResponse != null) ? apiResponse.getMessage() : "Lỗi xóa tác giả";
                FancyToast.makeText(getContext(), msg, FancyToast.LENGTH_SHORT, FancyToast.ERROR, true).show();
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

    private void checkEmptyState(List<Author> list) {
        if (list == null || list.isEmpty()) {
            layout_empty.setVisibility(View.VISIBLE);
            rvData.setVisibility(View.GONE);
        } else {
            layout_empty.setVisibility(View.GONE);
            rvData.setVisibility(View.VISIBLE);
        }
    }
}