package com.example.fa25_duan1.repository;

import android.util.Log; // Import Log

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.fa25_duan1.model.User;
import com.example.fa25_duan1.network.UserApi;
import com.example.fa25_duan1.network.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserRepository {
    private final UserApi userApi;

    public UserRepository() {
        userApi = RetrofitClient.getInstance().create(UserApi.class);
    }

    public LiveData<List<User>> getAllUsers() {
        MutableLiveData<List<User>> data = new MutableLiveData<>();

        userApi.getAllUsers().enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Thành công: trả về danh sách
                    Log.d("API_SUCCESS", "Lấy được " + response.body().size() + " users");
                    data.setValue(response.body());
                } else {
                    // Server trả về lỗi (ví dụ 404, 500)
                    Log.e("API_ERROR", "Server error: " + response.code() + " - " + response.message());
                    data.setValue(null);
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                // Lỗi mạng hoặc lỗi convert JSON
                Log.e("API_ERROR", "Lỗi kết nối/Parse: " + t.getMessage());
                t.printStackTrace(); // In chi tiết lỗi ra Logcat
                data.setValue(null);
            }
        });
        return data;
    }

    public LiveData<User> getUserByID(String id) {
        MutableLiveData<User> data = new MutableLiveData<>();

        userApi.getUserByID(id).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                // Log để kiểm tra
                if (response.isSuccessful() && response.body() != null) {
                    data.setValue(response.body());
                } else {
                    // Lỗi do không tìm thấy user hoặc lỗi server
                    Log.e("API_ERROR", "Get Detail Error: " + response.code());
                    data.setValue(null);
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                // Lỗi mạng
                Log.e("API_ERROR", "Get Detail Fail: " + t.getMessage());
                data.setValue(null);
            }
        });

        return data;
    }

    // ... Các hàm add, update, delete giữ nguyên hoặc thêm Log tương tự
    public LiveData<Boolean> deleteUser(String id) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        userApi.deleteUser(id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if(response.isSuccessful()){
                    result.setValue(true);
                } else {
                    Log.e("API_ERROR", "Delete failed: " + response.code());
                    result.setValue(false);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("API_ERROR", "Delete error: " + t.getMessage());
                result.setValue(false);
            }
        });
        return result;
    }

    // Thêm code cho addUser và updateUser tương tự nếu cần
    public void addUser(User user, Callback<Void> callback) {
        userApi.addUser(user).enqueue(callback);
    }

    public void updateUser(String id, User user, Callback<User> callback) {
        userApi.updateUser(id, user).enqueue(callback);
    }
}