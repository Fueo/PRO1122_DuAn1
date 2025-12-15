package com.example.fa25_duan1.view.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fa25_duan1.R;
import com.example.fa25_duan1.adapter.AddressAdapter;
import com.example.fa25_duan1.model.Address;
import com.example.fa25_duan1.viewmodel.AddressViewModel;
import com.google.android.material.button.MaterialButton;
import com.shashank.sony.fancytoastlib.FancyToast;

import net.cachapa.expandablelayout.ExpandableLayout;

import io.github.cutelibs.cutedialog.CuteDialog;

public class AddressFragment extends Fragment {

    // --- Views ---
    private ExpandableLayout expandableForm;
    private MaterialButton btnToggleAdd, btnSave, btnCancel;
    private EditText etName, etPhone, etAddress;
    private RadioGroup rgTag;
    private TextView tvFormTitle;
    private CheckBox cbIsDefault;
    private RecyclerView rvAddresses;
    private NestedScrollView nestedScrollView;

    // --- Data & Logic ---
    private AddressViewModel addressViewModel;
    private AddressAdapter adapter;

    // Cờ trạng thái: Đang thêm mới hay đang sửa
    private boolean isEditing = false;
    private String currentEditingId = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_address, container, false);

        initViews(view);

        addressViewModel = new ViewModelProvider(this).get(AddressViewModel.class);

        setupRecyclerView();
        setupEvents();
        observeData();

        return view;
    }

    private void initViews(View view) {
        expandableForm = view.findViewById(R.id.expandable_form);
        btnToggleAdd = view.findViewById(R.id.btn_toggle_add);

        tvFormTitle = view.findViewById(R.id.tv_form_title);
        etName = view.findViewById(R.id.et_fullname);
        etPhone = view.findViewById(R.id.et_phone);
        etAddress = view.findViewById(R.id.et_address);
        rgTag = view.findViewById(R.id.rg_tag);
        cbIsDefault = view.findViewById(R.id.cb_is_default);

        btnSave = view.findViewById(R.id.btn_save_address);
        btnCancel = view.findViewById(R.id.btn_cancel_edit);

        rvAddresses = view.findViewById(R.id.rv_addresses);
        nestedScrollView = view.findViewById(R.id.nested_scroll);
    }

    private void setupRecyclerView() {
        adapter = new AddressAdapter(new AddressAdapter.OnAddressActionListener() {
            @Override
            public void onEdit(Address address) {
                prepareEditForm(address);
            }

            @Override
            public void onDelete(Address address) {
                showDeleteConfirmation(address);
            }

            @Override
            public void onItemClick(Address address) {
            }
        });

        rvAddresses.setLayoutManager(new LinearLayoutManager(getContext()));
        rvAddresses.setAdapter(adapter);
    }

    private void setupEvents() {
        btnToggleAdd.setOnClickListener(v -> {
            if (expandableForm.isExpanded() && !isEditing) {
                closeForm();
            } else {
                prepareAddForm();
            }
        });

        btnCancel.setOnClickListener(v -> closeForm());

        btnSave.setOnClickListener(v -> handleSaveAddress());
    }

    private void observeData() {
        // Observe danh sách địa chỉ
        addressViewModel.getDisplayedAddresses().observe(getViewLifecycleOwner(), addresses -> {
            if (addresses != null) {
                adapter.setList(addresses);
            }
        });

        // [MỚI] Observe lỗi từ ViewModel
        addressViewModel.getMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                FancyToast.makeText(getContext(), msg, FancyToast.LENGTH_SHORT, FancyToast.ERROR, false).show();
            }
        });
    }

    // --- Logic Xử lý Form ---

    private void handleSaveAddress() {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String addressDetail = etAddress.getText().toString().trim();
        String tag = (rgTag.getCheckedRadioButtonId() == R.id.rb_office) ? "Văn phòng" : "Nhà riêng";
        boolean isDefault = cbIsDefault.isChecked();

        if (name.isEmpty() || phone.isEmpty() || addressDetail.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidPhoneNumber(phone)) {
            Toast.makeText(getContext(), "Số điện thoại không hợp lệ", Toast.LENGTH_SHORT).show();
            etPhone.setError("SĐT phải có 10 số (VD: 09xx, 03xx...)");
            etPhone.requestFocus();
            return;
        }

        Address address = new Address();
        address.setName(name);
        address.setPhone(phone);
        address.setAddress(addressDetail);
        address.setTag(tag);
        address.setDefault(isDefault);

        if (isEditing && currentEditingId != null) {
            // [SỬA] Case: Update (Xử lý ApiResponse<Address>)
            addressViewModel.updateAddress(currentEditingId, address).observe(getViewLifecycleOwner(), apiResponse -> {
                if (apiResponse != null && apiResponse.isStatus()) {
                    Toast.makeText(getContext(), "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                    closeForm();
                    addressViewModel.refreshData();
                } else {
                    String msg = (apiResponse != null) ? apiResponse.getMessage() : "Cập nhật thất bại";
                    Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // [SỬA] Case: Add New (Xử lý ApiResponse<Address>)
            addressViewModel.addAddress(address).observe(getViewLifecycleOwner(), apiResponse -> {
                if (apiResponse != null && apiResponse.isStatus()) {
                    Toast.makeText(getContext(), "Thêm mới thành công", Toast.LENGTH_SHORT).show();
                    closeForm();
                    addressViewModel.refreshData();
                } else {
                    String msg = (apiResponse != null) ? apiResponse.getMessage() : "Thêm mới thất bại";
                    Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void prepareAddForm() {
        // 1. Xóa trắng form trước
        clearFormInput();

        isEditing = false;
        currentEditingId = null;

        // 2. Kiểm tra số lượng địa chỉ hiện tại trong Adapter
        int currentCount = adapter.getItemCount();

        // --- LOGIC MỚI Ở ĐÂY ---
        if (currentCount == 0) {
            // Nếu chưa có địa chỉ nào (List rỗng)
            cbIsDefault.setChecked(true);        // Tự động tích V
            cbIsDefault.setEnabled(false);       // Vô hiệu hóa (làm mờ) để user không thể bỏ tích
            cbIsDefault.setText("Địa chỉ mặc định (Bắt buộc)"); // Đổi text để user hiểu
        } else {
            // Nếu đã có địa chỉ khác
            cbIsDefault.setChecked(false);       // Mặc định không tích
            cbIsDefault.setEnabled(true);        // Cho phép user tự chọn
            cbIsDefault.setText("Đặt làm địa chỉ mặc định");
        }
        // -----------------------

        tvFormTitle.setText("Thêm địa chỉ mới");
        btnSave.setText("Lưu địa chỉ");
        expandableForm.expand();
    }

    private void prepareEditForm(Address address) {
        etName.setText(address.getName());
        etPhone.setText(address.getPhone());
        etAddress.setText(address.getAddress());

        if ("Văn phòng".equals(address.getTag())) {
            rgTag.check(R.id.rb_office);
        } else {
            rgTag.check(R.id.rb_home);
        }

        int currentCount = adapter.getItemCount();

        if (currentCount == 1) {
            cbIsDefault.setChecked(true);
            cbIsDefault.setEnabled(false);
            cbIsDefault.setText("Đặt làm địa chỉ mặc định (Bắt buộc)");
        } else {
            cbIsDefault.setChecked(address.isDefault());
            cbIsDefault.setEnabled(true);
            cbIsDefault.setText("Đặt làm địa chỉ mặc định");
        }

        isEditing = true;
        currentEditingId = address.getId();

        tvFormTitle.setText("Cập nhật địa chỉ");
        btnSave.setText("Lưu thay đổi");

        if (!expandableForm.isExpanded()) {
            expandableForm.expand();
        }
        nestedScrollView.smoothScrollTo(0, 0);
    }

    private void showDeleteConfirmation(Address address) {
        new CuteDialog.withIcon(requireActivity())
                .setIcon(R.drawable.ic_dialog_confirm)
                .setTitle("Xóa địa chỉ")
                .setDescription("Bạn có chắc chắn muốn xóa địa chỉ của " + address.getName() + "?")
                .setPrimaryColor(R.color.blue)
                .setPositiveButtonColor(R.color.blue)
                .setTitleTextColor(R.color.black)
                .setDescriptionTextColor(R.color.gray_text)
                .setPositiveButtonText("Xóa", v -> {
                    // [SỬA] Xử lý ApiResponse<Void>
                    addressViewModel.deleteAddress(address.getId()).observe(getViewLifecycleOwner(), apiResponse -> {
                        if (apiResponse != null && apiResponse.isStatus()) {
                            Toast.makeText(getContext(), "Đã xóa địa chỉ", Toast.LENGTH_SHORT).show();
                            addressViewModel.refreshData();

                            if (isEditing && address.getId().equals(currentEditingId)) {
                                closeForm();
                            }
                        } else {
                            String msg = (apiResponse != null) ? apiResponse.getMessage() : "Xóa thất bại";
                            Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButtonText("Hủy", v -> {})
                .show();
    }

    private void closeForm() {
        expandableForm.collapse();
        clearFormInput();
        isEditing = false;
        currentEditingId = null;
        btnToggleAdd.setText("+ Thêm địa chỉ mới");
    }

    private void clearFormInput() {
        etName.setText("");
        etPhone.setText("");
        etAddress.setText("");
        rgTag.check(R.id.rb_home);

        // Reset về trạng thái bình thường (Cho phép nhập)
        // Logic Bắt buộc/Không bắt buộc sẽ được prepareAddForm xử lý lại ngay sau đó
        cbIsDefault.setChecked(false);
        cbIsDefault.setEnabled(true);
        cbIsDefault.setText("Đặt làm địa chỉ mặc định");

        etName.requestFocus();
    }

    private boolean isValidPhoneNumber(String phone) {
        if (phone == null || phone.isEmpty()) {
            return false;
        }
        return phone.matches("^0[35789]\\d{8}$");
    }
}