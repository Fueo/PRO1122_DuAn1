package com.example.fa25_duan1.view.profile;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.os.Bundle;
import android.util.Base64;
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
import com.example.fa25_duan1.model.User;
import com.example.fa25_duan1.viewmodel.AuthViewModel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ChangeContactInforFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private EditText edtFullName, edtEmail, edtPhone, edtAddress;
    private Button btnSave;
    private ImageView ivProfile, btnChangeAvatar;
    private AuthViewModel authViewModel;

    private Uri selectedImageUri = null;
    private String encodedImageString = null;



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
        edtPhone = view.findViewById(R.id.edtPhone);
        edtAddress = view.findViewById(R.id.edtAddress);
        btnSave = view.findViewById(R.id.btnSave);
        ivProfile = view.findViewById(R.id.ivProfile);
        btnChangeAvatar = view.findViewById(R.id.btnChangeAvatar);

        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        // 2. Load dữ liệu cũ
        authViewModel.getMyInfo().observe(getViewLifecycleOwner(), response -> {
            if (response != null && response.getData() != null) {
                User user = response.getData();
                edtFullName.setText(user.getName() != null ? user.getName() : "");
                edtEmail.setText(user.getEmail() != null ? user.getEmail() : "");
                edtPhone.setText(user.getPhone() != null ? user.getPhone() : "");
                edtAddress.setText(user.getAddress() != null ? user.getAddress() : "");

                if (selectedImageUri == null && user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                    Glide.with(requireContext())
                            .load(user.getAvatar())
                            .placeholder(R.drawable.ic_avatar_placeholder)
                            .into(ivProfile);
                }
            }
        });

        btnChangeAvatar.setOnClickListener(v -> pickImageFromGallery());

        // 3. Xử lý lưu
        btnSave.setOnClickListener(v -> {
            String name = edtFullName.getText().toString().trim();
            String email = edtEmail.getText().toString().trim();
            String phone = edtPhone.getText().toString().trim();
            String address = edtAddress.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty()) {
                Toast.makeText(getContext(), "Tên và Email không được để trống", Toast.LENGTH_SHORT).show();
                return;
            }

            authViewModel.updateProfile(name, email, phone, address, encodedImageString)
                    .observe(getViewLifecycleOwner(), response -> {
                        if (response != null && response.getData() != null) {
                            Toast.makeText(getContext(), "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                            requireActivity().finish();
                        } else {
                            Toast.makeText(getContext(), "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

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

            // 1. Hiển thị ảnh xem trước
            Glide.with(this).load(selectedImageUri).into(ivProfile);

            // 2. Chuyển ảnh thành Base64 String để chuẩn bị gửi
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), selectedImageUri);
                // Nén ảnh nhỏ lại để gửi nhanh hơn (quan trọng!)
                Bitmap resizedBitmap = getResizedBitmap(bitmap, 600);
                encodedImageString = bitmapToBase64(resizedBitmap);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Lỗi xử lý ảnh", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        // Nén định dạng JPEG, chất lượng 80%
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        // Trả về chuỗi Base64 có prefix để hiển thị được trên web/app
        return "data:image/jpeg;base64," + Base64.encodeToString(byteArray, Base64.DEFAULT);
    }
    private Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }
}