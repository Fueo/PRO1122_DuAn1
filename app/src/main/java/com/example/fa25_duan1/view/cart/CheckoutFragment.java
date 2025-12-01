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
import android.widget.Toast;

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

import net.cachapa.expandablelayout.ExpandableLayout;

import java.text.DecimalFormat;
import java.util.List;

import io.github.cutelibs.cutedialog.CuteDialog;

public class CheckoutFragment extends Fragment {

    // --- Views ---
    private TextView tvChangeAddress, tvUserNamePhone, tvUserAddress;
    private EditText etNote;
    private MaterialButton btnBack, btnCheckout;
    private RadioGroup rgPayment;
    private ExpandableLayout expandableMomo;
    private TextView tvSubtotal, tvShippingFee, tvTotal;

    // --- ViewModels ---
    private CartViewModel cartViewModel;
    private OrderViewModel orderViewModel;
    private AddressViewModel addressViewModel;

    // --- Data Variables ---
    private Address selectedAddress = null;
    private String finalPaymentMethod = "Thanh toán khi nhận hàng";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_checkout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Khởi tạo ViewModel (Dùng requireActivity để share data nếu cần)
        cartViewModel = new ViewModelProvider(requireActivity()).get(CartViewModel.class);
        orderViewModel = new ViewModelProvider(requireActivity()).get(OrderViewModel.class);
        addressViewModel = new ViewModelProvider(requireActivity()).get(AddressViewModel.class);

        initViews(view);
        setupEvents();
        setupDataObservers();
        loadDefaultAddress();

