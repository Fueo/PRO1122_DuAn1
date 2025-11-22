package com.example.fa25_duan1.view.detail;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.fa25_duan1.R;
import com.example.fa25_duan1.view.cart.CartFragment;
import com.example.fa25_duan1.view.cart.CheckoutFragment;
import com.example.fa25_duan1.view.home.AdminFragment;

public class DetailActivity extends AppCompatActivity {

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
        if ("profile".equals(fragmentName)) {
            return new ProfileFragment();
        } else if ("cart".equals(fragmentName)) {
            return new CartFragment();
        } else if ("checkout".equals(fragmentName)) {
            return new CheckoutFragment();
        } else if ("orderhistory".equals(fragmentName)) {
        return new OrderHistoryFragment();
        }  else if ("product".equals(fragmentName)) {
        return new ProductFragment();
    }

        return null;
    }
}