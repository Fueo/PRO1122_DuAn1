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

    private OrderViewModel viewModel;

    private NiceSpinner spSort;
    private LinearLayout llToggleFilter;
    private ExpandableLayout expandableLayout;
    private ImageView ivClose;

    private TextView tvStartDate, tvEndDate;
    private Calendar calendarStart, calendarEnd;
    private long startTimeMillis = 0;
    private long endTimeMillis = 0;

    private CheckBox cbWaitConfirm, cbPending, cbShipping, cbCompleted, cbCancelled;
    private CheckBox cbTotalRange1, cbTotalRange2, cbTotalRange3, cbTotalRange4;

    private Button btnReset, btnApply;

    private final SimpleDateFormat displayDateFormat =
            new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_orderfilter, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(OrderViewModel.class);

        initViews(view);
        setupSpinner();
        setupDatePicker();
        setupExpandableLayout();

        restoreFilterState(); // ✅ QUAN TRỌNG

        btnApply.setOnClickListener(v -> applyFilter());
        btnReset.setOnClickListener(v -> resetFilter());
    }

    // =====================================================
    // ===================== RESTORE UI ====================
    // =====================================================

    private void restoreFilterState() {
        // Sort
        spSort.setSelectedIndex(viewModel.getCurrentSortType());

        // Status
        List<Integer> status = viewModel.getCurrentStatusFilter();
        if (status != null) {
            cbWaitConfirm.setChecked(status.contains(0));
            cbPending.setChecked(status.contains(1));
            cbShipping.setChecked(status.contains(2));
            cbCompleted.setChecked(status.contains(3));
            cbCancelled.setChecked(status.contains(4));
        }

        // Price
        List<Integer> prices = viewModel.getCurrentPriceRanges();
        if (prices != null) {
            cbTotalRange1.setChecked(prices.contains(0));
            cbTotalRange2.setChecked(prices.contains(1));
            cbTotalRange3.setChecked(prices.contains(2));
            cbTotalRange4.setChecked(prices.contains(3));
        }

        // Date
        startTimeMillis = viewModel.getCurrentStartDate();
        endTimeMillis = viewModel.getCurrentEndDate();

        if (startTimeMillis > 0) {
            calendarStart.setTimeInMillis(startTimeMillis);
            tvStartDate.setText(displayDateFormat.format(calendarStart.getTime()));
        }

        if (endTimeMillis > 0) {
            calendarEnd.setTimeInMillis(endTimeMillis);
            tvEndDate.setText(displayDateFormat.format(calendarEnd.getTime()));
        }
    }

    // =====================================================
    // ===================== INIT ==========================
    // =====================================================

    private void initViews(View view) {
        spSort = view.findViewById(R.id.spSort);
        llToggleFilter = view.findViewById(R.id.llToggleFilter);
        expandableLayout = view.findViewById(R.id.expandable_layout);
        ivClose = view.findViewById(R.id.ivClose);

        tvStartDate = view.findViewById(R.id.tvStartDate);
        tvEndDate = view.findViewById(R.id.tvEndDate);
        calendarStart = Calendar.getInstance();
        calendarEnd = Calendar.getInstance();

        cbWaitConfirm = view.findViewById(R.id.cbWaitConfirm);
        cbPending = view.findViewById(R.id.cbPending);
        cbShipping = view.findViewById(R.id.cbShipping);
        cbCompleted = view.findViewById(R.id.cbCompleted);
        cbCancelled = view.findViewById(R.id.cbCancelled);

        cbTotalRange1 = view.findViewById(R.id.cbTotalRange1);
        cbTotalRange2 = view.findViewById(R.id.cbTotalRange2);
        cbTotalRange3 = view.findViewById(R.id.cbTotalRange3);
        cbTotalRange4 = view.findViewById(R.id.cbTotalRange4);

        btnReset = view.findViewById(R.id.btnReset);
        btnApply = view.findViewById(R.id.btnApply);
    }

    private void setupSpinner() {
        spSort.attachDataSource(new LinkedList<>(Arrays.asList(
                "Mới nhất",
                "Cũ nhất",
                "Giá thấp đến cao",
                "Giá cao đến thấp"
        )));

        spSort.setOnSpinnerItemSelectedListener((parent, v, position, id) -> applyFilter());
    }

    private void setupDatePicker() {
        tvStartDate.setOnClickListener(v -> {
            new DatePickerDialog(getContext(), (view, y, m, d) -> {
                calendarStart.set(y, m, d, 0, 0, 0);
                startTimeMillis = calendarStart.getTimeInMillis();
                tvStartDate.setText(displayDateFormat.format(calendarStart.getTime()));
            }, calendarStart.get(Calendar.YEAR),
                    calendarStart.get(Calendar.MONTH),
                    calendarStart.get(Calendar.DAY_OF_MONTH)).show();
        });

        tvEndDate.setOnClickListener(v -> {
            new DatePickerDialog(getContext(), (view, y, m, d) -> {
                calendarEnd.set(y, m, d, 23, 59, 59);
                endTimeMillis = calendarEnd.getTimeInMillis();
                tvEndDate.setText(displayDateFormat.format(calendarEnd.getTime()));
            }, calendarEnd.get(Calendar.YEAR),
                    calendarEnd.get(Calendar.MONTH),
                    calendarEnd.get(Calendar.DAY_OF_MONTH)).show();
        });
    }

    private void setupExpandableLayout() {
        llToggleFilter.setOnClickListener(v ->
        { if (expandableLayout.isExpanded()) expandableLayout.collapse();
        else expandableLayout.expand(); });

        ivClose.setOnClickListener(v -> expandableLayout.collapse());
    }

    // =====================================================
    // ===================== ACTIONS =======================
    // =====================================================

    private void applyFilter() {
        int sortType = spSort.getSelectedIndex();

        List<Integer> status = new ArrayList<>();
        if (cbWaitConfirm.isChecked()) status.add(0);
        if (cbPending.isChecked()) status.add(1);
        if (cbShipping.isChecked()) status.add(2);
        if (cbCompleted.isChecked()) status.add(3);
        if (cbCancelled.isChecked()) status.add(4);

        List<Integer> price = new ArrayList<>();
        if (cbTotalRange1.isChecked()) price.add(0);
        if (cbTotalRange2.isChecked()) price.add(1);
        if (cbTotalRange3.isChecked()) price.add(2);
        if (cbTotalRange4.isChecked()) price.add(3);

        if (startTimeMillis > 0 && endTimeMillis > 0 && startTimeMillis > endTimeMillis) {
            Toast.makeText(getContext(), "Ngày bắt đầu không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        viewModel.filterAndSortOrders(
                sortType,
                status,
                startTimeMillis,
                endTimeMillis,
                price
        );

        expandableLayout.collapse();
    }

    private void resetFilter() {
        spSort.setSelectedIndex(0);
        cbWaitConfirm.setChecked(false);
        cbPending.setChecked(false);
        cbShipping.setChecked(false);
        cbCompleted.setChecked(false);
        cbCancelled.setChecked(false);

        cbTotalRange1.setChecked(false);
        cbTotalRange2.setChecked(false);
        cbTotalRange3.setChecked(false);
        cbTotalRange4.setChecked(false);

        tvStartDate.setText("");
        tvEndDate.setText("");
        startTimeMillis = 0;
        endTimeMillis = 0;

        viewModel.filterAndSortOrders(0, null, 0, 0, null);
        Toast.makeText(getContext(), "Đã đặt lại bộ lọc", Toast.LENGTH_SHORT).show();
    }
}
