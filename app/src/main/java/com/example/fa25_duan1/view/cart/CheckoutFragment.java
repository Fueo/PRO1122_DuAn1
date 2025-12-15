package com.example.fa25_duan1.view.cart;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.github.cutelibs.cutedialog.CuteDialog;
import vn.zalopay.sdk.ZaloPayError;
import vn.zalopay.sdk.ZaloPaySDK;
import vn.zalopay.sdk.listeners.PayOrderListener;

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
    private String finalPaymentMethod = "COD"; // Mặc định là COD

    // === CÁC BIẾN KIỂM SOÁT ZALOPAY (NEW) ===
    private String pendingZaloPayTransId = null;
    private boolean isCheckingStatus = false;
    private Set<String> processedTransactions = new HashSet<>();
    private CuteDialog.withIcon loadingDialogRef = null;

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

        // 3. Load dữ liệu
        loadDefaultAddress();
        cartViewModel.refreshCart();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (addressViewModel != null) addressViewModel.refreshData();
        // Không gọi check status ở đây để tránh xung đột với SDK Callback
    }

    private void initViews(View view) {
        tvChangeAddress = view.findViewById(R.id.tv_change_address);
        tvUserNamePhone = view.findViewById(R.id.tv_user_name_phone);
        tvUserAddress = view.findViewById(R.id.tv_user_address);
        etNote = view.findViewById(R.id.et_note);

        rgPayment = view.findViewById(R.id.rg_payment);

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

        tvChangeAddress.setOnClickListener(v -> {
            List<Address> currentList = addressViewModel.getDisplayedAddresses().getValue();
            if (currentList == null || currentList.isEmpty()) {
                navigateToAddressManagement();
            } else {
                showAddressPickerDialog();
            }
        });

        rgPayment.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_qr_payment) {
                finalPaymentMethod = "QR";
            } else if (checkedId == R.id.rb_zalopay) {
                finalPaymentMethod = "ZaloPay";
            } else {
                finalPaymentMethod = "COD";
            }
        });

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
    // LOGIC ĐỊA CHỈ
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
        if (!isAdded()) return;
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        View view = getLayoutInflater().inflate(R.layout.layout_bottom_sheet_address, null);
        bottomSheetDialog.setContentView(view);
        if (bottomSheetDialog.getWindow() != null) bottomSheetDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        RecyclerView rvPicker = view.findViewById(R.id.rv_address_picker);
        Button btnAddNew = view.findViewById(R.id.btn_add_new_address);
        rvPicker.setLayoutManager(new LinearLayoutManager(getContext()));

        AddressAdapter adapter = new AddressAdapter(new AddressAdapter.OnAddressActionListener() {
            @Override public void onItemClick(Address address) {
                updateAddressUI(address);
                bottomSheetDialog.dismiss();
            }
            @Override public void onEdit(Address address) {}
            @Override public void onDelete(Address address) {}
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
    // LOGIC XỬ LÝ NÚT CHECKOUT
    // =========================================================================

    private void handleCheckoutButton() {
        if (selectedAddress == null) {
            FancyToast.makeText(getContext(), "Vui lòng chọn địa chỉ nhận hàng!", FancyToast.LENGTH_SHORT, FancyToast.WARNING, true).show();
            return;
        }

        btnCheckout.setEnabled(false);
        cartViewModel.checkCartAvailability().observe(getViewLifecycleOwner(), isValid -> {
            btnCheckout.setEnabled(true);
            if (Boolean.TRUE.equals(isValid.getData())) {
                showConfirmCheckoutDialog();
            } else {
                FancyToast.makeText(getContext(), "Giỏ hàng có thay đổi, vui lòng kiểm tra lại!", FancyToast.LENGTH_LONG, FancyToast.ERROR, true).show();
                cartViewModel.refreshCart();
            }
        });
    }

    private void showConfirmCheckoutDialog() {
        String message = "Tổng thanh toán: " + tvTotal.getText().toString();
        String methodName = finalPaymentMethod;
        if(methodName.equals("COD")) methodName = "Thanh toán khi nhận hàng";
        if(methodName.equals("QR")) methodName = "Chuyển khoản VietQR";
        if(methodName.equals("ZaloPay")) methodName = "Ví điện tử ZaloPay";

        message += "\nPhương thức: " + methodName;

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
        btnCheckout.setText("Đang xử lý...");

        orderViewModel.checkout(
                selectedAddress.getName(),
                selectedAddress.getAddress(),
                selectedAddress.getPhone(),
                userNote,
                finalPaymentMethod
        ).observe(getViewLifecycleOwner(), response -> {
            btnCheckout.setEnabled(true);
            btnCheckout.setText("Đặt hàng");

            if (response != null && response.isStatus()) {
                // Refresh cart locally ngay khi tạo đơn xong (dù chưa thanh toán)
                cartViewModel.refreshCart();

                String orderId = response.getData().getOrderId();
                String transactionCode = response.getData().getTransactionCode();
                long total = response.getData().getTotal();

                if (finalPaymentMethod.equals("QR")) {
                    showRedirectToPaymentDialog(orderId, total, transactionCode);
                } else if (finalPaymentMethod.equals("ZaloPay")) {
                    showRedirectToZaloPayDialog(orderId);
                } else {
                    showSuccessCODDialog(orderId);
                }
            } else {
                String errorMsg = (response != null) ? response.getMessage() : "Lỗi kết nối server";
                FancyToast.makeText(getContext(), errorMsg, FancyToast.LENGTH_SHORT, FancyToast.ERROR, true).show();
            }
        });
    }

    // --- CASE 1: VietQR ---
    private void showRedirectToPaymentDialog(String orderId, long totalAmount, String transCode) {
        new CuteDialog.withIcon(requireActivity())
                .setIcon(R.drawable.ic_dialog_success)
                .setTitle("Tạo đơn thành công!")
                .setDescription("Mã đơn hàng: " + orderId + "\n\nVui lòng thực hiện thanh toán ngay để hoàn tất đơn hàng.")
                .setPrimaryColor(R.color.blue)
                .setPositiveButtonText("Thanh toán ngay", v -> {
                    Intent intent = new Intent(requireContext(), HomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    intent.putExtra("target_fragment", "payment_qr");
                    intent.putExtra("ORDER_ID", orderId);
                    intent.putExtra("TOTAL_AMOUNT", totalAmount);
                    intent.putExtra("TRANS_CODE", transCode);
                    startActivity(intent);
                    requireActivity().finish();
                })
                .setNegativeButtonText("Để sau", v -> navigateToHome())
                .show();
    }

    // --- CASE 2: ZaloPay (BƯỚC ĐỆM) ---
    private void showRedirectToZaloPayDialog(String orderId) {
        new CuteDialog.withIcon(requireActivity())
                .setIcon(R.drawable.ic_dialog_success)
                .setTitle("Tạo đơn thành công!")
                .setDescription("Mã đơn hàng: " + orderId + "\n\nNhấn xác nhận để chuyển sang ứng dụng ZaloPay.")
                .setPrimaryColor(R.color.blue)
                .setPositiveButtonText("Thanh toán ZaloPay", v -> {
                    requestZaloPayTokenFromServer(orderId);
                })
                .setNegativeButtonText("Để sau", v -> navigateToHome())
                .show();
    }

    // --- BƯỚC 3: GỌI VIEWMODEL ĐỂ LẤY TOKEN ZALO ---
    private void requestZaloPayTokenFromServer(String orderId) {
        orderViewModel.createZaloPayPayment(orderId).observe(getViewLifecycleOwner(), response -> {
            if (response == null) return;

            if (response.isStatus() && response.getData() != null) {
                String zpToken = response.getData().getZpTransToken();
                String appTransId = response.getData().getAppTransId();

                // Lưu lại mã giao dịch
                pendingZaloPayTransId = appTransId;

                if (zpToken != null && !zpToken.isEmpty()) {
                    requestZaloPayPayment(zpToken, appTransId);
                } else {
                    FancyToast.makeText(getContext(), "Lỗi: Token thanh toán trống!", FancyToast.LENGTH_SHORT, FancyToast.ERROR, true).show();
                }
            } else {
                String error = response.getMessage() != null ? response.getMessage() : "Lỗi tạo giao dịch ZaloPay";
                FancyToast.makeText(getContext(), error, FancyToast.LENGTH_SHORT, FancyToast.ERROR, true).show();
            }
        });
    }

    // --- BƯỚC 4: GỌI SDK ZALOPAY (ĐÃ SỬA LOGIC) ---
    private void requestZaloPayPayment(String zpToken, String appTransId) {
        if (!isAdded() || getActivity() == null) return;

        ZaloPaySDK.getInstance().payOrder(
                requireActivity(),
                zpToken,
                "demozpdk://app",
                new PayOrderListener() {
                    @Override
                    public void onPaymentSucceeded(String transactionId, String transToken, String returnedAppTransID) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                // [CHẶN TRÙNG LẶP]
                                if (processedTransactions.contains(returnedAppTransID)) {
                                    return;
                                }

                                String finalId = (returnedAppTransID != null && !returnedAppTransID.isEmpty()) ? returnedAppTransID : appTransId;

                                // Thay vì hiện dialog ngay, ta gọi Check Status
                                checkZaloPayStatusFromBackend(finalId, 3);
                            });
                        }
                    }

                    @Override
                    public void onPaymentCanceled(String zpTransToken, String appTransID) {
                        if(getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                FancyToast.makeText(getContext(), "Bạn đã hủy thanh toán", FancyToast.LENGTH_SHORT, FancyToast.WARNING, false).show();
                                navigateToHome(); // Hoặc ở lại trang
                            });
                        }
                    }

                    @Override
                    public void onPaymentError(ZaloPayError zaloPayError, String zpTransToken, String appTransID) {
                        if(getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                if (zaloPayError == ZaloPayError.PAYMENT_APP_NOT_FOUND) {
                                    ZaloPaySDK.getInstance().navigateToZaloOnStore(requireActivity());
                                } else {
                                    FancyToast.makeText(getContext(), "Lỗi thanh toán: " + zaloPayError.toString(), FancyToast.LENGTH_SHORT, FancyToast.ERROR, false).show();
                                }
                            });
                        }
                    }
                }
        );
    }

    // --- BƯỚC 5: CHECK STATUS (RECURSIVE RETRY) ---
    private void checkZaloPayStatusFromBackend(String appTransId, int retryCount) {
        // [CHỐT CHẶN 2]
        if (processedTransactions.contains(appTransId)) return;
        if (isCheckingStatus && retryCount == 3) return;

        // Bắt đầu check
        if (retryCount == 3) {
            isCheckingStatus = true;
            loadingDialogRef = new CuteDialog.withIcon(requireActivity())
                    .setIcon(R.drawable.ic_dialog_info)
                    .setTitle("Đang xác thực...")
                    .setDescription("Vui lòng đợi hệ thống xác nhận...")
                    .hideNegativeButton(true).hidePositiveButton(true);
            loadingDialogRef.show();

            // Auto dismiss 8s
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if(loadingDialogRef != null && loadingDialogRef.isShowing()) {
                    try { loadingDialogRef.dismiss(); } catch (Exception e) {}
                }
            }, 8000);
        }

        orderViewModel.checkZaloPayStatus(appTransId).observe(getViewLifecycleOwner(), response -> {
            if (!isAdded() || getActivity() == null) return;

            boolean isValid = (response != null && response.getData() != null);
            int returnCode = -1;
            if (isValid) {
                try {
                    Object rc = response.getData().get("return_code");
                    returnCode = (rc instanceof Number) ? ((Number) rc).intValue() : Double.valueOf(String.valueOf(rc)).intValue();
                } catch (Exception e) { returnCode = -1; }
            }

            // === TRƯỜNG HỢP 1: THÀNH CÔNG ===
            if (isValid && returnCode == 1) {
                if (loadingDialogRef != null && loadingDialogRef.isShowing()) loadingDialogRef.dismiss();

                processedTransactions.add(appTransId);
                isCheckingStatus = false;
                cartViewModel.refreshCart(); // Update lại giỏ hàng (về 0)

                new CuteDialog.withIcon(requireActivity())
                        .setIcon(R.drawable.ic_check_circle)
                        .setTitle("Thanh toán thành công!")
                        .setDescription("Đơn hàng của bạn đã được xác nhận.")
                        .setPositiveButtonText("Về trang chủ", v -> navigateToHome())
                        .hideNegativeButton(true)
                        .show();
                return;
            }

            // === TRƯỜNG HỢP 2: RETRY ===
            if (retryCount > 0) {
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    checkZaloPayStatusFromBackend(appTransId, retryCount - 1);
                }, 2000);
            } else {
                // === TRƯỜNG HỢP 3: THẤT BẠI ===
                if (loadingDialogRef != null && loadingDialogRef.isShowing()) loadingDialogRef.dismiss();
                isCheckingStatus = false;

                new CuteDialog.withIcon(requireActivity())
                        .setIcon(R.drawable.ic_dialog_warning)
                        .setTitle("Giao dịch đang xử lý")
                        .setDescription("Hệ thống chưa nhận được kết quả. Vui lòng kiểm tra lại sau trong Lịch sử đơn hàng.")
                        .setPositiveButtonText("Kiểm tra lại", v -> checkZaloPayStatusFromBackend(appTransId, 3))
                        .setNegativeButtonText("Về trang chủ", v -> navigateToHome())
                        .show();
            }
        });
    }

    // --- CASE 3: COD ---
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