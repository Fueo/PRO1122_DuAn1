package com.example.fa25_duan1.view.management.account;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.fa25_duan1.R;
import com.example.fa25_duan1.model.User;
import com.example.fa25_duan1.utils.FileUtils;
import com.example.fa25_duan1.viewmodel.UserViewModel;

import org.angmarch.views.NiceSpinner;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

import com.shashank.sony.fancytoastlib.FancyToast;
import io.github.cutelibs.cutedialog.CuteDialog;

public class AccountUpdateFragment extends Fragment {

    private NiceSpinner spRole;
    // Đã xóa edtPhone, edtAddress
    private EditText edtUsername, edtPassword, edtNewPassword, edtConfirmPassword, edtFullName, edtEmail;
    private TextView tvTitlePassword, tvTitleOldPassword, tvTitleNewPassword;
    private LinearLayout llNewPassword;
    private ImageView ivProfile, btnChangeAvatar;
    private Button btnUpdate;

    private UserViewModel viewModel;
    private Uri selectedAvatarUri = null;
    private String userId;
    private User currentUser;

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
        edtEmail = view.findViewById(R.id.edtEmail);

        // Đã xóa ánh xạ edtPhone, edtAddress

        ivProfile = view.findViewById(R.id.ivProfile);
        btnUpdate = view.findViewById(R.id.btnSave);
        btnChangeAvatar = view.findViewById(R.id.btnChangeAvatar);
        tvTitlePassword = view.findViewById(R.id.tvTitlePassword);
        tvTitleOldPassword = view.findViewById(R.id.tvTitleOldPassword);
        tvTitleNewPassword = view.findViewById(R.id.tvTitleNewPassword);
        llNewPassword = view.findViewById(R.id.llNewPassword);

        LinkedList<String> data = new LinkedList<>(Arrays.asList("Khách hàng", "Nhân viên", "Admin"));
        spRole.attachDataSource(data);

