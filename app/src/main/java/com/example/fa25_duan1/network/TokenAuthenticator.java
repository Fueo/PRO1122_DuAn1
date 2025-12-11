package com.example.fa25_duan1.network;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.fa25_duan1.BuildConfig;
import com.example.fa25_duan1.R;
import com.example.fa25_duan1.model.ApiResponse;
import com.example.fa25_duan1.view.auth.AuthActivity;
import com.example.fa25_duan1.model.auth.RefreshTokenRequest;
import com.example.fa25_duan1.model.auth.RefreshTokenResponse;

import java.io.IOException;

import io.github.cutelibs.cutedialog.CuteDialog;
import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TokenAuthenticator implements Authenticator {

    private final Context context;
    private static final String TAG = "AuthDebug";

    public TokenAuthenticator(Context context) {
        this.context = context;
    }

    @Override
    public Request authenticate(Route route, Response response) throws IOException {
        Log.d(TAG, "Authenticator kích hoạt (401 Detected)!");

        // 1. Lấy SharedPreferences để kiểm tra trạng thái đăng nhập
        SharedPreferences prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String accessToken = prefs.getString("accessToken", null);
        String refreshToken = prefs.getString("refreshToken", null);

        // [LOGIC MỚI] Check xem đã đăng nhập chưa
        // Nếu accessToken hoặc refreshToken là null => Tức là User chưa đăng nhập (Guest) hoặc đã Logout.
        if (accessToken == null || refreshToken == null) {
            Log.w(TAG, "User chưa đăng nhập hoặc là Guest. Bỏ qua Refresh.");
            // Return null nghĩa là: "OkHttp ơi, đừng thử lại nữa, chấp nhận lỗi 401 đi".
            // Lúc này ở UI (Activity/Fragment) sẽ nhận được lỗi 401 để xử lý (vd: hiện thông báo cần đăng nhập).
            return null;
        }

        // 2. Kiểm tra Retry Count (Tránh vòng lặp vô tận nếu Refresh Token cũng bị lỗi)
        if (responseCount(response) >= 2) {
            Log.e(TAG, "Đã thử refresh 2 lần nhưng vẫn thất bại -> Buộc đăng xuất.");
            forceLogout();
            return null;
        }

        // 3. Bắt đầu quy trình Refresh Token
        // Tạo Retrofit riêng (Sync) để tránh vòng lặp phụ thuộc với RetrofitClient chính
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        AuthApi authApi = retrofit.create(AuthApi.class);

        try {
            Log.d(TAG, "Đang gọi API Refresh Token...");

            // Gọi Sync (execute)
            retrofit2.Response<ApiResponse<RefreshTokenResponse>> refreshResponse =
                    authApi.refreshToken(new RefreshTokenRequest(refreshToken)).execute();

            // 4. Kiểm tra kết quả từ Server
            if (refreshResponse.isSuccessful() && refreshResponse.body() != null) {
                ApiResponse<RefreshTokenResponse> apiResponse = refreshResponse.body();

                if (apiResponse.isStatus() && apiResponse.getData() != null) {

                    String newAccessToken = apiResponse.getData().getAccessToken();

                    // Lưu Token mới vào SharedPreferences
                    prefs.edit().putString("accessToken", newAccessToken).apply();
                    Log.i(TAG, "Refresh thành công! Token mới đã lưu.");

                    // [QUAN TRỌNG] Trả về Request mới kèm Header mới để Retrofit tự gọi lại API cũ
                    return response.request().newBuilder()
                            .header("Authorization", "Bearer " + newAccessToken)
                            .build();
                } else {
                    // Server trả về 200 nhưng status false (vd: Refresh token bị thu hồi/hết hạn)
                    Log.e(TAG, "Refresh thất bại (Logic Server): " + apiResponse.getMessage());
                    forceLogout();
                    return null;
                }
            } else {
                // Server trả về 400, 401, 500 cho API refresh
                Log.e(TAG, "Refresh thất bại (HTTP Code: " + refreshResponse.code() + ") -> Buộc đăng xuất.");
                forceLogout();
                return null;
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi mạng khi refresh: " + e.getMessage());
            // Trả về null để API gốc ném ra lỗi mạng cho UI xử lý
            return null;
        }
    }

    // Đếm số lần retry của Request
    private int responseCount(Response response) {
        int result = 1;
        while ((response = response.priorResponse()) != null) {
            result++;
        }
        return result;
    }

    /**
     * Hàm hiển thị Dialog Hết phiên và Xóa dữ liệu
     * Phải chạy trên Main Thread (UI Thread)
     */
    private void forceLogout() {
        new Handler(Looper.getMainLooper()).post(() -> {
            try {
                // Chỉ hiện Dialog nếu Context là Activity đang chạy (để tránh crash app)
                if (context instanceof Activity && !((Activity) context).isFinishing()) {
                    new CuteDialog.withIcon(context)
                            .setIcon(R.drawable.ic_dialog_error)
                            .setTitle("Phiên đăng nhập hết hạn")
                            .setDescription("Vui lòng đăng nhập lại để tiếp tục.")
                            .setPositiveButtonText("Đồng ý", v -> performClearAndRedirect())
                            .isCancelable(false) // Bắt buộc bấm Đồng ý
                            .show();
                } else {
                    // Nếu không hiện được Dialog (vd đang ở background), logout ngầm luôn
                    performClearAndRedirect();
                }
            } catch (Exception e) {
                Log.e(TAG, "Lỗi UI Force Logout: " + e.getMessage());
                performClearAndRedirect();
            }
        });
    }

    /**
     * Xóa SharedPreferences và chuyển về màn hình Login
     */
    private void performClearAndRedirect() {
        SharedPreferences prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        prefs.edit().clear().apply(); // Xóa sạch token

        Log.d(TAG, "Đã xóa dữ liệu. Chuyển hướng về AuthActivity.");

        Intent intent = new Intent(context, AuthActivity.class);
        // Cờ này giúp xóa sạch Stack Activity cũ, user không thể bấm Back để quay lại
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }
}