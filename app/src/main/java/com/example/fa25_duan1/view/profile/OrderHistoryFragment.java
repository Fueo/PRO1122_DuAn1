package com.example.fa25_duan1.view.profile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.example.fa25_duan1.model.Order;
import com.example.fa25_duan1.model.OrderDetail;
import com.example.fa25_duan1.view.detail.DetailActivity;
import com.example.fa25_duan1.viewmodel.CartViewModel;
import com.example.fa25_duan1.viewmodel.OrderViewModel;
import com.shashank.sony.fancytoastlib.FancyToast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.github.cutelibs.cutedialog.CuteDialog;
import vn.zalopay.sdk.ZaloPayError;
import vn.zalopay.sdk.ZaloPaySDK;
import vn.zalopay.sdk.listeners.PayOrderListener;

public class OrderHistoryFragment extends Fragment {

    private RecyclerView rvOrders;
    private View layoutEmpty;
    private OrderAdapter orderAdapter;
    private List<Order> orderList = new ArrayList<>();

    private OrderViewModel orderViewModel;
    private CartViewModel cartViewModel;

    // === CÁC BIẾN KIỂM SOÁT ZALOPAY ===
    // 1. Cờ đánh dấu đang kiểm tra trạng thái (tránh gọi chồng chéo)
    private boolean isCheckingStatus = false;

    // 2. [QUAN TRỌNG] Danh sách các mã giao dịch ĐÃ XỬ LÝ THÀNH CÔNG.
    // Dùng Set để chặn SDK gọi lại lần 2, lần 3 gây treo app.
    private Set<String> processedTransactions = new HashSet<>();

