package com.example.fa25_duan1.view.profile;

import android.app.AlertDialog;
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

        // Khởi tạo ViewModel
        addressViewModel = new ViewModelProvider(this).get(AddressViewModel.class);

        setupRecyclerView();
        setupEvents();
        observeData();

        return view;
    }

    private void initViews(View view) {
        expandableForm = view.findViewById(R.id.expandable_form);
        btnToggleAdd = view.findViewById(R.id.btn_toggle_add);

        // Form inputs
        tvFormTitle = view.findViewById(R.id.tv_form_title);
        etName = view.findViewById(R.id.et_fullname);
        etPhone = view.findViewById(R.id.et_phone);
        etAddress = view.findViewById(R.id.et_address);
        rgTag = view.findViewById(R.id.rg_tag);
        cbIsDefault = view.findViewById(R.id.cb_is_default);

        // Buttons
        btnSave = view.findViewById(R.id.btn_save_address);
        btnCancel = view.findViewById(R.id.btn_cancel_edit);

        // List & Scroll
        rvAddresses = view.findViewById(R.id.rv_addresses);
        nestedScrollView = view.findViewById(R.id.nested_scroll);
    }

    private void setupRecyclerView() {
        adapter = new AddressAdapter(new AddressAdapter.OnAddressActionListener() {
            @Override
            public void onEdit(Address address) {
                // Khi bấm nút Edit trên item: Đổ dữ liệu lên form và cuộn lên
                prepareEditForm(address);
            }

            @Override
            public void onDelete(Address address) {
                // Khi bấm nút Delete: Hiện dialog xác nhận
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
        // 1. Nút "+ Thêm địa chỉ mới"
        btnToggleAdd.setOnClickListener(v -> {
            if (expandableForm.isExpanded() && !isEditing) {
                // Nếu đang mở form thêm mới rồi thì đóng lại
                closeForm();
            } else {
                // Mở form để thêm mới
                prepareAddForm();
            }
        });

        // 2. Nút "Hủy" trong form
        btnCancel.setOnClickListener(v -> closeForm());

        // 3. Nút "Lưu địa chỉ"
        btnSave.setOnClickListener(v -> handleSaveAddress());
    }

    private void observeData() {
        // Lắng nghe dữ liệu từ ViewModel để cập nhật RecyclerView
        addressViewModel.getDisplayedAddresses().observe(getViewLifecycleOwner(), addresses -> {
            if (addresses != null) {
                adapter.setList(addresses);
            }
        });
    }

    // --- Logic Xử lý Form ---

    private void handleSaveAddress() {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String addressDetail = etAddress.getText().toString().trim();
        String tag = (rgTag.getCheckedRadioButtonId() == R.id.rb_office) ? "Văn phòng" : "Nhà riêng";

        // Lấy giá trị CheckBox
        boolean isDefault = cbIsDefault.isChecked();

        // 1. Validate rỗng
        if (name.isEmpty() || phone.isEmpty() || addressDetail.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Validate số điện thoại (MỚI THÊM)
        if (!isValidPhoneNumber(phone)) {
            Toast.makeText(getContext(), "Số điện thoại không hợp lệ", Toast.LENGTH_SHORT).show();
            // Set lỗi hiển thị ngay tại ô nhập liệu để user biết
            etPhone.setError("SĐT phải có 10 số (VD: 09xx, 03xx...)");
            etPhone.requestFocus(); // Focus vào ô lỗi
            return;
        }

        Address address = new Address();
        address.setName(name);
        address.setPhone(phone);
        address.setAddress(addressDetail);
        address.setTag(tag);
        address.setDefault(isDefault);

        if (isEditing && currentEditingId != null) {
            // Case: Update
            addressViewModel.updateAddress(currentEditingId, address).observe(getViewLifecycleOwner(), res -> {
                if (res != null) {
                    Toast.makeText(getContext(), "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                    closeForm();
                    addressViewModel.refreshData();
                } else {
                    Toast.makeText(getContext(), "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Case: Add New
            addressViewModel.addAddress(address).observe(getViewLifecycleOwner(), res -> {
                if (res != null) {
                    Toast.makeText(getContext(), "Thêm mới thành công", Toast.LENGTH_SHORT).show();
                    closeForm();
                    addressViewModel.refreshData();
                } else {
                    Toast.makeText(getContext(), "Thêm mới thất bại", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void prepareAddForm() {
        clearFormInput();
        isEditing = false;
        currentEditingId = null;

        // --- LOGIC MỚI: KIỂM TRA ĐỊA CHỈ ĐẦU TIÊN ---
        int currentCount = adapter.getItemCount();

        if (currentCount == 0) {
            // Nếu chưa có địa chỉ nào: Bắt buộc chọn và KHÓA checkbox
            cbIsDefault.setChecked(true);
            cbIsDefault.setEnabled(false); // Không cho bỏ tick
            cbIsDefault.setText("Đặt làm địa chỉ mặc định (Bắt buộc)");
        } else {
            // Nếu đã có địa chỉ: Cho phép tùy chọn
            cbIsDefault.setChecked(false);
            cbIsDefault.setEnabled(true);
            cbIsDefault.setText("Đặt làm địa chỉ mặc định");
        }
        // ---------------------------------------------

        tvFormTitle.setText("Thêm địa chỉ mới");
        btnSave.setText("Lưu địa chỉ");
        expandableForm.expand();
    }

    private void prepareEditForm(Address address) {
        // 1. Đổ dữ liệu cũ vào form
        etName.setText(address.getName());
        etPhone.setText(address.getPhone());
        etAddress.setText(address.getAddress());

        if ("Văn phòng".equals(address.getTag())) {
            rgTag.check(R.id.rb_office);
        } else {
            rgTag.check(R.id.rb_home);
        }

        // 2. --- LOGIC MỚI: KIỂM TRA SỐ LƯỢNG KHI EDIT ---
        int currentCount = adapter.getItemCount();

        if (currentCount == 1) {
            // Trường hợp đặc biệt: Chỉ có duy nhất 1 địa chỉ trong danh sách
            // -> Bắt buộc phải là mặc định, không cho bỏ chọn
            cbIsDefault.setChecked(true);
            cbIsDefault.setEnabled(false); // Khóa
            cbIsDefault.setText("Đặt làm địa chỉ mặc định (Bắt buộc)");
        } else {
            // Trường hợp thường: Có > 1 địa chỉ
            // -> Load trạng thái theo dữ liệu cũ và cho phép sửa
            cbIsDefault.setChecked(address.isDefault());
            cbIsDefault.setEnabled(true);
            cbIsDefault.setText("Đặt làm địa chỉ mặc định");
        }
        // ------------------------------------------------

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
                .setIcon(R.drawable.ic_dialog_confirm) // Đảm bảo bạn có icon này (hoặc dùng ic_dialog_confirm)
                .setTitle("Xóa địa chỉ")
                .setDescription("Bạn có chắc chắn muốn xóa địa chỉ của " + address.getName() + "?")

                // Cấu hình màu sắc
                .setPrimaryColor(R.color.blue)
                .setPositiveButtonColor(R.color.blue)
                .setTitleTextColor(R.color.black)
                .setDescriptionTextColor(R.color.gray_text)

                // Sự kiện nút Xóa
                .setPositiveButtonText("Xóa", v -> {
                    addressViewModel.deleteAddress(address.getId()).observe(getViewLifecycleOwner(), success -> {
                        if (success) {
                            Toast.makeText(getContext(), "Đã xóa địa chỉ", Toast.LENGTH_SHORT).show();
                            addressViewModel.refreshData();

                            // Nếu đang edit đúng cái địa chỉ vừa xóa thì đóng form lại
                            if (isEditing && address.getId().equals(currentEditingId)) {
                                closeForm();
                            }
                        } else {
                            Toast.makeText(getContext(), "Xóa thất bại", Toast.LENGTH_SHORT).show();
                        }
                    });
                })

                // Sự kiện nút Hủy
                .setNegativeButtonText("Hủy", v -> {
                    // Không làm gì cả, dialog tự đóng
                })
                .show();
    }

    private void closeForm() {
        expandableForm.collapse();
        clearFormInput();
        isEditing = false;
        currentEditingId = null;
        // Trả lại text nút Thêm
        btnToggleAdd.setText("+ Thêm địa chỉ mới");
    }

    private void clearFormInput() {
        etName.setText("");
        etPhone.setText("");
        etAddress.setText("");
        rgTag.check(R.id.rb_home);

        // Reset hoàn toàn trạng thái checkbox về mặc định
        cbIsDefault.setChecked(false);
        cbIsDefault.setEnabled(true);
        cbIsDefault.setText("Đặt làm địa chỉ mặc định");

        etName.requestFocus();
    }

    private boolean isValidPhoneNumber(String phone) {
        if (phone == null || phone.isEmpty()) {
            return false;
        }
        // Regex: ^ bắt đầu, 0 là số đầu, [35789] là các đầu số, \\d{8} là 8 số cuối, $ là kết thúc
        return phone.matches("^0[35789]\\d{8}$");
    }
}