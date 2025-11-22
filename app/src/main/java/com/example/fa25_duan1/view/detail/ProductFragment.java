package com.example.fa25_duan1.view.detail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fa25_duan1.R;
import com.example.fa25_duan1.adapter.BookGridAdapter;
import com.example.fa25_duan1.model.Product;
import com.example.fa25_duan1.viewmodel.ProductViewModel;

import java.util.ArrayList;
import java.util.List;

public class ProductFragment extends Fragment {

    private RecyclerView rvProducts;
    private BookGridAdapter bookGridAdapter;
    private ProductViewModel productViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_product, container, false);

        rvProducts = view.findViewById(R.id.rv_products);

        // Giữ nguyên logic nạp Fragment lọc (nếu bạn vẫn muốn giữ giao diện lọc ở trên)
        if (savedInstanceState == null) {
            // Lưu ý: Đảm bảo ProductListFilterFragment đã được tạo hoặc import đúng
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_filter, new ProductListFilterFragment())
                    .commit();
        }

        setupProductGrid();
        setupViewModel();

        return view;
    }

    private void setupProductGrid() {
        // Khởi tạo Adapter với danh sách rỗng ban đầu
        bookGridAdapter = new BookGridAdapter(getContext(), new ArrayList<>(), new BookGridAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Product product) {
                Toast.makeText(requireActivity(), "Bạn đã click:" + product.getId(), Toast.LENGTH_SHORT).show();
            }
        });

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
        rvProducts.setLayoutManager(gridLayoutManager);
        rvProducts.setAdapter(bookGridAdapter);
    }

    private void setupViewModel() {
        // Khởi tạo ViewModel
        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);

        // Lắng nghe dữ liệu từ API (displayedProductsLiveData)
        productViewModel.getDisplayedProducts().observe(getViewLifecycleOwner(), products -> {
            if (products != null) {
                // Cập nhật dữ liệu vào Adapter khi tải xong
                bookGridAdapter.setProducts(products);
            }
        });

        // Nếu cần chắc chắn tải lại dữ liệu mới nhất mỗi khi vào màn hình này:
        // productViewModel.refreshData();
        // (Thường không cần thiết nếu ViewModel đã tự load trong constructor,
        // nhưng nếu muốn refresh khi quay lại từ chi tiết thì có thể bỏ comment)
    }
}