package com.example.fa25_duan1.view.management;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.fa25_duan1.R;

public class HeaderManagementFragment extends Fragment {
    ImageView iv_back;
    Spinner spSearch;
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

        String[] items = {"Theo tên"};

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                getContext(), R.layout.item_spinner, items) {
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view.findViewById(R.id.tvSpinnerItem);

                return view;
            }
        };
        spSearch.setAdapter(adapter);
    }
}
