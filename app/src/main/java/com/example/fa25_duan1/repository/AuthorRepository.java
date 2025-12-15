package com.example.fa25_duan1.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.fa25_duan1.model.ApiResponse;
import com.example.fa25_duan1.model.Author;
import com.example.fa25_duan1.network.AuthorApi;
import com.example.fa25_duan1.network.RetrofitClient;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;

// 1. Extend BaseRepository to use performRequest
public class AuthorRepository extends BaseRepository {
    private final AuthorApi authorApi;

    public AuthorRepository(Context context) {
        authorApi = RetrofitClient.getInstance(context).getAuthorApi();
    }

    // --- Get All Authors ---
    // Returns ApiResponse containing List<Author>
    public LiveData<ApiResponse<List<Author>>> getAllAuthors() {
        return performRequest(authorApi.getAllAuthors());
    }

    // --- Get Author by ID ---
    public LiveData<ApiResponse<Author>> getAuthorByID(String id) {
        return performRequest(authorApi.getAuthorByID(id));
    }

    // --- Add Author ---
    public LiveData<ApiResponse<Author>> addAuthorWithAvatar(RequestBody name,
                                                             RequestBody description,
                                                             MultipartBody.Part avatar) {
        return performRequest(authorApi.addAuthorWithAvatar(name, description, avatar));
    }

    // --- Update Author ---
    public LiveData<ApiResponse<Author>> updateAuthorWithAvatar(String id,
                                                                RequestBody name,
                                                                RequestBody description,
                                                                MultipartBody.Part avatar) {
        return performRequest(authorApi.updateAuthorWithAvatar(id, name, description, avatar));
    }

    // --- Delete Author ---
    // Returns ApiResponse<Void> to capture success/failure status and message
    public LiveData<ApiResponse<Void>> deleteAuthor(String id) {
        return performRequest(authorApi.deleteAuthor(id));
    }
}