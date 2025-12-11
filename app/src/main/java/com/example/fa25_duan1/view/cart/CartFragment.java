package com.example.fa25_duan1.view.cart;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fa25_duan1.R;
import com.example.fa25_duan1.adapter.CartAdapter;
import com.example.fa25_duan1.model.CartItem;
import com.example.fa25_duan1.view.detail.DetailActivity;
import com.example.fa25_duan1.view.zalo.ZaloRedirectActivity;
import com.example.fa25_duan1.viewmodel.CartViewModel;
import com.google.android.material.button.MaterialButton;

import java.text.DecimalFormat;
import java.util.ArrayList;

import io.github.cutelibs.cutedialog.CuteDialog;

public class CartFragment extends Fragment implements CartAdapter.OnCartItemClickListener {

    private RecyclerView recyclerView;
    private CartAdapter cartAdapter;
    private ArrayList<CartItem> cartItemsList = new ArrayList<>();
    private CartViewModel cartViewModel;

    private TextView tvSubtotal, tvShippingFee, tvTotal;
    private MaterialButton btnBack, btnContinue;
    private View layoutEmptyCart, scrollArea, bottomBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Dùng requireActivity để chia sẻ ViewModel nếu cần, hoặc dùng 'this' nếu chỉ riêng Fragment
        cartViewModel = new ViewModelProvider(requireActivity()).get(CartViewModel.class);

        initViews(view);
        setupRecyclerView();
        setupObservers();
        setupButtons();