        cartViewModel.refreshCart();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh lại địa chỉ phòng trường hợp user vừa đi thêm mới rồi quay lại
        if (addressViewModel != null) addressViewModel.refreshData();
    }

    private void initViews(View view) {
        tvChangeAddress = view.findViewById(R.id.tv_change_address);
        tvUserNamePhone = view.findViewById(R.id.tv_user_name_phone);
        tvUserAddress = view.findViewById(R.id.tv_user_address);
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

        // Chọn địa chỉ
        tvChangeAddress.setOnClickListener(v -> {
            List<Address> currentList = addressViewModel.getDisplayedAddresses().getValue();
            if (currentList == null || currentList.isEmpty()) {
                navigateToAddressManagement();
            } else {
                showAddressPickerDialog();
            }
        });

        // Chọn phương thức thanh toán
        rgPayment.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_momo) {
                expandableMomo.expand();
                finalPaymentMethod = "Chuyển khoản Momo";
            } else {
                expandableMomo.collapse();
                finalPaymentMethod = "Thanh toán khi nhận hàng";
            }
        });

        // Nút Đặt hàng (Logic chính)
        btnCheckout.setOnClickListener(v -> handleCheckoutButton());
    }

    private void setupDataObservers() {
        cartViewModel.getCartItems().observe(getViewLifecycleOwner(), items -> {

        });
        cartViewModel.getTotalPrice().observe(getViewLifecycleOwner(), this::updateOrderSummary);
    }

    private void updateOrderSummary(long subtotal) {
        DecimalFormat formatter = new DecimalFormat("###,###,###");

        // Logic: Nếu tổng tiền > 0 thì mới tính ship 25k, ngược lại là 0
        long shippingFee = (subtotal > 0) ? 25000 : 0;
        long total = subtotal + shippingFee;

        // Sửa lại đơn vị từ "VNĐ" thành "đ" cho đồng bộ
        tvSubtotal.setText(formatter.format(subtotal).replace(",", ".") + " đ");
        tvShippingFee.setText(formatter.format(shippingFee).replace(",", ".") + " đ");
        tvTotal.setText(formatter.format(total).replace(",", ".") + " đ");
    }

    // --- LOGIC ĐỊA CHỈ ---
    private void loadDefaultAddress() {
        addressViewModel.getDisplayedAddresses().observe(getViewLifecycleOwner(), addresses -> {
            if (addresses != null && !addresses.isEmpty()) {
                Address target = null;
                // Tìm địa chỉ mặc định
                for (Address addr : addresses) {
                    if (addr.isDefault()) { target = addr; break; }
                }
                // Nếu không có mặc định thì lấy cái đầu tiên
                if (target == null) target = addresses.get(0);

                updateAddressUI(target);
                tvChangeAddress.setText(getString(R.string.thay_doi_checkout));
            } else {
                tvUserNamePhone.setText("Chưa có thông tin nhận hàng");
                tvUserAddress.setText("Vui lòng thêm địa chỉ mới để đặt hàng");
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

        if (bottomSheetDialog.getWindow() != null)
            bottomSheetDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

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

        // Load danh sách địa chỉ vào dialog
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
    // KHU VỰC XỬ LÝ LOGIC CHECKOUT (QUAN TRỌNG)
    // =========================================================================

    private void handleCheckoutButton() {
        // 1. Kiểm tra địa chỉ
        if (selectedAddress == null) {
            FancyToast.makeText(getContext(), "Vui lòng chọn địa chỉ nhận hàng!", FancyToast.LENGTH_SHORT, FancyToast.WARNING, true).show();
            return;
        }

        // 2. Kiểm tra tồn kho (Gọi API checkCartAvailability)
        // Disable nút để tránh user bấm liên tục
        btnCheckout.setEnabled(false);

        cartViewModel.checkCartAvailability().observe(getViewLifecycleOwner(), isValid -> {
            btnCheckout.setEnabled(true); // Enable lại nút

            if (Boolean.TRUE.equals(isValid)) {
                // Hợp lệ -> Hiện dialog xác nhận
                showConfirmCheckoutDialog();
            } else {
                // Không hợp lệ -> Báo lỗi & Refresh giỏ hàng
                FancyToast.makeText(getContext(), "Sản phẩm đã thay đổi số lượng hoặc hết hàng. Vui lòng kiểm tra lại!", FancyToast.LENGTH_LONG, FancyToast.ERROR, true).show();
                cartViewModel.refreshCart();

                // Tùy chọn: Có thể back user về màn hình giỏ hàng
                // if(getActivity() != null) getActivity().onBackPressed();
            }
        });
    }

    private void showConfirmCheckoutDialog() {
        new CuteDialog.withIcon(requireActivity())
                .setIcon(R.drawable.ic_dialog_confirm)
                .setTitle("Xác nhận đặt hàng")
                .setDescription("Tổng thanh toán: " + tvTotal.getText().toString())
                .setPrimaryColor(R.color.blue)
                .setPositiveButtonText("Đặt hàng", v -> {
                    performCheckoutAPI();
                })
                .setNegativeButtonText("Hủy", v -> {})
                .show();
    }

    private void performCheckoutAPI() {
        String note = etNote.getText().toString().trim();

        // 1. Khóa giao diện (UI Blocking)
        btnCheckout.setEnabled(false);
        btnCheckout.setText("Đang xử lý...");

        // 2. Gọi API Checkout từ ViewModel và LẮNG NGHE TRỰC TIẾP
        orderViewModel.checkout(
                selectedAddress.getName(),
                selectedAddress.getAddress(),
                selectedAddress.getPhone(),
                note,
                finalPaymentMethod
        ).observe(getViewLifecycleOwner(), response -> {

            // 3. Mở khóa giao diện
            btnCheckout.setEnabled(true);
            btnCheckout.setText("Đặt hàng");

            // 4. Xử lý kết quả
            if (response != null && response.isStatus()) {
                // --- THÀNH CÔNG ---

                // Quan trọng: Làm mới giỏ hàng để số lượng về 0 (Badge update)
                cartViewModel.refreshCart();

                // Lấy OrderId hiển thị
                String orderId = (response.getData() != null) ? response.getData().getOrderId() : "Mới";
                showSuccessCheckoutDialog(orderId);

            } else {
                // --- THẤT BẠI ---
                String errorMsg = (response != null) ? response.getMessage() : "Lỗi kết nối server";
                FancyToast.makeText(getContext(), errorMsg, FancyToast.LENGTH_SHORT, FancyToast.ERROR, true).show();
            }
        });
    }

    private void showSuccessCheckoutDialog(String orderId) {
        new CuteDialog.withIcon(requireActivity())
                .setIcon(R.drawable.ic_dialog_success)
                .setTitle("Đặt hàng thành công!")
                .setDescription("Mã đơn hàng: " + orderId + "\nCảm ơn bạn đã mua sắm.")
                .setPrimaryColor(R.color.blue)
                .setPositiveButtonText("Về trang chủ", v -> {
                    // Chuyển về màn hình Home và xóa backstack
                    Intent intent = new Intent(requireContext(), HomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    if (getActivity() != null) getActivity().finish();
                })
                .hideNegativeButton(true) // Ẩn nút Cancel
                .show();
    }
}