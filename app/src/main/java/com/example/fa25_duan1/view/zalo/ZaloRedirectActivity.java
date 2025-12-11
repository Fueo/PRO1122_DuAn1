package com.example.fa25_duan1.view.zalo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.fa25_duan1.R;
import com.example.fa25_duan1.view.cart.CartFragment;
import com.example.fa25_duan1.view.cart.CheckoutFragment;
import com.example.fa25_duan1.view.cart.PaymentFragment;
import com.example.fa25_duan1.view.detail.HeaderDetailFragment;
import com.example.fa25_duan1.view.home.ProductFragment;
import com.example.fa25_duan1.view.profile.AddressFragment;
import com.example.fa25_duan1.view.profile.ChangeContactInforFragment;
import com.example.fa25_duan1.view.profile.ChangePasswordFragment;
import com.example.fa25_duan1.view.profile.OrderHistoryFragment;
import com.example.fa25_duan1.view.profile.OrderViewFragment;
import com.example.fa25_duan1.view.profile.ProfileFragment;

import vn.zalopay.sdk.ZaloPaySDK;

public class ZaloRedirectActivity extends AppCompatActivity {

    public static final String EXTRA_HEADER_TITLE = "extra_header_title";
    public static final String EXTRA_CONTENT_FRAGMENT = "extra_content_fragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            // 1. Lấy title từ intent
            String headerTitle = getIntent().getStringExtra(EXTRA_HEADER_TITLE);
            if (headerTitle == null) headerTitle = "Default Title";

            // 2. Load HeaderFragment với title
            HeaderDetailFragment headerFragment = HeaderDetailFragment.newInstance(headerTitle);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_header, headerFragment)
                    .commit();

            // 3. Load ContentFragment từ intent
            Fragment contentFragment = getFragmentFromIntent();
            if (contentFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_content, contentFragment)
                        .commit();
            }
        }
    }

    // Hàm helper để trả về fragment muốn render
    private Fragment getFragmentFromIntent() {
        String fragmentName = getIntent().getStringExtra(EXTRA_CONTENT_FRAGMENT);
        if ("checkout".equals(fragmentName)) {
            return new CheckoutFragment();
        } else if ("orderhistory".equals(fragmentName)) {
        return new OrderHistoryFragment();
        }

        return null;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        ZaloPaySDK.getInstance().onResult(intent); // Bắt buộc phải có dòng này
        processDeepLink(intent);
    }

    private void processDeepLink(Intent intent) {
        if (intent == null || intent.getData() == null) {
            Log.d("ZaloDebug", "Intent Data is Null");
            return;
        }

        Uri data = intent.getData();
        Log.d("ZaloDebug", "DeepLink nhận được: " + data.toString());

        // Kiểm tra xem có phải link ZaloPay trả về không (chứa scheme demozpdk)
        if (data.toString().contains("demozpdk")) {
            Log.d("ZaloDebug", "Đây là link ZaloPay -> Gọi SDK onResult");
            ZaloPaySDK.getInstance().onResult(intent);
        }
    }
}