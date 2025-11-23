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

import net.cachapa.expandablelayout.ExpandableLayout;

import org.angmarch.views.NiceSpinner;

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

    // Toggle controls (để bấm vào mở filter)
    private TextView tvFilterLabel;
    private ImageView ivFilterIcon;

    // List Category (Ngang)
    private RecyclerView rvCategories;

    // CheckBox Trạng thái
    private CheckBox cbSelling, cbNotSelling;

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
        // Đảm bảo file layout của bạn tên là fragment_productfilter (hoặc tên file XML bạn đã gộp)
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
        // Header & Layout
        clFilter = view.findViewById(R.id.clFilter);
        expandableLayout = view.findViewById(R.id.expandable_layout);
        ivClose = view.findViewById(R.id.ivClose);
        spSort = view.findViewById(R.id.spSort);
        tvFilterLabel = view.findViewById(R.id.tv_filter);
        ivFilterIcon = view.findViewById(R.id.ivFilter);

        // RecyclerView Category
        rvCategories = view.findViewById(R.id.rvCategories);

        // CheckBox Trạng thái
        cbSelling = view.findViewById(R.id.cbSelling);
        cbNotSelling = view.findViewById(R.id.cbNotSelling);

        // CheckBox Giá
        cbPrice0 = view.findViewById(R.id.cbPrice0);
        cbPrice150 = view.findViewById(R.id.cbPrice150);
        cbPrice3 = view.findViewById(R.id.cbPrice3);
        cbPrice4 = view.findViewById(R.id.cbPrice4);

        // Mặc định chọn hiển thị sản phẩm "Đang bán"
        cbSelling.setChecked(true);
        // Tùy logic: ban đầu có muốn hiện hàng ngừng bán ko? Nếu có thì set true
        cbNotSelling.setChecked(false);
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

                // 1. Tạo mục "Tất cả"
                Category allCategory = new Category("Tất cả", true);
                allCategory.setCreateAt("ALL"); // ID định danh cho mục Tất cả
                mCategoryList.add(allCategory);

                // 2. Thêm danh sách từ API
                for (Category c : categories) {
                    c.setSelected(false); // Reset trạng thái chọn
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

        // Cập nhật UI (Highlight item được chọn)
        updateCategorySelectionUI();

        // Gọi hàm lọc
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
        // Toggle Filter: Click vào chữ hoặc icon đều mở
        View.OnClickListener toggleListener = v -> toggleFilter();
        tvFilterLabel.setOnClickListener(toggleListener);
        ivFilterIcon.setOnClickListener(toggleListener);

        // Nút đóng
        ivClose.setOnClickListener(v -> expandableLayout.collapse());

        // Lắng nghe sự kiện checkbox thay đổi -> Lọc ngay lập tức
        CompoundButton.OnCheckedChangeListener filterListener = (buttonView, isChecked) -> applyFilters();

        cbSelling.setOnCheckedChangeListener(filterListener);
        cbNotSelling.setOnCheckedChangeListener(filterListener);

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
     * Thu thập toàn bộ điều kiện từ UI và gửi cho ViewModel
     */
    private void applyFilters() {
        // 1. Lấy Trạng thái
        boolean showSelling = cbSelling.isChecked();
        boolean showStopped = cbNotSelling.isChecked();

        // 2. Lấy Khoảng giá
        List<Integer> priceRanges = new ArrayList<>();
        if (cbPrice0.isChecked()) priceRanges.add(0);
        if (cbPrice150.isChecked()) priceRanges.add(1);
        if (cbPrice3.isChecked()) priceRanges.add(2);
        if (cbPrice4.isChecked()) priceRanges.add(3);

        // 3. Lấy Danh mục (Chuyển Set -> List)
        List<String> categoriesToFilter = new ArrayList<>(selectedCategoryIds);

        // 4. Gửi yêu cầu lọc
        viewModel.filterProducts(showSelling, showStopped, priceRanges, categoriesToFilter);
    }
}