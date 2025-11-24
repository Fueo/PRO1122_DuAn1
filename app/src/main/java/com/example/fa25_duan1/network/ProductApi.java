package com.example.fa25_duan1.network;

import com.example.fa25_duan1.model.ApiResponse;
import com.example.fa25_duan1.model.Product;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ProductApi {

    // GET: Lấy tất cả products (PUBLIC)
    @GET("products/")
    Call<ApiResponse<List<Product>>> getAllProducts();

    @GET("products/")
    Call<ApiResponse<List<Product>>> getProductsByCategory(@Query("categoryID") String categoryId);

    // 🆕 GET: Tìm kiếm sản phẩm theo tên (PUBLIC)
    // API: /api/products/search?name=keyword
    @GET("products/search")
    Call<ApiResponse<List<Product>>> searchProductsByName(@Query("name") String name);

    // 🆕 GET: Lấy danh sách sản phẩm ngẫu nhiên (PUBLIC)
    // API: /api/products/random/:limit
    @GET("products/random/{limit}")
    Call<ApiResponse<List<Product>>> getRandomProducts(@Path("limit") int limit);

    // GET: Lấy product theo ID (PUBLIC)
    @GET("products/{id}")
    Call<ApiResponse<Product>> getProductByID(@Path("id") String id);

    // GET: Lấy danh sách sản phẩm theo AuthorID
    @GET("products/getProductByAuthor/{id}")
    Call<ApiResponse<List<Product>>> getProductsByAuthor(@Path("id") String authorId);

    // GET: Lấy thông tin sản phẩm và tăng lượt view
    @GET("products/view/{id}")
    Call<ApiResponse<Product>> viewProduct(@Path("id") String id);

    // ... (Các method POST, PUT, DELETE giữ nguyên như cũ) ...
    @Multipart
    @POST("products/add")
    Call<ApiResponse<Product>> addProductWithImage(
            @Part("name") RequestBody name,
            @Part("description") RequestBody description,
            @Part("pages") RequestBody pages,
            @Part("publishDate") RequestBody publishDate,
            @Part("status") RequestBody status,
            @Part("categoryID") RequestBody categoryID,
            @Part("authorID") RequestBody authorID,
            @Part("price") RequestBody price,
            @Part("quantity") RequestBody quantity,
            @Part MultipartBody.Part image
    );

    @Multipart
    @PUT("products/update/{id}")
    Call<ApiResponse<Product>> updateProductWithImage(
            @Path("id") String id,
            @Part("name") RequestBody name,
            @Part("description") RequestBody description,
            @Part("pages") RequestBody pages,
            @Part("publishDate") RequestBody publishDate,
            @Part("status") RequestBody status,
            @Part("categoryID") RequestBody categoryID,
            @Part("authorID") RequestBody authorID,
            @Part("price") RequestBody price,
            @Part("quantity") RequestBody quantity,
            @Part MultipartBody.Part image
    );

    @DELETE("products/delete/{id}")
    Call<Void> deleteProduct(@Path("id") String id);
}