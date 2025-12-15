package com.example.fa25_duan1.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import com.example.fa25_duan1.model.ApiResponse;
import com.example.fa25_duan1.model.Product;
import com.example.fa25_duan1.network.ProductApi;
import com.example.fa25_duan1.network.RetrofitClient;
import java.util.List;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class ProductRepository extends BaseRepository { // <--- Kế thừa
    private final ProductApi productApi;

    public ProductRepository(Context context) {
        productApi = RetrofitClient.getInstance(context).getProductApi();
    }

    public LiveData<ApiResponse<List<Product>>> getAllProducts() {
        return performRequest(productApi.getAllProducts());
    }

    public LiveData<ApiResponse<List<Product>>> getOnSaleProducts(int limit) {
        return performRequest(productApi.getOnSaleProducts(limit));
    }

    public LiveData<ApiResponse<Integer>> getTotalProduct() {
        return performRequest(productApi.getTotalProduct());
    }

    public LiveData<ApiResponse<List<Product>>> getProductsByCategory(String categoryId) {
        return performRequest(productApi.getProductsByCategory(categoryId));
    }

    public LiveData<ApiResponse<List<Product>>> searchProductsByName(String name) {
        return performRequest(productApi.searchProductsByName(name));
    }

    public LiveData<ApiResponse<List<Product>>> getRandomProducts(int limit) {
        return performRequest(productApi.getRandomProducts(limit));
    }

    public LiveData<ApiResponse<Product>> getProductByID(String id) {
        return performRequest(productApi.getProductByID(id));
    }

    public LiveData<ApiResponse<List<Product>>> getProductsByAuthor(String authorId) {
        return performRequest(productApi.getProductsByAuthor(authorId));
    }

    public LiveData<ApiResponse<Product>> viewProduct(String id) {
        return performRequest(productApi.viewProduct(id));
    }

    public LiveData<ApiResponse<List<Product>>> getFavoriteProducts() {
        return performRequest(productApi.getFavoriteProducts());
    }

    public LiveData<ApiResponse<Product>> addProductWithImage(RequestBody name, RequestBody description, RequestBody pages, RequestBody publishDate, RequestBody status, RequestBody categoryID, RequestBody authorID, RequestBody price, RequestBody quantity, MultipartBody.Part image) {
        return performRequest(productApi.addProductWithImage(name, description, pages, publishDate, status, categoryID, authorID, price, quantity, image));
    }

    public LiveData<ApiResponse<Product>> updateProductWithImage(String id, RequestBody name, RequestBody description, RequestBody pages, RequestBody publishDate, RequestBody status, RequestBody categoryID, RequestBody authorID, RequestBody price, RequestBody quantity, MultipartBody.Part image) {
        return performRequest(productApi.updateProductWithImage(id, name, description, pages, publishDate, status, categoryID, authorID, price, quantity, image));
    }

    // Với API trả về Void (Delete), ta dùng ApiResponse<Void>
    public LiveData<ApiResponse<Void>> deleteProduct(String id) {
        return performRequest(productApi.deleteProduct(id));
    }
}