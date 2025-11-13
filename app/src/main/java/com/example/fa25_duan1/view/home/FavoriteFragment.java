package com.example.fa25_duan1.view.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fa25_duan1.R;
import com.example.fa25_duan1.adapter.ProductFavoriteAdapter; // (1)
import com.example.fa25_duan1.model.ProductFavorite;

import java.util.ArrayList;

public class FavoriteFragment extends Fragment {
    RelativeLayout rlProfile;

    private RecyclerView rvProducts;
    private ProductFavoriteAdapter productAdapter;
    private ArrayList<ProductFavorite> productList;


    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favorite, container, false);
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvProducts = view.findViewById(R.id.rv_products);

        productList = new ArrayList<>();

        productList.add(new ProductFavorite(
                "Và Rồi Chẳng Còn Ai - And Then There Were None",
                "Agatha Christie",
                "75.000 VNĐ",
                "105.000 VNĐ",
                "(-20%)",
                R.drawable.book_cover_placeholder
        ));

        productList.add(new ProductFavorite(
                "Và Rồi Chẳng Còn Ai - And Then There Were None",
                "Agatha Christie",
                "75.000 VNĐ",
                "105.000 VNĐ",
                "(-20%)",
                R.drawable.book_cover_placeholder
        ));

        productList.add(new ProductFavorite(
                "Và Rồi Chẳng Còn Ai - And Then There Were None",
                "Agatha Christie",
                "75.000 VNĐ",
                "105.000 VNĐ",
                "(-20%)",
                R.drawable.book_cover_placeholder
        ));

        productList.add(new ProductFavorite(
                "Và Rồi Chẳng Còn Ai - And Then There Were None",
                "Agatha Christie",
                "75.000 VNĐ",
                "105.000 VNĐ",
                "(-20%)",
                R.drawable.book_cover_placeholder
        ));

        productAdapter = new ProductFavoriteAdapter(getContext(), productList);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
        rvProducts.setLayoutManager(gridLayoutManager);

        rvProducts.setAdapter(productAdapter);

    }
}