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

import net.cachapa.expandablelayout.ExpandableLayout;

public class AccountFilterFragment extends Fragment {
    private ImageView ivClose;
    private ConstraintLayout clFilter;
    private CardView cvFilter;
    private ExpandableLayout expandableLayout;
    private boolean isFilterVisible = false; // trạng thái hiện/tắt của card filter
    private Spinner spinner;
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_accountfilter, container, false);

        return view;
    }
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        spinner = view.findViewById(R.id.spinner_sort);
        clFilter = view.findViewById(R.id.clFilter);
        ivClose = view.findViewById(R.id.ivClose);
        cvFilter = view.findViewById(R.id.cvFilter);
        expandableLayout = view.findViewById(R.id.expandable_layout);
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

        // Khi ấn vào icon bộ lọc → bật/tắt card
        clFilter.setOnClickListener(v -> {
            expandableLayout.toggle();
        });

        // Khi ấn vào icon close → tắt card
        ivClose.setOnClickListener(v -> expandableLayout.collapse());
    }
}
