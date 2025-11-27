package com.example.fa25_duan1.view.cart;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.fa25_duan1.R;
import com.example.fa25_duan1.model.User;
import com.example.fa25_duan1.viewmodel.AuthViewModel;
import com.example.fa25_duan1.viewmodel.CartViewModel;
import com.example.fa25_duan1.viewmodel.OrderViewModel;
import com.google.android.material.button.MaterialButton;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.text.DecimalFormat;
import java.util.regex.Pattern;

public class CheckoutFragment extends Fragment {

    // ... (Khai báo biến giữ nguyên) ...
    // Views
    private TextView tvChangeAddress, tvUserNamePhone, tvUserAddress;
    private ExpandableLayout expandableAddress;
    private EditText etFullname, etPhone, etAddress, etNote;
    private MaterialButton btnSaveAddress, btnBack, btnCheckout;

    private RadioGroup rgPayment;
    private ExpandableLayout expandableMomo;

    private TextView tvSubtotal, tvShippingFee, tvTotal;

    // ViewModels
    private AuthViewModel authViewModel;
    private CartViewModel cartViewModel;
    private OrderViewModel orderViewModel;

    // Data Variables
    private String finalName = "";
    private String finalPhone = "";
    private String finalAddress = "";
    private String finalPaymentMethod = "Thanh toán khi nhận hàng";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_checkout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Khởi tạo ViewModel
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
        cartViewModel = new ViewModelProvider(requireActivity()).get(CartViewModel.class);
        orderViewModel = new ViewModelProvider(requireActivity()).get(OrderViewModel.class);

        initViews(view);
        setupEvents();

        // Setup Observer (Cart, Order)
        setupCartAndOrderObservers();

