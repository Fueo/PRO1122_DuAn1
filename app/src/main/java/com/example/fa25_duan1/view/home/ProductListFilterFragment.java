package com.example.fa25_duan1.view.home;

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
import java.util.LinkedList;
import java.util.List;

public class ProductListFilterFragment extends Fragment {

    private View view;

    // --- Views ---
    private ConstraintLayout clFilter;
    private ExpandableLayout expandableLayout;
    private ImageView ivClose;
    private NiceSpinner spSort;
    private TextView tvFilterLabel;
    private ImageView ivFilterIcon;
    private RecyclerView rvCategories;

    // --- Filter Views MỚI ---
    private RangeSlider rsPrice;
    private TextView tvMinPrice, tvMaxPrice;

    // CheckBox (Sẽ ẩn đi)
    private CheckBox cbSelling, cbNotSelling;

    // --- ViewModels ---
    private ProductViewModel viewModel;
    private CategoryViewModel categoryViewModel;
    private CategoryProductAdapter categoryAdapter;

    // --- State ---
    private String currentSelectedCategoryId = "ALL";
    private List<Category> mCategoryList = new ArrayList<>();
    private boolean isCategorySwitching = false;

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
        initViewModels();

        if (getArguments() != null && getArguments().containsKey("category_id")) {
            String passedId = getArguments().getString("category_id");
            if (passedId != null && !passedId.isEmpty()) {
                currentSelectedCategoryId = passedId;
            }
        }

        loadInitialData();
        setupCategoryRecyclerView();
        setupSpinner();
        setupEvents();
    }

    private void initViews(View view) {
        clFilter = view.findViewById(R.id.clFilter);
        expandableLayout = view.findViewById(R.id.expandable_layout);
        ivClose = view.findViewById(R.id.ivClose);
        spSort = view.findViewById(R.id.spSort);
        tvFilterLabel = view.findViewById(R.id.tv_filter);
        ivFilterIcon = view.findViewById(R.id.ivFilter);
        rvCategories = view.findViewById(R.id.rvCategories);

        // Map RangeSlider
        rsPrice = view.findViewById(R.id.rsPrice);
        tvMinPrice = view.findViewById(R.id.tvMinPrice);
        tvMaxPrice = view.findViewById(R.id.tvMaxPrice);

        // Map CheckBox (để tránh null pointer nếu XML vẫn còn)
        cbSelling = view.findViewById(R.id.cbSelling);
        cbNotSelling = view.findViewById(R.id.cbNotSelling);

        // --- ẨN CÁC SECTION KHÔNG DÀNH CHO KHÁCH HÀNG ---

        // 1. Ẩn Trạng thái (Status)
        View tvStatusLabel = view.findViewById(R.id.tvCondition);
        View layoutStatus = view.findViewById(R.id.layout_status);
        if (tvStatusLabel != null) tvStatusLabel.setVisibility(View.GONE);
        if (layoutStatus != null) layoutStatus.setVisibility(View.GONE);

        // 2. Ẩn Tồn kho (Inventory) - MỚI
        View tvInventoryLabel = view.findViewById(R.id.tvCondition3);
        View layoutInventory = view.findViewById(R.id.layout_inventory);
        if (tvInventoryLabel != null) tvInventoryLabel.setVisibility(View.GONE);
        if (layoutInventory != null) layoutInventory.setVisibility(View.GONE);
    }

    private void initViewModels() {
        viewModel = new ViewModelProvider(requireActivity()).get(ProductViewModel.class);
        categoryViewModel = new ViewModelProvider(requireActivity()).get(CategoryViewModel.class);

        viewModel.getDisplayedProducts().observe(getViewLifecycleOwner(), products -> {
            if (isCategorySwitching) {
                isCategorySwitching = false;
                applyFilters();
            }
        });
    }

    private void loadInitialData() {
        if ("ALL".equals(currentSelectedCategoryId)) {
            viewModel.refreshData();
        } else if ("SALE".equals(currentSelectedCategoryId)) {
            viewModel.filterOnSaleProductsApi();
        } else {
            viewModel.filterProductsByCategoryApi(currentSelectedCategoryId);
        }
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

                Category saleCategory = new Category("Giảm giá", false);
                saleCategory.setCateID("SALE");
                mCategoryList.add(saleCategory);

                for (Category c : categories) {
                    c.setSelected(false);
                    mCategoryList.add(c);
                }
                updateCategorySelectionUI();
            }
        });
    }

    private void handleCategoryClick(Category clickedCategory) {
        String cateId = clickedCategory.get_id();
        if (cateId == null) cateId = "ALL";

        if (currentSelectedCategoryId.equals(cateId)) return;

        currentSelectedCategoryId = cateId;
        updateCategorySelectionUI();

        isCategorySwitching = true;

        if (currentSelectedCategoryId.equals("ALL")) {
            viewModel.refreshData();
        } else if (currentSelectedCategoryId.equals("SALE")) {
            viewModel.filterOnSaleProductsApi();
        } else {
            viewModel.filterProductsByCategoryApi(currentSelectedCategoryId);
        }
    }

    private void updateCategorySelectionUI() {
        int selectedPosition = 0;
        for (int i = 0; i < mCategoryList.size(); i++) {
            Category c = mCategoryList.get(i);
            String id = c.get_id();
            if (id == null) id = "ALL";

            if (id.equals(currentSelectedCategoryId)) {
                c.setSelected(true);
                selectedPosition = i;
            } else {
                c.setSelected(false);
            }
        }
        categoryAdapter.setCategoryList(mCategoryList);
        rvCategories.smoothScrollToPosition(selectedPosition);
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

        // RangeSlider (Giá)
        rsPrice.setValues(20000f, 500000f);
        rsPrice.addOnChangeListener((slider, value, fromUser) -> {
            List<Float> values = slider.getValues();
            float min = values.get(0);
            float max = values.get(1);

            tvMinPrice.setText(formatCurrency(min));
            tvMaxPrice.setText(formatCurrency(max));

            applyFilters();
        });

        // Không cần lắng nghe CheckBox nữa vì đã ẩn
    }

    private void toggleFilter() {
        if (expandableLayout.isExpanded()) expandableLayout.collapse();
        else expandableLayout.expand();
    }

    private void applyFilters() {
        // 1. Lấy khoảng giá từ Slider
        List<Float> values = rsPrice.getValues();
        float minPrice = values.get(0);
        float maxPrice = values.get(1);

        // 2. Cài đặt cứng các thông số không dành cho Client
        boolean showSelling = true;     // Luôn hiện hàng đang bán
        boolean showStopped = false;    // Ẩn hàng ngừng bán
        boolean showLowStock = false;   // Không lọc theo tồn kho (hiện tất cả)

        List<String> emptyCategories = new ArrayList<>();

        // 3. Gọi ViewModel với logic mới (7 tham số)
        // Tham số filterByOriginalPrice = false -> Lọc theo giá đã giảm
        viewModel.filterProducts(showSelling, showStopped, showLowStock, minPrice, maxPrice, false, emptyCategories);
    }

    private String formatCurrency(float amount) {
        DecimalFormat formatter = new DecimalFormat("###,###");
        return formatter.format(amount) + "đ";
    }
}