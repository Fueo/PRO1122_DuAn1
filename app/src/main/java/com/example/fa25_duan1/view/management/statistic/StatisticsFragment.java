package com.example.fa25_duan1.view.management.statistic;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.fa25_duan1.R;
import com.example.fa25_duan1.adapter.StatisticsPagerAdapter;
import com.example.fa25_duan1.adapter.TopProductAdapter; // Adapter mới
import com.example.fa25_duan1.model.statistic.StatsDailyOverview;
import com.example.fa25_duan1.model.statistic.StatsOrder;
import com.example.fa25_duan1.model.statistic.StatsProductOverview;
import com.example.fa25_duan1.model.statistic.StatsRevenue;
import com.example.fa25_duan1.view.management.ManageActivity;
import com.example.fa25_duan1.viewmodel.StatisticViewModel;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
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

    // --- ViewModel & Adapters ---
    private StatisticViewModel viewModel;
    private TopProductAdapter topProductAdapter; // Adapter cho list Top Sách

    // --- Colors ---
    private int colorPending, colorProcessing, colorShipping, colorCompleted, colorCancelled, colorDefault;
    private int colorBlue, colorDarkBlue, colorGrayText;

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
        colorGrayText = ContextCompat.getColor(context, R.color.gray_text);

        colorPending = Color.parseColor("#FF9800");
        colorProcessing = Color.parseColor("#2196F3");
        colorShipping = Color.parseColor("#673AB7");
        colorCompleted = Color.parseColor("#4CAF50");
        colorCancelled = Color.parseColor("#F44336");
        colorDefault = Color.parseColor("#9E9E9E");
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
                loadRevenueData(chart, tvTotalRevenue, tvGrowth, ivArrow, tvAvgOrder, tvTotalSuccess, pbCOD, pbBanking, tvPercentCOD, tvPercentBanking, "7_days");

                spTimeFilter.setOnSpinnerItemSelectedListener((parent, v, position, id) -> {
                    String selectedPeriod = getPeriodKey(position);
                    loadRevenueData(chart, tvTotalRevenue, tvGrowth, ivArrow, tvAvgOrder, tvTotalSuccess, pbCOD, pbBanking, tvPercentCOD, tvPercentBanking, selectedPeriod);
                });
            }

            // --- TAB 2: ĐƠN HÀNG ---
            @Override
            public void onBindOrders(View view) {
                PieChart chart = view.findViewById(R.id.pieChartOrderStatus);
                NiceSpinner spTimeFilter = view.findViewById(R.id.spTimeFilterOrders);
                TextView tvSuccessRate = view.findViewById(R.id.tvStatSuccessRate);
                TextView tvCancelRate = view.findViewById(R.id.tvStatCancelRate);
                TextView tvAvgOrderValue = view.findViewById(R.id.tvAvgOrderValue);
                TextView tvMaxOrderValue = view.findViewById(R.id.tvMaxOrderValue);

                spTimeFilter.attachDataSource(timeFilters);
                loadOrderStatsData(chart, tvSuccessRate, tvCancelRate, tvAvgOrderValue, tvMaxOrderValue, "7_days");

                spTimeFilter.setOnSpinnerItemSelectedListener((parent, v, position, id) -> {
                    String selectedPeriod = getPeriodKey(position);
                    loadOrderStatsData(chart, tvSuccessRate, tvCancelRate, tvAvgOrderValue, tvMaxOrderValue, selectedPeriod);
                });
            }

            // --- TAB 3: TOP SÁCH (DÙNG RECYCLERVIEW) ---
            @Override
            public void onBindProducts(View view) {
                // Ánh xạ RecyclerView thay vì Chart
                RecyclerView rvTopProducts = view.findViewById(R.id.rvTopProducts);
                NiceSpinner spTimeFilter = view.findViewById(R.id.spTimeFilterProducts);
                TextView tvLowStockAlert = view.findViewById(R.id.tvLowStockAlert);
                TextView tvStatTotalProducts = view.findViewById(R.id.tvStatTotalProducts);
                TextView tvStatOutOfStock = view.findViewById(R.id.tvStatOutOfStock);
                CardView cvLowStockAlert = view.findViewById(R.id.cvLowStockAlert);
                // Lấy View Card chứa cảnh báo tồn kho
                View cardLowStock = (View) tvLowStockAlert.getParent().getParent().getParent();

                // Cấu hình RecyclerView & Adapter
                rvTopProducts.setLayoutManager(new LinearLayoutManager(getContext()));
                topProductAdapter = new TopProductAdapter(getContext());
                rvTopProducts.setAdapter(topProductAdapter);

                spTimeFilter.attachDataSource(timeFilters);

                // Load dữ liệu
                loadProductOverviewData(tvLowStockAlert, tvStatTotalProducts, tvStatOutOfStock, cardLowStock, "7_days");

                spTimeFilter.setOnSpinnerItemSelectedListener((parent, v, position, id) -> {
                    String selectedPeriod = getPeriodKey(position);
                    loadProductOverviewData(tvLowStockAlert, tvStatTotalProducts, tvStatOutOfStock, cardLowStock, selectedPeriod);
                });

                cvLowStockAlert.setOnClickListener(v -> {
                    Intent intent = new Intent(getActivity(), ManageActivity.class);
                    intent.putExtra(ManageActivity.EXTRA_CONTENT_FRAGMENT, "product");
                    intent.putExtra("FILTER_LOW_STOCK", true);
                    startActivity(intent);
                });
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

    private String getPeriodKey(int position) {
        switch (position) {
            case 0: return "7_days";
            case 1: return "30_days";
            case 2: return "this_month";
            case 3: return "this_year";
            default: return "7_days";
        }
    }

    // --- Helper Load Data ---

    private void loadRevenueData(LineChart chart, TextView tvTotal, TextView tvGrowth, ImageView ivArrow, TextView tvAvg, TextView tvCount,
                                 ProgressBar pbCOD, ProgressBar pbBanking, TextView txtCOD, TextView txtBanking, String period) {
        viewModel.getRevenueStats(period).observe(getViewLifecycleOwner(), response -> {
            if (response != null && response.isStatus() && response.getData() != null) {
                StatsRevenue data = response.getData();
                if (tvTotal != null) tvTotal.setText(formatCurrency(data.getTotalRevenue()));
                if (tvAvg != null) tvAvg.setText(formatCurrency(data.getAvgOrderValue()));
                if (tvCount != null) tvCount.setText(data.getTotalOrders() + " đơn");
                if (tvGrowth != null) {
                    float growth = data.getGrowth();
                    tvGrowth.setText((growth > 0 ? "+" : "") + growth + "%");
                    int growthColor = growth >= 0 ? Color.parseColor("#4CAF50") : Color.RED;
                    tvGrowth.setTextColor(growthColor);
                    ivArrow.setImageTintList(ColorStateList.valueOf(growthColor));
                    ivArrow.setRotation(growth >= 0 ? 0 : 180);
                }
                if (pbCOD != null) { pbCOD.setMax(100); pbCOD.setProgress(data.getCodPercent()); }
                if (txtCOD != null) txtCOD.setText(data.getCodPercent() + "%");
                if (pbBanking != null) { pbBanking.setMax(100); pbBanking.setProgress(data.getBankingPercent()); }
                if (txtBanking != null) txtBanking.setText(data.getBankingPercent() + "%");
                setupRevenueChart(chart, data.getChartData());
            }
        });
    }

    private void loadOrderStatsData(PieChart chart, TextView tvSuccess, TextView tvCancel,
                                    TextView tvAvgValue, TextView tvMaxValue, String period) {
        viewModel.getOrderStats(period).observe(getViewLifecycleOwner(), response -> {
            if (response != null && response.isStatus() && response.getData() != null) {
                StatsOrder data = response.getData();
                if (tvSuccess != null) tvSuccess.setText(data.getSuccessRate() + "%");
                if (tvCancel != null) tvCancel.setText(data.getCancelRate() + "%");
                if (tvAvgValue != null) tvAvgValue.setText(formatCurrency(data.getAvgOrderValue()));
                if (tvMaxValue != null) tvMaxValue.setText(formatCurrency(data.getMaxOrderValue()));
                setupPieChart(chart, data.getPieData());
            } else {
                if (chart != null) chart.clear();
            }
        });
    }

    private void loadProductOverviewData(TextView tvLowStockAlert, TextView tvStatTotalProducts,
                                         TextView tvStatOutOfStock, View cardLowStock, String period) {
        viewModel.getProductOverview(period).observe(getViewLifecycleOwner(), response -> {
            if (response != null && response.isStatus() && response.getData() != null) {
                StatsProductOverview data = response.getData();

                if (tvStatTotalProducts != null) tvStatTotalProducts.setText(data.getActiveCount() + " đầu sách");
                if (tvStatOutOfStock != null) tvStatOutOfStock.setText(data.getOutOfStockCount() + " sản phẩm");

                if (data.getLowStockCount() > 0) {
                    if (cardLowStock != null) cardLowStock.setVisibility(View.VISIBLE);
                    if (tvLowStockAlert != null) tvLowStockAlert.setText("Có " + data.getLowStockCount() + " sách còn dưới 10 cuốn");
                } else {
                    if (cardLowStock != null) cardLowStock.setVisibility(View.GONE);
                }

                // Cập nhật Adapter cho RecyclerView
                if (topProductAdapter != null) {
                    topProductAdapter.setList(data.getTopProducts());
                }
            }
        });
    }

    // ========================================================================
    // CẤU HÌNH BIỂU ĐỒ (VISUALS)
    // ========================================================================

    private void setupRevenueChart(LineChart chart, List<StatsRevenue.ChartData> apiData) {
        if (chart == null || apiData == null || apiData.isEmpty()) { chart.clear(); return; }
        List<Entry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        for (int i = 0; i < apiData.size(); i++) {
            StatsRevenue.ChartData data = apiData.get(i);
            entries.add(new Entry(i, data.getValue() / 1000000f));
            String fullDate = data.getDate();
            String dateLabel = fullDate.length() > 5 ? fullDate.substring(5).replace('-', '/') : fullDate;
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
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.getAxisRight().setEnabled(false);
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chart.getXAxis().setDrawGridLines(false);
        chart.getAxisLeft().setDrawGridLines(true);
        chart.getAxisLeft().setGridColor(Color.parseColor("#E0E0E0"));
        chart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        chart.getXAxis().setLabelCount(labels.size(), true);
        chart.getXAxis().setTextColor(colorGrayText);
        chart.animateY(1000);
        chart.invalidate();
    }

    private void setupPieChart(PieChart chart, List<StatsOrder.PieDataEntry> pieDataList) {
        if (chart == null) return;
        if (pieDataList == null || pieDataList.isEmpty()) { chart.clear(); chart.setCenterText("Chưa có\ndữ liệu"); chart.invalidate(); return; }
        List<PieEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();
        int totalOrders = 0;
        for (StatsOrder.PieDataEntry item : pieDataList) {
            if (item.getValue() > 0) {
                String label = item.getLabel();
                entries.add(new PieEntry(item.getValue(), label));
                colors.add(getColorForStatus(label));
                totalOrders += item.getValue();
            }
        }
        if (entries.isEmpty()) { chart.clear(); return; }
        PieDataSet dataSet = new PieDataSet(entries, "");
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
        chart.setCenterText("Tổng đơn\n" + totalOrders);
        chart.setCenterTextSize(16f);
        chart.setCenterTextColor(colorGrayText);
        chart.getLegend().setEnabled(true);
        chart.getLegend().setTextColor(colorGrayText);
        chart.setEntryLabelTextSize(0f);
        chart.animateY(1000);
        chart.invalidate();
    }

    private int getColorForStatus(String label) {
        if (label == null) return colorDefault;
        String status = label.toLowerCase();
        if (status.contains("hoàn thành") || status.contains("completed") || status.contains("success") || status.contains("delivered")) return colorCompleted;
        if (status.contains("đang giao") || status.contains("shipping") || status.contains("on the way")) return colorShipping;
        if (status.contains("đang xử lý") || status.contains("processing") || status.contains("packed")) return colorProcessing;
        if (status.contains("chờ") || status.contains("pending") || status.contains("wait")) return colorPending;
        if (status.contains("hủy") || status.contains("cancel") || status.contains("fail") || status.contains("return")) return colorCancelled;
        return colorDefault;
    }

    private String formatCurrency(long amount) {
        if (amount >= 1000000000) return String.format("%.1fB", amount / 1000000000.0);
        else if (amount >= 1000000) return String.format("%.1fTr", amount / 1000000.0);
        else return new DecimalFormat("###,###").format(amount) + "đ";
    }
}