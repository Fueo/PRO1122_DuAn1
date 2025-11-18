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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
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

import org.angmarch.views.NiceSpinner;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class AccountUpdateFragment extends Fragment {

    private NiceSpinner spRole;
    private EditText edtUsername, edtPassword, edtNewPassword, edtConfirmPassword,
            edtFullName, edtPhone, edtEmail, edtAddress;
    private TextView tvTitlePassword, tvTitleOldPassword, tvTitleNewPassword;
    private LinearLayout llNewPassword;
    private ImageView ivProfile, btnChangeAvatar;
    private Button btnUpdate;

    private UserViewModel viewModel;
    private Uri selectedAvatarUri = null;
    private String userId;
    private User currentUser; // SỬA: Thêm biến để lưu user hiện tại (khi edit)

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

        // ... (binding các view) ...
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
        tvTitlePassword = view.findViewById(R.id.tvTitlePassword);
        tvTitleOldPassword = view.findViewById(R.id.tvTitleOldPassword);
        tvTitleNewPassword = view.findViewById(R.id.tvTitleNewPassword);
        llNewPassword = view.findViewById(R.id.llNewPassword);


        // Dữ liệu cho spinner
        LinkedList<String> data = new LinkedList<>(Arrays.asList(
                "Khách hàng", "Nhân viên", "Admin"));
        spRole.attachDataSource(data);

        // Lấy userId từ Intent
        userId = getActivity().getIntent().getStringExtra("Id");
        if (userId != null) {
            // Chế độ "Edit"
            edtUsername.setEnabled(false);
            edtUsername.setBackgroundTintList(ColorStateList.valueOf(0xFFCCCCCC));
            loadUserDetail(userId);
        } else {
            // Chế độ "Add"
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
                this.currentUser = user; // SỬA: Lưu user lại để dùng khi update

                spRole.setSelectedIndex(user.getRole());
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
        // ... (code không đổi) ...
        if (fileUri == null) return null;
        String filePath = FileUtils.getPath(getContext(), fileUri);
        if (filePath == null) return null;
        File file = new File(filePath);
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
        return MultipartBody.Part.createFormData(partName, file.getName(), requestFile);
    }

    private RequestBody toRequestBody(String value) {
        // ... (code không đổi) ...
        if (value == null) value = "";
        return RequestBody.create(MediaType.parse("text/plain"), value);
    }

    private void showErrorDialog(String content) {
        // ... (code không đổi) ...
        NotificationDialogFragment dialogFragment = NotificationDialogFragment.newInstance(
                "Lỗi",
                content,
                "Đóng",
                NotificationDialogFragment.TYPE_ERROR,
                () -> {}
        );
        dialogFragment.show(getParentFragmentManager(), "error_dialog");
    }

    private boolean validateInput() {
        // ... (code validate của bạn, giữ nguyên) ...
        String username = edtUsername.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String confirmPass = edtConfirmPassword.getText().toString().trim();
        String fullName = edtFullName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim(); // nếu có

        // Regex email
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";

        // Regex phone VN (nếu phone không rỗng)
        String phoneRegex = "^(0?)(3[2-9]|5[6|8|9]|7[0|6-9]|8[1-5]|9[0-9])[0-9]{7}$";

        // Kiểm tra username & password
        if (username.isEmpty() || password.isEmpty()) {
            edtUsername.setError("Username và mật khẩu không được để trống");
            edtUsername.requestFocus();
            return false;
        }

        // Kiểm tra confirm password
        if (!password.equals(confirmPass)) {
            showErrorDialog("Mật khẩu không trùng nhau");
            return false;
        }

        // Kiểm tra full name
        if (fullName.isEmpty()) {
            edtFullName.setError("Tên không được để trống");
            edtFullName.requestFocus();
            return false;
        }

        // Kiểm tra email
        if (email.isEmpty()) {
            edtEmail.setError("Email không được để trống");
            edtEmail.requestFocus();
            return false;
        } else if (!email.matches(emailRegex)) {
            edtEmail.setError("Email không hợp lệ");
            edtEmail.requestFocus();
            return false;
        }

        // Kiểm tra phone (nếu không rỗng)
        if (!phone.isEmpty() && !phone.matches(phoneRegex)) {
            edtPhone.setError("Số điện thoại không hợp lệ");
            edtPhone.requestFocus();
            return false;
        }
        return true;
    }

    private void addUser() {
        // 1. Validate trước
        if (!validateInput()) {
            return; // Dừng lại nếu validate thất bại
        }

        // 2. Lấy dữ liệu (sau khi đã validate)
        String username = edtUsername.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String fullName = edtFullName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String address = edtAddress.getText().toString().trim();
        int role = spRole.getSelectedIndex();

        // 3. Chuẩn bị RequestBody
        RequestBody usernameBody = toRequestBody(username);
        RequestBody passwordBody = toRequestBody(password);
        RequestBody nameBody = toRequestBody(fullName);
        RequestBody emailBody = toRequestBody(email);
        RequestBody phoneBody = toRequestBody(phone);
        RequestBody addressBody = toRequestBody(address);
        RequestBody roleBody = toRequestBody(String.valueOf(role));
        MultipartBody.Part avatarPart = prepareFilePart("avatar", selectedAvatarUri);

        // 4. Gọi ViewModel và OBSERVE
        viewModel.addUserWithAvatar(usernameBody, passwordBody, nameBody, emailBody, phoneBody,
                addressBody, roleBody, avatarPart).observe(getViewLifecycleOwner(), user -> {

            // 5. Chỉ xử lý khi API đã trả về (user != null là thành công)
            if (user != null) {
                Toast.makeText(getContext(), "Thêm user thành công", Toast.LENGTH_SHORT).show();
                getActivity().setResult(Activity.RESULT_OK);
                getActivity().finish();
            } else {
                // Xử lý lỗi (optional)
                showErrorDialog("Thêm user thất bại. Vui lòng thử lại.");
            }
        });
    }

    private void updateUser() {
        // 1. Kiểm tra xem đã load được user chưa
        if (currentUser == null) {
            Toast.makeText(getContext(), "Đang tải dữ liệu user, vui lòng đợi...", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Lấy dữ liệu
        String fullName = edtFullName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String address = edtAddress.getText().toString().trim();
        int role = spRole.getSelectedIndex();

        String oldPass = edtPassword.getText().toString().trim();
        String newPass = edtNewPassword.getText().toString().trim();
        String confirmPass = edtConfirmPassword.getText().toString().trim();

        // 3. Validate dữ liệu
        if (fullName.isEmpty()) {
            edtFullName.setError("Tên không được để trống");
            edtFullName.requestFocus();
            return;
        }
        if (email.isEmpty()) {
            edtEmail.setError("Email không được để trống");
            edtEmail.requestFocus();
            return;
        }

        // 4. Xử lý logic mật khẩu
        String finalPassword = currentUser.getPassword(); // Mặc định là mật khẩu cũ (đã hash)

        if (!oldPass.isEmpty() || !newPass.isEmpty() || !confirmPass.isEmpty()) {
            // Nếu người dùng có ý định đổi pass
            if (!newPass.equals(confirmPass)) {
                showErrorDialog("Mật khẩu mới không trùng khớp");
                return;
            }
            // (Bạn có thể thêm logic check mật khẩu cũ (oldPass) ở đây nếu API yêu cầu)

            // Nếu newPass không rỗng, ta sẽ dùng newPass
            if (!newPass.isEmpty()) {
                finalPassword = newPass;
            }
            // Nếu newPass rỗng (và oldPass/confirmPass cũng rỗng), finalPassword vẫn là pass cũ
        }

        // 5. Chuẩn bị RequestBody
        RequestBody usernameBody = toRequestBody(currentUser.getUsername()); // Username không đổi
        RequestBody passwordBody = toRequestBody(finalPassword); // Mật khẩu cuối cùng
        RequestBody nameBody = toRequestBody(fullName);
        RequestBody emailBody = toRequestBody(email);
        RequestBody phoneBody = toRequestBody(phone);
        RequestBody addressBody = toRequestBody(address);
        RequestBody roleBody = toRequestBody(String.valueOf(role));
        MultipartBody.Part avatarPart = prepareFilePart("avatar", selectedAvatarUri);

        // 6. Gọi ViewModel và OBSERVE
        viewModel.updateUserWithAvatar(userId, usernameBody, passwordBody, nameBody, emailBody,
                phoneBody, addressBody, roleBody, avatarPart).observe(getViewLifecycleOwner(), updatedUser -> {

            // 7. Chỉ xử lý khi API đã trả về
            if (updatedUser != null) {
                Toast.makeText(getContext(), "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                getActivity().setResult(Activity.RESULT_OK);
                getActivity().finish();
            } else {
                showErrorDialog("Cập nhật thất bại. Vui lòng thử lại.");
            }
        });
    }
}