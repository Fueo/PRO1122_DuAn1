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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fa25_duan1.R;
import com.example.fa25_duan1.adapter.AuthorManageAdapter;
import com.example.fa25_duan1.model.Author;
import com.example.fa25_duan1.model.User;
import com.example.fa25_duan1.view.dialog.ConfirmDialogFragment;
import com.example.fa25_duan1.view.dialog.NotificationDialogFragment;
import com.example.fa25_duan1.view.management.UpdateActivity;
import com.example.fa25_duan1.viewmodel.AuthorViewModel;

import java.util.ArrayList;
import java.util.List;

public class AuthorManageFragment extends Fragment {
    private View layout_empty;
    private RecyclerView rvData;
    private Button btnAdd;
    private AuthorManageAdapter authorManageAdapter;
    private AuthorViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_authormanagement, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_filter, new AuthorFilterFragment())
                .commit();

        rvData = view.findViewById(R.id.rvData);
        btnAdd = view.findViewById(R.id.btnAdd);
        layout_empty = view.findViewById(R.id.layout_empty);

        authorManageAdapter = new AuthorManageAdapter(requireActivity(), new ArrayList<>(), new AuthorManageAdapter.OnItemClickListener() {
            @Override
            public void onEditClick(Author author) {
                openUpdateActivity(author);
            }

            @Override
            public void onDeleteClick(Author author) {
                deleteAuthor(author);
            }

            @Override
            public void onItemClick(Author author) {

            }
        });

        rvData.setLayoutManager(new LinearLayoutManager(getContext()));
        rvData.setAdapter(authorManageAdapter);

        // Sử dụng ViewModel scoped to Activity nếu muốn chia sẻ dữ liệu giữa Fragment
// Trong Fragment
        viewModel = new ViewModelProvider(requireActivity()).get(AuthorViewModel.class);

        viewModel.getDisplayedAuthors().observe(getViewLifecycleOwner(), authors -> {
            authorManageAdapter.setData(authors);
            checkEmptyState(authors); // Gọi hàm kiểm tra
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

    private void deleteAuthor(Author author) {
        if (author == null) return;

        ConfirmDialogFragment dialog = new ConfirmDialogFragment(
                "Xóa tác giả",
                "Xác nhận xoá tác giả " + author.getName(),
                new ConfirmDialogFragment.OnConfirmListener() {
                    @Override
                    public void onConfirmed() {
                        // Gọi API và observe kết quả
                        viewModel.deleteAuthor(author.getAuthorID()).observe(getViewLifecycleOwner(), success -> {
                            if (success != null && success) {
                                // Nếu API báo thành công, tải lại toàn bộ danh sách
                                viewModel.refreshData();

                                // Hiển thị thông báo thành công
                                NotificationDialogFragment dialogFragment = NotificationDialogFragment.newInstance(
                                        "Thành công",
                                        "Bạn đã xóa tác giả thành công",
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
    private void checkEmptyState(List<Author> list) {
        if (list == null || list.isEmpty()) {
            layout_empty.setVisibility(View.VISIBLE); // Hiện ảnh rỗng
            rvData.setVisibility(View.GONE);         // Ẩn danh sách
        } else {
            layout_empty.setVisibility(View.GONE);    // Ẩn ảnh rỗng
            rvData.setVisibility(View.VISIBLE);      // Hiện danh sách
        }
    }
}
