package com.example.fa25_duan1.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import com.example.fa25_duan1.model.ApiResponse;
import com.example.fa25_duan1.model.User;
import com.example.fa25_duan1.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class UserViewModel extends AndroidViewModel {
    private final UserRepository repository;

    // Dùng để quản lý search/filter tại local
    private final MediatorLiveData<List<User>> allUsersLiveData = new MediatorLiveData<>();
    private final MediatorLiveData<List<User>> displayedUsersLiveData = new MediatorLiveData<>();

    // Biến để hứng LiveData từ Repo
    private LiveData<ApiResponse<List<User>>> currentRepoSource;

    public UserViewModel(@NonNull Application application) {
        super(application);
        repository = new UserRepository(application.getApplicationContext());
        refreshData();
    }

    public LiveData<List<User>> getDisplayedUsers() { return displayedUsersLiveData; }

    public void refreshData() {
        if (currentRepoSource != null) displayedUsersLiveData.removeSource(currentRepoSource);

        // Gọi Repo -> Repo trả về LiveData<ApiResponse> đã xử lý xong xuôi
        currentRepoSource = repository.getAllUsers();

        displayedUsersLiveData.addSource(currentRepoSource, response -> {
            List<User> users = new ArrayList<>();

            // ViewModel chỉ cần check status và lấy data
            if (response != null && response.isStatus() && response.getData() != null) {
                users = response.getData();
            }

            allUsersLiveData.setValue(users);

            // Sort mặc định (Mới nhất lên đầu)
            List<User> sorted = new ArrayList<>(users);
            sorted.sort((u1, u2) -> {
                if (u1.getCreateAt() == null || u2.getCreateAt() == null) return 0;
                return u2.getCreateAt().compareTo(u1.getCreateAt());
            });
            displayedUsersLiveData.setValue(sorted);
        });
    }

    // --- CÁC HÀM GỌI API ---
    // Trả thẳng ApiResponse về cho View xử lý hiển thị Message

    public LiveData<ApiResponse<Integer>> getTotalAccount() {
        return repository.getTotalAccount();
    }

    public LiveData<ApiResponse<User>> addUserWithAvatar(RequestBody username, RequestBody password, RequestBody name, RequestBody email, RequestBody role, MultipartBody.Part avatar) {
        return repository.addUserWithAvatar(username, password, name, email, role, avatar);
    }

    public LiveData<ApiResponse<User>> updateUserWithAvatar(String id, RequestBody username, RequestBody password, RequestBody name, RequestBody email, RequestBody role, MultipartBody.Part avatar) {
        return repository.updateUserWithAvatar(id, username, password, name, email, role, avatar);
    }

    public LiveData<ApiResponse<Object>> deleteUser(String id) {
        return repository.deleteUser(id);
    }

    public LiveData<ApiResponse<User>> getUserByID(String id) {
        return repository.getUserByID(id);
    }

    // --- CÁC HÀM SEARCH / FILTER GIỮ NGUYÊN ---
    // (Vì các hàm này thao tác trên List<User> đã lưu trong RAM ở biến allUsersLiveData)
    public void searchUsers(String query, String type) {
        List<User> masterList = allUsersLiveData.getValue();
        if (masterList == null) masterList = new ArrayList<>();
        if (query == null || query.trim().isEmpty()) {
            List<User> sorted = new ArrayList<>(masterList);
            sorted.sort((u1, u2) -> {
                if (u1.getCreateAt() == null || u2.getCreateAt() == null) return 0;
                return u2.getCreateAt().compareTo(u1.getCreateAt());
            });
            displayedUsersLiveData.setValue(sorted);
            return;
        }
        String q = query.toLowerCase().trim();
        List<User> result = new ArrayList<>();
        for (User u : masterList) {
            if ((u.getName() != null && u.getName().toLowerCase().contains(q)) ||
                    (u.getEmail() != null && u.getEmail().toLowerCase().contains(q))) {
                result.add(u);
            }
        }
        displayedUsersLiveData.setValue(result);
    }

    public void filterByRole(int role) {
        // ... (Giữ nguyên code filter cũ của bạn) ...
        List<User> masterList = allUsersLiveData.getValue();
        if (masterList == null) masterList = new ArrayList<>();

        if (role == -1) {
            List<User> sorted = new ArrayList<>(masterList);
            sorted.sort((u1, u2) -> {
                if (u1.getCreateAt() == null || u2.getCreateAt() == null) return 0;
                return u2.getCreateAt().compareTo(u1.getCreateAt());
            });
            displayedUsersLiveData.setValue(sorted);
            return;
        }

        List<User> result = new ArrayList<>();
        for (User u : masterList) {
            if (u.getRole() == role) result.add(u);
        }
        displayedUsersLiveData.setValue(result);
    }

    public void filterByRoles(List<Integer> roles) {
        // ... (Giữ nguyên code filter cũ của bạn) ...
        List<User> masterList = allUsersLiveData.getValue();
        if (masterList == null) masterList = new ArrayList<>();

        if (roles == null || roles.isEmpty()) {
            filterByRole(-1);
            return;
        }

        List<User> result = new ArrayList<>();
        for (User u : masterList) {
            if (roles.contains(u.getRole())) {
                result.add(u);
            }
        }

        result.sort((u1, u2) -> {
            if (u1.getCreateAt() == null || u2.getCreateAt() == null) return 0;
            return u2.getCreateAt().compareTo(u1.getCreateAt());
        });

        displayedUsersLiveData.setValue(result);
    }

    public void sortByCreateAt(boolean newestFirst) {
        // ... (Giữ nguyên code sort cũ của bạn) ...
        List<User> current = displayedUsersLiveData.getValue();
        if (current == null || current.isEmpty()) return;

        List<User> listToSort = new ArrayList<>(current);

        listToSort.sort((u1, u2) -> {
            if (u1.getCreateAt() == null || u2.getCreateAt() == null) return 0;
            return newestFirst ?
                    u2.getCreateAt().compareTo(u1.getCreateAt()) :
                    u1.getCreateAt().compareTo(u2.getCreateAt());
        });

        displayedUsersLiveData.setValue(listToSort);
    }
}