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

    // ... (Hàm getAllProducts giữ nguyên) ...
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

    public LiveData<List<Product>> getOnSaleProducts(int limit) {
        MutableLiveData<List<Product>> data = new MutableLiveData<>();
        productApi.getOnSaleProducts(limit).enqueue(new Callback<ApiResponse<List<Product>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Product>>> call, @NonNull Response<ApiResponse<List<Product>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Trả về danh sách hoặc list rỗng nếu null
                    data.setValue(response.body().getData() != null ? response.body().getData() : new ArrayList<>());
                } else {
                    data.setValue(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Product>>> call, @NonNull Throwable t) {
                Log.e("ProductRepo", "getOnSaleProducts Error: " + t.getMessage());
                data.setValue(new ArrayList<>());
            }
        });
        return data;
    }

    public LiveData<List<Product>> getProductsByCategory(String categoryId) {
        MutableLiveData<List<Product>> data = new MutableLiveData<>();

        productApi.getProductsByCategory(categoryId).enqueue(new Callback<ApiResponse<List<Product>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Product>>> call, Response<ApiResponse<List<Product>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isStatus()) {
                        data.setValue(response.body().getData());
                    } else {
                        data.setValue(new ArrayList<>()); // Trả về list rỗng nếu status false
                    }
                } else {
                    data.setValue(null);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Product>>> call, Throwable t) {
                Log.e("ProductRepository", "Error loading category products: " + t.getMessage());
                data.setValue(null);
            }
        });

        return data;
    }

    // 🆕 Search Products By Name (Server-side search)
    public LiveData<List<Product>> searchProductsByName(String name) {
        MutableLiveData<List<Product>> data = new MutableLiveData<>();
        productApi.searchProductsByName(name).enqueue(new Callback<ApiResponse<List<Product>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Product>>> call, @NonNull Response<ApiResponse<List<Product>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    data.setValue(response.body().getData() != null ? response.body().getData() : new ArrayList<>());
                } else {
                    // Có thể backend trả về 404 hoặc rỗng nếu không tìm thấy
                    data.setValue(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Product>>> call, @NonNull Throwable t) {
                Log.e("ProductRepo", "searchProductsByName Error: " + t.getMessage());
                data.setValue(new ArrayList<>());
            }
        });
        return data;
    }

    // 🆕 Get Random Products (Server-side random)
    public LiveData<List<Product>> getRandomProducts(int limit) {
        MutableLiveData<List<Product>> data = new MutableLiveData<>();
        productApi.getRandomProducts(limit).enqueue(new Callback<ApiResponse<List<Product>>>() {
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
                Log.e("ProductRepo", "getRandomProducts Error: " + t.getMessage());
                data.setValue(new ArrayList<>());
            }
        });
        return data;
    }

    // ... (Các hàm getProductByID, getProductsByAuthor, Add, Update, Delete giữ nguyên như cũ) ...
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

    public LiveData<List<Product>> getProductsByAuthor(String authorId) {
        MutableLiveData<List<Product>> data = new MutableLiveData<>();
        productApi.getProductsByAuthor(authorId).enqueue(new Callback<ApiResponse<List<Product>>>() {
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

    public LiveData<Product> viewProduct(String id) {
        MutableLiveData<Product> data = new MutableLiveData<>();
        productApi.viewProduct(id).enqueue(new Callback<ApiResponse<Product>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Product>> call, @NonNull Response<ApiResponse<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Trả về sản phẩm với số view mới
                    data.setValue(response.body().getData());
                } else {
                    data.setValue(null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Product>> call, @NonNull Throwable t) {
                Log.e("ProductRepo", "viewProduct Error: " + t.getMessage());
                data.setValue(null);
            }
        });
        return data;
    }

    public LiveData<Product> addProductWithImage(RequestBody name, RequestBody description, RequestBody pages, RequestBody publishDate, RequestBody status, RequestBody categoryID, RequestBody authorID, RequestBody price, RequestBody quantity, MultipartBody.Part image) {
        MutableLiveData<Product> result = new MutableLiveData<>();
        productApi.addProductWithImage(name, description, pages, publishDate, status, categoryID, authorID, price, quantity, image).enqueue(new Callback<ApiResponse<Product>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Product>> call, @NonNull Response<ApiResponse<Product>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    result.setValue(response.body().getData());
                } else {
                    result.setValue(response.isSuccessful() ? new Product() : null);
                }
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<Product>> call, @NonNull Throwable t) {
                result.setValue(null);
            }
        });
        return result;
    }

    public LiveData<Product> updateProductWithImage(String id, RequestBody name, RequestBody description, RequestBody pages, RequestBody publishDate, RequestBody status, RequestBody categoryID, RequestBody authorID, RequestBody price, RequestBody quantity, MultipartBody.Part image) {
        MutableLiveData<Product> result = new MutableLiveData<>();
        productApi.updateProductWithImage(id, name, description, pages, publishDate, status, categoryID, authorID, price, quantity, image).enqueue(new Callback<ApiResponse<Product>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Product>> call, @NonNull Response<ApiResponse<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(response.body().getData() != null ? response.body().getData() : new Product());
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