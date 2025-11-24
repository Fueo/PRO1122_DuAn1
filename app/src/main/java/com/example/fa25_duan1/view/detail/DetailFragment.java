package com.example.fa25_duan1.view.detail;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.fa25_duan1.R;
import com.example.fa25_duan1.model.Product;
import com.example.fa25_duan1.viewmodel.CartViewModel;
import com.example.fa25_duan1.viewmodel.ProductViewModel;
import com.google.android.material.button.MaterialButton;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class DetailFragment extends Fragment {

    // 1. Khai báo các View theo Layout XML
    private ImageView ivImage, ivHeart;
    private TextView tvName, tvAuthor, tvPrice, tvPages, tvPublished, tvViews, tvDescription, tvLikes;
    private MaterialButton btnAddToCart, btnBuyNow;

    // ViewModels
    private ProductViewModel productViewModel;
    private CartViewModel cartViewModel;

    private String productId;
    private Product currentProduct; // Lưu sản phẩm hiện tại để xử lý mua/giỏ hàng

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        initViewModel();
        getProductData();
        setupEvents();
    }

    private void initViews(View view) {
        // Ánh xạ theo ID trong layout XML bạn cung cấp
        ivImage = view.findViewById(R.id.ivImage);
        ivHeart = view.findViewById(R.id.iv_heart_icon);
        tvLikes = view.findViewById(R.id.tv_likes_count);

        tvName = view.findViewById(R.id.tvName);
        tvAuthor = view.findViewById(R.id.tv_book_author);
        tvPrice = view.findViewById(R.id.tv_book_price);

        tvPages = view.findViewById(R.id.tv_pages_value);
        tvPublished = view.findViewById(R.id.tv_published_value);
        tvViews = view.findViewById(R.id.tv_views_value);

        tvDescription = view.findViewById(R.id.tv_description_content);

        btnAddToCart = view.findViewById(R.id.btn_add_to_cart);
        btnBuyNow = view.findViewById(R.id.btnEdit); // Trong XML id là btnEdit (Nút mua ngay)
    }

    private void initViewModel() {
        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);
        cartViewModel = new ViewModelProvider(requireActivity()).get(CartViewModel.class);
    }

    private void getProductData() {
        // Cách 1: Lấy từ Arguments (Nếu Fragment được tạo và truyền bundle)
        if (getArguments() != null) {
            productId = getArguments().getString("product_id");
        }

        // Cách 2: Fallback lấy từ Intent của Activity (Nếu Activity truyền thẳng)
        if (productId == null && getActivity().getIntent() != null) {
            productId = getActivity().getIntent().getStringExtra("product_id");
        }

        if (productId != null) {
            // Gọi API viewProductApi (Vừa tăng view, vừa lấy chi tiết)
            productViewModel.viewProductApi(productId).observe(getViewLifecycleOwner(), product -> {
                if (product != null) {
                    currentProduct = product;
                    bindDataToUI(product);
                } else {
                    Toast.makeText(getContext(), "Không thể tải thông tin sản phẩm", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void bindDataToUI(Product product) {
        // 1. Load ảnh bằng Glide
        if (product.getImage() != null && !product.getImage().isEmpty()) {
            Glide.with(this)
                    .load(product.getImage())
                    .placeholder(R.drawable.book_cover_placeholder)
                    .error(R.drawable.book_cover_placeholder)
                    .into(ivImage);
        }

        // 2. Set Text thông tin cơ bản
        tvName.setText(product.getName());
        tvPrice.setText(formatPrice(product.getPrice()));

        if (product.getAuthor() != null) {
            tvAuthor.setText(product.getAuthor().getName());
        } else {
            tvAuthor.setText("Đang cập nhật");
        }

        // 3. Set thông tin chi tiết (Pages, Date, Views)
        tvPages.setText(String.valueOf(product.getPages()));
        tvViews.setText(String.valueOf(product.getView())); // Số view mới nhất từ API
        tvLikes.setText(String.valueOf(product.getFavorite()));

        // Format Date
        try {
            // Giả sử date trả về dạng ISO 8601, bạn có thể cần parse lại cho đẹp
            // Ở đây hiển thị thô hoặc xử lý chuỗi đơn giản
            if(product.getPublishDate() != null) {
                String rawDate = product.getPublishDate();
                // Cắt chuỗi lấy ngày tháng năm (ví dụ: 2025-12-16T...)
                if(rawDate.length() >= 10) {
                    tvPublished.setText(rawDate.substring(0, 10));
                } else {
                    tvPublished.setText(rawDate);
                }
            }
        } catch (Exception e) {
            tvPublished.setText("N/A");
        }

        // 4. Set mô tả
        if (product.getDescription() != null) {
            tvDescription.setText(product.getDescription());
        }
    }

    private void setupEvents() {
        // Nút Thêm vào giỏ
        btnAddToCart.setOnClickListener(v -> {
            if (currentProduct != null) {
                cartViewModel.increaseQuantity(currentProduct.getId());
                Toast.makeText(getContext(), "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
            }
        });

        // Nút Mua ngay (Ví dụ: Thêm vào giỏ rồi chuyển hướng sang giỏ hàng)
        btnBuyNow.setOnClickListener(v -> {
            if (currentProduct != null) {
                cartViewModel.increaseQuantity(currentProduct.getId());
                // Chuyển sang Fragment Giỏ hàng (Cần setup navigation trong Activity cha)
                Toast.makeText(getContext(), "Chức năng mua ngay đang phát triển", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Helper: Định dạng tiền tệ
    private String formatPrice(double price) {
        DecimalFormat formatter = new DecimalFormat("#,### đ");
        return formatter.format(price);
    }
}