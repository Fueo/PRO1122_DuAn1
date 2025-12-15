package com.example.fa25_duan1.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.fa25_duan1.model.ApiResponse; // Import ApiResponse
import com.example.fa25_duan1.model.Discount;
import com.example.fa25_duan1.repository.DiscountRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DiscountViewModel extends AndroidViewModel {

    private final DiscountRepository repository;

    // Danh sách hiển thị trên UI (đã qua filter/search)
    private final MediatorLiveData<List<Discount>> displayedDiscounts = new MediatorLiveData<>();

    // Danh sách gốc từ Server để phục vụ search/sort local
    private List<Discount> originalList = new ArrayList<>();

    // LiveData báo lỗi
    private final MutableLiveData<String> messageLiveData = new MutableLiveData<>();

    // Biến theo dõi nguồn repo
    private LiveData<ApiResponse<List<Discount>>> currentRepoSource;

    public DiscountViewModel(@NonNull Application application) {
        super(application);
        repository = new DiscountRepository(application.getApplicationContext());
        refreshData();
    }

    public LiveData<List<Discount>> getDisplayedDiscounts() {
        return displayedDiscounts;
    }

    public LiveData<String> getMessage() {
        return messageLiveData;
    }

    /**
     * Tải dữ liệu từ Server
     */
    public void refreshData() {
        if (currentRepoSource != null) {
            displayedDiscounts.removeSource(currentRepoSource);
        }

        currentRepoSource = repository.getAllDiscounts();

        displayedDiscounts.addSource(currentRepoSource, apiResponse -> {
            if (apiResponse != null) {
                if (apiResponse.isStatus()) {
                    // Thành công: Cập nhật list gốc và list hiển thị
                    originalList = apiResponse.getData() != null ? apiResponse.getData() : new ArrayList<>();
                    displayedDiscounts.setValue(new ArrayList<>(originalList));
                } else {
                    // Thất bại
                    messageLiveData.setValue(apiResponse.getMessage());
                }
            } else {
                messageLiveData.setValue("Lỗi kết nối");
            }
        });
    }

    // --- CRUD (Cập nhật kiểu trả về thành ApiResponse) ---

    public LiveData<ApiResponse<Discount>> addDiscount(Discount discount) {
        return repository.addDiscount(discount);
    }

    public LiveData<ApiResponse<Discount>> updateDiscount(String id, Discount discount) {
        return repository.updateDiscount(id, discount);
    }

    public LiveData<ApiResponse<Void>> deleteDiscount(String id) {
        return repository.deleteDiscount(id);
    }

    public LiveData<ApiResponse<Discount>> getDiscountByID(String id) {
        return repository.getDiscountById(id);
    }

    // --- SEARCH & SORT (LOCAL) ---

    public void searchDiscounts(String query) {
        if (query == null || query.trim().isEmpty()) {
            displayedDiscounts.setValue(new ArrayList<>(originalList));
        } else {
            List<Discount> filtered = new ArrayList<>();
            String q = query.toLowerCase().trim();
            for (Discount d : originalList) {
                // Tìm theo tên mã hoặc mã code (nếu có field code)
                if (d.getDiscountName() != null && d.getDiscountName().toLowerCase().contains(q)) {
                    filtered.add(d);
                }
            }
            displayedDiscounts.setValue(filtered);
        }
    }

    public void sortDiscounts(int position) {
        // Lấy list hiện tại đang hiển thị để sort (có thể đang là list search)
        List<Discount> currentList = new ArrayList<>(
                displayedDiscounts.getValue() != null ? displayedDiscounts.getValue() : originalList
        );

        switch (position) {
            case 0: // Mới nhất
                Collections.sort(currentList, (o1, o2) -> {
                    if (o1.getCreateAt() == null || o2.getCreateAt() == null) return 0;
                    return o2.getCreateAt().compareTo(o1.getCreateAt());
                });
                break;
            case 1: // Cũ nhất
                Collections.sort(currentList, (o1, o2) -> {
                    if (o1.getCreateAt() == null || o2.getCreateAt() == null) return 0;
                    return o1.getCreateAt().compareTo(o2.getCreateAt());
                });
                break;
            case 2: // A-Z
                Collections.sort(currentList, (o1, o2) -> {
                    String name1 = o1.getDiscountName() != null ? o1.getDiscountName() : "";
                    String name2 = o2.getDiscountName() != null ? o2.getDiscountName() : "";
                    return name1.compareToIgnoreCase(name2);
                });
                break;
        }
        displayedDiscounts.setValue(currentList);
    }
}