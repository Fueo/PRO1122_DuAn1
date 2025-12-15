package com.example.fa25_duan1.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.fa25_duan1.model.Address;
import com.example.fa25_duan1.model.ApiResponse; // Import ApiResponse
import com.example.fa25_duan1.repository.AddressRepository;

import java.util.ArrayList;
import java.util.List;

public class AddressViewModel extends AndroidViewModel {

    private final AddressRepository repository;

    // Nguồn dữ liệu gốc
    private final MediatorLiveData<List<Address>> allAddressesLiveData = new MediatorLiveData<>();

    // Danh sách hiển thị trên UI (đã qua filter/search)
    private final MediatorLiveData<List<Address>> displayedAddressesLiveData = new MediatorLiveData<>();

    // LiveData để báo lỗi cho View (Fragment) nếu cần
    private final MutableLiveData<String> messageLiveData = new MutableLiveData<>();

    // Biến theo dõi nguồn từ repo để refresh (Kiểu dữ liệu thay đổi thành ApiResponse)
    private LiveData<ApiResponse<List<Address>>> currentRepoSource;

    public AddressViewModel(@NonNull Application application) {
        super(application);
        repository = new AddressRepository(application.getApplicationContext());
        // Tải dữ liệu lần đầu
        refreshData();
    }

    public LiveData<List<Address>> getDisplayedAddresses() {
        return displayedAddressesLiveData;
    }

    public LiveData<String> getMessage() {
        return messageLiveData;
    }

    /**
     * Tải lại danh sách từ Server (Đã sửa để xử lý ApiResponse)
     */
    public void refreshData() {
        if (currentRepoSource != null) {
            displayedAddressesLiveData.removeSource(currentRepoSource);
        }

        // Gọi Repository (trả về ApiResponse)
        currentRepoSource = repository.getMyAddresses();

        displayedAddressesLiveData.addSource(currentRepoSource, apiResponse -> {
            List<Address> addresses = new ArrayList<>();

            if (apiResponse != null) {
                if (apiResponse.isStatus()) {
                    // Thành công: Lấy data
                    if (apiResponse.getData() != null) {
                        addresses = apiResponse.getData();
                    }
                } else {
                    // Thất bại: Gửi thông báo lỗi
                    messageLiveData.setValue(apiResponse.getMessage());
                }
            } else {
                messageLiveData.setValue("Lỗi kết nối");
            }

            // Cập nhật list gốc
            allAddressesLiveData.setValue(addresses);

            // Cập nhật list hiển thị (Copy để tránh tham chiếu)
            displayedAddressesLiveData.setValue(new ArrayList<>(addresses));
        });
    }

    // --- CRUD WRAPPERS (CẬP NHẬT KIỂU TRẢ VỀ) ---

    public LiveData<ApiResponse<Address>> addAddress(Address address) {
        return repository.addAddress(address);
    }

    public LiveData<ApiResponse<Address>> updateAddress(String id, Address address) {
        return repository.updateAddress(id, address);
    }

    // Delete trả về ApiResponse<Void> thay vì Boolean
    public LiveData<ApiResponse<Void>> deleteAddress(String id) {
        return repository.deleteAddress(id);
    }

    // --- CÁC HÀM SEARCH/FILTER GIỮ NGUYÊN (VÌ CHẠY TRÊN LIST ĐÃ TẢI VỀ) ---

    public void searchAddresses(String query) {
        List<Address> masterList = allAddressesLiveData.getValue();
        if (masterList == null) masterList = new ArrayList<>();

        if (query == null || query.trim().isEmpty()) {
            displayedAddressesLiveData.setValue(new ArrayList<>(masterList));
            return;
        }

        String q = query.toLowerCase().trim();
        List<Address> result = new ArrayList<>();

        for (Address a : masterList) {
            if ((a.getName() != null && a.getName().toLowerCase().contains(q)) ||
                    (a.getAddress() != null && a.getAddress().toLowerCase().contains(q)) ||
                    (a.getPhone() != null && a.getPhone().toLowerCase().contains(q))) {
                result.add(a);
            }
        }
        displayedAddressesLiveData.setValue(result);
    }

    public void filterByTag(String tag) {
        List<Address> masterList = allAddressesLiveData.getValue();
        if (masterList == null) masterList = new ArrayList<>();

        if (tag == null || tag.equals("Tất cả")) {
            displayedAddressesLiveData.setValue(new ArrayList<>(masterList));
            return;
        }

        List<Address> result = new ArrayList<>();
        for (Address a : masterList) {
            if (a.getTag() != null && a.getTag().equalsIgnoreCase(tag)) {
                result.add(a);
            }
        }
        displayedAddressesLiveData.setValue(result);
    }
}