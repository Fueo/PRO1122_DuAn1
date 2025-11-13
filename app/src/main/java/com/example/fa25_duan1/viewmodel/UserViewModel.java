package com.example.fa25_duan1.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.fa25_duan1.model.User;
import com.example.fa25_duan1.repository.UserRepository;

import java.util.List;

import retrofit2.Callback;

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

    public void addUser(User user, Callback<Void> callback) {
        repository.addUser(user, callback);
    }

    public void updateUser(String id, User user, Callback<User> callback) {
        repository.updateUser(id, user, callback);
    }

    public LiveData<Boolean> deleteUser(String id) {
        return repository.deleteUser(id);
    }
}

