package com.example.fa25_duan1.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.example.fa25_duan1.model.User;
import com.example.fa25_duan1.repository.UserRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class UserViewModel extends AndroidViewModel {

    private final UserRepository repository;

    // "allUsersLiveData" là nguồn dữ liệu gốc, không bao giờ bị sửa đổi bởi filter/search
    private final MediatorLiveData<List<User>> allUsersLiveData = new MediatorLiveData<>();

    // "displayedUsersLiveData" là danh sách mà Fragment sẽ observe để hiển thị
    private final MediatorLiveData<List<User>> displayedUsersLiveData = new MediatorLiveData<>();

    // Biến này để theo dõi nguồn dữ liệu từ repo, để có thể "remove" và "add" lại khi refresh
    private LiveData<List<User>> currentRepoSource;

    public UserViewModel(@NonNull Application application) {
        super(application);
        repository = new UserRepository(application.getApplicationContext());
        // Tải dữ liệu lần đầu tiên khi ViewModel được tạo
        refreshData();
    }

    /**
     * Lấy danh sách user để hiển thị (Fragment sẽ observe cái này)
     */
    public LiveData<List<User>> getDisplayedUsers() {
        return displayedUsersLiveData;
    }

    /**
     * HÀM QUAN TRỌNG: Tải lại (hoặc tải lần đầu) toàn bộ dữ liệu từ Repository.
     * Hàm này sẽ được gọi từ Fragment khi onActivityResult (sau khi Thêm/Sửa/Xóa thành công)
     */
    public void refreshData() {
        // 1. Nếu đã có nguồn cũ, gỡ nó ra
        if (currentRepoSource != null) {
            displayedUsersLiveData.removeSource(currentRepoSource);
        }

        // 2. Lấy nguồn dữ liệu mới từ Repository
        currentRepoSource = repository.getAllUsers();

        // 3. Lắng nghe nguồn dữ liệu mới này
        displayedUsersLiveData.addSource(currentRepoSource, users -> {
            if (users == null) {
                users = new ArrayList<>();
            }

            // 3.1 Cập nhật danh sách gốc (master list)
            allUsersLiveData.setValue(users);

            // 3.2 Sắp xếp danh sách để hiển thị (theo logic mặc định của bạn)
            List<User> sorted = new ArrayList<>(users);
            sorted.sort((u1, u2) -> {
                if (u1.getCreateAt() == null || u2.getCreateAt() == null) return 0;
                return u2.getCreateAt().compareTo(u1.getCreateAt()); // giảm dần: mới nhất trước
            });

            // 3.3 Cập nhật danh sách sẽ được hiển thị
            displayedUsersLiveData.setValue(sorted);
            Log.d("UserViewModel", "Data refreshed. Loaded " + users.size() + " users.");
        });
    }

    // --- CÁC HÀM CRUD (THÊM/SỬA/XÓA) ---
    // Các hàm này CHỈ gọi repository và trả về LiveData cho Activity/Fragment tự observe
    // Chúng KHÔNG tự ý sửa list trong ViewModel
    // Chúng KHÔNG dùng observeForever

    public LiveData<User> addUserWithAvatar(RequestBody username,
                                            RequestBody password,
                                            RequestBody name,
                                            RequestBody email,
                                            RequestBody phone,
                                            RequestBody address,
                                            RequestBody role,
                                            MultipartBody.Part avatar) {
        // Chỉ gọi và trả về. UpdateActivity sẽ observe kết quả này.
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
        // Chỉ gọi và trả về. UpdateActivity sẽ observe kết quả này.
        return repository.updateUserWithAvatar(id, username, password, name, email, phone, address, role, avatar);
    }

    public LiveData<Boolean> deleteUser(String id) {
        // Chỉ gọi và trả về. AccountManageFragment sẽ observe kết quả này.
        return repository.deleteUser(id);
    }

    public LiveData<User> getUserByID(String id) {
        // Đây là hàm pass-through, giữ nguyên
        return repository.getUserByID(id);
    }


    // --- CÁC HÀM LỌC, TÌM KIẾM, SẮP XẾP (LOCAL) ---
    // Các hàm này đọc từ "allUsersLiveData" và ghi vào "displayedUsersLiveData"

    /**
     * Tìm kiếm user. Sẽ đọc từ danh sách gốc (allUsersLiveData).
     */
    public void searchUsers(String query, String type) {
        List<User> masterList = allUsersLiveData.getValue();
        if (masterList == null) masterList = new ArrayList<>();

        // Nếu query rỗng, reset lại danh sách hiển thị về danh sách gốc (đã sắp xếp)
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
            switch (type.toLowerCase()) {
                case "name":
                    if (u.getName() != null && u.getName().toLowerCase().contains(q)) result.add(u);
                    break;
                case "phone":
                    if (u.getPhone() != null && u.getPhone().toLowerCase().contains(q)) result.add(u);
                    break;
                case "email":
                    if (u.getEmail() != null && u.getEmail().toLowerCase().contains(q)) result.add(u);
                    break;
                default:
                    if ((u.getName() != null && u.getName().toLowerCase().contains(q)) ||
                            (u.getPhone() != null && u.getPhone().toLowerCase().contains(q)) ||
                            (u.getEmail() != null && u.getEmail().toLowerCase().contains(q))) {
                        result.add(u);
                    }
                    break;
            }
        }
        displayedUsersLiveData.setValue(result);
    }

    /**
     * Lọc user theo role. Sẽ đọc từ danh sách gốc (allUsersLiveData).
     */
    public void filterByRole(int role) {
        List<User> masterList = allUsersLiveData.getValue();
        if (masterList == null) masterList = new ArrayList<>();

        // Giả sử -1 là "Tất cả"
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
        List<User> masterList = allUsersLiveData.getValue();
        if (masterList == null) masterList = new ArrayList<>();

        // Nếu danh sách vai trò rỗng, ta hiển thị "Tất cả"
        // (Chúng ta có thể gọi lại hàm cũ, hoặc sao chép logic "Tất cả" ở đây)
        if (roles == null || roles.isEmpty()) {
            // Gọi lại logic "Tất cả" (ví dụ: -1)
            filterByRole(-1);
            return;
        }

        // Nếu có vai trò được chọn, lọc theo danh sách
        List<User> result = new ArrayList<>();
        for (User u : masterList) {
            // Kiểm tra xem vai trò của người dùng có nằm trong danh sách được chọn không
            if (roles.contains(u.getRole())) {
                result.add(u);
            }
        }

        // (Quan trọng) Bạn có thể muốn sắp xếp danh sách kết quả này
        // trước khi hiển thị, ví dụ:
        result.sort((u1, u2) -> {
            if (u1.getCreateAt() == null || u2.getCreateAt() == null) return 0;
            return u2.getCreateAt().compareTo(u1.getCreateAt()); // Sắp xếp mới nhất
        });

        displayedUsersLiveData.setValue(result);
    }

    /**
     * Sắp xếp danh sách ĐANG HIỂN THỊ (displayedUsersLiveData).
     */
    public void sortByCreateAt(boolean newestFirst) {
        List<User> current = displayedUsersLiveData.getValue();
        if (current == null || current.isEmpty()) return;

        // Tạo 1 list mới để sort, không sort trực tiếp trên list của LiveData
        List<User> listToSort = new ArrayList<>(current);

        listToSort.sort((u1, u2) -> {
            if (u1.getCreateAt() == null || u2.getCreateAt() == null) return 0;
            return newestFirst ?
                    u2.getCreateAt().compareTo(u1.getCreateAt()) :  // giảm dần = mới nhất
                    u1.getCreateAt().compareTo(u2.getCreateAt()); // tăng dần = cũ nhất
        });

        displayedUsersLiveData.setValue(listToSort);
    }
}