package com.example.fa25_duan1.view.detail;

import android.app.Dialog;
import android.content.Intent; // Import thêm Intent
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
import com.example.fa25_duan1.viewmodel.FavoriteViewModel;
import com.example.fa25_duan1.viewmodel.ProductViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.shashank.sony.fancytoastlib.FancyToast;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import io.github.cutelibs.cutedialog.CuteDialog;

public class DetailFragment extends Fragment {

    // --- Views ---
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
    private FavoriteViewModel favoriteViewModel;

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

        // Setup Adapter
        setupRelatedProductsAdapter();

        // Setup lắng nghe dữ liệu
        setupDataObservation();

        getProductData();
        setupEvents();
    }

    private void initViews(View view) {
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
        btnBuyNow = view.findViewById(R.id.btnEdit); // ID nút Mua ngay

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
        favoriteViewModel = new ViewModelProvider(this).get(FavoriteViewModel.class);
    }

    private void setupRelatedProductsAdapter() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
        rvRelatedProducts.setLayoutManager(gridLayoutManager);
        rvRelatedProducts.setNestedScrollingEnabled(false);

        relatedProductAdapter = new BookGridAdapter(getContext(), new ArrayList<>(), new BookGridAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Product product) {
                navigateToProductDetail(product.getId());
            }

            @Override
            public void onFavoriteClick(String productId) {
                favoriteViewModel.toggleFavorite(productId);
            }

            @Override
            public void onAddToCartClick(Product product) {
                // Thêm vào giỏ hàng (không chuyển màn hình)
                addToCartLogic(product.getId());
            }

            // --- [MỚI] XỬ LÝ NÚT MUA NGAY TRÊN SẢN PHẨM LIÊN QUAN ---
            @Override
            public void onBuyNowClick(Product product) {
                handleBuyNow(product.getId());
            }
        });

        rvRelatedProducts.setAdapter(relatedProductAdapter);
    }

    private void setupDataObservation() {
        favoriteViewModel.getFavoriteIds().observe(getViewLifecycleOwner(), ids -> {
            if (relatedProductAdapter != null) {
                relatedProductAdapter.setFavoriteIds(ids);
            }
            if (currentProduct != null) {
                if (ids != null && ids.contains(currentProduct.getId())) {
                    ivHeart.setImageResource(R.drawable.ic_heart_filled_red);
                } else {
                    ivHeart.setImageResource(R.drawable.ic_heart_outline_gray);
                }
            }
        });
    }

    // --- LOGIC THÊM VÀO GIỎ (NÚT CART) ---
    private void addToCartLogic(String productId) {
        btnAddToCart.setEnabled(false);
        btnBuyNow.setEnabled(false);

        cartViewModel.increaseQuantity(productId).observe(getViewLifecycleOwner(), response -> {
            btnAddToCart.setEnabled(true);
            btnBuyNow.setEnabled(true);

            if (response != null && response.isStatus()) {
                cartViewModel.refreshCart();
                FancyToast.makeText(getContext(), "Đã thêm vào giỏ hàng", FancyToast.LENGTH_SHORT, FancyToast.SUCCESS, true).show();
            } else {
                String msg = (response != null) ? response.getMessage() : "Lỗi kết nối hoặc hết hàng";
                showErrorDialog(msg);
            }
        });
    }

    // --- [MỚI] LOGIC MUA NGAY (NÚT MUA NGAY) ---
    private void handleBuyNow(String productId) {
        if (productId == null) return;
        // 2. Gọi API thêm vào giỏ
        cartViewModel.increaseQuantity(productId).observe(getViewLifecycleOwner(), response -> {
            // 3. Tắt Loading

            if (response != null && response.isStatus()) {
                // Thành công -> Refresh lại giỏ hàng
                cartViewModel.refreshCart();

                // 4. Chuyển sang màn hình Giỏ hàng (DetailActivity - Fragment Cart)
                Intent intent = new Intent(getContext(), DetailActivity.class);
                intent.putExtra(DetailActivity.EXTRA_HEADER_TITLE, "Giỏ hàng");
                intent.putExtra(DetailActivity.EXTRA_CONTENT_FRAGMENT, "cart");
                startActivity(intent);

            } else {
                // Thất bại
                String msg = (response != null && response.getMessage() != null)
                        ? response.getMessage()
                        : "Lỗi kết nối server";
                showErrorDialog(msg);
            }
        });
    }

    private void setupEvents() {
        ivImage.setOnClickListener(v -> {
            if (currentProduct != null && currentProduct.getImage() != null) {
                showFullScreenImage(currentProduct.getImage());
            }
        });

        ivHeart.setOnClickListener(v -> {
            if (currentProduct != null) {
                List<String> currentIds = favoriteViewModel.getFavoriteIds().getValue();
                boolean isCurrentlyFavorite = currentIds != null && currentIds.contains(currentProduct.getId());

                try {
                    int currentCount = Integer.parseInt(tvLikes.getText().toString());
                    if (isCurrentlyFavorite) {
                        currentCount = Math.max(0, currentCount - 1);
                    } else {
                        currentCount = currentCount + 1;
                    }
                    tvLikes.setText(String.valueOf(currentCount));
                    currentProduct.setFavorite(currentCount);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }

                favoriteViewModel.toggleFavorite(currentProduct.getId());

                v.animate().scaleX(1.2f).scaleY(1.2f).setDuration(100).withEndAction(() ->
                        v.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                ).start();
            }
        });

        // Nút Thêm vào giỏ (Chỉ thêm, không chuyển trang)
        btnAddToCart.setOnClickListener(v -> {
            if (currentProduct != null) {
                addToCartLogic(currentProduct.getId());
            }
        });

        // Nút Mua ngay (Thêm + Chuyển trang thanh toán)
        btnBuyNow.setOnClickListener(v -> {
            if (currentProduct != null) {
                handleBuyNow(currentProduct.getId());
            }
        });
    }

    // ... (Các phần code hiển thị dữ liệu giữ nguyên) ...

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
                    favoriteViewModel.refreshFavorites();
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
                relatedProductAdapter.setProducts(listRelated);
            }
        });
    }

    private void bindDataToUI(Product product) {
        if (product.getImage() != null && !product.getImage().isEmpty()) {
            Glide.with(this).load(product.getImage()).placeholder(R.drawable.book_cover_placeholder).into(ivImage);
        }
        tvName.setText(product.getName());
        tvLikes.setText(String.valueOf(product.getFavorite()));
        tvPages.setText(String.valueOf(product.getPages()));
        tvViews.setText(String.valueOf(product.getView()));

        try {
            if (product.getPublishDate() != null && !product.getPublishDate().isEmpty()) {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
                inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Date date = inputFormat.parse(product.getPublishDate());
                tvPublished.setText(date != null ? outputFormat.format(date) : product.getPublishDate());
            } else {
                tvPublished.setText("N/A");
            }
        } catch (Exception e) {
            tvPublished.setText("N/A");
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
            tvAuthorBio.setText(product.getAuthor().getDescription() != null ? product.getAuthor().getDescription() : "Chưa có thông tin tiểu sử.");
            if (product.getAuthor().getAvatar() != null) {
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
                .hideNegativeButton(true)
                .setPositiveButtonText("Đóng", v -> {})
                .show();
    }

    private void showFullScreenImage(String imageUrl) {
        if (getContext() == null) return;
        Dialog dialog = new Dialog(getContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.setContentView(R.layout.layout_fullscreen_image);
        ImageView ivFull = dialog.findViewById(R.id.ivFullImage);
        ImageView ivClose = dialog.findViewById(R.id.ivCloseFull);
        Glide.with(this).load(imageUrl).placeholder(R.drawable.book_cover_placeholder).into(ivFull);
        ivClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
}