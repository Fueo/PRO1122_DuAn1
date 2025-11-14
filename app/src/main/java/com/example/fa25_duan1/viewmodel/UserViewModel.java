package com.example.fa25_duan1.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.fa25_duan1.model.User;
import com.example.fa25_duan1.repository.UserRepository;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class UserViewModel extends ViewModel {
    private final UserRepository repository;

    public UserViewModel() {
        repository = new UserRepository();
    }

    public LiveData<List<User>> getAllUsers() {
        return repository.getAllUsers();
    }

    public LiveData<User> getUserByID(String id) {
        return repository.getUserByID(id);
    }

//    public void addUser(User user, retrofit2.Callback<Void> callback) {
//        repository.addUser(user, callback);
//    }

    public LiveData<User> addUserWithAvatar(RequestBody username,
                                                   RequestBody password,
                                                   RequestBody name,
                                                   RequestBody email,
                                                   RequestBody phone,
                                                   RequestBody address,
                                                   RequestBody role,
                                                   MultipartBody.Part avatar) {
        return repository.addUserWithAvatar(username, password, name, email, phone, address, role, avatar);
    }


    public LiveData<User> updateUserWithAvatar(String id,
                                               RequestBody username,
                                               RequestBody password,
                                               RequestBody name,
                                               RequestBody email,
                                               RequestBody phone,
                                               RequestBody address,
                                               RequestBody role,
                                               MultipartBody.Part avatar) {
        return repository.updateUserWithAvatar(id, username, password, name, email, phone, address, role, avatar);
    }

    public LiveData<Boolean> deleteUser(String id) {
        return repository.deleteUser(id);
    }
}
