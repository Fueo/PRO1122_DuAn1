package com.example.fa25_duan1.repository;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.fa25_duan1.model.ApiResponse;
import com.example.fa25_duan1.model.User;
import com.example.fa25_duan1.network.RetrofitClient;
import com.example.fa25_duan1.network.UserApi;
import java.util.ArrayList;
import java.util.List;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserRepository {
    private final UserApi userApi;

    public UserRepository(Context context) {
        userApi = RetrofitClient.getInstance(context).getUserApi();
    }

    public LiveData<List<User>> getAllUsers() {
        MutableLiveData<List<User>> data = new MutableLiveData<>();
        userApi.getAllUsers().enqueue(new Callback<ApiResponse<List<User>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<User>>> call, @NonNull Response<ApiResponse<List<User>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    data.setValue(response.body().getData() != null ? response.body().getData() : new ArrayList<>());
                } else {
                    data.setValue(new ArrayList<>());
                }
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<User>>> call, @NonNull Throwable t) {
                data.setValue(new ArrayList<>());
            }
        });
        return data;
    }

    public LiveData<Integer> getTotalAccount() {
        MutableLiveData<Integer> countData = new MutableLiveData<>();
        userApi.getTotalAccount().enqueue(new Callback<ApiResponse<Integer>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Integer>> call, @NonNull Response<ApiResponse<Integer>> response) {
                if (response.isSuccessful() && response.body() != null) countData.setValue(response.body().getData());
                else countData.setValue(0);
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<Integer>> call, @NonNull Throwable t) { countData.setValue(0); }
        });
        return countData;
    }

    public LiveData<User> getUserByID(String id) {
        MutableLiveData<User> data = new MutableLiveData<>();
        userApi.getUserByID(id).enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                if (response.isSuccessful() && response.body() != null) data.setValue(response.body().getData());
                else data.setValue(null);
            }
            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) { data.setValue(null); }
        });
        return data;
    }

    public LiveData<User> addUserWithAvatar(RequestBody username, RequestBody password, RequestBody name, RequestBody email, RequestBody role, MultipartBody.Part avatar) {
        MutableLiveData<User> result = new MutableLiveData<>();
        userApi.addUserWithAvatar(username, password, name, email, role, avatar).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) result.setValue(response.body());
                else result.setValue(null);
            }
            @Override
            public void onFailure(Call<User> call, Throwable t) { result.setValue(null); }
        });
        return result;
    }

    public LiveData<User> updateUserWithAvatar(String id, RequestBody username, RequestBody password, RequestBody name, RequestBody email, RequestBody role, MultipartBody.Part avatar) {
        MutableLiveData<User> result = new MutableLiveData<>();
        userApi.updateUserWithAvatar(id, username, password, name, email, role, avatar).enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                if (response.isSuccessful() && response.body() != null) result.setValue(response.body().getData());
                else result.setValue(null);
            }
            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) { result.setValue(null); }
        });
        return result;
    }

    public LiveData<Boolean> deleteUser(String id) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        userApi.deleteUser(id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) { result.setValue(response.isSuccessful()); }
            @Override
            public void onFailure(Call<Void> call, Throwable t) { result.setValue(false); }
        });
        return result;
    }


}