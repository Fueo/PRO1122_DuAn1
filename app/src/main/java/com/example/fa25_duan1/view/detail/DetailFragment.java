package com.example.fa25_duan1.view.detail;

import android.app.Dialog;
import android.graphics.Paint;
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
import com.example.fa25_duan1.viewmodel.FavoriteViewModel; // 1. Import thêm
import com.example.fa25_duan1.viewmodel.ProductViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import io.github.cutelibs.cutedialog.CuteDialog;

public class DetailFragment extends Fragment {

    // ... (Các khai báo View giữ nguyên)
    private ImageView ivImage, ivHeart;
    private TextView tvName, tvAuthorHeader, tvLikes;
    private TextView tvSalePrice, tvOriginalPrice, tvDiscount;
    private TextView tvPages, tvPublished, tvViews;
    private TabLayout tabLayout;
    private LinearLayout layoutDescription;
    private LinearLayout layoutAuthorInfo;
    private TextView tvDescription;
    private ImageView ivAuthorAvatar;
    private TextView tvAuthorNameDetail, tvAuthorBio;
    private MaterialButton btnAddToCart, btnBuyNow;

    // RecyclerView
    private RecyclerView rvRelatedProducts;
    private BookGridAdapter relatedProductAdapter;
    private List<Product> listRelated = new ArrayList<>();

    // ViewModels
    private ProductViewModel productViewModel;
    private CartViewModel cartViewModel;
    private FavoriteViewModel favoriteViewModel; // 2. Khai báo thêm

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

        // Setup Adapter trước khi load data
        setupRelatedProductsAdapter();

        // Setup lắng nghe dữ liệu
        setupDataObservation();

