package com.example.fa25_duan1.view.management;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.fa25_duan1.R;
import com.example.fa25_duan1.model.User;
import com.example.fa25_duan1.utils.FileUtils;
import com.example.fa25_duan1.view.dialog.NotificationDialogFragment;
import com.example.fa25_duan1.viewmodel.UserViewModel;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class AccountUpdateFragment extends Fragment {

    private Spinner spRole;
    private EditText edtUsername, edtPassword, edtNewPassword, edtConfirmPassword,
            edtFullName, edtPhone, edtEmail, edtAddress;
    private ImageView ivProfile, btnChangeAvatar;
    private Button btnUpdate;

    private UserViewModel viewModel;
    private Uri selectedAvatarUri = null;
    private String userId;

    private static final int PICK_IMAGE_REQUEST = 1002;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_accountupdate, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(UserViewModel.class);

        spRole = view.findViewById(R.id.spRole);
        edtUsername = view.findViewById(R.id.edtUsername);
        edtPassword = view.findViewById(R.id.edtPassword);
        edtNewPassword = view.findViewById(R.id.edtNewPassword);
        edtConfirmPassword = view.findViewById(R.id.edtConfirmPassword);
        edtFullName = view.findViewById(R.id.edtFullName);
        edtPhone = view.findViewById(R.id.edtPhone);
        edtEmail = view.findViewById(R.id.edtEmail);
        edtAddress = view.findViewById(R.id.edtAddress);
        ivProfile = view.findViewById(R.id.ivProfile);
        btnUpdate = view.findViewById(R.id.btnSave);
        btnChangeAvatar = view.findViewById(R.id.btnChangeAvatar);

        // Spinner role
        String[] items = {"Khách hàng", "Nhân viên", "Admin"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                R.layout.item_spinner, items);
        spRole.setAdapter(adapter);

        // Lấy userId từ Intent
        userId = getActivity().getIntent().getStringExtra("Id");
        if (userId != null) {
            edtUsername.setEnabled(false);
            edtUsername.setBackgroundTintList(ColorStateList.valueOf(0xFFCCCCCC));
            loadUserDetail(userId);
        }

        btnChangeAvatar.setOnClickListener(v -> pickImageFromGallery());
        btnUpdate.setOnClickListener(v -> {
            if (userId == null) {
                addUser();
            } else {
                updateUser();
            }
        });
    }

    private void loadUserDetail(String id) {
        viewModel.getUserByID(id).observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                spRole.setSelection(user.getRole());
                edtUsername.setText(user.getUsername());
                edtFullName.setText(user.getName());
                edtEmail.setText(user.getEmail());
                edtPhone.setText(user.getPhone());
                edtAddress.setText(user.getAddress());

                if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                    Glide.with(getContext())
                            .load(user.getAvatar())
                            .placeholder(R.drawable.ic_avatar_placeholder)
                            .error(R.drawable.ic_avatar_placeholder)
                            .into(ivProfile);
                } else {
                    ivProfile.setImageResource(R.drawable.ic_avatar_placeholder);
                }
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
        String filePath = FileUtils.getPath(getContext(), fileUri);
        if (filePath == null) return null;
        File file = new File(filePath);
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
        return MultipartBody.Part.createFormData(partName, file.getName(), requestFile);
    }

    private void addUser() {
        String username = edtUsername.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String fullName = edtFullName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String address = edtAddress.getText().toString().trim();
        int role = spRole.getSelectedItemPosition();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(getContext(), "Username và mật khẩu không được để trống", Toast.LENGTH_SHORT).show();
            return;
        }

        RequestBody usernameBody = RequestBody.create(MediaType.parse("text/plain"), username);
        RequestBody passwordBody = RequestBody.create(MediaType.parse("text/plain"), password);
        RequestBody nameBody = RequestBody.create(MediaType.parse("text/plain"), fullName);
        RequestBody emailBody = RequestBody.create(MediaType.parse("text/plain"), email);
        RequestBody phoneBody = RequestBody.create(MediaType.parse("text/plain"), phone);
        RequestBody addressBody = RequestBody.create(MediaType.parse("text/plain"), address);
        RequestBody roleBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(role));
        MultipartBody.Part avatarPart = prepareFilePart("avatar", selectedAvatarUri);

        viewModel.addUserWithAvatar(usernameBody, passwordBody, nameBody, emailBody, phoneBody,
                addressBody, roleBody, avatarPart).observe(getViewLifecycleOwner(), addedUser -> {
            if (addedUser != null) {
                Toast.makeText(getContext(), "Thêm user thành công", Toast.LENGTH_SHORT).show();
                getActivity().setResult(Activity.RESULT_OK);
                getActivity().finish();
            } else {
                Toast.makeText(getContext(), "Thêm user thất bại", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUser() {
        String fullName = edtFullName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String address = edtAddress.getText().toString().trim();
        int role = spRole.getSelectedItemPosition();

        String oldPass = edtPassword.getText().toString().trim();
        String newPass = edtNewPassword.getText().toString().trim();
        String confirmPass = edtConfirmPassword.getText().toString().trim();

        viewModel.getUserByID(userId).observe(getViewLifecycleOwner(), user -> {
            if (user == null) {
                Toast.makeText(getContext(), "Không lấy được thông tin user", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!oldPass.isEmpty() || !newPass.isEmpty()) {
                if (!oldPass.equals(user.getPassword())) {
                    NotificationDialogFragment dialogFragment = NotificationDialogFragment.newInstance(
                            "Lỗi",
                            "Mật khẩu cũ không đúng",
                            "Đóng",
                            NotificationDialogFragment.TYPE_ERROR,
                            () -> {}
                    );
                    dialogFragment.show(getParentFragmentManager(), "error_dialog");
                    return;
                }
                if (!newPass.equals(confirmPass)) {
                    NotificationDialogFragment dialogFragment = NotificationDialogFragment.newInstance(
                            "Lỗi",
                            "Mật khẩu mới không trùng khớp",
                            "Đóng",
                            NotificationDialogFragment.TYPE_ERROR,
                            () -> {}
                    );
                    dialogFragment.show(getParentFragmentManager(), "error_dialog");
                    return;
                }
            }

            RequestBody usernameBody = RequestBody.create(MediaType.parse("text/plain"), edtUsername.getText().toString().trim());
            RequestBody passwordBody = RequestBody.create(MediaType.parse("text/plain"), newPass.isEmpty() ? user.getPassword() : newPass);
            RequestBody nameBody = RequestBody.create(MediaType.parse("text/plain"), fullName);
            RequestBody emailBody = RequestBody.create(MediaType.parse("text/plain"), email);
            RequestBody phoneBody = RequestBody.create(MediaType.parse("text/plain"), phone);
            RequestBody addressBody = RequestBody.create(MediaType.parse("text/plain"), address);
            RequestBody roleBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(role));
            MultipartBody.Part avatarPart = prepareFilePart("avatar", selectedAvatarUri);

            viewModel.updateUserWithAvatar(userId, usernameBody, passwordBody, nameBody, emailBody,
                            phoneBody, addressBody, roleBody, avatarPart)
                    .observe(getViewLifecycleOwner(), updatedUser -> {
                        if (updatedUser != null) {
                            Toast.makeText(getContext(), "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                            getActivity().setResult(Activity.RESULT_OK);
                            getActivity().finish();
                        } else {
                            Toast.makeText(getContext(), "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}
