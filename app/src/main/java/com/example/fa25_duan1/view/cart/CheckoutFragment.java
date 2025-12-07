package com.example.fa25_duan1.view.cart;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fa25_duan1.R;
import com.example.fa25_duan1.adapter.AddressAdapter;
import com.example.fa25_duan1.model.Address;
import com.example.fa25_duan1.view.detail.DetailActivity;
import com.example.fa25_duan1.view.home.HomeActivity;
import com.example.fa25_duan1.viewmodel.AddressViewModel;
import com.example.fa25_duan1.viewmodel.CartViewModel;
import com.example.fa25_duan1.viewmodel.OrderViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.shashank.sony.fancytoastlib.FancyToast;

import java.text.DecimalFormat;
import java.util.List;

import io.github.cutelibs.cutedialog.CuteDialog;

public class CheckoutFragment extends Fragment {

    // --- Views ---
    private TextView tvChangeAddress, tvUserNamePhone, tvUserAddress;
    private EditText etNote;
    private MaterialButton btnBack, btnCheckout;
    private RadioGroup rgPayment;

    // Views cho phần tổng tiền
    private TextView tvSubtotal, tvShippingFee, tvTotal;

    // --- ViewModels ---
    private CartViewModel cartViewModel;
    private OrderViewModel orderViewModel;
    private AddressViewModel addressViewModel;

    // --- Biến dữ liệu ---
    private Address selectedAddress = null;
    private String finalPaymentMethod = "Thanh toán khi nhận hàng"; // Mặc định

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_checkout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Khởi tạo ViewModel
        cartViewModel = new ViewModelProvider(requireActivity()).get(CartViewModel.class);
        orderViewModel = new ViewModelProvider(requireActivity()).get(OrderViewModel.class);
        addressViewModel = new ViewModelProvider(requireActivity()).get(AddressViewModel.class);

        // 2. Setup giao diện và sự kiện
        initViews(view);
        setupEvents();
        setupDataObservers();
        loadDefaultAddress();

