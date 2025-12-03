package com.example.fa25_duan1.view.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fa25_duan1.R;
import com.example.fa25_duan1.adapter.OrderAdapter;
import com.example.fa25_duan1.model.CartItem;
import com.example.fa25_duan1.model.Order;
import com.example.fa25_duan1.model.OrderDetail;
import com.example.fa25_duan1.view.detail.DetailActivity;
import com.example.fa25_duan1.viewmodel.OrderViewModel;
import com.example.fa25_duan1.viewmodel.CartViewModel; // 1. Import CartViewModel

import com.shashank.sony.fancytoastlib.FancyToast;

import java.util.ArrayList;
import java.util.List;

import io.github.cutelibs.cutedialog.CuteDialog;

public class OrderHistoryFragment extends Fragment {

    private RecyclerView rvOrders;
    private View layoutEmpty;
    private OrderAdapter orderAdapter;
    private List<Order> orderList = new ArrayList<>();

    private OrderViewModel orderViewModel;
    private CartViewModel cartViewModel; // 2. Khai báo biến

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_purchasehistory, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvOrders = view.findViewById(R.id.rvOrders);
        layoutEmpty = view.findViewById(R.id.layout_empty_cart);

        rvOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        rvOrders.setHasFixedSize(true);

        // 3. Khởi tạo ViewModel
        // Dùng requireActivity() để CartViewModel sống chung với Activity (giữ data giỏ hàng toàn cục)
        orderViewModel = new ViewModelProvider(this).get(OrderViewModel.class);
        cartViewModel = new ViewModelProvider(requireActivity()).get(CartViewModel.class);

        orderAdapter = new OrderAdapter(getContext(), orderList, new OrderAdapter.OnOrderActionListener() {
            @Override
            public void onCancelOrder(String orderId) {
                showConfirmCancelDialog(orderId);
            }

            @Override
            public void onRepurchase(Order order) {
                handleRepurchaseOrder(order);
            }
        });
        rvOrders.setAdapter(orderAdapter);

        setupObservers();
        orderViewModel.fetchOrderHistory();
    }

    private void setupObservers() {
        orderViewModel.getOrderHistory().observe(getViewLifecycleOwner(), orders -> {
            orderList.clear();
            if (orders != null) {
                orderList.addAll(orders);
            }
            orderAdapter.notifyDataSetChanged();

            if (orderList.isEmpty()) {
                rvOrders.setVisibility(View.GONE);
                layoutEmpty.setVisibility(View.VISIBLE);
            } else {
                rvOrders.setVisibility(View.VISIBLE);
                layoutEmpty.setVisibility(View.GONE);
            }
        });
    }

    private void handleRepurchaseOrder(Order order) {
    }

    /**
     * Helper: observe LiveData chỉ 1 lần
     */
    private <T> void observeOnce(LiveData<T> liveData, Observer<T> observer) {
        liveData.observe(getViewLifecycleOwner(), new Observer<T>() {
            @Override
            public void onChanged(T t) {
                liveData.removeObserver(this);
                observer.onChanged(t);
            }
        });
    }

    private void addItemsFromOrderToCart(Order order) {
        FancyToast.makeText(getContext(), "Đang thêm sản phẩm vào giỏ...", FancyToast.LENGTH_SHORT, FancyToast.INFO, false).show();

        for (OrderDetail item : order.getOrderDetails()) {

            if (item.getProduct() != null && item.getProduct().getId() != null) {

                String productId = String.valueOf(item.getProduct().getId());
                int quantity = item.getQuantity();

                // Thêm đúng số lượng đã mua
                for (int i = 0; i < quantity; i++) {
                    cartViewModel.increaseQuantity(productId).observe(getViewLifecycleOwner(), res -> {
                        cartViewModel.refreshCart(); // refresh ngầm
                    });
                }
            }
        }

        FancyToast.makeText(getContext(), "Đã thêm vào giỏ hàng!", FancyToast.LENGTH_SHORT, FancyToast.SUCCESS, false).show();

        Intent intent = new Intent(getContext(), DetailActivity.class);
        intent.putExtra(DetailActivity.EXTRA_HEADER_TITLE, "Giỏ hàng");
        intent.putExtra(DetailActivity.EXTRA_CONTENT_FRAGMENT, "cart");
        startActivity(intent);
        requireActivity().finish();
    }

    private void showConfirmCancelDialog(String orderId) {
        new CuteDialog.withIcon(requireActivity())
                .setIcon(R.drawable.ic_dialog_confirm)
                .setTitle("Xác nhận hủy đơn")
                .setDescription("Bạn có chắc chắn muốn hủy đơn hàng này không?")
                .setPrimaryColor(R.color.blue)
                .setPositiveButtonColor(R.color.blue)
                .setPositiveButtonText("Xác nhận", v -> performCancelOrder(orderId))
                .setNegativeButtonText("Hủy", v -> {})
                .show();
    }

    private void performCancelOrder(String orderId) {
        orderViewModel.cancelOrder(orderId).observe(getViewLifecycleOwner(), response -> {
            if (response != null && response.isStatus()) {
                FancyToast.makeText(getContext(), "Đã hủy đơn hàng thành công", FancyToast.LENGTH_SHORT, FancyToast.SUCCESS, false).show();
                orderViewModel.fetchOrderHistory();
            } else {
                String msg = (response != null) ? response.getMessage() : "Lỗi khi hủy đơn";
                FancyToast.makeText(getContext(), msg, FancyToast.LENGTH_SHORT, FancyToast.ERROR, false).show();
            }
        });
    }
}