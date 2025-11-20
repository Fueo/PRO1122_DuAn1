package com.example.fa25_duan1.view.home;

import static com.example.fa25_duan1.data.MenuAdminData.getListMenuAdminData;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fa25_duan1.R;
import com.example.fa25_duan1.adapter.ActionButtonAdapter;
import com.example.fa25_duan1.model.MenuItem;
import com.example.fa25_duan1.view.detail.DetailActivity;
import com.example.fa25_duan1.view.management.ManageActivity;

import java.util.ArrayList;

public class AdminFragment extends Fragment {
    ArrayList<MenuItem> listButtonData;
    RecyclerView rvButton;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvButton = view.findViewById(R.id.rvButton);
        listButtonData = getListMenuAdminData();
        GridLayoutManager gridLayoutManager1 = new GridLayoutManager(getActivity(), 4) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };

        rvButton.setLayoutManager(gridLayoutManager1); // 4 cột
        ActionButtonAdapter adapter = new ActionButtonAdapter(getActivity(), listButtonData, item -> {
            Intent intent = new Intent(getActivity(), ManageActivity.class);
            switch (item.getId()) {
                case 0:
                    intent.putExtra(DetailActivity.EXTRA_CONTENT_FRAGMENT, "category");
                    break;
                case 1:
                    intent.putExtra(DetailActivity.EXTRA_CONTENT_FRAGMENT, "product");
                    break;
                case 2:
                    Toast.makeText(getActivity(), "Vào trang Invoice", Toast.LENGTH_SHORT).show();
                    break;
                case 3:
                    Toast.makeText(getActivity(), "Vào trang Sales", Toast.LENGTH_SHORT).show();
                    break;
                case 4:
                    intent.putExtra(DetailActivity.EXTRA_CONTENT_FRAGMENT, "author");
                    break;
                case 5:
                    intent.putExtra(DetailActivity.EXTRA_CONTENT_FRAGMENT, "account");
                    break;
                case 6:
                    Toast.makeText(getActivity(), "Vào trang Statistic", Toast.LENGTH_SHORT).show();
                    break;
            }
            startActivity(intent);
        });

        rvButton.setAdapter(adapter);
    }
}
