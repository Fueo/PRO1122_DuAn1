package com.example.fa25_duan1.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.example.fa25_duan1.model.Author;
import com.example.fa25_duan1.model.User;
import com.example.fa25_duan1.repository.AuthorRepository;

import java.util.ArrayList;
import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class AuthorViewModel extends AndroidViewModel {

    private final AuthorRepository repository;

    // "allAuthorsLiveData" là nguồn dữ liệu gốc
    private final MediatorLiveData<List<Author>> allAuthorsLiveData = new MediatorLiveData<>();

    // "displayedAuthorsLiveData" là danh sách mà Fragment sẽ observe để hiển thị
    private final MediatorLiveData<List<Author>> displayedAuthorsLiveData = new MediatorLiveData<>();

    private LiveData<List<Author>> currentRepoSource;

    public AuthorViewModel(@NonNull Application application) {
        super(application);
        repository = new AuthorRepository(application.getApplicationContext());
        refreshData();
    }

    public LiveData<List<Author>> getDisplayedAuthors() {
        return displayedAuthorsLiveData;
    }

    public void refreshData() {
        if (currentRepoSource != null) {
            displayedAuthorsLiveData.removeSource(currentRepoSource);
        }

        currentRepoSource = repository.getAllAuthors();

        displayedAuthorsLiveData.addSource(currentRepoSource, authors -> {
            if (authors == null) {
                authors = new ArrayList<>();
            }

            // Cập nhật danh sách gốc
            allAuthorsLiveData.setValue(authors);

            // Sắp xếp mặc định theo tên (A -> Z) vì model không có createAt
            List<Author> sorted = new ArrayList<>(authors);
            sorted.sort((a1, a2) -> {
                if (a1.getCreateAt() == null || a2.getCreateAt() == null) return 0;
                return a2.getCreateAt().compareTo(a1.getCreateAt());
            });

            displayedAuthorsLiveData.setValue(sorted);
        });
    }

    // --- CRUD ---

    public LiveData<Author> addAuthorWithAvatar(RequestBody name,
                                                RequestBody description,
                                                MultipartBody.Part avatar) {
        return repository.addAuthorWithAvatar(name, description, avatar);
    }

    public LiveData<Author> updateAuthorWithAvatar(String id,
                                                   RequestBody name,
                                                   RequestBody description,
                                                   MultipartBody.Part avatar) {
        return repository.updateAuthorWithAvatar(id, name, description, avatar);
    }

    public LiveData<Boolean> deleteAuthor(String id) {
        return repository.deleteAuthor(id);
    }

    public LiveData<Author> getAuthorByID(String id) {
        return repository.getAuthorByID(id);
    }

    // --- TÌM KIẾM ---

    /**
     * Tìm kiếm tác giả theo tên
     */
    public void searchAuthors(String query) {
        List<Author> masterList = allAuthorsLiveData.getValue();
        if (masterList == null) masterList = new ArrayList<>();

        // Nếu query rỗng, trả về danh sách gốc (đã sort A-Z)
        if (query == null || query.trim().isEmpty()) {
            List<Author> sorted = new ArrayList<>(masterList);
            sorted.sort((a1, a2) -> {
                if (a1.getName() == null || a2.getName() == null) return 0;
                return a1.getName().compareToIgnoreCase(a2.getName());
            });
            displayedAuthorsLiveData.setValue(sorted);
            return;
        }

        String q = query.toLowerCase().trim();
        List<Author> result = new ArrayList<>();

        for (Author a : masterList) {
            // Tìm theo tên tác giả
            if (a.getName() != null && a.getName().toLowerCase().contains(q)) {
                result.add(a);
            }
        }
        displayedAuthorsLiveData.setValue(result);
    }

    public void sortByCreateAt(boolean newestFirst) {
        List<Author> current = displayedAuthorsLiveData.getValue();
        if (current == null || current.isEmpty()) return;

        // Tạo 1 list mới để sort, không sort trực tiếp trên list của LiveData
        List<Author> listToSort = new ArrayList<>(current);

        listToSort.sort((u1, u2) -> {
            if (u1.getCreateAt() == null || u2.getCreateAt() == null) return 0;
            return newestFirst ?
                    u2.getCreateAt().compareTo(u1.getCreateAt()) :  // giảm dần = mới nhất
                    u1.getCreateAt().compareTo(u2.getCreateAt()); // tăng dần = cũ nhất
        });

        displayedAuthorsLiveData.setValue(listToSort);
    }
}