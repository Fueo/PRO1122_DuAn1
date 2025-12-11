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
import com.example.fa25_duan1.model.ApiResponse; // IMPORT QUAN TRỌNG
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
    private EditText edtUsername, edtNewPassword, edtConfirmPassword, edtFullName, edtEmail;
    private TextView tvTitlePassword, tvTitleNewPassword;
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

        // Ánh xạ View
        spRole = view.findViewById(R.id.spRole);
        edtUsername = view.findViewById(R.id.edtUsername);
        edtNewPassword = view.findViewById(R.id.edtNewPassword);
        edtConfirmPassword = view.findViewById(R.id.edtConfirmPassword);
        edtFullName = view.findViewById(R.id.edtFullName);
        edtEmail = view.findViewById(R.id.edtEmail);
        ivProfile = view.findViewById(R.id.ivProfile);
        btnUpdate = view.findViewById(R.id.btnSave);
        btnChangeAvatar = view.findViewById(R.id.btnChangeAvatar);
        tvTitlePassword = view.findViewById(R.id.tvTitlePassword);
        tvTitleNewPassword = view.findViewById(R.id.tvTitleNewPassword);
        llNewPassword = view.findViewById(R.id.llNewPassword);

        // Setup Spinner Role
        LinkedList<String> data = new LinkedList<>(Arrays.asList("Khách hàng", "Nhân viên", "Admin"));
        spRole.attachDataSource(data);

        // Logic phân biệt Thêm hay Sửa
        userId = getActivity().getIntent().getStringExtra("Id");

        if (userId != null) {
            // --- CHẾ ĐỘ UPDATE ---
            edtUsername.setEnabled(false);
            edtUsername.setBackgroundTintList(ColorStateList.valueOf(0xFFCCCCCC));
            loadUserDetail(userId);
        } else {
            // --- CHẾ ĐỘ ADD NEW ---
            tvTitlePassword.setText("Tạo mật khẩu");
            tvTitleNewPassword.setText("Mật khẩu");
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

    // --- CẬP NHẬT LOGIC LOAD USER ---
    private void loadUserDetail(String id) {
        viewModel.getUserByID(id).observe(getViewLifecycleOwner(), response -> {
            // Check ApiResponse
            if (response != null && response.isStatus() && response.getData() != null) {
                this.currentUser = response.getData(); // Lấy data từ response

                spRole.setSelectedIndex(currentUser.getRole());
                edtUsername.setText(currentUser.getUsername());
                edtFullName.setText(currentUser.getName());
                edtEmail.setText(currentUser.getEmail());

                if (currentUser.getAvatar() != null && !currentUser.getAvatar().isEmpty()) {
                    Glide.with(getContext())
                            .load(currentUser.getAvatar())
                            .placeholder(R.drawable.ic_avatar_placeholder)
                            .error(R.drawable.ic_avatar_placeholder)
                            .into(ivProfile);
                } else {
                    ivProfile.setImageResource(R.drawable.ic_avatar_placeholder);
                }
            } else {
                String msg = (response != null && response.getMessage() != null) ? response.getMessage() : "Lỗi tải thông tin!";
                FancyToast.makeText(getContext(), msg, FancyToast.LENGTH_SHORT, FancyToast.ERROR, true).show();
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

    private boolean validateCommonInput() {
        String username = edtUsername.getText().toString().trim();
        String fullName = edtFullName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";

        if (username.isEmpty()) {
            edtUsername.setError("Username không được để trống");
            edtUsername.requestFocus();
            return false;
        }
        if (fullName.isEmpty()) {
            edtFullName.setError("Tên không được để trống");
            edtFullName.requestFocus();
            return false;
        }
        if (email.isEmpty()) {
            edtEmail.setError("Email không được để trống");
            edtEmail.requestFocus();
            return false;
        } else if (!email.matches(emailRegex)) {
            edtEmail.setError("Email không hợp lệ");
            edtEmail.requestFocus();
            return false;
        }
        return true;
    }

    // --- CẬP NHẬT LOGIC ADD USER ---
    private void addUser() {
        if (!validateCommonInput()) return;

        String password = edtNewPassword.getText().toString().trim();
        String confirmPass = edtConfirmPassword.getText().toString().trim();

        if (password.isEmpty()) {
            edtNewPassword.setError("Mật khẩu không được để trống");
            edtNewPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPass)) {
            edtConfirmPassword.setError("Mật khẩu không khớp");
            FancyToast.makeText(getContext(), "Xác thực mật khẩu không khớp", FancyToast.LENGTH_SHORT, FancyToast.ERROR, true).show();
            return;
        }

        String username = edtUsername.getText().toString().trim();
        String fullName = edtFullName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        int role = spRole.getSelectedIndex();

        viewModel.addUserWithAvatar(
                toRequestBody(username),
                toRequestBody(password),
                toRequestBody(fullName),
                toRequestBody(email),
                toRequestBody(String.valueOf(role)),
                prepareFilePart("avatar", selectedAvatarUri)
        ).observe(getViewLifecycleOwner(), response -> {
            // Check ApiResponse
            if (response != null && response.isStatus()) {
                String msg = response.getMessage() != null ? response.getMessage() : "Thêm user thành công!";
                showSuccessDialog(msg);
            } else {
                String msg = (response != null && response.getMessage() != null) ? response.getMessage() : "Thêm thất bại!";
                FancyToast.makeText(getContext(), msg, FancyToast.LENGTH_SHORT, FancyToast.ERROR, true).show();
            }
        });
    }

    // --- CẬP NHẬT LOGIC UPDATE USER ---
    private void updateUser() {
        if (currentUser == null) {
            FancyToast.makeText(requireContext(), "Chưa tải xong dữ liệu...", FancyToast.LENGTH_SHORT, FancyToast.INFO, true).show();
            return;
        }
        if (!validateCommonInput()) return;

        String newPass = edtNewPassword.getText().toString().trim();
        String confirmPass = edtConfirmPassword.getText().toString().trim();
        String finalPassword = currentUser.getPassword();

        if (!newPass.isEmpty()) {
            if (!newPass.equals(confirmPass)) {
                edtConfirmPassword.setError("Mật khẩu không khớp");
                FancyToast.makeText(getContext(), "Mật khẩu mới không trùng khớp", FancyToast.LENGTH_SHORT, FancyToast.ERROR, true).show();
                return;
            }
            finalPassword = newPass;
        }

        String fullName = edtFullName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        int role = spRole.getSelectedIndex();

        viewModel.updateUserWithAvatar(
                userId,
                toRequestBody(currentUser.getUsername()),
                toRequestBody(finalPassword),
                toRequestBody(fullName),
                toRequestBody(email),
                toRequestBody(String.valueOf(role)),
                prepareFilePart("avatar", selectedAvatarUri)
        ).observe(getViewLifecycleOwner(), response -> {
            // Check ApiResponse
            if (response != null && response.isStatus()) {
                String msg = response.getMessage() != null ? response.getMessage() : "Cập nhật thành công!";
                showSuccessDialog(msg);
            } else {
                String msg = (response != null && response.getMessage() != null) ? response.getMessage() : "Cập nhật thất bại!";
                FancyToast.makeText(getContext(), msg, FancyToast.LENGTH_SHORT, FancyToast.ERROR, true).show();
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