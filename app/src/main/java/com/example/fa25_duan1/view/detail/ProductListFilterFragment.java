package com.example.fa25_duan1.view.detail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fa25_duan1.R;
import com.example.fa25_duan1.adapter.ProductFilterAdapter;
import com.example.fa25_duan1.model.Category;
import com.example.fa25_duan1.view.management.product.ProductFilterFragment;
import com.example.fa25_duan1.viewmodel.CategoryViewModel;
import com.example.fa25_duan1.viewmodel.ProductViewModel;

import net.cachapa.expandablelayout.ExpandableLayout;

import org.angmarch.views.NiceSpinner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ProductListFilterFragment extends Fragment {
    private ImageView ivClose;
    private ConstraintLayout clFilter;
    private ExpandableLayout expandableLayout;
    private NiceSpinner spSort;
    private RecyclerView rvFilterOption;

    // Khai báo các CheckBox
    private CheckBox cbSelling, cbNotSelling;
    private CheckBox cbPrice0, cbPrice150, cbPrice3, cbPrice4;

    private ProductViewModel viewModel;
    private CategoryViewModel categoryViewModel;
    private ProductFilterAdapter productFilterAdapter;

    // List lưu các category ID đang được chọn
    private final List<String> selectedCategoryIds = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_productlistfilter, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Ánh xạ Views
        clFilter = view.findViewById(R.id.clFilter);
        ivClose = view.findViewById(R.id.ivClose);
        expandableLayout = view.findViewById(R.id.expandable_layout);
        spSort = view.findViewById(R.id.spSort);
        rvFilterOption = view.findViewById(R.id.rvFilterOption);

        // CheckBox Giá
        cbPrice0 = view.findViewById(R.id.cbPrice0);
        cbPrice150 = view.findViewById(R.id.cbPrice150);
        cbPrice3 = view.findViewById(R.id.cbPrice3);
        cbPrice4 = view.findViewById(R.id.cbPrice4);

        viewModel = new ViewModelProvider(requireActivity()).get(ProductViewModel.class);
        categoryViewModel = new ViewModelProvider(requireActivity()).get(CategoryViewModel.class);

        // 2. Sự kiện đóng mở Filter
        clFilter.setOnClickListener(v -> expandableLayout.toggle());
        ivClose.setOnClickListener(v -> expandableLayout.collapse());

        // 3. Adapter Category Filter
        productFilterAdapter = new ProductFilterAdapter(requireActivity(), new LinkedList<>(), new ProductFilterAdapter.OnCategorySelectionListener() {
            @Override
            public void onCategorySelected(Category category, boolean isChecked) {
                if (isChecked) {
                    selectedCategoryIds.add(category.get_id());
                } else {
                    selectedCategoryIds.remove(category.get_id());
                }
                // Gọi lọc ngay khi chọn danh mục
                applyFilters();
            }
        });

        GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), 2);
        rvFilterOption.setAdapter(productFilterAdapter);
        rvFilterOption.setLayoutManager(layoutManager);

        categoryViewModel.getDisplayedCategories().observe(getViewLifecycleOwner(), categories -> {
            if (categories != null) {
                // Thêm mục "Chưa có danh mục" với ID = "0"
                // Lưu ý: Clone list để tránh lỗi ConcurrentModification nếu list là LiveData gốc
                List<Category> filterList = new ArrayList<>(categories);
                filterList.add(0, new Category("Chưa có danh mục", "0", null, false));

                productFilterAdapter.setData(filterList);
            }
        });

        // 4. Setup Spinner Sort
        LinkedList<String> data = new LinkedList<>(Arrays.asList("Mới nhất", "Cũ nhất"));
        spSort.attachDataSource(data);
        spSort.setOnSpinnerItemSelectedListener((parent, v, position, id) -> {
            switch (position) {
                case 0: viewModel.sortByCreateAt(true); break;
                case 1: viewModel.sortByCreateAt(false); break;
            }
        });

        // 5. Setup Listener cho các CheckBox (Lọc ngay khi bấm)
        CompoundButton.OnCheckedChangeListener filterListener = (buttonView, isChecked) -> applyFilters();
        cbPrice0.setOnCheckedChangeListener(filterListener);
        cbPrice150.setOnCheckedChangeListener(filterListener);
        cbPrice3.setOnCheckedChangeListener(filterListener);
        cbPrice4.setOnCheckedChangeListener(filterListener);
    }

    /**
     * Thu thập dữ liệu từ UI và gọi ViewModel để lọc
     */
    private void applyFilters() {
        // 1. Trạng thái
        boolean showSelling = cbSelling.isChecked();
        boolean showStopped = cbNotSelling.isChecked();

        // 2. Giá (Tạo list các mã range)
        List<Integer> priceRanges = new ArrayList<>();
        if (cbPrice0.isChecked()) priceRanges.add(0);     // 0 - 150k
        if (cbPrice150.isChecked()) priceRanges.add(1);   // 150k - 300k
        if (cbPrice3.isChecked()) priceRanges.add(2);     // 300k - 500k
        if (cbPrice4.isChecked()) priceRanges.add(3);     // > 500k

        // 3. Gọi ViewModel
        // selectedCategoryIds đã được cập nhật trong adapter listener
        viewModel.filterProducts(showSelling, showStopped, priceRanges, selectedCategoryIds);
    }
}