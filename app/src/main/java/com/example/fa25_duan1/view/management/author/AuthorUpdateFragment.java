package com.example.fa25_duan1.view.management.author;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.fa25_duan1.R;
import com.example.fa25_duan1.model.Author;
import com.example.fa25_duan1.utils.FileUtils;
import com.example.fa25_duan1.viewmodel.AuthorViewModel;

import java.io.File;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class AuthorUpdateFragment extends Fragment {

    // Khai báo Views
    private CircleImageView ivProfile;
    private ImageView btnChangeAvatar;
    private EditText etName, etDescription;
    private Button btnSave;

    // Logic variables
    private AuthorViewModel viewModel;
    private Uri selectedAvatarUri = null;
    private String authorId;
    private Author currentAuthor; // Lưu thông tin tác giả hiện tại khi Edit

    private static final int PICK_IMAGE_REQUEST = 1005; // Mã request code khác User chút để tránh nhầm lẫn

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Đảm bảo tên layout trùng với file xml bạn tạo (ví dụ: fragment_author_update.xml)
        return inflater.inflate(R.layout.fragment_authorupdate, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Khởi tạo ViewModel
        viewModel = new ViewModelProvider(this).get(AuthorViewModel.class);

        // 2. Ánh xạ View
        ivProfile = view.findViewById(R.id.ivProfile);
        btnChangeAvatar = view.findViewById(R.id.btnChangeAvatar);
        etName = view.findViewById(R.id.etName);
        etDescription = view.findViewById(R.id.etDescription);
        btnSave = view.findViewById(R.id.btnSave);

        // 3. Kiểm tra xem đang ở chế độ "Thêm mới" hay "Cập nhật"
        // Key "id" phải trùng với key bạn putExtra bên Activity hoặc Fragment quản lý
        if (getActivity().getIntent() != null) {
            authorId = getActivity().getIntent().getStringExtra("Id");
        }

        if (authorId != null) {
            // Chế độ EDIT: Load dữ liệu cũ
            btnSave.setText("Cập nhật");
            loadAuthorDetail(authorId);
        } else {
            // Chế độ ADD
            btnSave.setText("Thêm mới");
        }

        // 4. Sự kiện Click
        btnChangeAvatar.setOnClickListener(v -> pickImageFromGallery());

        btnSave.setOnClickListener(v -> {
            if (authorId == null) {
                addAuthor();
            } else {
                updateAuthor();
            }
        });
    }

    private void loadAuthorDetail(String id) {
        viewModel.getAuthorByID(id).observe(getViewLifecycleOwner(), author -> {
            if (author != null) {
                this.currentAuthor = author;
                etName.setText(author.getName());
                etDescription.setText(author.getDescription());

                // Load ảnh Avatar
                if (author.getAvatar() != null && !author.getAvatar().isEmpty()) {
                    Glide.with(requireContext())
                            .load(author.getAvatar())
                            .placeholder(R.drawable.ic_avatar_placeholder)
                            .error(R.drawable.ic_avatar_placeholder)
                            .into(ivProfile);
                }
            } else {
                Toast.makeText(getContext(), "Không tìm thấy thông tin tác giả", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Kiểm tra dữ liệu nhập vào (Validate)
     */
    private boolean validateInput() {
        String name = etName.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        if (name.isEmpty()) {
            etName.setError("Tên tác giả không được để trống");
            etName.requestFocus();
            return false;
        }

        if (description.isEmpty()) {
            etDescription.setError("Mô tả không được để trống");
            etDescription.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * Xử lý thêm tác giả mới
     */
    private void addAuthor() {
        if (!validateInput()) return;

        String name = etName.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        RequestBody nameBody = toRequestBody(name);
        RequestBody descBody = toRequestBody(description);
        MultipartBody.Part avatarPart = prepareFilePart("avatar", selectedAvatarUri);

        // Gọi API Thêm
        viewModel.addAuthorWithAvatar(nameBody, descBody, avatarPart).observe(getViewLifecycleOwner(), author -> {
            if (author != null) {
                Toast.makeText(getContext(), "Thêm tác giả thành công", Toast.LENGTH_SHORT).show();
                requireActivity().setResult(Activity.RESULT_OK);
                requireActivity().finish();
            } else {
                Toast.makeText(getContext(), "Thêm thất bại. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Xử lý cập nhật tác giả
     */
    private void updateAuthor() {
        if (currentAuthor == null) return;
        if (!validateInput()) return;

        String name = etName.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        RequestBody nameBody = toRequestBody(name);
        RequestBody descBody = toRequestBody(description);
        MultipartBody.Part avatarPart = prepareFilePart("avatar", selectedAvatarUri);

        // Gọi API Cập nhật
        viewModel.updateAuthorWithAvatar(authorId, nameBody, descBody, avatarPart).observe(getViewLifecycleOwner(), author -> {
            if (author != null) {
                Toast.makeText(getContext(), "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                requireActivity().setResult(Activity.RESULT_OK);
                requireActivity().finish();
            } else {
                Toast.makeText(getContext(), "Cập nhật thất bại.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedAvatarUri = data.getData();
            ivProfile.setImageURI(selectedAvatarUri);
        }
    }

    private MultipartBody.Part prepareFilePart(String partName, Uri fileUri) {
        if (fileUri == null) return null;
        try {
            String filePath = FileUtils.getPath(getContext(), fileUri);
            if (filePath == null) return null;
            File file = new File(filePath);
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
            return MultipartBody.Part.createFormData(partName, file.getName(), requestFile);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private RequestBody toRequestBody(String value) {
        if (value == null) value = "";
        return RequestBody.create(MediaType.parse("text/plain"), value);
    }
}