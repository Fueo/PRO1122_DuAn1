package com.example.fa25_duan1.repository;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.fa25_duan1.model.Address;
import com.example.fa25_duan1.model.ApiResponse;
import com.example.fa25_duan1.network.AddressApi;
import com.example.fa25_duan1.network.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddressRepository {
    private final AddressApi addressApi;

    public AddressRepository(Context context) {
        addressApi = RetrofitClient.getInstance(context).getAddressApi();
    }

    // --- Get My Addresses ---
    public LiveData<List<Address>> getMyAddresses() {
        MutableLiveData<List<Address>> data = new MutableLiveData<>();
        addressApi.getMyAddresses().enqueue(new Callback<ApiResponse<List<Address>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Address>>> call, @NonNull Response<ApiResponse<List<Address>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    data.setValue(response.body().getData() != null ? response.body().getData() : new ArrayList<>());
                } else {
                    data.setValue(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Address>>> call, @NonNull Throwable t) {
                data.setValue(new ArrayList<>());
            }
        });
        return data;
    }

    // --- Add Address ---
    public LiveData<Address> addAddress(Address address) {
        MutableLiveData<Address> result = new MutableLiveData<>();
        addressApi.addAddress(address).enqueue(new Callback<ApiResponse<Address>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Address>> call, @NonNull Response<ApiResponse<Address>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(response.body().getData());
                } else {
                    result.setValue(null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Address>> call, @NonNull Throwable t) {
                result.setValue(null);
            }
        });
        return result;
    }

    // --- Update Address ---
    public LiveData<Address> updateAddress(String id, Address address) {
        MutableLiveData<Address> result = new MutableLiveData<>();
        addressApi.updateAddress(id, address).enqueue(new Callback<ApiResponse<Address>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Address>> call, @NonNull Response<ApiResponse<Address>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(response.body().getData());
                } else {
                    result.setValue(null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Address>> call, @NonNull Throwable t) {
                result.setValue(null);
            }
        });
        return result;
    }

    // --- Delete Address ---
    public LiveData<Boolean> deleteAddress(String id) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        addressApi.deleteAddress(id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                // Backend trả về 200 OK là thành công
                result.setValue(response.isSuccessful());
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                result.setValue(false);
            }
        });
        return result;
    }
}