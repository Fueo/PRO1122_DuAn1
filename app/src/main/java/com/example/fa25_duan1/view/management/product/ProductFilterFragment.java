package com.example.fa25_duan1.view.management.product;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fa25_duan1.R;
import com.example.fa25_duan1.adapter.CategoryProductAdapter;
import com.example.fa25_duan1.model.Category;
import com.example.fa25_duan1.viewmodel.CategoryViewModel;
import com.example.fa25_duan1.viewmodel.ProductViewModel;
import com.google.android.material.slider.RangeSlider;

import net.cachapa.expandablelayout.ExpandableLayout;

import org.angmarch.views.NiceSpinner;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class ProductFilterFragment extends Fragment {

    private View view;
    // Header controls
    private ConstraintLayout clFilter;
    private ExpandableLayout expandableLayout;
    private ImageView ivClose;
    private NiceSpinner spSort;

    // Toggle controls
    private TextView tvFilterLabel;
    private ImageView ivFilterIcon;

    // List Category (Ngang)
    private RecyclerView rvCategories;

    // CheckBox Trạng thái
    private CheckBox cbSelling, cbNotSelling;

    // CheckBox Tồn kho (MỚI)
    private CheckBox cbLowStock;

    // Range Slider
    private RangeSlider rsPrice;
    private TextView tvMinPrice, tvMaxPrice;

    // ViewModels & Adapter
    private ProductViewModel viewModel;
    private CategoryViewModel categoryViewModel;
    private CategoryProductAdapter categoryAdapter;
    private boolean pendingLowStockFilter = false;

    // Quản lý đa chọn Category
    private final Set<String> selectedCategoryIds = new HashSet<>();
    private List<Category> mCategoryList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_productfilter, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        initViewModels(); // Observer nằm trong này
        setupCategoryRecyclerView();
        setupSpinner();
        setupEvents();


// 2. KIỂM TRA INTENT VÀ BẬT CỜ
        if (getActivity() != null && getActivity().getIntent() != null) {
            boolean isFilterRequest = getActivity().getIntent().getBooleanExtra("FILTER_LOW_STOCK", false);

            if (isFilterRequest) {
                pendingLowStockFilter = true; // Đánh dấu: Cần lọc ngay khi data về

                // Tích sẵn vào checkbox (nhưng chưa lọc được vì data chưa về)
                if (cbLowStock != null) cbLowStock.setChecked(true);

                // Xóa extra để không bị lọc lại khi xoay màn hình
                getActivity().getIntent().removeExtra("FILTER_LOW_STOCK");
            }
        }

        // 3. LUÔN GỌI REFRESH DATA (Để lấy dữ liệu gốc về trước)
        viewModel.refreshData();
    }

    private void initViews(View view) {
        clFilter = view.findViewById(R.id.clFilter);
        expandableLayout = view.findViewById(R.id.expandable_layout);
        ivClose = view.findViewById(R.id.ivClose);
        spSort = view.findViewById(R.id.spSort);
        tvFilterLabel = view.findViewById(R.id.tv_filter);
        ivFilterIcon = view.findViewById(R.id.ivFilter);

        rvCategories = view.findViewById(R.id.rvCategories);

        cbSelling = view.findViewById(R.id.cbSelling);
        cbNotSelling = view.findViewById(R.id.cbNotSelling);

        // Map checkbox Tồn kho
        cbLowStock = view.findViewById(R.id.cbLowStock);

        rsPrice = view.findViewById(R.id.rsPrice);
        tvMinPrice = view.findViewById(R.id.tvMinPrice);
        tvMaxPrice = view.findViewById(R.id.tvMaxPrice);

        if (cbSelling != null) cbSelling.setChecked(true);
        if (cbNotSelling != null) cbNotSelling.setChecked(false);
    }

    private void initViewModels() {
        viewModel = new ViewModelProvider(requireActivity()).get(ProductViewModel.class);
        categoryViewModel = new ViewModelProvider(requireActivity()).get(CategoryViewModel.class);

        // 4. LẮNG NGHE DỮ LIỆU VỀ
        viewModel.getDisplayedProducts().observe(getViewLifecycleOwner(), products -> {
            // Khi có dữ liệu mới về và danh sách không rỗng
            if (products != null && !products.isEmpty()) {

                // Kiểm tra xem có cờ "Chờ lọc" không
                if (pendingLowStockFilter) {
                    applyFilters();
                    pendingLowStockFilter = false;
                }
            }
        });
    }

    private void setupCategoryRecyclerView() {
        categoryAdapter = new CategoryProductAdapter(requireContext(), new ArrayList<>(), (category, position) -> {
            handleCategoryClick(category);
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
        rvCategories.setLayoutManager(layoutManager);
        rvCategories.setAdapter(categoryAdapter);

        categoryViewModel.getDisplayedCategories().observe(getViewLifecycleOwner(), categories -> {
            if (categories != null) {
                mCategoryList.clear();
                Category allCategory = new Category("Tất cả", true);
                allCategory.setCateID("ALL");
                mCategoryList.add(allCategory);

                for (Category c : categories) {
                    c.setSelected(false);
                    mCategoryList.add(c);
                }
                categoryAdapter.setCategoryList(mCategoryList);
            }
        });
    }

    private void handleCategoryClick(Category clickedCategory) {
        String cateId = clickedCategory.get_id();
        if (cateId == null || cateId.equals("ALL")) {
            selectedCategoryIds.clear();
        } else {
            if (selectedCategoryIds.contains(cateId)) {
                selectedCategoryIds.remove(cateId);
            } else {
                selectedCategoryIds.add(cateId);
            }
        }
        updateCategorySelectionUI();
        applyFilters();
    }

    private void updateCategorySelectionUI() {
        boolean isAllSelected = selectedCategoryIds.isEmpty();
        for (Category c : mCategoryList) {
            String id = c.get_id();
            if (id == null || id.equals("ALL")) {
                c.setSelected(isAllSelected);
            } else {
                c.setSelected(selectedCategoryIds.contains(id));
            }
        }
        categoryAdapter.notifyDataSetChanged();
    }

    private void setupSpinner() {
        LinkedList<String> data = new LinkedList<>(Arrays.asList(
                "Mới nhất", "Cũ nhất", "Giá tăng dần", "Giá giảm dần"
        ));
        spSort.attachDataSource(data);
        spSort.setOnSpinnerItemSelectedListener((parent, v, position, id) -> {
            switch (position) {
                case 0: viewModel.sortProducts(ProductViewModel.SORT_DATE_NEWEST); break;
                case 1: viewModel.sortProducts(ProductViewModel.SORT_DATE_OLDEST); break;
                case 2: viewModel.sortProducts(ProductViewModel.SORT_PRICE_ASC); break;
                case 3: viewModel.sortProducts(ProductViewModel.SORT_PRICE_DESC); break;
            }
        });
    }

    private void setupEvents() {
        View.OnClickListener toggleListener = v -> toggleFilter();
        tvFilterLabel.setOnClickListener(toggleListener);
        ivFilterIcon.setOnClickListener(toggleListener);
        ivClose.setOnClickListener(v -> expandableLayout.collapse());

        CompoundButton.OnCheckedChangeListener filterListener = (buttonView, isChecked) -> applyFilters();
        if (cbSelling != null) cbSelling.setOnCheckedChangeListener(filterListener);
        if (cbNotSelling != null) cbNotSelling.setOnCheckedChangeListener(filterListener);

        // Lắng nghe sự kiện checkbox Tồn kho
        if (cbLowStock != null) cbLowStock.setOnCheckedChangeListener(filterListener);

        rsPrice.setValues(20000f, 500000f);
        rsPrice.addOnChangeListener((slider, value, fromUser) -> {
            List<Float> values = slider.getValues();
            tvMinPrice.setText(formatCurrency(values.get(0)));
            tvMaxPrice.setText(formatCurrency(values.get(1)));
            applyFilters();
        });
    }

    private void toggleFilter() {
        if (expandableLayout.isExpanded()) expandableLayout.collapse();
        else expandableLayout.expand();
    }

    private void applyFilters() {
        boolean showSelling = (cbSelling != null) && cbSelling.isChecked();
        boolean showStopped = (cbNotSelling != null) && cbNotSelling.isChecked();
        if (!showSelling && !showStopped) { showSelling = true; showStopped = true; }

        // Lấy giá trị checkbox Tồn kho
        boolean showLowStock = (cbLowStock != null) && cbLowStock.isChecked();

        List<Float> values = rsPrice.getValues();
        float minPrice = values.get(0);
        float maxPrice = values.get(1);

        List<String> categoriesToFilter = new ArrayList<>(selectedCategoryIds);

        // Gọi ViewModel với tham số mới
        // showLowStock: true/false
        // filterByOriginalPrice: true (Admin luôn lọc theo giá gốc)
        viewModel.filterProducts(showSelling, showStopped, showLowStock, minPrice, maxPrice, true, categoriesToFilter);
    }

    private String formatCurrency(float amount) {
        DecimalFormat formatter = new DecimalFormat("###,###");
        return formatter.format(amount) + "đ";
    }
}