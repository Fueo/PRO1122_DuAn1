package com.example.fa25_duan1.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.fa25_duan1.model.ApiResponse;
import com.example.fa25_duan1.model.User;
import com.example.fa25_duan1.network.RetrofitClient;
import com.example.fa25_duan1.network.UserApi;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;

// 1. Kế thừa BaseRepository
public class UserRepository extends BaseRepository {
    private final UserApi userApi;

    public UserRepository(Context context) {
        userApi = RetrofitClient.getInstance(context).getUserApi();
    }

    // --- CÁC HÀM PUBLIC ---
    // Sử dụng performRequest() từ lớp cha

    public LiveData<ApiResponse<List<User>>> getAllUsers() {
        return performRequest(userApi.getAllUsers());
    }

    public LiveData<ApiResponse<Integer>> getTotalAccount() {
        return performRequest(userApi.getTotalAccount());
    }

    public LiveData<ApiResponse<User>> getUserByID(String id) {
        return performRequest(userApi.getUserByID(id));
    }

    public LiveData<ApiResponse<User>> addUserWithAvatar(RequestBody username, RequestBody password, RequestBody name, RequestBody email, RequestBody role, MultipartBody.Part avatar) {
        return performRequest(userApi.addUserWithAvatar(username, password, name, email, role, avatar));
    }

    public LiveData<ApiResponse<User>> updateUserWithAvatar(String id, RequestBody username, RequestBody password, RequestBody name, RequestBody email, RequestBody role, MultipartBody.Part avatar) {
        return performRequest(userApi.updateUserWithAvatar(id, username, password, name, email, role, avatar));
    }

    // Nếu API trả về message thành công dạng Object hoặc Void
    public LiveData<ApiResponse<Object>> deleteUser(String id) {
        return performRequest(userApi.deleteUser(id));
    }
}