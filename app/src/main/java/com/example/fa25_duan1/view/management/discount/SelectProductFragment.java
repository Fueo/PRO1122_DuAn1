package com.example.fa25_duan1.view.management.discount;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fa25_duan1.R;
import com.example.fa25_duan1.adapter.ProductSelectionAdapter;
import com.example.fa25_duan1.viewmodel.ProductViewModel;

import java.util.ArrayList;

public class SelectProductFragment extends Fragment {
    private RecyclerView rvProducts;
    private Button btnConfirm;
    private CheckBox cbSelectAll;

    private ProductViewModel productViewModel;
    private ProductSelectionAdapter adapter;
    private ArrayList<String> currentSelectedIds;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_select_product, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);

        // --- 1. NHẬN DỮ LIỆU TỪ INTENT ---
        // Vì Activity mở Activity, nên dữ liệu nằm trong Intent của Activity chứa Fragment này
        if (getActivity().getIntent() != null) {
            currentSelectedIds = getActivity().getIntent().getStringArrayListExtra("current_selected_ids");
        }

        // Phòng hờ nếu ManageActivity chuyển dữ liệu vào Bundle arguments
        if (currentSelectedIds == null && getArguments() != null) {
            currentSelectedIds = getArguments().getStringArrayList("current_selected_ids");
        }

        if (currentSelectedIds == null) currentSelectedIds = new ArrayList<>();

        productViewModel = new ViewModelProvider(requireActivity()).get(ProductViewModel.class);
        setupRecyclerView();
        observeData();
        setupEvents();
        productViewModel.resetToAllProducts();
    }

    private void initViews(View view) {
        rvProducts = view.findViewById(R.id.rvProductSelection);
        btnConfirm = view.findViewById(R.id.btnConfirmSelection);
        cbSelectAll = view.findViewById(R.id.cbSelectAll);
    }

    private void setupRecyclerView() {
        adapter = new ProductSelectionAdapter(getContext(), (count, selectedIds) -> {
            btnConfirm.setText("Xác nhận (" + count + ")");
            if (adapter.getItemCount() > 0 && count < adapter.getItemCount()) {
                cbSelectAll.setOnCheckedChangeListener(null);
                cbSelectAll.setChecked(false);
                setupSelectAllEvent();
            } else if (adapter.getItemCount() > 0 && count == adapter.getItemCount()) {
                cbSelectAll.setChecked(true);
            }
        });

        adapter.setSelectedIds(currentSelectedIds);
        rvProducts.setLayoutManager(new LinearLayoutManager(getContext()));
        rvProducts.setAdapter(adapter);
    }

    private void observeData() {
        productViewModel.getDisplayedProducts().observe(getViewLifecycleOwner(), products -> {
            if (products != null) {
                adapter.setList(products);
                if (!products.isEmpty() && adapter.getSelectedProductIds().size() == products.size()) {
                    cbSelectAll.setChecked(true);
                } else {
                    cbSelectAll.setChecked(false);
                }
            }
        });
    }

    private void setupEvents() {
        setupSelectAllEvent();

        // --- 2. TRẢ KẾT QUẢ VỀ KHI BẤM XÁC NHẬN ---
        btnConfirm.setOnClickListener(v -> {
            ArrayList<String> resultIds = (ArrayList<String>) adapter.getSelectedProductIds();

            // Tạo Intent chứa dữ liệu kết quả
            Intent resultIntent = new Intent();
            resultIntent.putStringArrayListExtra("selected_ids", resultIds);

            // Set Result cho Activity chứa Fragment này
            if (getActivity() != null) {
                getActivity().setResult(Activity.RESULT_OK, resultIntent);
                getActivity().finish(); // Đóng ManageActivity để quay về DiscountUpdate
            }
        });
    }

    private void setupSelectAllEvent() {
        cbSelectAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            adapter.toggleSelectAll(isChecked);
        });
    }
}