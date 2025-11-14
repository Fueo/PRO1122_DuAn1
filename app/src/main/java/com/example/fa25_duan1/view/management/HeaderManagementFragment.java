package com.example.fa25_duan1.view.management;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.fa25_duan1.R;

import org.angmarch.views.NiceSpinner;

import java.util.Arrays;
import java.util.LinkedList;

public class HeaderManagementFragment extends Fragment {
    ImageView iv_back;
    NiceSpinner spSearch;
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_headermanagement, container, false);

        return view;
    }
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Kiểm tra để tránh chèn nhiều lần khi rotate
        iv_back = view.findViewById(R.id.iv_back);
        spSearch = view.findViewById(R.id.spSearch);

        iv_back.setOnClickListener(v -> {
            getActivity().finish();
        });

        // Dữ liệu cho spinner
        LinkedList<String> data = new LinkedList<>(Arrays.asList(
                "Theo tên", "Option 2", "Option 3", "Option 4"));

        // Gắn dữ liệu vào spinner
        spSearch.attachDataSource(data);

        // Bắt sự kiện chọn item
        spSearch.setOnSpinnerItemSelectedListener((parent, v, position, id) -> {
            String selectedItem = data.get(position);
            Toast.makeText(getActivity(),
                    "Bạn chọn: " + selectedItem,
                    Toast.LENGTH_SHORT).show();
        });
    }
}