        // Load dữ liệu Giỏ hàng
        cartViewModel.fetchCart();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserData();
    }

    // --- LOGIC VALIDATE SỐ ĐIỆN THOẠI VIỆT NAM ---
    private boolean isValidPhoneNumber(String phone) {
        // Regex: Bắt đầu bằng 0, theo sau là 9 chữ số (Tổng 10 số)
        // Ví dụ: 0912345678 -> True
        // Có thể mở rộng regex: "^(03|05|07|08|09)+([0-9]{8})$" nếu muốn check đầu số nhà mạng
        String phoneRegex = "^0\\d{9}$";
        return phone != null && Pattern.matches(phoneRegex, phone);
    }

    // ... (loadUserData, updateUserUI, initViews giữ nguyên) ...
    private void loadUserData() {
        if (authViewModel.getMyInfo() != null) {
            authViewModel.getMyInfo().removeObservers(getViewLifecycleOwner());
        }
        authViewModel.getMyInfo().observe(getViewLifecycleOwner(), response -> {
            if (response != null && response.getData() != null) {
                updateUserUI(response.getData());
            }
        });
    }

    private void updateUserUI(User user) {
        if (TextUtils.isEmpty(finalName)) {
            finalName = user.getName() != null ? user.getName() : "";
            finalPhone = user.getPhone() != null ? user.getPhone() : "";
            finalAddress = user.getAddress() != null ? user.getAddress() : "";
            updateAddressDisplayText();
            etFullname.setText(finalName);
            etPhone.setText(finalPhone);
            etAddress.setText(finalAddress);
        }
    }

    private void initViews(View view) {
        tvChangeAddress = view.findViewById(R.id.tv_change_address);
        tvUserNamePhone = view.findViewById(R.id.tv_user_name_phone);
        tvUserAddress = view.findViewById(R.id.tv_user_address);
        expandableAddress = view.findViewById(R.id.expandable_address);
        etFullname = view.findViewById(R.id.et_fullname);
        etPhone = view.findViewById(R.id.et_phone);
        etAddress = view.findViewById(R.id.et_address);
        btnSaveAddress = view.findViewById(R.id.btn_save_address);
        etNote = view.findViewById(R.id.et_note);
        rgPayment = view.findViewById(R.id.rg_payment);
        expandableMomo = view.findViewById(R.id.expandable_momo);
        tvSubtotal = view.findViewById(R.id.tv_subtotal);
        tvShippingFee = view.findViewById(R.id.tv_shipping_fee);
        tvTotal = view.findViewById(R.id.tv_total);
        btnBack = view.findViewById(R.id.btn_back);
        btnCheckout = view.findViewById(R.id.btn_checkout);
    }

    private void setupEvents() {
        btnBack.setOnClickListener(v -> {
            if (getActivity() != null) getActivity().onBackPressed();
        });

        // Mở form sửa địa chỉ
        tvChangeAddress.setOnClickListener(v -> {
            if (expandableAddress.isExpanded()) {
                expandableAddress.collapse();
            } else {
                expandableAddress.expand();
                etFullname.setText(finalName);
                etPhone.setText(finalPhone);
                etAddress.setText(finalAddress);
            }
        });

        // --- NÚT LƯU ĐỊA CHỈ (CÓ VALIDATE) ---
        btnSaveAddress.setOnClickListener(v -> {
            String newName = etFullname.getText().toString().trim();
            String newPhone = etPhone.getText().toString().trim();
            String newAddress = etAddress.getText().toString().trim();

            // 1. Validate Tên
            if (TextUtils.isEmpty(newName)) {
                etFullname.setError("Vui lòng nhập họ tên người nhận");
                etFullname.requestFocus(); // Trỏ chuột vào ô lỗi
                return;
            }

            // 2. Validate SĐT
            if (TextUtils.isEmpty(newPhone)) {
                etPhone.setError("Vui lòng nhập số điện thoại");
                etPhone.requestFocus();
                return;
            }

            if (!isValidPhoneNumber(newPhone)) {
                etPhone.setError("SĐT không hợp lệ (phải có 10 số, bắt đầu bằng 0)");
                etPhone.requestFocus();
                return;
            }

            // 3. Validate Địa chỉ
            if (TextUtils.isEmpty(newAddress)) {
                etAddress.setError("Vui lòng nhập địa chỉ nhận hàng");
                etAddress.requestFocus();
                return;
            }

            // Nếu tất cả hợp lệ -> Lưu và đóng form
            finalName = newName;
            finalPhone = newPhone;
            finalAddress = newAddress;

            updateAddressDisplayText();
            expandableAddress.collapse();

            // Ẩn bàn phím (Tùy chọn)
            // InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            // imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        });

        rgPayment.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_momo) {
                expandableMomo.expand();
                finalPaymentMethod = "Chuyển khoản Momo";
            } else {
                expandableMomo.collapse();
                finalPaymentMethod = "Thanh toán khi nhận hàng";
            }
        });

        btnCheckout.setOnClickListener(v -> handleCheckout());
    }

    // ... (Các hàm còn lại giữ nguyên) ...
    private void setupCartAndOrderObservers() {
        cartViewModel.getTotalPrice().observe(getViewLifecycleOwner(), subtotal -> {
            DecimalFormat formatter = new DecimalFormat("###,###,###");
            long shippingFee = (subtotal > 0) ? 25000 : 0;
            long total = subtotal + shippingFee;

            tvSubtotal.setText(formatter.format(subtotal).replace(",", ".") + " VNĐ");
            tvShippingFee.setText(formatter.format(shippingFee).replace(",", ".") + " VNĐ");
            tvTotal.setText(formatter.format(total).replace(",", ".") + " VNĐ");
        });

        orderViewModel.getMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });

        orderViewModel.getCheckoutSuccessOrderId().observe(getViewLifecycleOwner(), orderId -> {
            if (orderId != null) {
                if (getActivity() != null) {
                    Toast.makeText(getActivity(), "Đơn hàng " + orderId + " thành công!", Toast.LENGTH_LONG).show();
                    getActivity().finish();
                }
            }
        });
    }

    private void updateAddressDisplayText() {
        String namePhone = "";
        if (!TextUtils.isEmpty(finalName)) {
            namePhone = finalName;
            if (!TextUtils.isEmpty(finalPhone)) {
                namePhone += " | " + finalPhone;
            }
        } else {
            namePhone = "Chưa có thông tin";
        }

        tvUserNamePhone.setText(namePhone);
        tvUserAddress.setText(!TextUtils.isEmpty(finalAddress) ? finalAddress : "Chưa có địa chỉ");
    }

    private void handleCheckout() {
        if (TextUtils.isEmpty(finalName) || TextUtils.isEmpty(finalPhone) || TextUtils.isEmpty(finalAddress)) {
            Toast.makeText(getContext(), "Vui lòng cập nhật thông tin nhận hàng", Toast.LENGTH_SHORT).show();
            expandableAddress.expand(); // Mở form lên cho người dùng nhập
            return;
        }
        String note = etNote.getText().toString().trim();
        orderViewModel.checkout(finalName, finalAddress, finalPhone, note, finalPaymentMethod);
    }
}