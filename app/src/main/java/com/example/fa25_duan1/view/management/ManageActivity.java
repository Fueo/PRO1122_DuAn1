package com.example.fa25_duan1.view.management;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.fa25_duan1.R;
import com.example.fa25_duan1.view.detail.HeaderDetailFragment;
import com.example.fa25_duan1.view.home.AdminFragment;
import com.example.fa25_duan1.view.management.account.AccountHeaderFragment;
import com.example.fa25_duan1.view.management.account.AccountManageFragment;
import com.example.fa25_duan1.view.management.author.AuthorHeaderFragment;
import com.example.fa25_duan1.view.management.author.AuthorManageFragment;
import com.example.fa25_duan1.view.management.category.CategoryHeaderFragment;
import com.example.fa25_duan1.view.management.category.CategoryManageFragment;
import com.example.fa25_duan1.view.management.product.ProductHeaderFragment;
import com.example.fa25_duan1.view.management.product.ProductManageFragment;

public class ManageActivity extends AppCompatActivity {


    public static final String EXTRA_HEADER_TITLE = "extra_header_title";
    public static final String EXTRA_CONTENT_FRAGMENT = "extra_content_fragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_management);

        if (savedInstanceState == null) {

            // 2. Load HeaderFragment với title
            Fragment headerFragment = getHeaderFragmentFromIntent();
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
            return new AccountManageFragment();
        } else if ("author".equals(fragmentName)) {
            return new AuthorManageFragment();
        } else if ("category".equals(fragmentName)) {
            return new CategoryManageFragment();
        }else if ("product".equals(fragmentName)) {
            return new ProductManageFragment();
        }
        return null;
    }

    private Fragment getHeaderFragmentFromIntent() {
        String fragmentName = getIntent().getStringExtra(EXTRA_CONTENT_FRAGMENT);
        if ("account".equals(fragmentName)) {
            return new AccountHeaderFragment();
        } else if ("author".equals(fragmentName)) {
            return new AuthorHeaderFragment();
        } else if ("category".equals(fragmentName)) {
            return new CategoryHeaderFragment();
        } else if ("product".equals(fragmentName)) {
            return new ProductHeaderFragment();
        }
        return null;
    }
}