        getProductData();
        setupEvents();
    }

    private void initViews(View view) {
        // ... (Giữ nguyên phần ánh xạ View cũ của bạn)
        ivImage = view.findViewById(R.id.ivImage);
        ivHeart = view.findViewById(R.id.iv_heart_icon);
        tvLikes = view.findViewById(R.id.tv_likes_count);
        tvName = view.findViewById(R.id.tvName);
        tvAuthorHeader = view.findViewById(R.id.tv_book_author);

        tvSalePrice = view.findViewById(R.id.tv_sale_price);
        tvOriginalPrice = view.findViewById(R.id.tv_original_price);
        tvDiscount = view.findViewById(R.id.tv_discount);

        tvPages = view.findViewById(R.id.tv_pages_value);
        tvPublished = view.findViewById(R.id.tv_published_value);
        tvViews = view.findViewById(R.id.tv_views_value);

        btnAddToCart = view.findViewById(R.id.btn_add_to_cart);
        btnBuyNow = view.findViewById(R.id.btnEdit);

        tabLayout = view.findViewById(R.id.tab_layout);
        layoutDescription = view.findViewById(R.id.layout_description);
        layoutAuthorInfo = view.findViewById(R.id.layout_author_info);
        tvDescription = view.findViewById(R.id.tv_description_content);
        ivAuthorAvatar = view.findViewById(R.id.iv_author_avatar);
        tvAuthorNameDetail = view.findViewById(R.id.tv_author_name_detail);
        tvAuthorBio = view.findViewById(R.id.tv_author_bio);

        rvRelatedProducts = view.findViewById(R.id.rv_related_products);
    }

    private void initViewModel() {
        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);
        cartViewModel = new ViewModelProvider(requireActivity()).get(CartViewModel.class);

        // 3. Khởi tạo FavoriteViewModel
        favoriteViewModel = new ViewModelProvider(this).get(FavoriteViewModel.class);
    }

    // 4. Sửa lại hàm setup Adapter giống HomeFragment
    private void setupRelatedProductsAdapter() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
        rvRelatedProducts.setLayoutManager(gridLayoutManager);
        rvRelatedProducts.setNestedScrollingEnabled(false);

        // Khởi tạo Adapter với 3 phương thức override chuẩn (Item, Favorite, AddCart)
        relatedProductAdapter = new BookGridAdapter(getContext(), new ArrayList<>(), new BookGridAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Product product) {
                // Logic mở chi tiết (Khác Home một chút là dùng fragment transaction)
                navigateToProductDetail(product.getId());
            }

            @Override
            public void onFavoriteClick(String productId) {
                // Logic thả tim dùng ViewModel giống Home
                favoriteViewModel.toggleFavorite(productId);
            }

            @Override
            public void onAddToCartClick(Product product) {
                // Logic thêm giỏ hàng dùng ViewModel giống Home
                cartViewModel.increaseQuantity(product.getId());
                Toast.makeText(getContext(), "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
            }
        });

        rvRelatedProducts.setAdapter(relatedProductAdapter);
    }

    // 5. Thêm hàm lắng nghe dữ liệu Favorite giống Home
    private void setupDataObservation() {
        // Lắng nghe danh sách yêu thích để cập nhật trái tim đỏ/xám trong list Related
        favoriteViewModel.getFavoriteIds().observe(getViewLifecycleOwner(), ids -> {
            if (relatedProductAdapter != null) {
                relatedProductAdapter.setFavoriteIds(ids);
            }

            // Cập nhật trái tim của sản phẩm chính đang xem (nếu muốn)
            if (currentProduct != null && ids.contains(currentProduct.getId())) {
                ivHeart.setImageResource(R.drawable.ic_heart_filled_red);
            } else {
                ivHeart.setImageResource(R.drawable.ic_heart); // Icon mặc định (viền)
            }
        });

        cartViewModel.getMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                // Kiểm tra sơ bộ: Nếu tin nhắn chứa từ "thành công" thì có thể vẫn dùng Toast hoặc Dialog xanh
                // Còn ở đây ta mặc định backend trả về lỗi (status: false) thì hiện Dialog đỏ

                showErrorDialog(message);

                // Clear message để tránh hiện lại khi xoay màn hình (nếu ViewModel chưa xử lý)
                // cartViewModel.clearMessage();
            }
        });
    }

    // ... (Các hàm navigateToProductDetail, setupTabEvents, getProductData, loadRelatedProducts giữ nguyên logic cũ)

    private void navigateToProductDetail(String newProductId) {
        DetailFragment newFragment = new DetailFragment();
        Bundle args = new Bundle();
        args.putString("product_id", newProductId);
        newFragment.setArguments(args);

        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out);
        transaction.replace(getId(), newFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void setupTabEvents() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    layoutDescription.setVisibility(View.VISIBLE);
                    layoutAuthorInfo.setVisibility(View.GONE);
                } else if (tab.getPosition() == 1) {
                    layoutDescription.setVisibility(View.GONE);
                    layoutAuthorInfo.setVisibility(View.VISIBLE);
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
                    if (product.getCategory() != null) {
                        loadRelatedProducts(product.getCategory().get_id());
                    }
                } else {
                    Toast.makeText(getContext(), "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void loadRelatedProducts(String categoryId) {
        productViewModel.getProductsByCategory(categoryId).observe(getViewLifecycleOwner(), products -> {
            if (products != null) {
                List<Product> filteredList = new ArrayList<>();
                for (Product p : products) {
                    if (!p.getId().equals(currentProduct.getId())) {
                        filteredList.add(p);
                    }
                }
                listRelated = filteredList;
                // Cập nhật list cho adapter
                relatedProductAdapter.setProducts(listRelated);
            }
        });
    }

    private void bindDataToUI(Product product) {
        // ... (Giữ nguyên logic bindDataToUI đã sửa ở bước trước)
        if (product.getImage() != null && !product.getImage().isEmpty()) {
            Glide.with(this).load(product.getImage()).placeholder(R.drawable.book_cover_placeholder).into(ivImage);
        }
        tvName.setText(product.getName());
        tvLikes.setText(String.valueOf(product.getFavorite()));
        tvPages.setText(String.valueOf(product.getPages()));
        tvViews.setText(String.valueOf(product.getView()));

        try {
            if (product.getPublishDate() != null && !product.getPublishDate().isEmpty()) {
                String rawDate = product.getPublishDate();

                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
                inputFormat.setTimeZone(TimeZone.getTimeZone("UTC")); // Quan trọng: Set múi giờ UTC để parse đúng 'Z'

                // 2. Định dạng Output: dd/MM/yyyy
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

                // 3. Thực hiện chuyển đổi
                Date date = inputFormat.parse(rawDate);
                if (date != null) {
                    tvPublished.setText(outputFormat.format(date));
                } else {
                    tvPublished.setText(rawDate); // Fallback nếu date null
                }
            } else {
                tvPublished.setText("N/A");
            }
        } catch (Exception e) {
            // Nếu lỗi parse (do format không khớp), hiển thị tạm chuỗi gốc hoặc N/A
            tvPublished.setText("N/A");
            e.printStackTrace();
        }

        double originalPrice = product.getPrice();
        int discount = product.getDiscount();

        if (discount > 0) {
            double newPrice = originalPrice * (100 - discount) / 100;
            tvSalePrice.setText(formatPrice(newPrice));
            tvOriginalPrice.setText(formatPrice(originalPrice));
            tvOriginalPrice.setPaintFlags(tvOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            tvOriginalPrice.setVisibility(View.VISIBLE);
            tvDiscount.setText("-" + discount + "%");
            tvDiscount.setVisibility(View.VISIBLE);
        } else {
            tvSalePrice.setText(formatPrice(originalPrice));
            tvOriginalPrice.setVisibility(View.GONE);
            tvDiscount.setVisibility(View.GONE);
        }

        if (product.getAuthor() != null) {
            tvAuthorHeader.setText(product.getAuthor().getName());
            tvAuthorNameDetail.setText(product.getAuthor().getName());
            if (product.getAuthor().getDescription() != null) {
                tvAuthorBio.setText(product.getAuthor().getDescription());
            } else {
                tvAuthorBio.setText("Chưa có thông tin tiểu sử.");
            }
            if (product.getAuthor().getAvatar() != null && !product.getAuthor().getAvatar().isEmpty()) {
                Glide.with(this).load(product.getAuthor().getAvatar()).placeholder(R.drawable.book_cover_placeholder).into(ivAuthorAvatar);
            }
        } else {
            tvAuthorHeader.setText("Đang cập nhật");
            tvAuthorNameDetail.setText("Đang cập nhật");
        }

        if (product.getDescription() != null) {
            tvDescription.setText(product.getDescription());
        }
    }

    private void setupEvents() {
        // Sự kiện click nút tim ở header (Detail Fragment)
        ivImage.setOnClickListener(v -> {
            if (currentProduct != null && currentProduct.getImage() != null) {
                showFullScreenImage(currentProduct.getImage());
            }
        });

        btnAddToCart.setOnClickListener(v -> {
            if (currentProduct != null) {
                cartViewModel.increaseQuantity(currentProduct.getId());
            }
        });

        btnBuyNow.setOnClickListener(v -> {
            if (currentProduct != null) {
                cartViewModel.increaseQuantity(currentProduct.getId());
            }
        });
    }

    private String formatPrice(double price) {
        DecimalFormat formatter = new DecimalFormat("#,### đ");
        return formatter.format(price);
    }

    private void showErrorDialog(String message) {
        new CuteDialog.withIcon(requireActivity())
                .setIcon(R.drawable.ic_dialog_error)
                .setTitle("Thông báo")
                .setDescription(message)
                .setPrimaryColor(R.color.red)
                .setPositiveButtonColor(R.color.red)
                .setTitleTextColor(R.color.black)
                .setDescriptionTextColor(R.color.gray_text)
                .hideNegativeButton(true)
                .setPositiveButtonText("Đóng", v -> {
                })
                .show();
    }

    private void showFullScreenImage(String imageUrl) {
        if (getContext() == null) return;

        // 1. Khởi tạo Dialog
        Dialog dialog = new Dialog(getContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.setContentView(R.layout.layout_fullscreen_image);

        // 2. Ánh xạ View trong Dialog
        ImageView ivFull = dialog.findViewById(R.id.ivFullImage);
        ImageView ivClose = dialog.findViewById(R.id.ivCloseFull);

        // 3. Load ảnh bằng Glide
        Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.book_cover_placeholder) // Ảnh chờ
                .into(ivFull);

        // 4. Xử lý sự kiện đóng
        ivClose.setOnClickListener(v -> dialog.dismiss());
//        ivFull.setOnClickListener(v -> dialog.dismiss());

        // 5. Hiển thị
        dialog.show();
    }
}