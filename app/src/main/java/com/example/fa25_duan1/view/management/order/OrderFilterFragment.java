package com.example.fa25_duan1.view.management.order;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.fa25_duan1.R;
import com.example.fa25_duan1.viewmodel.OrderViewModel;

import net.cachapa.expandablelayout.ExpandableLayout;

import org.angmarch.views.NiceSpinner;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class OrderFilterFragment extends Fragment {

    // ViewModel
    private OrderViewModel viewModel;

    // UI Components
    private NiceSpinner spSort;
    private LinearLayout llToggleFilter;
    private ExpandableLayout expandableLayout;
    private ImageView ivClose;

    // Date Selectors
    private TextView tvStartDate, tvEndDate;
    private Calendar calendarStart, calendarEnd;
    private long startTimeMillis = 0;
    private long endTimeMillis = 0;

    // [CẬP NHẬT] Status Checkboxes (Thêm cbWaitConfirm)
    private CheckBox cbWaitConfirm, cbPending, cbShipping, cbCompleted, cbCancelled;

    // Price Checkboxes
    private CheckBox cbTotalRange1, cbTotalRange2, cbTotalRange3, cbTotalRange4;

    // Buttons
    private Button btnReset, btnApply;

    // Format ngày hiển thị lên TextView
    private final SimpleDateFormat displayDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // [LƯU Ý] Hãy đảm bảo tên file XML đúng với file bạn vừa tạo (ví dụ: fragment_order_filter.xml)
        return inflater.inflate(R.layout.fragment_orderfilter, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Kết nối ViewModel (Dùng requireActivity để share data với OrderManageFragment)
        viewModel = new ViewModelProvider(requireActivity()).get(OrderViewModel.class);

        // 2. Ánh xạ View
        initViews(view);

        // 3. Thiết lập các thành phần
        setupSpinner();
        setupDatePicker();
        setupExpandableLayout();

        // 4. Sự kiện Click
        btnApply.setOnClickListener(v -> applyFilter());
        btnReset.setOnClickListener(v -> resetFilter());
    }

    private void initViews(View view) {
        // Header
        spSort = view.findViewById(R.id.spSort);
        llToggleFilter = view.findViewById(R.id.llToggleFilter);
        expandableLayout = view.findViewById(R.id.expandable_layout);
        ivClose = view.findViewById(R.id.ivClose);

        // Date
        tvStartDate = view.findViewById(R.id.tvStartDate);
        tvEndDate = view.findViewById(R.id.tvEndDate);
        calendarStart = Calendar.getInstance();
        calendarEnd = Calendar.getInstance();

        // [CẬP NHẬT] Checkbox Status (Khớp ID trong XML)
        cbWaitConfirm = view.findViewById(R.id.cbWaitConfirm); // Mới
        cbPending = view.findViewById(R.id.cbPending);
        cbShipping = view.findViewById(R.id.cbShipping);
        cbCompleted = view.findViewById(R.id.cbCompleted);
        cbCancelled = view.findViewById(R.id.cbCancelled);

        // Checkbox Price
        cbTotalRange1 = view.findViewById(R.id.cbTotalRange1); // < 500k
        cbTotalRange2 = view.findViewById(R.id.cbTotalRange2); // 500k - 1tr
        cbTotalRange3 = view.findViewById(R.id.cbTotalRange3); // 1tr - 5tr
        cbTotalRange4 = view.findViewById(R.id.cbTotalRange4); // > 5tr

        // Buttons
        btnReset = view.findViewById(R.id.btnReset);
        btnApply = view.findViewById(R.id.btnApply);
    }

    private void setupSpinner() {
        LinkedList<String> data = new LinkedList<>(Arrays.asList(
                "Mới nhất",
                "Cũ nhất",
                "Giá thấp đến cao",
                "Giá cao đến thấp"
        ));
        spSort.attachDataSource(data);

        // Khi người dùng chọn sort, có thể gọi apply luôn hoặc đợi bấm nút
        spSort.setOnSpinnerItemSelectedListener((parent, v, position, id) -> {
            applyFilter(); // Tự động apply khi chọn sort
        });
    }

    private void setupDatePicker() {
        // Chọn ngày bắt đầu
        tvStartDate.setOnClickListener(v -> {
            DatePickerDialog dialog = new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
                // Set thời gian là 00:00:00 của ngày đó
                calendarStart.set(year, month, dayOfMonth, 0, 0, 0);
                startTimeMillis = calendarStart.getTimeInMillis();
                tvStartDate.setText(displayDateFormat.format(calendarStart.getTime()));
            }, calendarStart.get(Calendar.YEAR), calendarStart.get(Calendar.MONTH), calendarStart.get(Calendar.DAY_OF_MONTH));
            dialog.show();
        });

        // Chọn ngày kết thúc
        tvEndDate.setOnClickListener(v -> {
            DatePickerDialog dialog = new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
                // Set thời gian là 23:59:59 của ngày đó để bao trọn ngày
                calendarEnd.set(year, month, dayOfMonth, 23, 59, 59);
                endTimeMillis = calendarEnd.getTimeInMillis();
                tvEndDate.setText(displayDateFormat.format(calendarEnd.getTime()));
            }, calendarEnd.get(Calendar.YEAR), calendarEnd.get(Calendar.MONTH), calendarEnd.get(Calendar.DAY_OF_MONTH));
            dialog.show();
        });
    }

    private void setupExpandableLayout() {
        // Bấm vào header thì mở/đóng layout lọc
        llToggleFilter.setOnClickListener(v -> {
            if (expandableLayout.isExpanded()) {
                expandableLayout.collapse();
            } else {
                expandableLayout.expand();
            }
        });

        // Bấm nút X thì đóng
        ivClose.setOnClickListener(v -> expandableLayout.collapse());
    }

    private void applyFilter() {
        // 1. Lấy kiểu sắp xếp
        int sortType = spSort.getSelectedIndex();

        // 2. Lấy danh sách trạng thái [QUAN TRỌNG: Khớp logic ViewModel]
        List<Integer> statusList = new ArrayList<>();

        if (cbWaitConfirm.isChecked()) statusList.add(0); // Chờ xác nhận
        if (cbPending.isChecked()) statusList.add(1);     // Đang xử lý
        if (cbShipping.isChecked()) statusList.add(2);    // Đang giao
        if (cbCompleted.isChecked()) statusList.add(3);   // Hoàn thành
        if (cbCancelled.isChecked()) statusList.add(4);   // Đã hủy

        // 3. Lấy khoảng giá
        List<Integer> priceRanges = new ArrayList<>();
        if (cbTotalRange1.isChecked()) priceRanges.add(0);
        if (cbTotalRange2.isChecked()) priceRanges.add(1);
        if (cbTotalRange3.isChecked()) priceRanges.add(2);
        if (cbTotalRange4.isChecked()) priceRanges.add(3);

        // 4. Validate ngày tháng (nếu có chọn)
        if (startTimeMillis > 0 && endTimeMillis > 0 && startTimeMillis > endTimeMillis) {
            Toast.makeText(getContext(), "Ngày bắt đầu không được lớn hơn ngày kết thúc", Toast.LENGTH_SHORT).show();
            return;
        }

        // 5. Gửi sang ViewModel xử lý
        viewModel.filterAndSortOrders(sortType, statusList, startTimeMillis, endTimeMillis, priceRanges);

        // 6. Đóng panel sau khi áp dụng
        expandableLayout.collapse();
    }

    private void resetFilter() {
        // Reset UI về mặc định
        spSort.setSelectedIndex(0);

        // Reset Status
        cbWaitConfirm.setChecked(false);
        cbPending.setChecked(false);
        cbShipping.setChecked(false);
        cbCompleted.setChecked(false);
        cbCancelled.setChecked(false);

        // Reset Price
        cbTotalRange1.setChecked(false);
        cbTotalRange2.setChecked(false);
        cbTotalRange3.setChecked(false);
        cbTotalRange4.setChecked(false);

        // Reset Date
        tvStartDate.setText("");
        tvEndDate.setText("");
        startTimeMillis = 0;
        endTimeMillis = 0;

        // Reset dữ liệu (gọi filter với tham số rỗng)
        viewModel.filterAndSortOrders(0, null, 0, 0, null);

        Toast.makeText(getContext(), "Đã đặt lại bộ lọc", Toast.LENGTH_SHORT).show();
    }
}