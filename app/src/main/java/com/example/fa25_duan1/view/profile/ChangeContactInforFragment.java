package com.example.fa25_duan1.view.profile;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.fa25_duan1.R;
import com.example.fa25_duan1.model.User;
import com.example.fa25_duan1.utils.FileUtils;
import com.example.fa25_duan1.viewmodel.AuthViewModel;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

// Import thư viện UI
import com.shashank.sony.fancytoastlib.FancyToast;
import io.github.cutelibs.cutedialog.CuteDialog;

public class ChangeContactInforFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    // Đã xóa edtPhone, edtAddress
    private EditText edtFullName, edtEmail;
    private Button btnSave;
    private ImageView ivProfile, btnChangeAvatar;
    private AuthViewModel authViewModel;

    private Uri selectedImageUri = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_changecontactinfo_employ, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Ánh xạ View
        edtFullName = view.findViewById(R.id.edtFullName);
        edtEmail = view.findViewById(R.id.edtEmail);
        btnSave = view.findViewById(R.id.btnSave);
        ivProfile = view.findViewById(R.id.ivProfile);
        btnChangeAvatar = view.findViewById(R.id.btnChangeAvatar);

        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        // 2. Load dữ liệu cũ (Chỉ Name, Email, Avatar)
        authViewModel.getMyInfo().observe(getViewLifecycleOwner(), response -> {
            if (response != null && response.getData() != null) {
                User user = response.getData();
                edtFullName.setText(user.getName() != null ? user.getName() : "");
                edtEmail.setText(user.getEmail() != null ? user.getEmail() : "");

                // Nếu chưa chọn ảnh mới thì hiển thị ảnh cũ từ server
                if (selectedImageUri == null && user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                    Glide.with(requireContext())
                            .load(user.getAvatar())
                            .placeholder(R.drawable.ic_avatar_placeholder)
                            .error(R.drawable.ic_avatar_placeholder)
                            .into(ivProfile);
                }
            }
        });

        btnChangeAvatar.setOnClickListener(v -> pickImageFromGallery());

        // 3. Xử lý lưu
        btnSave.setOnClickListener(v -> {
            String name = edtFullName.getText().toString().trim();
            String email = edtEmail.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty()) {
                FancyToast.makeText(getContext(), "Tên và Email không được để trống", FancyToast.LENGTH_SHORT, FancyToast.WARNING, true).show();
                return;
            }

            // --- CHUYỂN ĐỔI SANG REQUEST BODY & MULTIPART ---
            RequestBody nameBody = toRequestBody(name);
            RequestBody emailBody = toRequestBody(email);
            MultipartBody.Part avatarPart = prepareFilePart("avatar", selectedImageUri);

            // Gọi API Update (chỉ truyền name, email, avatar part)
            authViewModel.updateProfile(nameBody, emailBody, avatarPart)
                    .observe(getViewLifecycleOwner(), response -> {
                        if (response != null && response.getData() != null) {
                            showSuccessDialog();
                        } else {
                            FancyToast.makeText(getContext(), "Cập nhật thất bại", FancyToast.LENGTH_SHORT, FancyToast.ERROR, true).show();
                        }
                    });
        });
    }

    private void showSuccessDialog() {
        new CuteDialog.withIcon(requireActivity())
                .setIcon(R.drawable.ic_dialog_success)
                .setTitle("Thành công")
                .setDescription("Cập nhật thông tin cá nhân thành công!")
                .setPrimaryColor(R.color.blue)
                .setPositiveButtonColor(R.color.blue)
                .setTitleTextColor(R.color.black)
                .setDescriptionTextColor(R.color.gray_text)
                .setPositiveButtonText("Đóng", v -> {
                    requireActivity().finish();
                })
                .hideNegativeButton(true)
                .show();
    }

    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            // Hiển thị ảnh xem trước
            Glide.with(this).load(selectedImageUri).into(ivProfile);
        }
    }

    // --- HELPER METHODS CHO MULTIPART (GIỐNG ACCOUNT UPDATE) ---

    private MultipartBody.Part prepareFilePart(String partName, Uri fileUri) {
        if (fileUri == null) return null;
        try {
            // Sử dụng FileUtils để lấy đường dẫn thực
            String filePath = FileUtils.getPath(getContext(), fileUri);
            if (filePath == null) return null;
            File file = new File(filePath);

            // Tạo RequestBody từ file
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);

            // Tạo MultipartBody.Part
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