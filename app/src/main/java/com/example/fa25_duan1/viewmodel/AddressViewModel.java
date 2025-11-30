package com.example.fa25_duan1.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.example.fa25_duan1.model.Address;
import com.example.fa25_duan1.repository.AddressRepository;

import java.util.ArrayList;
import java.util.List;

public class AddressViewModel extends AndroidViewModel {

    private final AddressRepository repository;

    // Nguồn dữ liệu gốc
    private final MediatorLiveData<List<Address>> allAddressesLiveData = new MediatorLiveData<>();

    // Danh sách hiển thị trên UI (đã qua filter/search)
    private final MediatorLiveData<List<Address>> displayedAddressesLiveData = new MediatorLiveData<>();

    // Biến theo dõi nguồn từ repo để refresh
    private LiveData<List<Address>> currentRepoSource;

    public AddressViewModel(@NonNull Application application) {
        super(application);
        repository = new AddressRepository(application.getApplicationContext());
        // Tải dữ liệu lần đầu
        refreshData();
    }

    /**
     * Fragment sẽ observe hàm này để lấy list hiển thị lên RecyclerView
     */
    public LiveData<List<Address>> getDisplayedAddresses() {
        return displayedAddressesLiveData;
    }

    /**
     * Tải lại danh sách từ Server
     */
    public void refreshData() {
        if (currentRepoSource != null) {
            displayedAddressesLiveData.removeSource(currentRepoSource);
        }
        currentRepoSource = repository.getMyAddresses();
        displayedAddressesLiveData.addSource(currentRepoSource, addresses -> {
            if (addresses == null) {
                addresses = new ArrayList<>();
            }
            allAddressesLiveData.setValue(addresses);

            // Mặc định sắp xếp cái mới nhất lên đầu (theo createAt hoặc đơn giản là đảo ngược list)
            // Giả sử Address.getDate() trả về String dạng ISO date có thể so sánh
            List<Address> sorted = new ArrayList<>(addresses);
            // Sắp xếp custom nếu cần, ví dụ:
            // sorted.sort((a1, a2) -> a2.getDate().compareTo(a1.getDate()));

            displayedAddressesLiveData.setValue(sorted);
        });
    }

    // --- CRUD WRAPPERS ---

    public LiveData<Address> addAddress(Address address) {
        return repository.addAddress(address);
    }

    public LiveData<Address> updateAddress(String id, Address address) {
        return repository.updateAddress(id, address);
    }

    public LiveData<Boolean> deleteAddress(String id) {
        return repository.deleteAddress(id);
    }

    /**
     * Tìm kiếm địa chỉ (Local Search trên list đã tải về)
     */
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
            // Tìm theo tên người nhận, địa chỉ hoặc số điện thoại
            if ((a.getName() != null && a.getName().toLowerCase().contains(q)) ||
                    (a.getAddress() != null && a.getAddress().toLowerCase().contains(q)) ||
                    (a.getPhone() != null && a.getPhone().toLowerCase().contains(q))) {
                result.add(a);
            }
        }
        displayedAddressesLiveData.setValue(result);
    }

    /**
     * Lọc theo Tag (Ví dụ: Nhà riêng, Công ty)
     */
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