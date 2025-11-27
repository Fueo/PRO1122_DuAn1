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

import net.cachapa.expandablelayout.ExpandableLayout;
import org.angmarch.views.NiceSpinner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ProductListFilterFragment extends Fragment {

    private View view;
    private ConstraintLayout clFilter;
    private ExpandableLayout expandableLayout;
    private ImageView ivClose;
    private NiceSpinner spSort;
    private TextView tvFilterLabel;
    private ImageView ivFilterIcon;
    private RecyclerView rvCategories;

    private CheckBox cbPrice0, cbPrice150, cbPrice3, cbPrice4;

    private ProductViewModel viewModel;
    private CategoryViewModel categoryViewModel;
    private CategoryProductAdapter categoryAdapter;

    private String currentSelectedCategoryId = "ALL";
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
        initViewModels();

        // 1. Nhận ID
        if (getArguments() != null && getArguments().containsKey("category_id")) {
            String passedId = getArguments().getString("category_id");
            if (passedId != null && !passedId.isEmpty()) {
                currentSelectedCategoryId = passedId;
            }
        }

        // 2. Gọi API
        if ("ALL".equals(currentSelectedCategoryId)) {
            viewModel.refreshData();
        } else {
            viewModel.filterProductsByCategoryApi(currentSelectedCategoryId);
        }

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

        cbPrice0 = view.findViewById(R.id.cbPrice0);
        cbPrice150 = view.findViewById(R.id.cbPrice150);
        cbPrice3 = view.findViewById(R.id.cbPrice3);
        cbPrice4 = view.findViewById(R.id.cbPrice4);

        View tvStatusLabel = view.findViewById(R.id.tvCondition);
        View layoutStatus = view.findViewById(R.id.layout_status);
        if (tvStatusLabel != null) tvStatusLabel.setVisibility(View.GONE);
        if (layoutStatus != null) layoutStatus.setVisibility(View.GONE);
    }

    private void initViewModels() {
        viewModel = new ViewModelProvider(requireActivity()).get(ProductViewModel.class);
        categoryViewModel = new ViewModelProvider(requireActivity()).get(CategoryViewModel.class);
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

                // 1. Tạo mục "Tất cả"
                Category allCategory = new Category("Tất cả", true);
                allCategory.setCreateAt("ALL");

                // --- SỬA: Dùng setCateID ---
                allCategory.setCateID("ALL");
                // --------------------------

                mCategoryList.add(allCategory);

                // 2. Thêm danh sách API
                for (Category c : categories) {
                    c.setSelected(false);
                    mCategoryList.add(c);
                }

                updateCategorySelectionUI();
            }
        });
    }

    private void handleCategoryClick(Category clickedCategory) {
        // --- SỬA: Dùng getCateID ---
        String cateId = clickedCategory.get_id();
        // --------------------------

        if (cateId == null) cateId = "ALL";

        if (currentSelectedCategoryId.equals(cateId)) return;

        currentSelectedCategoryId = cateId;
        updateCategorySelectionUI();

        if (currentSelectedCategoryId.equals("ALL")) {
            viewModel.refreshData();
        } else {
            viewModel.filterProductsByCategoryApi(currentSelectedCategoryId);
        }

        applyFilters();
    }

    private void updateCategorySelectionUI() {
        int selectedPosition = 0;
        for (int i = 0; i < mCategoryList.size(); i++) {
            Category c = mCategoryList.get(i);

            // --- SỬA: Dùng getCateID ---
            String id = c.get_id();
            // --------------------------

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

        CompoundButton.OnCheckedChangeListener filterListener = (buttonView, isChecked) -> applyFilters();
        cbPrice0.setOnCheckedChangeListener(filterListener);
        cbPrice150.setOnCheckedChangeListener(filterListener);
        cbPrice3.setOnCheckedChangeListener(filterListener);
        cbPrice4.setOnCheckedChangeListener(filterListener);
    }

    private void toggleFilter() {
        if (expandableLayout.isExpanded()) expandableLayout.collapse();
        else expandableLayout.expand();
    }

    private void applyFilters() {
        List<Integer> priceRanges = new ArrayList<>();
        if (cbPrice0.isChecked()) priceRanges.add(0);
        if (cbPrice150.isChecked()) priceRanges.add(1);
        if (cbPrice3.isChecked()) priceRanges.add(2);
        if (cbPrice4.isChecked()) priceRanges.add(3);

        List<String> emptyCategories = new ArrayList<>();
        viewModel.filterProducts(true, false, priceRanges, emptyCategories);
    }
}