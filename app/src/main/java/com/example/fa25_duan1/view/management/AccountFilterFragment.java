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
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.example.fa25_duan1.R;

public class AccountFilterFragment extends Fragment {
    private ImageView ivClose;
    private ConstraintLayout clFilter;
    private CardView cvFilter;
    private boolean isFilterVisible = false; // trạng thái hiện/tắt của card filter
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_accountfilter, container, false);

        return view;
    }
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Spinner spinner = view.findViewById(R.id.spinner_sort);
        clFilter = view.findViewById(R.id.clFilter);
        ivClose = view.findViewById(R.id.ivClose);
        cvFilter = view.findViewById(R.id.cvFilter);
        String[] items = {"Mới nhất", "Cũ nhất", "Theo tên"};

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                getContext(), R.layout.item_spinner, items) {
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view.findViewById(R.id.tvSpinnerItem);

                return view;
            }
        };
        spinner.setAdapter(adapter);

        // Ban đầu ẩn card filter
        cvFilter.setVisibility(View.GONE);

        // Khi ấn vào icon bộ lọc → bật/tắt card
        clFilter.setOnClickListener(v -> {
            if (cvFilter.getVisibility() == View.VISIBLE) {
                cvFilter.setVisibility(View.GONE);
            } else {
                cvFilter.setVisibility(View.VISIBLE);
            }
        });

        // Khi ấn vào icon close → tắt card
        ivClose.setOnClickListener(v -> cvFilter.setVisibility(View.GONE));
    }
}
