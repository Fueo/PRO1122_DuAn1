package com.example.fa25_duan1.network;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.fa25_duan1.BuildConfig;
import com.example.fa25_duan1.R; // Import R để lấy icon lỗi
import com.example.fa25_duan1.view.auth.AuthActivity;
import com.example.fa25_duan1.model.auth.RefreshTokenRequest;
import com.example.fa25_duan1.model.auth.RefreshTokenResponse;

import java.io.IOException;

import io.github.cutelibs.cutedialog.CuteDialog; // Import thư viện CuteDialog
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
        // ... (Giữ nguyên logic authenticate như code cũ của bạn) ...
        Log.d(TAG, "Authenticator kích hoạt! Mã lỗi: " + response.code());

        SharedPreferences prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String refreshToken = prefs.getString("refreshToken", null);

        if (refreshToken == null) {
            Log.e(TAG, "Không có Refresh Token -> Logout.");
            forceLogout();
            return null;
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL_ATHOME)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        AuthApi authApi = retrofit.create(AuthApi.class);

        try {
            // LƯU Ý: Vẫn đang để token giả để test như bạn yêu cầu
            retrofit2.Response<RefreshTokenResponse> refreshResponse =
                    authApi.refreshToken(new RefreshTokenRequest(refreshToken)).execute();

            if (refreshResponse.isSuccessful() && refreshResponse.body() != null) {
                String newAccessToken = refreshResponse.body().getAccessToken();
                prefs.edit().putString("accessToken", newAccessToken).apply();
                Log.i(TAG, "Refresh thành công.");

                return response.request().newBuilder()
                        .header("Authorization", "Bearer " + newAccessToken)
                        .build();
            } else {
                Log.e(TAG, "Refresh thất bại -> Logout.");
                forceLogout();
                return null;
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi mạng khi refresh: " + e.getMessage());
            return null;
        }
    }

    /**
     * Hàm xử lý hiển thị Dialog và Logout
     * Phải chạy trên UI Thread
     */
    private void forceLogout() {
        // Sử dụng Handler để chuyển sang Main Thread (UI Thread)
        new Handler(Looper.getMainLooper()).post(() -> {
            try {
                // Kiểm tra xem Context có phải là Activity đang chạy không để tránh lỗi bad token
                if (context instanceof Activity && !((Activity) context).isFinishing()) {

                    new CuteDialog.withIcon(context)
                            .setIcon(R.drawable.ic_dialog_error) // Thay bằng icon lỗi của bạn
                            .setTitle("Phiên đăng nhập hết hạn")
                            .setDescription("Vui lòng đăng nhập lại để tiếp tục sử dụng.")
                            .setPositiveButtonText("Đồng ý", v -> {
                                // Khi người dùng bấm Đồng ý thì mới xóa data và chuyển màn hình
                                performClearAndRedirect();
                            })
                            .isCancelable(false)
                            .show();
                } else {
                    // Nếu context không phải Activity (ví dụ ApplicationContext)
                    // hoặc Activity đã đóng -> Logout luôn không hiện Dialog để tránh crash
                    performClearAndRedirect();
                }
            } catch (Exception e) {
                Log.e(TAG, "Lỗi hiển thị dialog: " + e.getMessage());
                performClearAndRedirect(); // Fallback an toàn
            }
        });
    }

    /**
     * Hàm thực hiện xóa dữ liệu và chuyển trang
     */
    private void performClearAndRedirect() {
        // 1. Xóa dữ liệu
        SharedPreferences prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();

        Log.d(TAG, "Đã xóa dữ liệu và chuyển về Login.");

        // 2. Chuyển về AuthActivity
        Intent intent = new Intent(context, AuthActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }
}