        // Tải dữ liệu lần đầu (Nếu ViewModel chưa tự tải)
        // Lưu ý: ViewModel mới đã gọi refreshCart() trong constructor rồi,
        // nhưng gọi lại ở đây cũng không sao để đảm bảo dữ liệu mới nhất khi quay lại màn hình này.
        cartViewModel.refreshCart();
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_view_cart);
        tvSubtotal = view.findViewById(R.id.tv_subtotal);
        tvShippingFee = view.findViewById(R.id.tv_shipping_fee);
        tvTotal = view.findViewById(R.id.tv_total);
        btnBack = view.findViewById(R.id.btn_back);
        btnContinue = view.findViewById(R.id.btn_continue);
        layoutEmptyCart = view.findViewById(R.id.layout_empty_cart);
        scrollArea = view.findViewById(R.id.scroll_area);
        bottomBar = view.findViewById(R.id.bottom_bar);
    }

    private void setupRecyclerView() {
        if (getContext() != null) {
            cartAdapter = new CartAdapter(getContext(), cartItemsList, this);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerView.setAdapter(cartAdapter);
        }
    }

    private void setupObservers() {
        // 1. Lắng nghe danh sách giỏ hàng thay đổi
        cartViewModel.getCartItems().observe(getViewLifecycleOwner(), items -> {
            cartItemsList.clear();
            if (items != null) {
                cartItemsList.addAll(items);
            }
            cartAdapter.notifyDataSetChanged();
            checkEmptyState();
        });

        // 2. Lắng nghe tổng tiền (được tính toán tự động trong ViewModel mới)
        cartViewModel.getTotalPrice().observe(getViewLifecycleOwner(), this::updateOrderSummary);

        // LƯU Ý: Đã bỏ phần observe getMessage() vì ViewModel mới không dùng biến message chung nữa.
        // Các thông báo lỗi sẽ được xử lý trực tiếp khi gọi hàm action (tăng/giảm/xóa).
    }

    private void updateOrderSummary(long subtotal) {
        DecimalFormat formatter = new DecimalFormat("###,###,###");
        long shippingFee = (subtotal > 0) ? 25000 : 0;
        long total = subtotal + shippingFee;

        tvSubtotal.setText(formatter.format(subtotal).replace(",", ".") + " đ");
        tvShippingFee.setText(formatter.format(shippingFee).replace(",", ".") + " đ");
        tvTotal.setText(formatter.format(total).replace(",", ".") + " đ");
    }

    private void checkEmptyState() {
        if (cartItemsList.isEmpty()) {
            layoutEmptyCart.setVisibility(View.VISIBLE);
            scrollArea.setVisibility(View.GONE);
            bottomBar.setVisibility(View.GONE);
        } else {
            layoutEmptyCart.setVisibility(View.GONE);
            scrollArea.setVisibility(View.VISIBLE);
            bottomBar.setVisibility(View.VISIBLE);
        }
    }

    private void setupButtons() {
        btnBack.setOnClickListener(v -> {
            if (getActivity() != null) getActivity().onBackPressed();
        });

        btnContinue.setOnClickListener(v -> {
            if (cartItemsList.isEmpty()) {
                Toast.makeText(requireContext(), "Giỏ hàng đang trống!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Gọi hàm kiểm tra tồn kho
            cartViewModel.checkCartAvailability().observe(getViewLifecycleOwner(), isValid -> {
                if (Boolean.TRUE.equals(isValid)) {
                    // Hợp lệ -> Chuyển trang
                    Intent intent = new Intent(requireContext(), ZaloRedirectActivity.class);
                    intent.putExtra(ZaloRedirectActivity.EXTRA_HEADER_TITLE, "Trang thanh toán");
                    intent.putExtra(ZaloRedirectActivity.EXTRA_CONTENT_FRAGMENT, "checkout");
                    startActivity(intent);
                } else {
                    // Không hợp lệ -> Báo lỗi & Load lại để cập nhật số lượng thực tế
                    Toast.makeText(requireContext(),
                            "Một số sản phẩm không đủ số lượng. Đang cập nhật lại giỏ hàng...",
                            Toast.LENGTH_LONG).show();

                    cartViewModel.refreshCart();
                }
            });
        });
    }

    // --- XỬ LÝ SỰ KIỆN TĂNG / GIẢM / XÓA (LOGIC MỚI) ---

    @Override
    public void onIncreaseClick(CartItem item, int position) {
        if (item.getProduct() != null) {
            // Logic mới: Gọi hàm -> Nhận LiveData -> Observe kết quả
            cartViewModel.increaseQuantity(item.getProduct().getId())
                    .observe(getViewLifecycleOwner(), response -> {
                        if (response != null && response.isStatus()) {
                            // Thành công: Refresh lại giỏ hàng để cập nhật giá tiền
                            cartViewModel.refreshCart();
                        } else {
                            // Thất bại: Hiển thị lỗi (ví dụ: Không đủ hàng trong kho)
                            String msg = (response != null) ? response.getMessage() : "Lỗi kết nối";
                            showErrorDialog(msg);
                        }
                    });
        }
    }

    @Override
    public void onDecreaseClick(CartItem item, int position) {
        if (item.getProduct() != null) {
            if (item.getQuantity() > 1) {
                // Logic mới tương tự increase
                cartViewModel.decreaseQuantity(item.getProduct().getId())
                        .observe(getViewLifecycleOwner(), response -> {
                            if (response != null && response.isStatus()) {
                                cartViewModel.refreshCart();
                            } else {
                                String msg = (response != null) ? response.getMessage() : "Lỗi giảm số lượng";
                                showErrorDialog(msg);
                            }
                        });
            } else {
                showErrorDialog("Số lượng tối thiểu là 1. Hãy bấm nút Xóa nếu muốn bỏ sản phẩm.");
            }
        }
    }

    private void showErrorDialog(String msg) {
        new CuteDialog.withIcon(requireActivity())
                .setIcon(R.drawable.ic_dialog_error)
                .setTitle("Lỗi")
                .setDescription(msg)

                .setPrimaryColor(R.color.blue)
                .setPositiveButtonColor(R.color.blue)
                .setTitleTextColor(R.color.black)
                .setDescriptionTextColor(R.color.gray_text)

                .setPositiveButtonText("Đóng", v -> {})
                .hideNegativeButton(true)
                .show();
    }

    @Override
    public void onDeleteClick(CartItem item, int position) {
        // Logic mới cho xóa
        cartViewModel.deleteItem(item.getId())
                .observe(getViewLifecycleOwner(), isSuccess -> {
                    if (Boolean.TRUE.equals(isSuccess)) {
                        Toast.makeText(getContext(), "Đã xóa sản phẩm", Toast.LENGTH_SHORT).show();
                        cartViewModel.refreshCart();
                    } else {
                        Toast.makeText(getContext(), "Xóa thất bại", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}