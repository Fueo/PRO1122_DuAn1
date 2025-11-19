package com.example.fa25_duan1.network;

import com.example.fa25_duan1.model.ApiResponse;
import com.example.fa25_duan1.model.Author;

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

public interface AuthorApi {

    // GET: Lấy tất cả authors
    @GET("authors/")
    Call<ApiResponse<List<Author>>> getAllAuthors();

    // GET: Lấy author theo ID
    @GET("authors/{id}")
    Call<ApiResponse<Author>> getAuthorByID(@Path("id") String id);

    // POST: Thêm author có avatar (Multipart)
    @Multipart
    @POST("authors/add")
    Call<ApiResponse<Author>> addAuthorWithAvatar(
            @Part("name") RequestBody name,
            @Part("description") RequestBody description,
            @Part MultipartBody.Part avatar
    );

    // PUT: Cập nhật author có kèm avatar
    @Multipart
    @PUT("authors/update/{id}")
    Call<ApiResponse<Author>> updateAuthorWithAvatar(
            @Path("id") String id,
            @Part("name") RequestBody name,
            @Part("description") RequestBody description,
            @Part MultipartBody.Part avatar
    );

    // DELETE: Xóa author theo ID
    @DELETE("authors/delete/{id}")
    Call<Void> deleteAuthor(@Path("id") String id);
}