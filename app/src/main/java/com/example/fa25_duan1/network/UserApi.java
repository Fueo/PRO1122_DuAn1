package com.example.fa25_duan1.network;

import com.example.fa25_duan1.model.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface UserApi {

    // GET: Lấy tất cả users
    @GET("users/")
    Call<List<User>> getAllUsers();

    // GET: Lấy user theo ID
    @GET("users/{id}")
    Call<User> getUserByID(@Path("id") String id);

    // POST: Thêm user
    @POST("users/add")
    Call<Void> addUser(@Body User user);

    // PUT: Cập nhật user theo ID
    @PUT("users/update/{id}")
    Call<User> updateUser(@Path("id") String id, @Body User user);

    // DELETE: Xóa user theo ID
    @DELETE("users/delete/{id}")
    Call<Void> deleteUser(@Path("id") String id);
}

