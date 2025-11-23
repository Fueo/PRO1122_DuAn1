package com.example.fa25_duan1.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.fa25_duan1.repository.FavoriteRepository;

import java.util.ArrayList;
import java.util.List;

public class FavoriteViewModel extends AndroidViewModel {

    private final FavoriteRepository repository;

    // LiveData chứa danh sách các ProductID mà user đã thích
    private final MutableLiveData<List<String>> favoriteIdsLiveData = new MutableLiveData<>(new ArrayList<>());

    public FavoriteViewModel(@NonNull Application application) {
        super(application);
        repository = new FavoriteRepository(application.getApplicationContext());
        // Load danh sách yêu thích ngay khi khởi tạo
        refreshFavorites();
    }

    public LiveData<List<String>> getFavoriteIds() {
        return favoriteIdsLiveData;
    }

    /**
     * Tải lại danh sách yêu thích từ Server
     */
    public void refreshFavorites() {
        repository.getMyFavorites().observeForever(ids -> {
            if (ids != null) {
                favoriteIdsLiveData.setValue(ids);
            }
        });
    }

    /**
     * Kiểm tra nhanh xem 1 sản phẩm có nằm trong danh sách yêu thích không
     * (Hàm hỗ trợ UI, dùng trong onBindViewHolder của Adapter nếu cần)
     */
    public boolean isProductFavorited(String productId) {
        List<String> currentList = favoriteIdsLiveData.getValue();
        return currentList != null && currentList.contains(productId);
    }

    /**
     * Xử lý Toggle: Nếu chưa thích thì Add, nếu thích rồi thì Remove
     * Đồng thời cập nhật lại LiveData ngay lập tức để UI phản hồi nhanh
     */
    public void toggleFavorite(String productId) {
        List<String> currentList = favoriteIdsLiveData.getValue();
        if (currentList == null) currentList = new ArrayList<>();

        // Clone list để tránh sửa trực tiếp vào LiveData cũ
        List<String> newList = new ArrayList<>(currentList);

        if (newList.contains(productId)) {
            // Đang thích -> Bỏ thích
            newList.remove(productId);
            favoriteIdsLiveData.setValue(newList); // Update UI ngay (Optimistic UI)

            // Gọi API xóa
            repository.removeFavorite(productId).observeForever(success -> {
                if (!success) {
                    // Nếu lỗi API thì hoàn tác lại UI
                    newList.add(productId);
                    favoriteIdsLiveData.setValue(newList);
                }
            });

        } else {
            // Chưa thích -> Thêm thích
            newList.add(productId);
            favoriteIdsLiveData.setValue(newList); // Update UI ngay

            // Gọi API thêm
            repository.addFavorite(productId).observeForever(success -> {
                if (!success) {
                    // Nếu lỗi API thì hoàn tác
                    newList.remove(productId);
                    favoriteIdsLiveData.setValue(newList);
                }
            });
        }
    }
}