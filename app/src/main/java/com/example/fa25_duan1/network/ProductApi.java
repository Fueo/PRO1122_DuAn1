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

public interface ProductApi {

    // GET: Lấy tất cả products (PUBLIC)
    @GET("products/")
    Call<ApiResponse<List<Product>>> getAllProducts();

    // GET: Lấy product theo ID (PUBLIC)
    @GET("products/{id}")
    Call<ApiResponse<Product>> getProductByID(@Path("id") String id);

    // POST: Thêm product có image (PRIVATE)
    // POST: Thêm product có image (PRIVATE) - ĐÃ SỬA THỨ TỰ
    @Multipart
    @POST("products/add")
    Call<ApiResponse<Product>> addProductWithImage(
            @Part("name") RequestBody name,
            @Part("description") RequestBody description,
            @Part("pages") RequestBody pages, // ĐÃ ĐỔI VỊ TRÍ
            @Part("publishDate") RequestBody publishDate, // ĐÃ ĐỔI VỊ TRÍ
            @Part("status") RequestBody status, // ĐÃ ĐỔI VỊ TRÍ
            @Part("categoryID") RequestBody categoryID, // ĐÃ ĐỔI VỊ TRÍ
            @Part("authorID") RequestBody authorID, // ĐÃ ĐỔI VỊ TRÍ
            @Part("price") RequestBody price, // ĐÃ ĐỔI VỊ TRÍ
            @Part("quantity") RequestBody quantity, // ĐÃ ĐỔI VỊ TRÍ
            @Part MultipartBody.Part image
    );

    // PUT: Cập nhật product có kèm image (PRIVATE) - ĐÃ SỬA THỨ TỰ
    @Multipart
    @PUT("products/update/{id}")
    Call<ApiResponse<Product>> updateProductWithImage(
            @Path("id") String id,
            @Part("name") RequestBody name,
            @Part("description") RequestBody description,
            @Part("pages") RequestBody pages, // ĐÃ ĐỔI VỊ TRÍ
            @Part("publishDate") RequestBody publishDate, // ĐÃ ĐỔI VỊ TRÍ
            @Part("status") RequestBody status, // ĐÃ ĐỔI VỊ TRÍ
            @Part("categoryID") RequestBody categoryID, // ĐÃ ĐỔI VỊ TRÍ
            @Part("authorID") RequestBody authorID, // ĐÃ ĐỔI VỊ TRÍ
            @Part("price") RequestBody price, // ĐÃ ĐỔI VỊ TRÍ
            @Part("quantity") RequestBody quantity, // ĐÃ ĐỔI VỊ TRÍ
            @Part MultipartBody.Part image
    );

    // DELETE: Xóa product theo ID (PRIVATE)
    @DELETE("products/delete/{id}")
    Call<Void> deleteProduct(@Path("id") String id);
}