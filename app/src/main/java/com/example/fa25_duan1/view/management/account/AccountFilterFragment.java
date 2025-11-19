package com.example.fa25_duan1.view.management.account;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.fa25_duan1.R;
import com.example.fa25_duan1.viewmodel.UserViewModel;

import net.cachapa.expandablelayout.ExpandableLayout;
import org.angmarch.views.NiceSpinner; // Đảm bảo bạn đã import đúng thư viện NiceSpinner

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class AccountFilterFragment extends Fragment {
    private ImageView ivClose;
    private ConstraintLayout clFilter;
    private ExpandableLayout expandableLayout;
    private NiceSpinner spSort;
    UserViewModel viewModel;

    private CheckBox cbCustomer, cbEmployee, cbAdmin;
    private LinearLayout condition1, condition2, condition3;
    // Xóa: private List<CheckBox> roleCheckBoxes; (Không cần thiết nữa)

    // Các hằng số vai trò vẫn giữ nguyên
    private static final int ROLE_CUSTOMER = 0;
    private static final int ROLE_EMPLOYEE = 1;
    private static final int ROLE_ADMIN = 2;
    // Xóa: private static final int ROLE_ALL = -1; (ViewModel sẽ tự xử lý danh sách rỗng)

    // ... (onCreateView vẫn giữ nguyên) ...
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_accountfilter, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        spSort = view.findViewById(R.id.spSort);
        clFilter = view.findViewById(R.id.clFilter);
        ivClose = view.findViewById(R.id.ivClose);
        expandableLayout = view.findViewById(R.id.expandable_layout);

        cbCustomer = view.findViewById(R.id.cbCustomer);
        cbEmployee = view.findViewById(R.id.cbEmployee);
        cbAdmin = view.findViewById(R.id.cbAdmin);
        condition1 = view.findViewById(R.id.condition1);
        condition2 = view.findViewById(R.id.condition2);
        condition3 = view.findViewById(R.id.condition3);

        viewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        LinkedList<String> data = new LinkedList<>(Arrays.asList(
                "Mới nhất", "Cũ nhất"));
        spSort.attachDataSource(data);
        spSort.setOnSpinnerItemSelectedListener((parent, v, position, id) -> {
            switch (position) {
                case 0: viewModel.sortByCreateAt(true); break;
                case 1: viewModel.sortByCreateAt(false); break;
            }
        });

        clFilter.setOnClickListener(v -> expandableLayout.toggle());
        ivClose.setOnClickListener(v -> expandableLayout.collapse());

        setupRoleFilterListeners();
    }

    private void setupRoleFilterListeners() {

        // Tạo một listener chung
        // Bất cứ khi nào một CheckBox thay đổi, gọi hàm applyRoleFilter()
        CompoundButton.OnCheckedChangeListener listener = (buttonView, isChecked) -> {
            applyRoleFilter();
        };

        cbCustomer.setOnCheckedChangeListener(listener);
        cbEmployee.setOnCheckedChangeListener(listener);
        cbAdmin.setOnCheckedChangeListener(listener);

        condition1.setOnClickListener(v -> cbCustomer.toggle());
        condition2.setOnClickListener(v -> cbEmployee.toggle());
        condition3.setOnClickListener(v -> cbAdmin.toggle());
    }

    private void applyRoleFilter() {
        List<Integer> selectedRoles = new ArrayList<>();

        if (cbCustomer.isChecked()) {
            selectedRoles.add(ROLE_CUSTOMER);
        }
        if (cbEmployee.isChecked()) {
            selectedRoles.add(ROLE_EMPLOYEE);
        }
        if (cbAdmin.isChecked()) {
            selectedRoles.add(ROLE_ADMIN);
        }

        viewModel.filterByRoles(selectedRoles);
    }

}