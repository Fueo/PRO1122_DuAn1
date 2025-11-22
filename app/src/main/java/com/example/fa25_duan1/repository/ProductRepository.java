package com.example.fa25_duan1.repository;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.fa25_duan1.model.ApiResponse;
import com.example.fa25_duan1.model.Product;
import com.example.fa25_duan1.network.ProductApi;
import com.example.fa25_duan1.network.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductRepository {
    private final ProductApi productApi;

    public ProductRepository(Context context) {
        productApi = RetrofitClient.getInstance(context).getProductApi();
    }

    // --- Get All Products ---
    public LiveData<List<Product>> getAllProducts() {
        MutableLiveData<List<Product>> data = new MutableLiveData<>();
        productApi.getAllProducts().enqueue(new Callback<ApiResponse<List<Product>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Product>>> call, @NonNull Response<ApiResponse<List<Product>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    data.setValue(response.body().getData() != null ? response.body().getData() : new ArrayList<>());
                } else {
                    data.setValue(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Product>>> call, @NonNull Throwable t) {
                data.setValue(new ArrayList<>());
            }
        });
        return data;
    }

    // --- Get Product by ID ---
    public LiveData<Product> getProductByID(String id) {
        MutableLiveData<Product> data = new MutableLiveData<>();
        productApi.getProductByID(id).enqueue(new Callback<ApiResponse<Product>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Product>> call, @NonNull Response<ApiResponse<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    data.setValue(response.body().getData());
                } else {
                    data.setValue(null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Product>> call, @NonNull Throwable t) {
                data.setValue(null);
            }
        });
        return data;
    }

    // --- Get Products By Author ID ---
    public LiveData<List<Product>> getProductsByAuthor(String authorId) {
        MutableLiveData<List<Product>> data = new MutableLiveData<>();

        productApi.getProductsByAuthor(authorId).enqueue(new Callback<ApiResponse<List<Product>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Product>>> call, @NonNull Response<ApiResponse<List<Product>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Trả về list data hoặc list rỗng nếu data null
                    data.setValue(response.body().getData() != null ? response.body().getData() : new ArrayList<>());
                } else {
                    // Nếu lỗi server hoặc không tìm thấy, trả về list rỗng để UI không bị crash
                    data.setValue(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Product>>> call, @NonNull Throwable t) {
                Log.e("ProductRepo", "getProductsByAuthor Error: " + t.getMessage());
                data.setValue(new ArrayList<>());
            }
        });
        return data;
    }

    // --- Add Product ---
    public LiveData<Product> addProductWithImage(RequestBody name,
                                                 RequestBody description,
                                                 RequestBody pages,
                                                 RequestBody publishDate,
                                                 RequestBody status,
                                                 RequestBody categoryID,
                                                 RequestBody authorID,
                                                 RequestBody price,
                                                 RequestBody quantity,
                                                 MultipartBody.Part image) {
        MutableLiveData<Product> result = new MutableLiveData<>();

        productApi.addProductWithImage(name, description, pages, publishDate, status, categoryID, authorID, price, quantity, image)
                .enqueue(new Callback<ApiResponse<Product>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<Product>> call, @NonNull Response<ApiResponse<Product>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            // SỬA: Nếu data là null (có thể do lỗi parse), trả về đối tượng Product rỗng để báo hiệu thành công.
                            if (response.body().getData() != null) {
                                result.setValue(response.body().getData());
                            } else {
                                // Giả định Product model có constructor không tham số
                                result.setValue(new Product());
                            }
                        } else {
                            result.setValue(null);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<Product>> call, @NonNull Throwable t) {
                        result.setValue(null);
                    }
                });
        return result;
    }

    // --- Update Product ---
    public LiveData<Product> updateProductWithImage(String id,
                                                    RequestBody name,
                                                    RequestBody description,
                                                    RequestBody pages,
                                                    RequestBody publishDate,
                                                    RequestBody status,
                                                    RequestBody categoryID,
                                                    RequestBody authorID,
                                                    RequestBody price,
                                                    RequestBody quantity,
                                                    MultipartBody.Part image) {
        MutableLiveData<Product> result = new MutableLiveData<>();

        productApi.updateProductWithImage(id, name, description, pages, publishDate, status, categoryID, authorID, price, quantity, image)
                .enqueue(new Callback<ApiResponse<Product>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<Product>> call, @NonNull Response<ApiResponse<Product>> response) {
                        if (response.isSuccessful()) {
                            ApiResponse<Product> apiResponse = response.body();

                            if (apiResponse != null && apiResponse.getData() != null) {
                                result.setValue(apiResponse.getData());
                            } else if (response.body() != null && response.body().getMessage() != null) {
                                // Trường hợp server báo thành công nhưng data null, log message để xác nhận
                                Log.w("Repo", "Success but Data Null: " + response.body().getMessage());
                                result.setValue(new Product());
                            } else {
                                // Trường hợp parsing thất bại hoàn toàn (response.body() là null)
                                Log.e("Repo", "Update success but response.body() is NULL. Check Product Model mapping.");
                                result.setValue(null);
                            }
                        } else {
                            Log.e("Repo", "Update failed with code: " + response.code());
                            result.setValue(null);
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<Product>> call, @NonNull Throwable t) {
                        result.setValue(null);
                    }
                });
        return result;
    }

    // --- Delete Product ---
    public LiveData<Boolean> deleteProduct(String id) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        productApi.deleteProduct(id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                result.setValue(response.isSuccessful());
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                result.setValue(false);
            }
        });
        return result;
    }
}