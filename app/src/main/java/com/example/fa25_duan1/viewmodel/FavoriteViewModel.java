package com.example.fa25_duan1.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.fa25_duan1.model.ApiResponse;
import com.example.fa25_duan1.repository.FavoriteRepository;

import java.util.ArrayList;
import java.util.List;

public class FavoriteViewModel extends AndroidViewModel {

    private final FavoriteRepository repository;

    // LiveData chứa danh sách các ProductID mà user đã thích
    private final MediatorLiveData<List<String>> favoriteIdsLiveData = new MediatorLiveData<>();
    private final MutableLiveData<String> messageLiveData = new MutableLiveData<>(); // Báo lỗi

    // Biến theo dõi nguồn repo để tránh duplicate observer khi refresh
    private LiveData<ApiResponse<List<String>>> currentRepoSource;

    public FavoriteViewModel(@NonNull Application application) {
        super(application);
        repository = new FavoriteRepository(application.getApplicationContext());
        // Load danh sách yêu thích ngay khi khởi tạo (nếu muốn)
        // refreshFavorites();
    }

    public LiveData<List<String>> getFavoriteIds() {
        return favoriteIdsLiveData;
    }

    public LiveData<String> getMessage() {
        return messageLiveData;
    }

    /**
     * Tải lại danh sách yêu thích từ Server (Cập nhật ApiResponse)
     */
    public void refreshFavorites() {
        // Hủy lắng nghe nguồn cũ nếu có
        if (currentRepoSource != null) {
            favoriteIdsLiveData.removeSource(currentRepoSource);
        }

        currentRepoSource = repository.getMyFavorites();

        favoriteIdsLiveData.addSource(currentRepoSource, apiResponse -> {
            if (apiResponse != null) {
                if (apiResponse.isStatus()) {
                    List<String> ids = apiResponse.getData();
                    favoriteIdsLiveData.setValue(ids != null ? ids : new ArrayList<>());
                } else {
                    // Log lỗi hoặc báo về UI
                    messageLiveData.setValue(apiResponse.getMessage());
                }
            }
        });
    }

    /**
     * Kiểm tra nhanh xem 1 sản phẩm có nằm trong danh sách yêu thích không
     */
    public boolean isProductFavorited(String productId) {
        List<String> currentList = favoriteIdsLiveData.getValue();
        return currentList != null && currentList.contains(productId);
    }

    /**
     * Xử lý Toggle: Optimistic UI (Cập nhật trước - Gọi API sau)
     * Trả về LiveData để Fragment có thể observe và reload lại trang chi tiết.
     */
    public LiveData<ApiResponse<Object>> toggleFavorite(String productId) {
        // 1. Tạo MediatorLiveData để trả kết quả về cho Fragment
        MediatorLiveData<ApiResponse<Object>> resultLiveData = new MediatorLiveData<>();

        // 2. Lấy danh sách hiện tại
        List<String> rawList = favoriteIdsLiveData.getValue();

        // QUAN TRỌNG: Tạo biến final để lưu trạng thái gốc -> Dùng cho Rollback trong Lambda
        final List<String> originalList = (rawList != null) ? rawList : new ArrayList<>();

        // 3. Tạo list mới để xử lý Optimistic UI (Cập nhật giao diện ngay lập tức)
        List<String> optimisticList = new ArrayList<>(originalList);
        boolean isCurrentlyFavorite = originalList.contains(productId);

        if (isCurrentlyFavorite) {
            optimisticList.remove(productId);
        } else {
            optimisticList.add(productId);
        }

        // Cập nhật UI ngay lập tức (icon tim sẽ đổi màu ngay)
        favoriteIdsLiveData.setValue(optimisticList);

        // 4. Gọi API tương ứng
        LiveData<ApiResponse<Object>> apiSource;
        if (isCurrentlyFavorite) {
            apiSource = repository.removeFavorite(productId);
        } else {
            apiSource = repository.addFavorite(productId);
        }

        // 5. Lắng nghe kết quả từ API và trả về cho Fragment
        resultLiveData.addSource(apiSource, apiResponse -> {
            // Kiểm tra kết quả
            if (apiResponse == null || !apiResponse.isStatus()) {
                // --- THẤT BẠI: ROLLBACK ---
                // Trả lại danh sách gốc ban đầu cho UI
                favoriteIdsLiveData.setValue(originalList);

                String msg = (apiResponse != null) ? apiResponse.getMessage() : "Lỗi kết nối server";
                messageLiveData.setValue("Thao tác thất bại: " + msg);
            }

            // Đẩy kết quả ra cho Fragment (để Fragment biết mà gọi getProductData)
            resultLiveData.setValue(apiResponse);

            // Hủy lắng nghe nguồn này (Cleanup)
            resultLiveData.removeSource(apiSource);
        });

        return resultLiveData;
    }
}