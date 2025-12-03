package com.example.fa25_duan1.view.management.statistic;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.media.Image;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.example.fa25_duan1.R;
import com.example.fa25_duan1.adapter.StatisticsPagerAdapter;
import com.example.fa25_duan1.model.statistic.StatsDailyOverview;
import com.example.fa25_duan1.model.statistic.StatsOrderStatus;
import com.example.fa25_duan1.model.statistic.StatsRevenue;
import com.example.fa25_duan1.viewmodel.StatisticViewModel;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.angmarch.views.NiceSpinner;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class StatisticsFragment extends Fragment {

    // --- Views ---
    private TextView tvTodayRevenue, tvNewOrders, tvNewUsers;
    private TabLayout tabLayoutStats;
    private ViewPager2 viewPagerStats;

    // --- ViewModel ---
    private StatisticViewModel viewModel;

    // --- Colors ---
    private int colorBlue, colorDarkBlue, colorRed, colorGray, colorGrayText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_statistics, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initColors();
        initViews(view);

        viewModel = new ViewModelProvider(this).get(StatisticViewModel.class);

        loadDailyOverview();
        setupTabs();
    }

    private void initColors() {
        Context context = requireContext();
        colorBlue = ContextCompat.getColor(context, R.color.blue);
        colorDarkBlue = ContextCompat.getColor(context, R.color.dark_blue);
        colorRed = ContextCompat.getColor(context, R.color.red);
        colorGray = ContextCompat.getColor(context, R.color.gray);
        colorGrayText = ContextCompat.getColor(context, R.color.gray_text);
    }

    private void initViews(View view) {
        tvTodayRevenue = view.findViewById(R.id.tvTodayRevenue);
        tvNewOrders = view.findViewById(R.id.tvNewOrders);
        tvNewUsers = view.findViewById(R.id.tvNewUsers);

        tabLayoutStats = view.findViewById(R.id.tabLayoutStats);
        viewPagerStats = view.findViewById(R.id.viewPagerStats);
    }

    private void loadDailyOverview() {
        viewModel.getDailyOverview().observe(getViewLifecycleOwner(), response -> {
            if (response != null && response.isStatus() && response.getData() != null) {
                StatsDailyOverview data = response.getData();
                tvTodayRevenue.setText(formatCurrency(data.getTodayRevenue()));
                tvNewOrders.setText(String.valueOf(data.getNewOrders()));
                tvNewUsers.setText(String.valueOf(data.getNewUsers()));
            } else {
                tvTodayRevenue.setText("0đ");
                tvNewOrders.setText("0");
                tvNewUsers.setText("0");
            }
        });
    }

    // ========================================================================
    // SETUP TABS & DATA LOADING
    // ========================================================================
    private void setupTabs() {
        List<String> timeFilters = new LinkedList<>(Arrays.asList("7 ngày qua", "30 ngày qua", "Tháng này", "Năm nay"));

        StatisticsPagerAdapter adapter = new StatisticsPagerAdapter(new StatisticsPagerAdapter.OnBindChartListener() {

            // --- TAB 1: DOANH THU ---
            @Override
            public void onBindRevenue(View view) {
                LineChart chart = view.findViewById(R.id.lineChartRevenue);
                NiceSpinner spTimeFilter = view.findViewById(R.id.spTimeFilter);

                TextView tvTotalRevenue = view.findViewById(R.id.tvStatTotalRevenue);
                ImageView ivArrow = view.findViewById(R.id.ivArrow);
                TextView tvGrowth = view.findViewById(R.id.tvStatGrowth);
                TextView tvAvgOrder = view.findViewById(R.id.tvStatAvgOrder);
                TextView tvTotalSuccess = view.findViewById(R.id.tvStatTotalSuccess);

                ProgressBar pbCOD = view.findViewById(R.id.progressCOD);
                ProgressBar pbBanking = view.findViewById(R.id.progressBanking);
                TextView tvPercentCOD = view.findViewById(R.id.tvPercentCOD);
                TextView tvPercentBanking = view.findViewById(R.id.tvPercentBanking);

                spTimeFilter.attachDataSource(timeFilters);

                // Load dữ liệu mặc định 7 ngày
                loadRevenueData(chart, tvTotalRevenue, tvGrowth, ivArrow, tvAvgOrder, tvTotalSuccess, pbCOD, pbBanking, tvPercentCOD, tvPercentBanking, "7_days");

                spTimeFilter.setOnSpinnerItemSelectedListener((parent, v, position, id) -> {
                    String selectedPeriod = "7_days";
                    switch (position) {
                        case 0: selectedPeriod = "7_days"; break;
                        case 1: selectedPeriod = "30_days"; break;
                        case 2: selectedPeriod = "this_month"; break;
                        case 3: selectedPeriod = "this_year"; break;
                    }
                    loadRevenueData(chart, tvTotalRevenue, tvGrowth, ivArrow, tvAvgOrder, tvTotalSuccess, pbCOD, pbBanking, tvPercentCOD, tvPercentBanking, selectedPeriod);
                });
            }

            // --- TAB 2: ĐƠN HÀNG (Giữ nguyên) ---
            @Override
            public void onBindOrders(View view) {
                PieChart chart = view.findViewById(R.id.pieChartOrderStatus);
                NiceSpinner spTimeFilter = view.findViewById(R.id.spTimeFilterOrders);
                TextView tvSuccessRate = view.findViewById(R.id.tvStatSuccessRate);
                TextView tvCancelRate = view.findViewById(R.id.tvStatCancelRate);

                spTimeFilter.attachDataSource(timeFilters);
                loadOrderStatusData(chart, tvSuccessRate, tvCancelRate);
            }

            // --- TAB 3: TẠM ẨN ---
            @Override
            public void onBindProducts(View view) {
                // Không làm gì hoặc ẩn view đi
            }
        });

        viewPagerStats.setAdapter(adapter);

        new TabLayoutMediator(tabLayoutStats, viewPagerStats, (tab, position) -> {
            switch (position) {
                case 0: tab.setText("Doanh thu"); break;
                case 1: tab.setText("Đơn hàng"); break;
                 case 2: tab.setText("Top Sách"); break;
            }
        }).attach();
    }

    // --- Helper Load Data: Revenue (Chi tiết) ---
    private void loadRevenueData(LineChart chart, TextView tvTotal, TextView tvGrowth, ImageView ivArrow, TextView tvAvg, TextView tvCount,
                                 ProgressBar pbCOD, ProgressBar pbBanking, TextView txtCOD, TextView txtBanking, String period) {

        viewModel.getRevenueStats(period).observe(getViewLifecycleOwner(), response -> {
            if (response != null && response.isStatus() && response.getData() != null) {
                StatsRevenue data = response.getData();

                // 1. Cập nhật Text chỉ số
                if (tvTotal != null) tvTotal.setText(formatCurrency(data.getTotalRevenue()));
                if (tvAvg != null) tvAvg.setText(formatCurrency(data.getAvgOrderValue()));
                if (tvCount != null) tvCount.setText(data.getTotalOrders() + " đơn");

                if (tvGrowth != null) {
                    float growth = data.getGrowth();
                    tvGrowth.setText((growth > 0 ? "+" : "") + growth + "%");
                    tvGrowth.setTextColor(growth >= 0 ? Color.parseColor("#4CAF50") : Color.RED);
                    ivArrow.setImageTintList(growth >= 0 ? ColorStateList.valueOf(Color.parseColor("#4CAF50")): ColorStateList.valueOf(Color.RED));
                    ivArrow.setRotation(growth >= 0 ? 0 : 180);
                }

                // 2. Cập nhật Nguồn tiền
                if (pbCOD != null) {
                    pbCOD.setMax(100);
                    pbCOD.setProgress(data.getCodPercent());
                }
                if (txtCOD != null) txtCOD.setText(data.getCodPercent() + "%");

                if (pbBanking != null) {
                    pbBanking.setMax(100);
                    pbBanking.setProgress(data.getBankingPercent());
                }
                if (txtBanking != null) txtBanking.setText(data.getBankingPercent() + "%");

                // 3. Cập nhật biểu đồ LineChart bằng dữ liệu API
                setupRevenueChart(chart, data.getChartData());
            } else {
                android.util.Log.e("STATS_DEBUG", "Response null hoặc lỗi khi load Revenue");
            }
        });
    }

    // --- Helper Load Data: Order Status ---
    private void loadOrderStatusData(PieChart chart, TextView tvSuccess, TextView tvCancel) {
        viewModel.getOrderStatusStats().observe(getViewLifecycleOwner(), response -> {
            if (response != null && response.isStatus() && response.getData() != null) {
                StatsOrderStatus stats = response.getData();
                updateOrderStatusChart(chart, stats);

                int total = stats.getCompleted() + stats.getPending() + stats.getProcessing() + stats.getShipping() + stats.getCancelled();
                if (total > 0) {
                    float successRate = (float) stats.getCompleted() / total * 100;
                    float cancelRate = (float) stats.getCancelled() / total * 100;

                    if (tvSuccess != null) tvSuccess.setText(String.format("%.1f%%", successRate));
                    if (tvCancel != null) tvCancel.setText(String.format("%.1f%%", cancelRate));
                }
            }
        });
    }

    // ========================================================================
    // CẬP NHẬT BIỂU ĐỒ (VISUALS)
    // ========================================================================

    // [CẬP NHẬT] Hàm nhận dữ liệu API
    private void setupRevenueChart(LineChart chart, List<StatsRevenue.ChartData> apiData) {
        if (chart == null || apiData == null || apiData.isEmpty()) {
            chart.clear();
            return;
        }

        List<Entry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        // 1. Chuyển đổi dữ liệu API sang định dạng Chart Entry
        for (int i = 0; i < apiData.size(); i++) {
            StatsRevenue.ChartData data = apiData.get(i);
            // Giả sử API trả về số tiền nguyên (VND), chia cho 1,000,000 để vẽ đơn vị Triệu
            entries.add(new Entry(i, data.getValue() / 1000000f));

            // Lấy 5 ký tự cuối (MM-DD) và thay '-' bằng '/' cho label
            String fullDate = data.getDate();
            String dateLabel = fullDate.substring(5).replace('-', '/');
            labels.add(dateLabel);
        }

        LineDataSet dataSet = new LineDataSet(entries, "Doanh thu (Triệu VNĐ)");
        dataSet.setColor(colorBlue);
        dataSet.setLineWidth(2.5f);
        dataSet.setCircleColor(colorDarkBlue);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawValues(false);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawFilled(true);
        dataSet.setFillDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.fade_blue));

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);

        // Styling
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.getAxisRight().setEnabled(false);
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chart.getXAxis().setDrawGridLines(false);
        chart.getAxisLeft().setDrawGridLines(true);
        chart.getAxisLeft().setGridColor(Color.parseColor("#E0E0E0"));

        // Thiết lập nhãn X-Axis từ dữ liệu API
        chart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        chart.getXAxis().setLabelCount(labels.size(), true); // Bắt buộc hiện hết nhãn
        chart.getXAxis().setTextColor(colorGrayText);

        chart.animateY(1000);
        chart.invalidate();
    }

    private void updateOrderStatusChart(PieChart chart, StatsOrderStatus stats) {
        if (chart == null) return;

        List<PieEntry> entries = new ArrayList<>();
        int completed = stats.getCompleted();
        int processing = stats.getPending() + stats.getProcessing() + stats.getShipping();
        int cancelled = stats.getCancelled();

        if (completed > 0) entries.add(new PieEntry(completed, "Hoàn thành"));
        if (processing > 0) entries.add(new PieEntry(processing, "Đang xử lý"));
        if (cancelled > 0) entries.add(new PieEntry(cancelled, "Đã hủy"));

        if (entries.isEmpty()) {
            chart.clear();
            chart.setCenterText("Chưa có\ndữ liệu");
            return;
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        ArrayList<Integer> colors = new ArrayList<>();
        if (completed > 0) colors.add(colorBlue);
        if (processing > 0) colors.add(colorGray);
        if (cancelled > 0) colors.add(colorRed);
        dataSet.setColors(colors);

        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(12f);
        dataSet.setValueFormatter(new PercentFormatter(chart));

        PieData pieData = new PieData(dataSet);
        chart.setData(pieData);
        chart.setUsePercentValues(true);
        chart.getDescription().setEnabled(false);
        chart.setDrawHoleEnabled(true);
        chart.setHoleColor(Color.WHITE);
        chart.setTransparentCircleRadius(0f);
        chart.setHoleRadius(50f);

        int total = completed + processing + cancelled;
        chart.setCenterText("Tổng đơn\n" + total);
        chart.setCenterTextSize(16f);
        chart.setCenterTextColor(colorGrayText);
        chart.getLegend().setTextColor(colorGrayText);
        chart.getLegend().setEnabled(true);
        chart.setEntryLabelColor(Color.WHITE);
        chart.setEntryLabelTextSize(0f);

        chart.animateY(1000);
        chart.invalidate();
    }

    private String formatCurrency(long amount) {
        if (amount >= 1000000000) {
            return String.format("%.1fB", amount / 1000000000.0);
        } else if (amount >= 1000000) {
            return String.format("%.1fTr", amount / 1000000.0);
        } else {
            return new DecimalFormat("###,###").format(amount) + "đ";
        }
    }
}