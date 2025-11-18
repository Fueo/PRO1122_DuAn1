package com.example.fa25_duan1.network;

import com.example.fa25_duan1.model.ApiResponse;
import com.example.fa25_duan1.model.User;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface UserApi {

    // GET: Lấy tất cả users
    @GET("users/")
    Call<ApiResponse<List<User>>> getAllUsers();

    // GET: Lấy user theo ID
    @GET("users/{id}")
    Call<ApiResponse<User>> getUserByID(@Path("id") String id);

//    // POST: Thêm user
//    @POST("users/add")
//    Call<Void> addUser(@Body User user);

    // POST: Thêm user có avatar** (Multipart)
    @Multipart
    @POST("users/add")
    Call<User> addUserWithAvatar(
            @Part("username") RequestBody username,
            @Part("password") RequestBody password,
            @Part("name") RequestBody name,
            @Part("email") RequestBody email,
            @Part("phone") RequestBody phone,
            @Part("address") RequestBody address,
            @Part("role") RequestBody role,
            @Part MultipartBody.Part avatar
    );

    // PUT: Cập nhật user theo ID
    @PUT("users/update/{id}")
    Call<ApiResponse<User>> updateUser(@Path("id") String id, @Body User user);

    //PUT: Cập nhật user có kèm avatar
    @Multipart
    @PUT("users/update/{id}")
    Call<ApiResponse<User>> updateUserWithAvatar(
            @Path("id") String id,
            @Part("username") RequestBody username,
            @Part("password") RequestBody password,
            @Part("name") RequestBody name,
            @Part("email") RequestBody email,
            @Part("phone") RequestBody phone,
            @Part("address") RequestBody address,
            @Part("role") RequestBody role,
            @Part MultipartBody.Part avatar
    );

    // DELETE: Xóa user theo ID
    @DELETE("users/delete/{id}")
    Call<Void> deleteUser(@Path("id") String id);
}

