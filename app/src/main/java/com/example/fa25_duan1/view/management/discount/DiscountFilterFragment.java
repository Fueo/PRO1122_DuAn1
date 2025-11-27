package com.example.fa25_duan1.view.management.discount;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.fa25_duan1.R;
import com.example.fa25_duan1.viewmodel.DiscountViewModel;

import org.angmarch.views.NiceSpinner;

import java.util.Arrays;
import java.util.LinkedList;

public class DiscountFilterFragment extends Fragment {

    private NiceSpinner spSort;
    private DiscountViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Dùng lại layout fragment_catetfilter (vì chỉ có 1 spinner, tái sử dụng được)
        return inflater.inflate(R.layout.fragment_catetfilter, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        spSort = view.findViewById(R.id.spSort);
        viewModel = new ViewModelProvider(requireActivity()).get(DiscountViewModel.class);

        setupSpinner();
    }

    private void setupSpinner() {
        LinkedList<String> data = new LinkedList<>(Arrays.asList(
                "Mới nhất",
                "Cũ nhất",
                "Theo tên"
        ));
        spSort.attachDataSource(data);

        spSort.setOnSpinnerItemSelectedListener((parent, v, position, id) -> {
            viewModel.sortDiscounts(position);
        });
    }
}