        // 3. Load lại giỏ hàng
        cartViewModel.refreshCart();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (addressViewModel != null) addressViewModel.refreshData();
    }

    private void initViews(View view) {
        tvChangeAddress = view.findViewById(R.id.tv_change_address);
        tvUserNamePhone = view.findViewById(R.id.tv_user_name_phone);
        tvUserAddress = view.findViewById(R.id.tv_user_address);
        etNote = view.findViewById(R.id.et_note);

        rgPayment = view.findViewById(R.id.rg_payment);

        // Đã xóa các view liên quan đến QR (expandableQr, imgQrCode...) vì không dùng ở đây nữa

        tvSubtotal = view.findViewById(R.id.tv_subtotal);
        tvShippingFee = view.findViewById(R.id.tv_shipping_fee);
        tvTotal = view.findViewById(R.id.tv_total);
        btnBack = view.findViewById(R.id.btn_back);
        btnCheckout = view.findViewById(R.id.btn_checkout);
    }

    private void setupEvents() {
        // Nút quay lại
        btnBack.setOnClickListener(v -> {
            if (getActivity() != null) getActivity().onBackPressed();
        });

        // Chọn địa chỉ
        tvChangeAddress.setOnClickListener(v -> {
            List<Address> currentList = addressViewModel.getDisplayedAddresses().getValue();
            if (currentList == null || currentList.isEmpty()) {
                navigateToAddressManagement();
            } else {
                showAddressPickerDialog();
            }
        });

        // Xử lý logic khi chọn Phương thức thanh toán
        rgPayment.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_qr_payment) {
                // Chỉ lưu trạng thái, KHÔNG hiện QR tại đây
                finalPaymentMethod = "Chuyển khoản ngân hàng";
            } else {
                finalPaymentMethod = "Thanh toán khi nhận hàng";
            }
        });

        // Nút Đặt Hàng
        btnCheckout.setOnClickListener(v -> handleCheckoutButton());
    }

    private void setupDataObservers() {
        cartViewModel.getCartItems().observe(getViewLifecycleOwner(), items -> {});
        cartViewModel.getTotalPrice().observe(getViewLifecycleOwner(), this::updateOrderSummary);
    }

    private void updateOrderSummary(long subtotal) {
        DecimalFormat formatter = new DecimalFormat("###,###,###");
        long shippingFee = (subtotal > 0) ? 25000 : 0;
        long total = subtotal + shippingFee;

        tvSubtotal.setText(formatter.format(subtotal).replace(",", ".") + " đ");
        tvShippingFee.setText(formatter.format(shippingFee).replace(",", ".") + " đ");
        tvTotal.setText(formatter.format(total).replace(",", ".") + " đ");
    }

    // =========================================================================
    // LOGIC ĐỊA CHỈ (GIỮ NGUYÊN)
    // =========================================================================
    private void loadDefaultAddress() {
        addressViewModel.getDisplayedAddresses().observe(getViewLifecycleOwner(), addresses -> {
            if (addresses != null && !addresses.isEmpty()) {
                Address target = null;
                for (Address addr : addresses) {
                    if (addr.isDefault()) { target = addr; break; }
                }
                if (target == null) target = addresses.get(0);
                updateAddressUI(target);
                tvChangeAddress.setText("Thay đổi");
            } else {
                tvUserNamePhone.setText("Chưa có thông tin");
                tvUserAddress.setText("Vui lòng thêm địa chỉ nhận hàng");
                selectedAddress = null;
                tvChangeAddress.setText("+ Thêm mới");
            }
        });
    }

    private void updateAddressUI(Address address) {
        this.selectedAddress = address;
        tvUserNamePhone.setText(address.getName() + " | " + address.getPhone());
        tvUserAddress.setText(address.getAddress());
    }

    private void showAddressPickerDialog() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        View view = getLayoutInflater().inflate(R.layout.layout_bottom_sheet_address, null);
        bottomSheetDialog.setContentView(view);
        if (bottomSheetDialog.getWindow() != null) bottomSheetDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        RecyclerView rvPicker = view.findViewById(R.id.rv_address_picker);
        Button btnAddNew = view.findViewById(R.id.btn_add_new_address);
        rvPicker.setLayoutManager(new LinearLayoutManager(getContext()));

        AddressAdapter adapter = new AddressAdapter(new AddressAdapter.OnAddressActionListener() {
            @Override public void onEdit(Address address) {}
            @Override public void onDelete(Address address) {}
            @Override public void onItemClick(Address address) {
                updateAddressUI(address);
                bottomSheetDialog.dismiss();
            }
        });
        adapter.setShowActionButtons(false);
        addressViewModel.getDisplayedAddresses().observe(getViewLifecycleOwner(), adapter::setList);
        rvPicker.setAdapter(adapter);

        btnAddNew.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            navigateToAddressManagement();
        });
        bottomSheetDialog.show();
    }

    private void navigateToAddressManagement() {
        Intent intent = new Intent(requireContext(), DetailActivity.class);
        intent.putExtra(DetailActivity.EXTRA_HEADER_TITLE, "Sổ địa chỉ");
        intent.putExtra(DetailActivity.EXTRA_CONTENT_FRAGMENT, "address");
        startActivity(intent);
    }

    // =========================================================================
    // LOGIC XỬ LÝ NÚT CHECKOUT (MỚI)
    // =========================================================================

    private void handleCheckoutButton() {
        if (selectedAddress == null) {
            FancyToast.makeText(getContext(), "Vui lòng chọn địa chỉ nhận hàng!", FancyToast.LENGTH_SHORT, FancyToast.WARNING, true).show();
            return;
        }

        // Kiểm tra tồn kho trước khi đặt
        btnCheckout.setEnabled(false);
        cartViewModel.checkCartAvailability().observe(getViewLifecycleOwner(), isValid -> {
            btnCheckout.setEnabled(true);
            if (Boolean.TRUE.equals(isValid)) {
                showConfirmCheckoutDialog();
            } else {
                FancyToast.makeText(getContext(), "Giỏ hàng có thay đổi, vui lòng kiểm tra lại!", FancyToast.LENGTH_LONG, FancyToast.ERROR, true).show();
                cartViewModel.refreshCart();
            }
        });
    }

    private void showConfirmCheckoutDialog() {
        String message = "Tổng thanh toán: " + tvTotal.getText().toString();
        message += "\nPhương thức: " + finalPaymentMethod;

        new CuteDialog.withIcon(requireActivity())
                .setIcon(R.drawable.ic_dialog_confirm)
                .setTitle("Xác nhận đặt hàng")
                .setDescription(message)
                .setPrimaryColor(R.color.blue)
                .setPositiveButtonText("Đặt hàng", v -> performCheckoutAPI())
                .setNegativeButtonText("Hủy", v -> {})
                .show();
    }

    private void performCheckoutAPI() {
        String userNote = etNote.getText().toString().trim();

        btnCheckout.setEnabled(false);
        btnCheckout.setText("Đang tạo đơn...");

        // Gọi API Checkout (Không cần gửi transactionCode vì Server tự sinh)
        orderViewModel.checkout(
                selectedAddress.getName(),
                selectedAddress.getAddress(),
                selectedAddress.getPhone(),
                userNote, // Note sạch, không kèm mã GD
                finalPaymentMethod
        ).observe(getViewLifecycleOwner(), response -> {
            btnCheckout.setEnabled(true);
            btnCheckout.setText("Đặt hàng");

            if (response != null && response.isStatus()) {
                // Xóa giỏ hàng local
                cartViewModel.refreshCart();

                // Lấy dữ liệu quan trọng từ Server trả về
                String orderId = response.getData().getOrderId();
                String transactionCode = response.getData().getTransactionCode();
                long total = response.getData().getTotal();

                // ĐIỀU HƯỚNG DỰA TRÊN PHƯƠNG THỨC THANH TOÁN
                if (finalPaymentMethod.equals("Chuyển khoản ngân hàng")) {
                    showRedirectToPaymentDialog(orderId, total, transactionCode);
                } else {
                    showSuccessCODDialog(orderId);
                }
            } else {
                String errorMsg = (response != null) ? response.getMessage() : "Lỗi kết nối server";
                FancyToast.makeText(getContext(), errorMsg, FancyToast.LENGTH_SHORT, FancyToast.ERROR, true).show();
            }
        });
    }

    // --- CASE 1: Chuyển khoản -> Dialog xác nhận sang màn hình Payment ---
    private void showRedirectToPaymentDialog(String orderId, long totalAmount, String transCode) {
        new CuteDialog.withIcon(requireActivity())
                .setIcon(R.drawable.ic_dialog_success)
                .setTitle("Tạo đơn thành công!")
                .setDescription("Mã đơn hàng: " + orderId + "\n\nVui lòng thực hiện thanh toán ngay để hoàn tất đơn hàng.")
                .setPrimaryColor(R.color.blue)
                .setPositiveButtonText("Thanh toán ngay", v -> {

                    // --- LOGIC MỚI: ĐIỀU HƯỚNG VỀ HOME RỒI TỰ ĐỘNG MỞ PAYMENT ---
                    Intent intent = new Intent(requireContext(), HomeActivity.class);

                    // Cờ này sẽ xóa CartActivity, CheckoutActivity... và đưa HomeActivity lên đầu
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                    // Gửi tín hiệu để Home biết cần mở màn hình Payment
                    intent.putExtra("target_fragment", "payment_qr"); // Key này phải khớp với HomeActivity
                    intent.putExtra("ORDER_ID", orderId);
                    intent.putExtra("TOTAL_AMOUNT", totalAmount);
                    intent.putExtra("TRANS_CODE", transCode);

                    startActivity(intent);
                    requireActivity().finish(); // Đóng Checkout hiện tại
                })
                .setNegativeButtonText("Để sau", v -> {
                    navigateToHome();
                })
                .show();
    }

    // --- CASE 2: COD -> Dialog thành công bình thường ---
    private void showSuccessCODDialog(String orderId) {
        new CuteDialog.withIcon(requireActivity())
                .setIcon(R.drawable.ic_dialog_success)
                .setTitle("Đặt hàng thành công!")
                .setDescription("Mã đơn hàng: " + orderId + "\n\nVui lòng chú ý điện thoại để nhận hàng.")
                .setPrimaryColor(R.color.blue)
                .setPositiveButtonText("Về trang chủ", v -> navigateToHome())
                .hideNegativeButton(true)
                .show();
    }

    private void navigateToHome() {
        Intent intent = new Intent(requireContext(), HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        if (getActivity() != null) getActivity().finish();
    }
}