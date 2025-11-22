package com.example.fa25_duan1.view.detail;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.fa25_duan1.R;
import com.example.fa25_duan1.view.cart.CartFragment;
import com.example.fa25_duan1.view.cart.CheckoutFragment;

public class ProductDetailActivity extends AppCompatActivity {

    public static final String EXTRA_HEADER_TITLE = "extra_header_title";
    public static final String EXTRA_CONTENT_FRAGMENT = "extra_content_fragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detail);

        if (savedInstanceState == null) {
            // 1. Lấy title từ intent

            // 2. Load HeaderFragment với title
            HeaderProductDetailFragment headerFragment = new HeaderProductDetailFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_header, headerFragment)
                    .commit();

            // 3. Load ContentFragment từ intent
            Fragment contentFragment = new DetailFragment();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_content, contentFragment)
                        .commit();
        }
    }
}