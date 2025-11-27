package com.example.fa25_duan1.view.management.discount;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fa25_duan1.R;
import com.example.fa25_duan1.adapter.DiscountManageAdapter;
import com.example.fa25_duan1.model.Discount;
import com.example.fa25_duan1.view.dialog.ConfirmDialogFragment;
import com.example.fa25_duan1.view.management.UpdateActivity;
import com.example.fa25_duan1.viewmodel.DiscountViewModel;

import java.util.ArrayList;
import java.util.List;

public class DiscountManageFragment extends Fragment {
    private View layout_empty;
    private RecyclerView rvData;
    private Button btnAdd;
    private DiscountManageAdapter adapter;
    private DiscountViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Dùng layout fragment_discount_management.xml bạn đã cung cấp
        return inflater.inflate(R.layout.fragment_discount_management, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Load Filter Fragment
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_filter, new DiscountFilterFragment())
                .commit();

        rvData = view.findViewById(R.id.rvDiscountData);
        btnAdd = view.findViewById(R.id.btnAddDiscount);
        layout_empty = view.findViewById(R.id.layout_empty);

        // Setup Adapter
        adapter = new DiscountManageAdapter(getContext(), new ArrayList<>(), new DiscountManageAdapter.OnDiscountActionListener() {
            @Override
            public void onEditClick(Discount discount) {
                openUpdateActivity(discount);
            }

            @Override
            public void onDeleteClick(Discount discount) {
                deleteDiscount(discount);
            }
        });

        rvData.setLayoutManager(new LinearLayoutManager(getContext()));
        rvData.setAdapter(adapter);

        // ViewModel setup
        viewModel = new ViewModelProvider(requireActivity()).get(DiscountViewModel.class);

        // Load data lần đầu (nếu cần)
        viewModel.refreshData();

        viewModel.getDisplayedDiscounts().observe(getViewLifecycleOwner(), discounts -> {
            if (discounts != null) {
                adapter.setData(discounts);
                checkEmptyState(discounts);
            }
        });

        btnAdd.setOnClickListener(v -> openUpdateActivity(null));
    }

    private void openUpdateActivity(Discount discount) {
        Intent intent = new Intent(getContext(), UpdateActivity.class);
        intent.putExtra(UpdateActivity.EXTRA_HEADER_TITLE, "Thêm mã giảm giá");

        if (discount != null) {
            intent.putExtra(UpdateActivity.EXTRA_HEADER_TITLE, "Chỉnh sửa mã giảm giá");
            intent.putExtra("Id", discount.get_id());
        }
        // Key quan trọng để UpdateActivity biết load DiscountUpdateFragment
        intent.putExtra(UpdateActivity.EXTRA_CONTENT_FRAGMENT, "discount");

        startActivityForResult(intent, 2002);
    }

    private void deleteDiscount(Discount discount) {
        ConfirmDialogFragment dialog = new ConfirmDialogFragment(
                "Xóa mã giảm giá",
                "Bạn có chắc muốn xóa: " + discount.getDiscountName() + "?",
                new ConfirmDialogFragment.OnConfirmListener() {
                    @Override
                    public void onConfirmed() {
                        viewModel.deleteDiscount(discount.get_id()).observe(getViewLifecycleOwner(), success -> {
                            if (success) {
                                viewModel.refreshData();
                                Toast.makeText(getContext(), "Đã xóa thành công", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "Xóa thất bại", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
        );
        dialog.show(getParentFragmentManager(), "ConfirmDialog");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2002 && resultCode == Activity.RESULT_OK) {
            viewModel.refreshData();
        }
    }

    private void checkEmptyState(List<Discount> list) {
        if (list == null || list.isEmpty()) {
            layout_empty.setVisibility(View.VISIBLE);
            rvData.setVisibility(View.GONE);
        } else {
            layout_empty.setVisibility(View.GONE);
            rvData.setVisibility(View.VISIBLE);
        }
    }
}