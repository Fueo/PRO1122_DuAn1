package com.example.fa25_duan1.view.cart;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.example.fa25_duan1.view.profile.AddressFragment;
import com.example.fa25_duan1.viewmodel.AddressViewModel;
import com.example.fa25_duan1.viewmodel.CartViewModel;
import com.example.fa25_duan1.viewmodel.OrderViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.text.DecimalFormat;
import java.util.List;

import com.shashank.sony.fancytoastlib.FancyToast;
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

        cartViewModel = new ViewModelProvider(requireActivity()).get(CartViewModel.class);
        orderViewModel = new ViewModelProvider(requireActivity()).get(OrderViewModel.class);
        addressViewModel = new ViewModelProvider(requireActivity()).get(AddressViewModel.class);

        initViews(view);
        setupEvents();
        setupCartAndOrderObservers();

        cartViewModel.fetchCart();
        loadDefaultAddress();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (addressViewModel != null) {
            addressViewModel.refreshData();
        }
        if (cartViewModel != null) {
            cartViewModel.fetchCart();
        }
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

        // Xử lý sự kiện nút Thay đổi / Thêm mới
        tvChangeAddress.setOnClickListener(v -> {
            List<Address> currentList = addressViewModel.getDisplayedAddresses().getValue();
            if (currentList == null || currentList.isEmpty()) {
                // Nếu chưa có địa chỉ -> Chuyển sang màn hình thêm mới luôn
                navigateToAddressManagement();
            } else {
                // Nếu đã có -> Hiện dialog chọn
                showAddressPickerDialog();
            }
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

    // --- HÀM CHUNG ĐỂ CHUYỂN SANG QUẢN LÝ ĐỊA CHỈ ---
    private void navigateToAddressManagement() {
        Intent intent = new Intent(requireContext(), DetailActivity.class);
        intent.putExtra(DetailActivity.EXTRA_HEADER_TITLE, "Sổ địa chỉ");
        intent.putExtra(DetailActivity.EXTRA_CONTENT_FRAGMENT, "address");
        startActivity(intent);
    }

    // --- LOGIC 1: TỰ ĐỘNG LẤY ĐỊA CHỈ MẶC ĐỊNH ---
    private void loadDefaultAddress() {
        addressViewModel.getDisplayedAddresses().observe(getViewLifecycleOwner(), addresses -> {
            if (addresses != null && !addresses.isEmpty()) {
                // CÓ ĐỊA CHỈ
                Address target = null;
                for (Address addr : addresses) {
                    if (addr.isDefault()) {
                        target = addr;
                        break;
                    }
                }
                if (target == null) target = addresses.get(0);

                updateAddressUI(target);

                // Cập nhật giao diện nút bấm
                tvChangeAddress.setText(getString(R.string.thay_doi_checkout)); // "Thay đổi"

            } else {
                // KHÔNG CÓ ĐỊA CHỈ
                tvUserNamePhone.setText("Chưa có thông tin nhận hàng");
                tvUserAddress.setText("Vui lòng thêm địa chỉ mới để đặt hàng");
                selectedAddress = null;

                // Đổi nút "Thay đổi" thành "Thêm mới" cho hợp lý
                tvChangeAddress.setText("+ Thêm mới");
            }
        });
    }

    private void updateAddressUI(Address address) {
        this.selectedAddress = address;
        tvUserNamePhone.setText(address.getName() + " | " + address.getPhone());
        tvUserAddress.setText(address.getAddress());
    }

    // --- LOGIC 2: HIỆN DIALOG CHỌN ĐỊA CHỈ ---
    private void showAddressPickerDialog() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        View view = getLayoutInflater().inflate(R.layout.layout_bottom_sheet_address, null);
        bottomSheetDialog.setContentView(view);

        if (bottomSheetDialog.getWindow() != null) {
            bottomSheetDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        RecyclerView rvPicker = view.findViewById(R.id.rv_address_picker);
        Button btnAddNew = view.findViewById(R.id.btn_add_new_address);

        rvPicker.setLayoutManager(new LinearLayoutManager(getContext()));

        AddressAdapter adapter = new AddressAdapter(new AddressAdapter.OnAddressActionListener() {
            @Override public void onEdit(Address address) {}
            @Override public void onDelete(Address address) {}
            @Override
            public void onItemClick(Address address) {
                updateAddressUI(address);
                bottomSheetDialog.dismiss();
            }
        });

        adapter.setShowActionButtons(false);

        addressViewModel.getDisplayedAddresses().observe(getViewLifecycleOwner(), list -> {
            adapter.setList(list);
        });
        rvPicker.setAdapter(adapter);

        btnAddNew.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            navigateToAddressManagement();
        });

        bottomSheetDialog.show();
    }

    private void setupCartAndOrderObservers() {
        // ... (Giữ nguyên logic Observer Cart & Order) ...
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
                FancyToast.makeText(getContext(), msg, FancyToast.LENGTH_SHORT, FancyToast.ERROR, true).show();
            }
        });

        orderViewModel.getCheckoutSuccessOrderId().observe(getViewLifecycleOwner(), orderId -> {
            if (orderId != null) {
                showSuccessCheckoutDialog(orderId);
            }
        });
    }

    // --- XỬ LÝ NÚT CHECKOUT ---
    private void handleCheckout() {
        // TRƯỜNG HỢP 1: Chưa chọn địa chỉ (có thể do chưa có hoặc chưa chọn)
        if (selectedAddress == null) {
            List<Address> currentList = addressViewModel.getDisplayedAddresses().getValue();

            // Nếu list rỗng hoàn toàn -> Điều hướng đi thêm mới
            if (currentList == null || currentList.isEmpty()) {
                FancyToast.makeText(getContext(), "Bạn chưa có địa chỉ nhận hàng!", FancyToast.LENGTH_SHORT, FancyToast.WARNING, true).show();
                navigateToAddressManagement();
            } else {
                // Nếu có list nhưng chưa chọn (hiếm khi xảy ra do logic loadDefault) -> Hiện dialog chọn
                FancyToast.makeText(getContext(), "Vui lòng chọn địa chỉ nhận hàng", FancyToast.LENGTH_SHORT, FancyToast.WARNING, true).show();
                showAddressPickerDialog();
            }
            return;
        }

        showConfirmCheckoutDialog();
    }

    private void showConfirmCheckoutDialog() {
        new CuteDialog.withIcon(requireActivity())
                .setIcon(R.drawable.ic_dialog_confirm)
                .setTitle("Xác nhận đặt hàng")
                .setDescription("Bạn có chắc chắn muốn đặt đơn hàng này không?")
                .setPrimaryColor(R.color.blue)
                .setPositiveButtonColor(R.color.blue)
                .setTitleTextColor(R.color.black)
                .setDescriptionTextColor(R.color.gray_text)
                .setPositiveButtonText("Đặt hàng", v -> {
                    performCheckoutAPI();
                })
                .setNegativeButtonText("Hủy", v -> {})
                .show();
    }

    private void performCheckoutAPI() {
        String note = etNote.getText().toString().trim();
        orderViewModel.checkout(
                selectedAddress.getName(),
                selectedAddress.getAddress(),
                selectedAddress.getPhone(),
                note,
                finalPaymentMethod
        );
    }

    private void showSuccessCheckoutDialog(String orderId) {
        new CuteDialog.withIcon(requireActivity())
                .setIcon(R.drawable.ic_dialog_success)
                .setTitle("Đặt hàng thành công!")
                .setDescription("Mã đơn hàng: " + orderId + "\nCảm ơn bạn đã mua sắm cùng chúng tôi.")
                .setPrimaryColor(R.color.blue)
                .setPositiveButtonColor(R.color.blue)
                .setTitleTextColor(R.color.black)
                .setDescriptionTextColor(R.color.gray_text)
                .setPositiveButtonText("Trở về", v -> {
                    Intent intent = new Intent(requireContext(), HomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    if (getActivity() != null) getActivity().finish();
                })
                .hideNegativeButton(true)
                .show();
    }
}