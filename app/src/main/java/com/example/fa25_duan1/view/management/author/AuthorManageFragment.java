package com.example.fa25_duan1.view.management.author;

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
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fa25_duan1.R;
import com.example.fa25_duan1.adapter.AuthorManageAdapter;
import com.example.fa25_duan1.model.Author;
import com.example.fa25_duan1.model.Product; // Import model Product
import com.example.fa25_duan1.view.dialog.ConfirmDialogFragment;
import com.example.fa25_duan1.view.dialog.NotificationDialogFragment;
import com.example.fa25_duan1.view.management.UpdateActivity;
import com.example.fa25_duan1.viewmodel.AuthorViewModel;
import com.example.fa25_duan1.viewmodel.ProductViewModel;

import java.util.ArrayList;
import java.util.List;

public class AuthorManageFragment extends Fragment {
    // ... (Các biến khai báo giữ nguyên)
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
                // Gọi hàm check trước khi xóa
                checkAndDeleteAuthor(author);
            }

            @Override
            public void onItemClick(Author author) {

            }
        }, productViewModel, getViewLifecycleOwner());

        rvData.setLayoutManager(new LinearLayoutManager(getContext()));
        rvData.setAdapter(authorManageAdapter);

        viewModel.getDisplayedAuthors().observe(getViewLifecycleOwner(), authors -> {
            authorManageAdapter.setData(authors);
            checkEmptyState(authors);
        });

        btnAdd.setOnClickListener(v -> openUpdateActivity(null));
    }

    // ... (Hàm openUpdateActivity giữ nguyên)

    /**
     * Hàm xử lý logic xóa: Check sản phẩm -> Xác nhận -> Xóa
     */
    private void checkAndDeleteAuthor(Author author) {
        if (author == null) return;

        // BƯỚC 1: Kiểm tra xem tác giả có sách nào không
        // Lưu ý: Chúng ta dùng observe một lần cho hành động này.
        // Vì repository trả về LiveData mới mỗi lần gọi, nên logic này an toàn.
        productViewModel.getProductsByAuthor(author.getAuthorID()).observe(getViewLifecycleOwner(), new Observer<List<Product>>() {
            @Override
            public void onChanged(List<Product> products) {
                // Quan trọng: Remove observer ngay sau khi nhận kết quả để tránh leak hoặc chạy lại không mong muốn
                // productViewModel.getProductsByAuthor(author.getAuthorID()).removeObserver(this);
                // (Tuy nhiên, với cách viết Repo tạo MutableLiveData mới mỗi lần gọi thì không bắt buộc remove, nhưng nên lưu ý)

                if (products != null && !products.isEmpty()) {
                    // TRƯỜNG HỢP 1: Tác giả CÓ sách -> Chặn xóa
                    showCannotDeleteDialog(author.getName(), products.size());
                } else {
                    // TRƯỜNG HỢP 2: Tác giả KHÔNG có sách -> Hiện popup xác nhận xóa
                    showConfirmDeleteDialog(author);
                }
            }
        });
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
    /**
     * Hiển thị thông báo không cho phép xóa
     */
    private void showCannotDeleteDialog(String authorName, int bookCount) {
        NotificationDialogFragment dialogFragment = NotificationDialogFragment.newInstance(
                "Không thể xóa",
                "Tác giả " + authorName + " đang sở hữu " + bookCount + " đầu sách trên hệ thống. Vui lòng xóa hết sách của tác giả này trước.",
                "Đã hiểu",
                NotificationDialogFragment.TYPE_ERROR, // Đảm bảo bạn có TYPE_WARNING hoặc dùng TYPE_ERROR
                () -> {}
        );
        dialogFragment.show(getParentFragmentManager(), "WarningDialog");
    }

    /**
     * Hiển thị popup xác nhận xóa (Logic cũ)
     */
    private void showConfirmDeleteDialog(Author author) {
        ConfirmDialogFragment dialog = new ConfirmDialogFragment(
                "Xóa tác giả",
                "Bạn có chắc chắn muốn xóa tác giả " + author.getName() + "? Hành động này không thể hoàn tác.",
                new ConfirmDialogFragment.OnConfirmListener() {
                    @Override
                    public void onConfirmed() {
                        performDelete(author);
                    }
                }
        );
        dialog.show(getParentFragmentManager(), "ConfirmDialog");
    }

    /**
     * Thực hiện gọi API xóa sau khi đã qua các bước kiểm tra
     */
    private void performDelete(Author author) {
        viewModel.deleteAuthor(author.getAuthorID()).observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                viewModel.refreshData();
                NotificationDialogFragment dialogFragment = NotificationDialogFragment.newInstance(
                        "Thành công",
                        "Đã xóa tác giả thành công.",
                        "Đóng",
                        NotificationDialogFragment.TYPE_SUCCESS,
                        () -> {}
                );
                dialogFragment.show(getParentFragmentManager(), "SuccessDialog");
            } else {
                Toast.makeText(getContext(), "Xóa thất bại. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ... (Các hàm onActivityResult, checkEmptyState giữ nguyên)
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