package com.example.fa25_duan1.repository;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.fa25_duan1.model.ApiResponse;
import com.example.fa25_duan1.model.Author;
import com.example.fa25_duan1.network.AuthorApi;
import com.example.fa25_duan1.network.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthorRepository {
    private final AuthorApi authorApi;

    public AuthorRepository(Context context) {
        authorApi = RetrofitClient.getInstance(context).getAuthorApi();
    }

    // --- Get All Authors ---
    public LiveData<List<Author>> getAllAuthors() {
        MutableLiveData<List<Author>> data = new MutableLiveData<>();
        authorApi.getAllAuthors().enqueue(new Callback<ApiResponse<List<Author>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Author>>> call, @NonNull Response<ApiResponse<List<Author>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    data.setValue(response.body().getData() != null ? response.body().getData() : new ArrayList<>());
                } else {
                    data.setValue(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Author>>> call, @NonNull Throwable t) {
                data.setValue(new ArrayList<>());
            }
        });
        return data;
    }

    // --- Get Author by ID ---
    public LiveData<Author> getAuthorByID(String id) {
        MutableLiveData<Author> data = new MutableLiveData<>();
        authorApi.getAuthorByID(id).enqueue(new Callback<ApiResponse<Author>>() {
            @Override
            public void onResponse(Call<ApiResponse<Author>> call, Response<ApiResponse<Author>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    data.setValue(response.body().getData());
                } else {
                    data.setValue(null);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Author>> call, Throwable t) {
                data.setValue(null);
            }
        });
        return data;
    }

    // --- Add Author ---
    public LiveData<Author> addAuthorWithAvatar(RequestBody name,
                                                RequestBody description,
                                                MultipartBody.Part avatar) {
        MutableLiveData<Author> result = new MutableLiveData<>();
        authorApi.addAuthorWithAvatar(name, description, avatar)
                .enqueue(new Callback<ApiResponse<Author>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Author>> call, Response<ApiResponse<Author>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            result.setValue(response.body().getData());
                        } else {
                            result.setValue(null);
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Author>> call, Throwable t) {
                        result.setValue(null);
                    }
                });
        return result;
    }

    // --- Update Author ---
    public LiveData<Author> updateAuthorWithAvatar(String id,
                                                   RequestBody name,
                                                   RequestBody description,
                                                   MultipartBody.Part avatar) {
        MutableLiveData<Author> result = new MutableLiveData<>();
        authorApi.updateAuthorWithAvatar(id, name, description, avatar)
                .enqueue(new Callback<ApiResponse<Author>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Author>> call, Response<ApiResponse<Author>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            result.setValue(response.body().getData());
                        } else {
                            result.setValue(null);
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Author>> call, Throwable t) {
                        result.setValue(null);
                    }
                });
        return result;
    }

    // --- Delete Author ---
    public LiveData<Boolean> deleteAuthor(String id) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        authorApi.deleteAuthor(id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                result.setValue(response.isSuccessful());
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                result.setValue(false);
            }
        });
        return result;
    }
}