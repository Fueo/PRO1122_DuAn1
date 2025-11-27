package com.example.fa25_duan1.view.management.discount;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.fa25_duan1.R;
import com.example.fa25_duan1.model.Discount;
import com.example.fa25_duan1.viewmodel.DiscountViewModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DiscountUpdateFragment extends Fragment {

    private EditText edtCode, edtPercent, edtStartDate, edtEndDate;
    private Button btnSave;
    private DiscountViewModel viewModel;
    private String discountId;

    // Calendar để lưu giá trị ngày chọn
    private final Calendar startCalendar = Calendar.getInstance();
    private final Calendar endCalendar = Calendar.getInstance();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()); // Format chuẩn để gửi lên Server (hoặc dd/MM/yyyy tùy backend)

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Sử dụng layout fragment_discount_update.xml
        return inflater.inflate(R.layout.fragment_discount_update, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        viewModel = new ViewModelProvider(this).get(DiscountViewModel.class);

        // Lấy ID từ Intent
        if (getActivity().getIntent() != null) {
            discountId = getActivity().getIntent().getStringExtra("Id");
        }

        setupDatePickers();

        // Load dữ liệu cũ nếu là Edit
        if (discountId != null) {
            loadDiscountData(discountId);
        }

        btnSave.setOnClickListener(v -> handleSave());
    }

    private void initViews(View view) {
        edtCode = view.findViewById(R.id.edtCode);
        edtPercent = view.findViewById(R.id.edtPercent);
        edtStartDate = view.findViewById(R.id.edtStartDate);
        edtEndDate = view.findViewById(R.id.edtEndDate);
        btnSave = view.findViewById(R.id.btnSaveDiscount);

        // Ẩn layout nhập tiền nếu chỉ dùng phần trăm (dựa theo layout xml bạn gửi có layoutInputAmount visibility=gone)
    }

    private void setupDatePickers() {
        // Sự kiện chọn ngày bắt đầu
        edtStartDate.setOnClickListener(v -> showDatePicker(startCalendar, edtStartDate));

        // Sự kiện chọn ngày kết thúc
        edtEndDate.setOnClickListener(v -> showDatePicker(endCalendar, edtEndDate));
    }

    private void showDatePicker(Calendar calendar, EditText editText) {
        new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            editText.setText(dateFormat.format(calendar.getTime()));
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void loadDiscountData(String id) {
        viewModel.getDiscountByID(id).observe(getViewLifecycleOwner(), discount -> {
            if (discount != null) {
                edtCode.setText(discount.getDiscountName());
                edtPercent.setText(String.valueOf((int)discount.getDiscountRate()));

                // Format lại ngày từ Server để hiển thị lên EditText
                // Lưu ý: Cần xử lý parse String từ server về object Calendar nếu muốn chính xác
                edtStartDate.setText(formatDateForDisplay(discount.getStartDate()));
                edtEndDate.setText(formatDateForDisplay(discount.getEndDate()));
            }
        });
    }

    private String formatDateForDisplay(String serverDate) {
        // Helper đơn giản để cắt chuỗi ISO 8601 lấy yyyy-MM-dd
        if (serverDate != null && serverDate.contains("T")) {
            return serverDate.split("T")[0];
        }
        return serverDate;
    }

    private void handleSave() {
        String name = edtCode.getText().toString().trim();
        String rateStr = edtPercent.getText().toString().trim();
        String start = edtStartDate.getText().toString().trim();
        String end = edtEndDate.getText().toString().trim();

        if (name.isEmpty() || rateStr.isEmpty() || start.isEmpty() || end.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        double rate = Double.parseDouble(rateStr);
        if (rate < 0 || rate > 100) {
            Toast.makeText(getContext(), "% giảm giá phải từ 0 - 100", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo object Discount
        // Lưu ý: Backend Nodejs của bạn nhận startDate, endDate format Date.
        // Bạn gửi chuỗi "yyyy-MM-dd" lên, Mongoose thường tự parse được.
        Discount discount = new Discount(name, rate, start, end);

        if (discountId == null) {
            // Thêm mới
            viewModel.addDiscount(discount).observe(getViewLifecycleOwner(), success -> {
                if (success) {
                    Toast.makeText(getContext(), "Thêm thành công", Toast.LENGTH_SHORT).show();
                    finishActivity();
                } else {
                    Toast.makeText(getContext(), "Thêm thất bại", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Cập nhật
            viewModel.updateDiscount(discountId, discount).observe(getViewLifecycleOwner(), success -> {
                if (success) {
                    Toast.makeText(getContext(), "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                    finishActivity();
                } else {
                    Toast.makeText(getContext(), "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void finishActivity() {
        if (getActivity() != null) {
            getActivity().setResult(Activity.RESULT_OK);
            getActivity().finish();
        }
    }
}