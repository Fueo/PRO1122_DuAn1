package com.example.fa25_duan1.view.management.discount;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fa25_duan1.R;
import com.example.fa25_duan1.adapter.DiscountManageAdapter;
import com.example.fa25_duan1.model.Discount;
import com.example.fa25_duan1.view.management.UpdateActivity;
import com.example.fa25_duan1.viewmodel.DiscountViewModel;

import java.util.ArrayList;
import java.util.List;

import com.shashank.sony.fancytoastlib.FancyToast;
import io.github.cutelibs.cutedialog.CuteDialog;

public class DiscountManageFragment extends Fragment {
    private View layout_empty;
    private RecyclerView rvData;
    private Button btnAdd;
    private DiscountManageAdapter adapter;
    private DiscountViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_discount_management, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_filter, new DiscountFilterFragment())
                .commit();

        rvData = view.findViewById(R.id.rvDiscountData);
        btnAdd = view.findViewById(R.id.btnAddDiscount);
        layout_empty = view.findViewById(R.id.layout_empty);

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

        viewModel = new ViewModelProvider(requireActivity()).get(DiscountViewModel.class);

        // Load data lần đầu
        viewModel.refreshData();

        viewModel.getDisplayedDiscounts().observe(getViewLifecycleOwner(), discounts -> {
            if (discounts != null) {
                adapter.setData(discounts);
                checkEmptyState(discounts);
            }
        });

        // Observe error message
        viewModel.getMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                FancyToast.makeText(requireContext(), msg, FancyToast.LENGTH_SHORT, FancyToast.ERROR, false).show();
            }
        });

        btnAdd.setOnClickListener(v -> openUpdateActivity(null));
    }

    private void openUpdateActivity(Discount discount) {
        Intent intent = new Intent(getContext(), UpdateActivity.class);
        intent.putExtra(UpdateActivity.EXTRA_HEADER_TITLE, "Thêm mã giảm giá");

        if (discount != null) {
            intent.putExtra(UpdateActivity.EXTRA_HEADER_TITLE, "Chỉnh sửa giảm giá");
            intent.putExtra("Id", discount.get_id());
        }
        intent.putExtra(UpdateActivity.EXTRA_CONTENT_FRAGMENT, "discount");

        startActivityForResult(intent, 2002);
    }

    private void deleteDiscount(Discount discount) {
        if (discount == null) return;

        new CuteDialog.withIcon(requireActivity())
                .setIcon(R.drawable.ic_dialog_confirm)
                .setTitle("Xóa mã giảm giá")
                .setDescription("Bạn có chắc muốn xóa mã: " + discount.getDiscountName() + "?")
                .setPrimaryColor(R.color.blue)
                .setPositiveButtonColor(R.color.blue)
                .setPositiveButtonText("Xóa", v -> performDelete(discount))
                .setNegativeButtonText("Hủy", v -> {})
                .show();
    }

    private void performDelete(Discount discount) {
        // [SỬA] Xử lý ApiResponse<Void>
        viewModel.deleteDiscount(discount.get_id()).observe(getViewLifecycleOwner(), apiResponse -> {
            if (apiResponse != null && apiResponse.isStatus()) {
                viewModel.refreshData();

                new CuteDialog.withIcon(requireActivity())
                        .setIcon(R.drawable.ic_dialog_success)
                        .setTitle("Thành công")
                        .setDescription("Đã xóa mã giảm giá thành công!")
                        .setPrimaryColor(R.color.blue)
                        .setPositiveButtonColor(R.color.blue)
                        .setPositiveButtonText("Đóng", v -> {})
                        .hideNegativeButton(true)
                        .show();
            } else {
                String msg = (apiResponse != null) ? apiResponse.getMessage() : "Xóa thất bại";
                FancyToast.makeText(getContext(), msg, FancyToast.LENGTH_SHORT, FancyToast.ERROR, true).show();
            }
        });
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