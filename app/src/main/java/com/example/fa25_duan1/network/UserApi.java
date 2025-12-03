package com.example.fa25_duan1.network;

import com.example.fa25_duan1.model.ApiResponse;
import com.example.fa25_duan1.model.User;
import java.util.List;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.*;

public interface UserApi {
    @GET("users/")
    Call<ApiResponse<List<User>>> getAllUsers();

    @GET("users/{id}")
    Call<ApiResponse<User>> getUserByID(@Path("id") String id);

    @GET("users/get-total-account")
    Call<ApiResponse<Integer>> getTotalAccount();

    @Multipart
    @POST("users/add")
    Call<User> addUserWithAvatar(
            @Part("username") RequestBody username,
            @Part("password") RequestBody password,
            @Part("name") RequestBody name,
            @Part("email") RequestBody email,
            @Part("role") RequestBody role,
            @Part MultipartBody.Part avatar
    );

    @Multipart
    @PUT("users/update/{id}")
    Call<ApiResponse<User>> updateUserWithAvatar(
            @Path("id") String id,
            @Part("username") RequestBody username,
            @Part("password") RequestBody password,
            @Part("name") RequestBody name,
            @Part("email") RequestBody email,
            @Part("role") RequestBody role,
            @Part MultipartBody.Part avatar
    );

    @DELETE("users/delete/{id}")
    Call<Void> deleteUser(@Path("id") String id);


}