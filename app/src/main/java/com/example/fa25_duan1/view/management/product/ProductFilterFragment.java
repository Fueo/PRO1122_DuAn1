package com.example.fa25_duan1.view.management.product;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.example.fa25_duan1.viewmodel.AuthorViewModel;
import com.example.fa25_duan1.viewmodel.CategoryViewModel;
import com.example.fa25_duan1.viewmodel.ProductViewModel;

import net.cachapa.expandablelayout.ExpandableLayout;

import org.angmarch.views.NiceSpinner;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ProductFilterFragment extends Fragment {
    private ImageView ivClose;
    private ConstraintLayout clFilter;
    private ExpandableLayout expandableLayout;
    private NiceSpinner spSort;
    RecyclerView rvFilterOption;
    ProductViewModel viewModel;
    CategoryViewModel categoryViewModel;
    ProductFilterAdapter productFilterAdapter;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_productfilter, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        clFilter = view.findViewById(R.id.clFilter);
        ivClose = view.findViewById(R.id.ivClose);
        expandableLayout = view.findViewById(R.id.expandable_layout);
        spSort = view.findViewById(R.id.spSort);
        rvFilterOption = view.findViewById(R.id.rvFilterOption);
        viewModel = new ViewModelProvider(requireActivity()).get(ProductViewModel.class);
        categoryViewModel = new ViewModelProvider(requireActivity()).get(CategoryViewModel.class);

        clFilter.setOnClickListener(v -> {
            expandableLayout.toggle();
        });
        ivClose.setOnClickListener(v -> {
            expandableLayout.collapse();
        });

        productFilterAdapter = new ProductFilterAdapter(requireActivity(), new LinkedList<>() , new ProductFilterAdapter.OnCategorySelectionListener() {
            @Override
            public void onCategorySelected(Category category, boolean isChecked) {

            }
        });

        GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), 2);
        rvFilterOption.setAdapter(productFilterAdapter);
        rvFilterOption.setLayoutManager(layoutManager);

        categoryViewModel.getDisplayedCategories().observe(getViewLifecycleOwner(), categories -> {
            if (categories != null && !categories.isEmpty()) {
                categories.add(0,new Category("Chưa có danh mục","0",null, false));

                // Dữ liệu đã có, cập nhật Adapter
                productFilterAdapter.setData(categories);
                // productFilterAdapter.notifyDataSetChanged() được gọi bên trong setData()
            }
        });

        LinkedList<String> data = new LinkedList<>(Arrays.asList(
                "Mới nhất", "Cũ nhất"));
        spSort.attachDataSource(data);
        spSort.setOnSpinnerItemSelectedListener((parent, v, position, id) -> {
            switch (position) {
                case 0: viewModel.sortByCreateAt(true); break;
                case 1: viewModel.sortByCreateAt(false); break;
            }
        });
    }
}