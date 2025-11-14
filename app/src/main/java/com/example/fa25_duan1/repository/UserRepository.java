package com.example.fa25_duan1.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.fa25_duan1.model.User;
import com.example.fa25_duan1.network.RetrofitClient;
import com.example.fa25_duan1.network.UserApi;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserRepository {
    private final UserApi userApi;

    public UserRepository() {
        userApi = RetrofitClient.getInstance().create(UserApi.class);
    }

    public LiveData<List<User>> getAllUsers() {
        MutableLiveData<List<User>> data = new MutableLiveData<>();

        userApi.getAllUsers().enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("API_SUCCESS", "Lấy được " + response.body().size() + " users");
                    data.setValue(response.body());
                } else {
                    Log.e("API_ERROR", "Server error: " + response.code());
                    data.setValue(null);
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                Log.e("API_ERROR", "Lỗi kết nối/Parse: " + t.getMessage());
                data.setValue(null);
            }
        });

        return data;
    }

    public LiveData<User> getUserByID(String id) {
        MutableLiveData<User> data = new MutableLiveData<>();

        userApi.getUserByID(id).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    data.setValue(response.body());
                } else {
                    Log.e("API_ERROR", "Get Detail Error: " + response.code());
                    data.setValue(null);
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.e("API_ERROR", "Get Detail Fail: " + t.getMessage());
                data.setValue(null);
            }
        });

        return data;
    }

    public LiveData<Boolean> deleteUser(String id) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        userApi.deleteUser(id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                result.setValue(response.isSuccessful());
                if (!response.isSuccessful()) {
                    Log.e("API_ERROR", "Delete failed: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("API_ERROR", "Delete error: " + t.getMessage());
                result.setValue(false);
            }
        });
        return result;
    }

//    public void addUser(User user, Callback<Void> callback) {
//        userApi.addUser(user).enqueue(callback);
//    }

    public LiveData<User> updateUserWithAvatar(String id,
                                               RequestBody username,
                                               RequestBody password,
                                               RequestBody name,
                                               RequestBody email,
                                               RequestBody phone,
                                               RequestBody address,
                                               RequestBody role,
                                               MultipartBody.Part avatar) {
        MutableLiveData<User> result = new MutableLiveData<>();

        userApi.updateUserWithAvatar(id, username, password, name, email, phone, address, role, avatar)
                .enqueue(new Callback<User>() {
                    @Override
                    public void onResponse(Call<User> call, Response<User> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Log.d("API_SUCCESS", "Cập nhật user thành công: " + response.body().getUsername());
                            result.setValue(response.body());
                        } else {
                            Log.e("API_ERROR", "Cập nhật user lỗi: " + response.code());
                            result.setValue(null);
                        }
                    }

                    @Override
                    public void onFailure(Call<User> call, Throwable t) {
                        Log.e("API_ERROR", "Cập nhật user lỗi mạng: " + t.getMessage());
                        result.setValue(null);
                    }
                });

        return result;
    }

    public MutableLiveData<User> addUserWithAvatar(RequestBody username,
                                                   RequestBody password,
                                                   RequestBody name,
                                                   RequestBody email,
                                                   RequestBody phone,
                                                   RequestBody address,
                                                   RequestBody role,
                                                   MultipartBody.Part avatar) {
        MutableLiveData<User> result = new MutableLiveData<>();
        userApi.addUserWithAvatar(username, password, name, email, phone, address, role, avatar)
                .enqueue(new Callback<User>() {
                    @Override
                    public void onResponse(Call<User> call, Response<User> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            result.setValue(response.body());
                        } else {
                            result.setValue(null);
                        }
                    }

                    @Override
                    public void onFailure(Call<User> call, Throwable t) {
                        result.setValue(null);
                    }
                });
        return result;
    }

}
