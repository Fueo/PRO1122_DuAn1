package com.example.fa25_duan1.view.management;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.fa25_duan1.R;
import com.example.fa25_duan1.viewmodel.UserViewModel;

import org.angmarch.views.NiceSpinner;

import java.util.Arrays;
import java.util.LinkedList;

public class HeaderManagementFragment extends Fragment {
    ImageView ivBack;
    EditText etSearch;
    NiceSpinner spSearch;
    UserViewModel viewModel;

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

        viewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        ivBack.setOnClickListener(v -> getActivity().finish());

        // Spinner data
        LinkedList<String> data = new LinkedList<>(Arrays.asList(
                "Theo tên", "Theo SĐT"));
        spSearch.attachDataSource(data);

        // Khi text thay đổi trong EditText
        etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchUsersFromHeader(s.toString());
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        // Khi spinner thay đổi
        spSearch.setOnSpinnerItemSelectedListener((parent, v, position, id) -> {
            // Cập nhật lại search khi đổi type
            searchUsersFromHeader(etSearch.getText().toString());
        });
    }

    private void searchUsersFromHeader(String query) {
        if (viewModel == null) return;

        String type = spSearch.getSelectedItem() != null && spSearch.getSelectedItem().toString().equals("Theo SĐT")
                ? "phone" : "name";

        viewModel.searchUsers(query, type);
    }
}

