package com.example.fa25_duan1.view.management;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.fa25_duan1.view.detail.HeaderDetailFragment;
import com.example.fa25_duan1.R;
import com.example.fa25_duan1.view.home.AdminFragment;
import com.example.fa25_duan1.view.management.account.AccountUpdateFragment;
import com.example.fa25_duan1.view.management.author.AuthorUpdateFragment;
import com.example.fa25_duan1.view.management.category.CategoryUpdateFragment;
import com.example.fa25_duan1.view.management.discount.DiscountUpdateFragment;
import com.example.fa25_duan1.view.management.product.ProductUpdateFragment;

public class UpdateActivity extends AppCompatActivity {


    public static final String EXTRA_HEADER_TITLE = "extra_header_title";
    public static final String EXTRA_CONTENT_FRAGMENT = "extra_content_fragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_account_update);

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
        if ("account".equals(fragmentName)) {
            return new AccountUpdateFragment();
        } else if ("author".equals(fragmentName)) {
            return new AuthorUpdateFragment();
        } else if ("category".equals(fragmentName)) {
            return new CategoryUpdateFragment();
        } else if ("product".equals(fragmentName)) {
            return new ProductUpdateFragment();
    }    else if ("discount".equals(fragmentName)) {
            return new DiscountUpdateFragment();
        }
        return null;
    }
}