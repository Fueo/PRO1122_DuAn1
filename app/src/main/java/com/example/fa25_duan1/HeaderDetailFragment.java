package com.example.fa25_duan1;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class HeaderDetailFragment extends Fragment {
    TextView tv_title;
    ImageView iv_back;

    private static final String ARG_TITLE = "arg_title";

    public static HeaderDetailFragment newInstance(String title) {
        HeaderDetailFragment fragment = new HeaderDetailFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ARG_TITLE, title);
        fragment.setArguments(bundle);
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_headerdetail, container, false);

        TextView tvTitle = view.findViewById(R.id.tv_title);
        String title = getArguments() != null ? getArguments().getString(ARG_TITLE) : "Header";
        tvTitle.setText(title);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tv_title = view.findViewById(R.id.tv_title);
        iv_back = view.findViewById(R.id.iv_back);

        iv_back.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().finish();
            }
        });

    }
}
