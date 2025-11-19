package com.example.fa25_duan1.view.management.author;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.fa25_duan1.R;
import com.example.fa25_duan1.viewmodel.AuthorViewModel;
import com.example.fa25_duan1.viewmodel.UserViewModel;

import org.angmarch.views.NiceSpinner;

import java.util.Arrays;
import java.util.LinkedList;

public class AuthorHeaderFragment extends Fragment {
    TextView tv_title;
    ImageView ivBack;
    EditText etSearch;
    NiceSpinner spSearch;
    AuthorViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_headermanagement, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ivBack = view.findViewById(R.id.ivBack);
        etSearch = view.findViewById(R.id.etSearch);
        spSearch = view.findViewById(R.id.spSearch);
        tv_title = view.findViewById(R.id.tv_title);

        tv_title.setText("Quản lí tác giả");
        etSearch.setHint("Tìm kiếm tác giả");

        viewModel = new ViewModelProvider(requireActivity()).get(AuthorViewModel.class);

        ivBack.setOnClickListener(v -> getActivity().finish());

        // Spinner data
        LinkedList<String> data = new LinkedList<>(Arrays.asList(
                "Theo tên", ""));
        spSearch.attachDataSource(data);
        spSearch.setEnabled(false);
        spSearch.setClickable(false);

        // Khi text thay đổi trong EditText
        etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchAuthorsFromHeader(s.toString());
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
    }

    private void searchAuthorsFromHeader(String query) {
        if (viewModel == null) return;
        viewModel.searchAuthors(query);
    }
}