        userId = getActivity().getIntent().getStringExtra("Id");
        if (userId != null) {
            edtUsername.setEnabled(false);
            edtUsername.setBackgroundTintList(ColorStateList.valueOf(0xFFCCCCCC));
            loadUserDetail(userId);
        } else {
            tvTitlePassword.setText("Mật khẩu");
            tvTitleOldPassword.setText("Mật khẩu");
            tvTitleNewPassword.setVisibility(View.GONE);
            llNewPassword.setVisibility(View.GONE);
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
                this.currentUser = user;

                spRole.setSelectedIndex(user.getRole());
                edtUsername.setText(user.getUsername());
                edtFullName.setText(user.getName());
                edtEmail.setText(user.getEmail());

                if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                    Glide.with(getContext())
                            .load(user.getAvatar())
                            .placeholder(R.drawable.ic_avatar_placeholder)
                            .error(R.drawable.ic_avatar_placeholder)
                            .into(ivProfile);
                } else {
                    ivProfile.setImageResource(R.drawable.ic_avatar_placeholder);
                }
            } else {
                FancyToast.makeText(getContext(), "Không tải được thông tin User", FancyToast.LENGTH_SHORT, FancyToast.ERROR, true).show();
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
            return null;
        }
    }

    private RequestBody toRequestBody(String value) {
        if (value == null) value = "";
        return RequestBody.create(MediaType.parse("text/plain"), value);
    }

    private boolean validateInput() {
        String username = edtUsername.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String confirmPass = edtConfirmPassword.getText().toString().trim();
        String fullName = edtFullName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();

        // Đã xóa biến phone và phoneRegex

        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";

        if (username.isEmpty() || password.isEmpty()) {
            edtUsername.setError("Username và mật khẩu không được để trống");
            edtUsername.requestFocus();
            FancyToast.makeText(getContext(), "Vui lòng nhập Username và Mật khẩu", FancyToast.LENGTH_SHORT, FancyToast.WARNING, true).show();
            return false;
        }

        if (!password.equals(confirmPass)) {
            FancyToast.makeText(getContext(), "Mật khẩu xác nhận không khớp", FancyToast.LENGTH_SHORT, FancyToast.ERROR, true).show();
            return false;
        }

        if (fullName.isEmpty()) {
            edtFullName.setError("Tên không được để trống");
            edtFullName.requestFocus();
            FancyToast.makeText(getContext(), "Vui lòng nhập họ tên", FancyToast.LENGTH_SHORT, FancyToast.WARNING, true).show();
            return false;
        }

        if (email.isEmpty()) {
            edtEmail.setError("Email không được để trống");
            edtEmail.requestFocus();
            FancyToast.makeText(getContext(), "Vui lòng nhập email", FancyToast.LENGTH_SHORT, FancyToast.WARNING, true).show();
            return false;
        } else if (!email.matches(emailRegex)) {
            edtEmail.setError("Email không hợp lệ");
            edtEmail.requestFocus();
            FancyToast.makeText(getContext(), "Email không đúng định dạng", FancyToast.LENGTH_SHORT, FancyToast.WARNING, true).show();
            return false;
        }

        // Đã xóa validate Phone

        return true;
    }

    private void addUser() {
        if (!validateInput()) {
            return;
        }

        String username = edtUsername.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String fullName = edtFullName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        // Đã xóa get phone, address
        int role = spRole.getSelectedIndex();

        RequestBody usernameBody = toRequestBody(username);
        RequestBody passwordBody = toRequestBody(password);
        RequestBody nameBody = toRequestBody(fullName);
        RequestBody emailBody = toRequestBody(email);
        RequestBody roleBody = toRequestBody(String.valueOf(role));
        MultipartBody.Part avatarPart = prepareFilePart("avatar", selectedAvatarUri);

        // Gọi hàm ViewModel đã cập nhật (không còn tham số phone, address)
        viewModel.addUserWithAvatar(usernameBody, passwordBody, nameBody, emailBody, roleBody, avatarPart)
                .observe(getViewLifecycleOwner(), user -> {
                    if (user != null) {
                        showSuccessDialog("Thêm user thành công!");
                    } else {
                        FancyToast.makeText(getContext(), "Thêm user thất bại. Vui lòng thử lại.", FancyToast.LENGTH_SHORT, FancyToast.ERROR, true).show();
                    }
                });
    }

    private void updateUser() {
        if (currentUser == null) {
            FancyToast.makeText(requireContext(), "Đang tải dữ liệu, vui lòng đợi...", FancyToast.LENGTH_SHORT, FancyToast.INFO, true).show();
            return;
        }

        String fullName = edtFullName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        // Đã xóa get phone, address
        int role = spRole.getSelectedIndex();

        String oldPass = edtPassword.getText().toString().trim();
        String newPass = edtNewPassword.getText().toString().trim();
        String confirmPass = edtConfirmPassword.getText().toString().trim();

        if (fullName.isEmpty()) {
            edtFullName.setError("Tên không được để trống");
            edtFullName.requestFocus();
            FancyToast.makeText(getContext(), "Vui lòng nhập tên", FancyToast.LENGTH_SHORT, FancyToast.WARNING, true).show();
            return;
        }
        if (email.isEmpty()) {
            edtEmail.setError("Email không được để trống");
            edtEmail.requestFocus();
            FancyToast.makeText(getContext(), "Vui lòng nhập email", FancyToast.LENGTH_SHORT, FancyToast.WARNING, true).show();
            return;
        }

        String finalPassword = currentUser.getPassword();

        if (!oldPass.isEmpty() || !newPass.isEmpty() || !confirmPass.isEmpty()) {
            if (!newPass.equals(confirmPass)) {
                FancyToast.makeText(getContext(), "Mật khẩu mới không trùng khớp", FancyToast.LENGTH_SHORT, FancyToast.ERROR, true).show();
                return;
            }
            if (!newPass.isEmpty()) {
                finalPassword = newPass;
            }
        }

        RequestBody usernameBody = toRequestBody(currentUser.getUsername());
        RequestBody passwordBody = toRequestBody(finalPassword);
        RequestBody nameBody = toRequestBody(fullName);
        RequestBody emailBody = toRequestBody(email);
        RequestBody roleBody = toRequestBody(String.valueOf(role));
        MultipartBody.Part avatarPart = prepareFilePart("avatar", selectedAvatarUri);

        // Gọi hàm ViewModel đã cập nhật (không còn tham số phone, address)
        viewModel.updateUserWithAvatar(userId, usernameBody, passwordBody, nameBody, emailBody,
                roleBody, avatarPart).observe(getViewLifecycleOwner(), updatedUser -> {

            if (updatedUser != null) {
                showSuccessDialog("Cập nhật thành công!");
            } else {
                FancyToast.makeText(getContext(), "Cập nhật thất bại. Vui lòng thử lại.", FancyToast.LENGTH_SHORT, FancyToast.ERROR, true).show();
            }
        });
    }

    private void showSuccessDialog(String message) {
        new CuteDialog.withIcon(requireActivity())
                .setIcon(R.drawable.ic_dialog_success)
                .setTitle("Thành công")
                .setDescription(message)
                .setPrimaryColor(R.color.blue)
                .setPositiveButtonColor(R.color.blue)
                .setTitleTextColor(R.color.black)
                .setDescriptionTextColor(R.color.gray_text)
                .setPositiveButtonText("Đóng", v -> {
                    requireActivity().setResult(Activity.RESULT_OK);
                    requireActivity().finish();
                })
                .hideNegativeButton(true)
                .show();
    }
}