    // 3. Giữ tham chiếu Dialog Loading để tắt nó thủ công nếu cần
    private CuteDialog.withIcon loadingDialogRef = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_purchasehistory, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        initViewModels();
        setupRecyclerView();
        setupObservers();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Chỉ fetch data để update UI, không gọi check status ở đây nữa
        if (orderViewModel != null) {
            orderViewModel.fetchOrderHistory();
        }
    }

    private void initViews(View view) {
        rvOrders = view.findViewById(R.id.rvOrders);
        layoutEmpty = view.findViewById(R.id.layout_empty_cart);
    }

    private void initViewModels() {
        orderViewModel = new ViewModelProvider(this).get(OrderViewModel.class);
        cartViewModel = new ViewModelProvider(requireActivity()).get(CartViewModel.class);
    }

    private void setupRecyclerView() {
        rvOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        rvOrders.setHasFixedSize(true);

        orderAdapter = new OrderAdapter(getContext(), orderList, new OrderAdapter.OnOrderActionListener() {
            @Override
            public void onCancelOrder(String orderId) { showConfirmCancelDialog(orderId); }
            @Override
            public void onRepurchase(Order order) { handleRepurchaseOrder(order); }
            @Override
            public void onViewDetail(Order order) { openViewActivity(order); }
            @Override
            public void onPayNowClick(Order order) { handlePayNow(order); }
        });
        rvOrders.setAdapter(orderAdapter);
    }

    private void setupObservers() {
        orderViewModel.getOrderHistory().observe(getViewLifecycleOwner(), orders -> {
            orderList.clear();
            if (orders != null) orderList.addAll(orders);
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

    // =========================================================================
    // LOGIC ZALOPAY (ĐÃ FIX LỖI TREO & DOUBLE DIALOG)
    // =========================================================================

    private void handlePayNow(Order order) {
        if (order == null) return;
        String paymentMethod = (order.getPaymentMethod() != null) ? order.getPaymentMethod() : "";

        if (paymentMethod.equalsIgnoreCase("cod")) {
            FancyToast.makeText(getContext(), "Đơn này COD.", FancyToast.LENGTH_SHORT, FancyToast.INFO, false).show();
        } else if (paymentMethod.equalsIgnoreCase("qr")) {
            Intent intent = new Intent(getContext(), DetailActivity.class);
            intent.putExtra(DetailActivity.EXTRA_HEADER_TITLE, "Thanh toán Online");
            intent.putExtra(DetailActivity.EXTRA_CONTENT_FRAGMENT, "payment");
            intent.putExtra("ORDER_ID", order.getId());
            intent.putExtra("TOTAL_AMOUNT", (long) order.getTotal());
            String transCode = (order.getTransactionCode() != null) ? order.getTransactionCode() : order.getId();
            intent.putExtra("TRANS_CODE", transCode);
            startActivity(intent);
        } else if (paymentMethod.equalsIgnoreCase("zalopay")) {
            new CuteDialog.withIcon(requireActivity())
                    .setIcon(R.drawable.ic_dialog_confirm)
                    .setTitle("Thanh toán ZaloPay")
                    .setDescription("Mở ZaloPay để thanh toán?")
                    .setPrimaryColor(R.color.blue)
                    .setPositiveButtonText("Mở App", v -> requestZaloPayToken(order.getId()))
                    .setNegativeButtonText("Hủy", v -> {})
                    .show();
        } else {
            FancyToast.makeText(getContext(), "Chưa hỗ trợ!", FancyToast.LENGTH_SHORT, FancyToast.WARNING, false).show();
        }
    }

    private void requestZaloPayToken(String orderId) {
        FancyToast.makeText(getContext(), "Đang khởi tạo...", FancyToast.LENGTH_SHORT, FancyToast.INFO, false).show();
        orderViewModel.createZaloPayPayment(orderId).observe(getViewLifecycleOwner(), response -> {
            if (response != null && response.isStatus() && response.getData() != null) {
                String zpToken = response.getData().getZpTransToken();
                String appTransId = response.getData().getAppTransId();

                if (zpToken != null && !zpToken.isEmpty()) {
                    requestZaloPaySDK(zpToken, appTransId);
                } else {
                    FancyToast.makeText(getContext(), "Lỗi Token!", FancyToast.LENGTH_SHORT, FancyToast.ERROR, true).show();
                }
            } else {
                FancyToast.makeText(getContext(), "Lỗi kết nối Server", FancyToast.LENGTH_SHORT, FancyToast.ERROR, true).show();
            }
        });
    }

    private void requestZaloPaySDK(String zpToken, String appTransId) {
        if (!isAdded() || getActivity() == null) return;

        ZaloPaySDK.getInstance().payOrder(requireActivity(), zpToken, "demozpdk://app", new PayOrderListener() {
            @Override
            public void onPaymentSucceeded(String transactionId, String transToken, String returnedAppTransID) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        // [CHỐT CHẶN 1]
                        // Nếu mã này đã có trong danh sách đã xử lý -> Dừng ngay lập tức
                        if (processedTransactions.contains(returnedAppTransID)) {
                            return;
                        }

                        // Sử dụng mã trả về từ SDK hoặc mã gốc
                        String finalId = (returnedAppTransID != null && !returnedAppTransID.isEmpty()) ? returnedAppTransID : appTransId;
                        checkZaloPayStatusFromBackend(finalId, 3);
                    });
                }
            }

            @Override
            public void onPaymentCanceled(String zpTransToken, String appTransID) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        FancyToast.makeText(getContext(), "Đã hủy thanh toán", FancyToast.LENGTH_SHORT, FancyToast.WARNING, false).show();
                        orderViewModel.fetchOrderHistory();
                    });
                }
            }

            @Override
            public void onPaymentError(ZaloPayError zaloPayError, String zpTransToken, String appTransID) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (zaloPayError == ZaloPayError.PAYMENT_APP_NOT_FOUND) openZaloPayOnStore();
                        else FancyToast.makeText(getContext(), "Lỗi ZaloPay: " + zaloPayError.toString(), FancyToast.LENGTH_SHORT, FancyToast.ERROR, false).show();
                    });
                }
            }
        });
    }

    // Hàm đệ quy kiểm tra trạng thái
    private void checkZaloPayStatusFromBackend(String appTransId, int retryCount) {

        // [CHỐT CHẶN 2] Kiểm tra lại lần nữa trong hàm
        if (processedTransactions.contains(appTransId)) return;

        // [CHỐT CHẶN 3] Nếu đang check rồi thì thôi
        if (isCheckingStatus && retryCount == 3) return;

        // Bắt đầu check
        if (retryCount == 3) {
            isCheckingStatus = true;

            // Tạo Dialog mới và lưu vào biến tham chiếu
            loadingDialogRef = new CuteDialog.withIcon(requireActivity())
                    .setIcon(R.drawable.ic_dialog_info)
                    .setTitle("Đang xác thực...")
                    .setDescription("Vui lòng đợi...")
                    .hideNegativeButton(true).hidePositiveButton(true);
            loadingDialogRef.show();

            // Auto dismiss sau 8s phòng trường hợp bị treo
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if(loadingDialogRef != null && loadingDialogRef.isShowing()) {
                    try { loadingDialogRef.dismiss(); } catch (Exception e) {}
                }
            }, 8000);
        }

        orderViewModel.checkZaloPayStatus(appTransId).observe(getViewLifecycleOwner(), response -> {
            // Fragment đã đóng -> Dừng
            if (!isAdded() || getActivity() == null) return;

            boolean isValid = (response != null && response.getData() != null);
            int returnCode = -1;
            if (isValid) {
                try {
                    Object rc = response.getData().get("return_code");
                    returnCode = (rc instanceof Number) ? ((Number) rc).intValue() : Double.valueOf(String.valueOf(rc)).intValue();
                } catch (Exception e) { returnCode = -1; }
            }

            // === TRƯỜNG HỢP 1: THÀNH CÔNG ===
            if (isValid && returnCode == 1) {
                // Tắt loading dialog trước
                if (loadingDialogRef != null && loadingDialogRef.isShowing()) loadingDialogRef.dismiss();

                // Đánh dấu mã này đã xong -> Chặn SDK gọi lại lần sau
                processedTransactions.add(appTransId);
                isCheckingStatus = false;

                new CuteDialog.withIcon(requireActivity())
                        .setIcon(R.drawable.ic_check_circle)
                        .setTitle("Thanh toán thành công!")
                        .setDescription("Đơn hàng đã được cập nhật.")
                        .setPositiveButtonText("Đóng", v -> orderViewModel.fetchOrderHistory())
                        .hideNegativeButton(true)
                        .show();
                return;
            }

            // === TRƯỜNG HỢP 2: CẦN THỬ LẠI ===
            if (retryCount > 0) {
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    checkZaloPayStatusFromBackend(appTransId, retryCount - 1);
                }, 2000);
            } else {
                // === TRƯỜNG HỢP 3: THẤT BẠI ===
                // Tắt loading dialog trước
                if (loadingDialogRef != null && loadingDialogRef.isShowing()) loadingDialogRef.dismiss();

                isCheckingStatus = false;

                new CuteDialog.withIcon(requireActivity())
                        .setIcon(R.drawable.ic_dialog_warning)
                        .setTitle("Đang xử lý")
                        .setDescription("Hệ thống chưa nhận được kết quả. Vui lòng kiểm tra lại sau.")
                        .setPositiveButtonText("Kiểm tra lại", v -> checkZaloPayStatusFromBackend(appTransId, 3))
                        .setNegativeButtonText("Đóng", v -> orderViewModel.fetchOrderHistory())
                        .show();
            }
        });
    }

    private void openZaloPayOnStore() {
        try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=vn.com.vng.zalopay"))); }
        catch (Exception e) { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=vn.com.vng.zalopay"))); }
    }

    // ... (Các hàm handleRepurchaseOrder, showConfirmCancelDialog, ... giữ nguyên)
    private void handleRepurchaseOrder(Order order) {
        FancyToast.makeText(getContext(), "Đang thêm vào giỏ...", FancyToast.LENGTH_SHORT, FancyToast.INFO, false).show();
        observeOnce(cartViewModel.clearCart(), isCleared -> prepareAndAddItems(order));
    }
    private void prepareAndAddItems(Order order) {
        if (order.getOrderDetails() == null || order.getOrderDetails().isEmpty()) return;
        List<OrderDetail> itemsToAdd = new ArrayList<>();
        for (OrderDetail item : order.getOrderDetails()) {
            if (item.getProduct() != null && item.getProduct().getId() != null) itemsToAdd.add(item);
        }
        if (!itemsToAdd.isEmpty()) addSingleItemRecursive(itemsToAdd, 0);
    }
    private void addSingleItemRecursive(List<OrderDetail> items, int index) {
        if (index >= items.size()) {
            cartViewModel.refreshCart();
            Intent intent = new Intent(getContext(), DetailActivity.class);
            intent.putExtra(DetailActivity.EXTRA_HEADER_TITLE, "Giỏ hàng");
            intent.putExtra(DetailActivity.EXTRA_CONTENT_FRAGMENT, "cart");
            startActivity(intent);
            return;
        }
        OrderDetail currentItem = items.get(index);
        observeOnce(cartViewModel.addToCart(String.valueOf(currentItem.getProduct().getId()), currentItem.getQuantity()), response -> addSingleItemRecursive(items, index + 1));
    }
    private void showConfirmCancelDialog(String orderId) {
        new CuteDialog.withIcon(requireActivity()).setIcon(R.drawable.ic_dialog_confirm).setTitle("Hủy đơn?").setDescription("Bạn muốn hủy đơn này?").setPrimaryColor(R.color.red)
                .setPositiveButtonText("Hủy", v -> performCancelOrder(orderId)).setNegativeButtonText("Đóng", v -> {}).show();
    }
    private void performCancelOrder(String orderId) {
        orderViewModel.cancelOrder(orderId).observe(getViewLifecycleOwner(), response -> {
            if(response!=null && response.isStatus()) orderViewModel.fetchOrderHistory();
            else FancyToast.makeText(getContext(), "Lỗi hủy đơn", FancyToast.LENGTH_SHORT, FancyToast.ERROR, false).show();
        });
    }
    private void openViewActivity(Order order) {
        if (order == null) return;
        Intent intent = new Intent(getContext(), DetailActivity.class);
        intent.putExtra(DetailActivity.EXTRA_HEADER_TITLE, "Chi tiết đơn");
        intent.putExtra(DetailActivity.EXTRA_CONTENT_FRAGMENT, "order");
        intent.putExtra("orderId", order.getId());
        startActivity(intent);
    }
    private <T> void observeOnce(LiveData<T> liveData, Observer<T> observer) {
        liveData.observe(getViewLifecycleOwner(), new Observer<T>() {
            @Override public void onChanged(T t) { liveData.removeObserver(this); observer.onChanged(t); }
        });
    }
}