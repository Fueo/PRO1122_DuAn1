package com.example.fa25_duan1.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import com.example.fa25_duan1.model.Address;
import com.example.fa25_duan1.model.ApiResponse;
import com.example.fa25_duan1.network.AddressApi;
import com.example.fa25_duan1.network.RetrofitClient;
import java.util.List;

public class AddressRepository extends BaseRepository {
    private final AddressApi addressApi;

    public AddressRepository(Context context) {
        addressApi = RetrofitClient.getInstance(context).getAddressApi();
    }

    // --- Get My Addresses ---
    public LiveData<ApiResponse<List<Address>>> getMyAddresses() {
        return performRequest(addressApi.getMyAddresses());
    }

    // --- Add Address ---
    public LiveData<ApiResponse<Address>> addAddress(Address address) {
        return performRequest(addressApi.addAddress(address));
    }

    // --- Update Address ---
    public LiveData<ApiResponse<Address>> updateAddress(String id, Address address) {
        return performRequest(addressApi.updateAddress(id, address));
    }

    // --- Delete Address ---
    // Note: If your API returns Void for delete, ensure AddressApi returns Call<ApiResponse<Void>>
    // If it returns Call<Void>, performRequest will need the generic type explicitly or a separate handler.
    // Assuming you update AddressApi to return Call<ApiResponse<Void>> for consistency:
    public LiveData<ApiResponse<Void>> deleteAddress(String id) {
        return performRequest(addressApi.deleteAddress(id));
    }
}