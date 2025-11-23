package com.example.fa25_duan1.view.detail;

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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class ProductListFilterFragment extends Fragment {

    private View view;
    // Header controls
    private ConstraintLayout clFilter;
    private ExpandableLayout expandableLayout;
    private ImageView ivClose;
    private NiceSpinner spSort;

    // Toggle controls
    private TextView tvFilterLabel;
    private ImageView ivFilterIcon;

    // RecyclerView Category (Ngang)
    private RecyclerView rvCategories;

    // CheckBox Giá
    private CheckBox cbPrice0, cbPrice150, cbPrice3, cbPrice4;

    // ViewModels & Adapter
    private ProductViewModel viewModel;
    private CategoryViewModel categoryViewModel;
    private CategoryProductAdapter categoryAdapter;

    // Quản lý đa chọn Category
    private final Set<String> selectedCategoryIds = new HashSet<>();
    private List<Category> mCategoryList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Sử dụng layout gộp (đảm bảo tên file XML đúng là fragment_productfilter hoặc tên bạn đã đặt)
        view = inflater.inflate(R.layout.fragment_productfilter, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        initViewModels();
        setupCategoryRecyclerView();
        setupSpinner();
        setupEvents();
    }

    private void initViews(View view) {
        // 1. Ánh xạ các View cơ bản
        clFilter = view.findViewById(R.id.clFilter);
        expandableLayout = view.findViewById(R.id.expandable_layout);
        ivClose = view.findViewById(R.id.ivClose);
        spSort = view.findViewById(R.id.spSort);
        tvFilterLabel = view.findViewById(R.id.tv_filter);
        ivFilterIcon = view.findViewById(R.id.ivFilter);
        rvCategories = view.findViewById(R.id.rvCategories);

        // 2. Ánh xạ CheckBox Giá
        cbPrice0 = view.findViewById(R.id.cbPrice0);
        cbPrice150 = view.findViewById(R.id.cbPrice150);
        cbPrice3 = view.findViewById(R.id.cbPrice3);
        cbPrice4 = view.findViewById(R.id.cbPrice4);

        // 3. ẨN PHẦN LỌC TRẠNG THÁI (Vì đây là màn hình khách hàng)
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
        // Init Adapter với callback xử lý click
        categoryAdapter = new CategoryProductAdapter(requireContext(), new ArrayList<>(), (category, position) -> {
            handleCategoryClick(category);
        });

        // Setup LayoutManager (Ngang)
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
        rvCategories.setLayoutManager(layoutManager);
        rvCategories.setAdapter(categoryAdapter);

        // Observe Data từ ViewModel
        categoryViewModel.getDisplayedCategories().observe(getViewLifecycleOwner(), categories -> {
            if (categories != null) {
                mCategoryList.clear();

                // Tạo mục "Tất cả"
                Category allCategory = new Category("Tất cả", true);
                allCategory.setCreateAt("ALL");
                mCategoryList.add(allCategory);

                // Thêm danh sách từ API
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

        // Logic chọn/bỏ chọn
        if (cateId == null || cateId.equals("ALL")) {
            selectedCategoryIds.clear(); // Chọn tất cả -> Xóa các filter con
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
                "Mới nhất",       // 0
                "Cũ nhất",        // 1
                "Giá tăng dần",   // 2
                "Giá giảm dần"    // 3
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
        // Toggle Filter
        View.OnClickListener toggleListener = v -> toggleFilter();
        tvFilterLabel.setOnClickListener(toggleListener);
        ivFilterIcon.setOnClickListener(toggleListener);

        ivClose.setOnClickListener(v -> expandableLayout.collapse());

        // Lắng nghe sự kiện checkbox giá
        CompoundButton.OnCheckedChangeListener filterListener = (buttonView, isChecked) -> applyFilters();

        cbPrice0.setOnCheckedChangeListener(filterListener);
        cbPrice150.setOnCheckedChangeListener(filterListener);
        cbPrice3.setOnCheckedChangeListener(filterListener);
        cbPrice4.setOnCheckedChangeListener(filterListener);
    }

    private void toggleFilter() {
        if (expandableLayout.isExpanded()) {
            expandableLayout.collapse();
        } else {
            expandableLayout.expand();
        }
    }

    /**
     * Thu thập dữ liệu và gọi ViewModel
     */
    private void applyFilters() {
        // 1. Khoảng giá
        List<Integer> priceRanges = new ArrayList<>();
        if (cbPrice0.isChecked()) priceRanges.add(0);
        if (cbPrice150.isChecked()) priceRanges.add(1);
        if (cbPrice3.isChecked()) priceRanges.add(2);
        if (cbPrice4.isChecked()) priceRanges.add(3);

        // 2. Danh mục
        List<String> categoriesToFilter = new ArrayList<>(selectedCategoryIds);

        // 3. Gọi ViewModel
        // Vì đây là App khách hàng, ta HARDCODE trạng thái:
        // showSelling = true (Chỉ hiện hàng đang bán)
        // showStopped = false (Ẩn hàng ngừng kinh doanh)
        viewModel.filterProducts(true, false, priceRanges, categoriesToFilter);
    }
}