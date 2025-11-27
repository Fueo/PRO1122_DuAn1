package com.example.fa25_duan1.view.detail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fa25_duan1.R;
import com.example.fa25_duan1.adapter.BookGridAdapter;
import com.example.fa25_duan1.model.Product;
import com.example.fa25_duan1.viewmodel.CartViewModel;
import com.example.fa25_duan1.viewmodel.ProductViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class DetailFragment extends Fragment {

    // 1. Khai báo các View cơ bản
    private ImageView ivImage, ivHeart;
    private TextView tvName, tvAuthor, tvPrice, tvPages, tvPublished, tvViews, tvLikes;
    private MaterialButton btnAddToCart, btnBuyNow;

    // 2. Khai báo View cho phần Tab và Nội dung Tab
    private TabLayout tabLayout;
    private TextView tvDescription;
    private View layoutDescription;
    private View layoutAuthorInfo;
    private ImageView ivAuthorAvatar;
    private TextView tvAuthorNameDetail, tvAuthorBio;

    // 3. Khai báo cho phần Sản phẩm liên quan
    private RecyclerView rvRelatedProducts;
    private BookGridAdapter relatedProductAdapter;
    private List<Product> listRelated = new ArrayList<>();

    // ViewModels & Data
    private ProductViewModel productViewModel;
    private CartViewModel cartViewModel;
    private String productId;
    private Product currentProduct;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        initViewModel();
        setupTabEvents();
        setupRelatedProductsAdapter(); // Cấu hình RecyclerView
        getProductData();
        setupEvents();
    }

    private void initViews(View view) {
        ivImage = view.findViewById(R.id.ivImage);
        ivHeart = view.findViewById(R.id.iv_heart_icon);
        tvLikes = view.findViewById(R.id.tv_likes_count);
        tvName = view.findViewById(R.id.tvName);
        tvAuthor = view.findViewById(R.id.tv_book_author);
        tvPrice = view.findViewById(R.id.tv_book_price);
        tvPages = view.findViewById(R.id.tv_pages_value);
        tvPublished = view.findViewById(R.id.tv_published_value);
        tvViews = view.findViewById(R.id.tv_views_value);
        btnAddToCart = view.findViewById(R.id.btn_add_to_cart);
        btnBuyNow = view.findViewById(R.id.btnEdit);

        // Tab & Author
        tabLayout = view.findViewById(R.id.tab_layout);
        layoutDescription = view.findViewById(R.id.layout_description);
        layoutAuthorInfo = view.findViewById(R.id.layout_author_info);
        tvDescription = view.findViewById(R.id.tv_description_content);
        ivAuthorAvatar = view.findViewById(R.id.iv_author_avatar);
        tvAuthorNameDetail = view.findViewById(R.id.tv_author_name_detail);
        tvAuthorBio = view.findViewById(R.id.tv_author_bio);

        // Related RecyclerView
        rvRelatedProducts = view.findViewById(R.id.rv_related_products);
    }

    private void initViewModel() {
        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);
        cartViewModel = new ViewModelProvider(requireActivity()).get(CartViewModel.class);
    }

    // --- CẤU HÌNH ADAPTER SẢN PHẨM LIÊN QUAN ---
    private void setupRelatedProductsAdapter() {
        // Sử dụng GridLayoutManager 2 cột (đã set trong XML nhưng set lại code cho chắc chắn)
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
        rvRelatedProducts.setLayoutManager(gridLayoutManager);

        // Quan trọng: Tắt scroll của RV để NestedScrollView hoạt động mượt
        rvRelatedProducts.setNestedScrollingEnabled(false);

        relatedProductAdapter = new BookGridAdapter(getContext(), listRelated, new BookGridAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Product product) {
                // KHI CLICK VÀO SẢN PHẨM LIÊN QUAN -> MỞ LẠI DETAIL FRAGMENT
                navigateToProductDetail(product.getId());
            }

            @Override
            public void onFavoriteClick(String productId) {
                // Xử lý yêu thích nếu cần
                Toast.makeText(getContext(), "Đã thích: " + productId, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAddToCartClick(Product product) {
                cartViewModel.increaseQuantity(product.getId());
                Toast.makeText(getContext(), "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
            }
        });

        rvRelatedProducts.setAdapter(relatedProductAdapter);
    }

    // --- HÀM ĐIỀU HƯỚNG SANG SẢN PHẨM MỚI ---
    private void navigateToProductDetail(String newProductId) {
        DetailFragment newFragment = new DetailFragment();
        Bundle args = new Bundle();
        args.putString("product_id", newProductId);
        newFragment.setArguments(args);

        // Thay thế Fragment hiện tại bằng Fragment mới
        // Lưu ý: R.id.fragment_container cần thay bằng ID của FrameLayout chứa Fragment trong Activity chính của bạn (ví dụ: MainActivity)
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();

        // Thêm animation chuyển cảnh cho mượt (tùy chọn)
        transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out);

        // Sử dụng replace để thay thế nội dung, addToBackStack để user có thể ấn Back quay lại sách trước
        transaction.replace(getId(), newFragment); // getId() lấy ID container hiện tại của Fragment này
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void setupTabEvents() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        layoutDescription.setVisibility(View.VISIBLE);
                        layoutAuthorInfo.setVisibility(View.GONE);
                        break;
                    case 1:
                        layoutDescription.setVisibility(View.GONE);
                        layoutAuthorInfo.setVisibility(View.VISIBLE);
                        break;
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void getProductData() {
        if (getArguments() != null) productId = getArguments().getString("product_id");
        if (productId == null && getActivity().getIntent() != null) productId = getActivity().getIntent().getStringExtra("product_id");

        if (productId != null) {
            productViewModel.viewProductApi(productId).observe(getViewLifecycleOwner(), product -> {
                if (product != null) {
                    currentProduct = product;
                    bindDataToUI(product);

                    // --- SAU KHI CÓ PRODUCT -> LOAD SẢN PHẨM LIÊN QUAN ---
                    if (product.getCategory() != null) {
                        loadRelatedProducts(product.getCategory().get_id());
                    }
                } else {
                    Toast.makeText(getContext(), "Không thể tải thông tin sản phẩm", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    // --- LOAD DATA TỪ API ---
    private void loadRelatedProducts(String categoryId) {
        // Gọi hàm getProductsByCategory (Bạn đã có sẵn trong ViewModel/Repo)
        // Lưu ý: ViewModel của bạn hàm getProductsByCategory trả về LiveData, nên cần observe
        productViewModel.getProductsByCategory(categoryId).observe(getViewLifecycleOwner(), products -> {
            if (products != null) {
                // Lọc bỏ sản phẩm hiện tại đang xem khỏi danh sách gợi ý
                List<Product> filteredList = new ArrayList<>();
                for (Product p : products) {
                    if (!p.getId().equals(currentProduct.getId())) {
                        filteredList.add(p);
                    }
                }

                // Cập nhật lên UI
                listRelated = filteredList;
                relatedProductAdapter.setProducts(listRelated);
            }
        });
    }

    private void bindDataToUI(Product product) {
        if (product.getImage() != null && !product.getImage().isEmpty()) {
            Glide.with(this).load(product.getImage()).placeholder(R.drawable.book_cover_placeholder).into(ivImage);
        }
        tvName.setText(product.getName());
        tvPrice.setText(formatPrice(product.getPrice()));

        if (product.getAuthor() != null) {
            tvAuthor.setText(product.getAuthor().getName());
            tvAuthorNameDetail.setText(product.getAuthor().getName());
            // Check nếu Model Author có field description
            if(product.getAuthor().getDescription() != null) tvAuthorBio.setText(product.getAuthor().getDescription());

            if (product.getAuthor().getAvatar() != null && !product.getAuthor().getAvatar().isEmpty()) {
                Glide.with(this).load(product.getAuthor().getAvatar()).placeholder(R.drawable.book_cover_placeholder).into(ivAuthorAvatar);
            }
        } else {
            tvAuthor.setText("Đang cập nhật");
        }

        tvPages.setText(String.valueOf(product.getPages()));
        tvViews.setText(String.valueOf(product.getView()));
        tvLikes.setText(String.valueOf(product.getFavorite()));

        try {
            if(product.getPublishDate() != null) {
                String rawDate = product.getPublishDate();
                tvPublished.setText(rawDate.length() >= 10 ? rawDate.substring(0, 10) : rawDate);
            }
        } catch (Exception e) {
            tvPublished.setText("N/A");
        }

        if (product.getDescription() != null) {
            tvDescription.setText(product.getDescription());
        }
    }

    private void setupEvents() {
        btnAddToCart.setOnClickListener(v -> {
            if (currentProduct != null) {
                cartViewModel.increaseQuantity(currentProduct.getId());
                Toast.makeText(getContext(), "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
            }
        });
        btnBuyNow.setOnClickListener(v -> {
            if (currentProduct != null) {
                cartViewModel.increaseQuantity(currentProduct.getId());
                Toast.makeText(getContext(), "Chức năng mua ngay đang phát triển", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String formatPrice(double price) {
        DecimalFormat formatter = new DecimalFormat("#,### đ");
        return formatter.format(price);
    }
}