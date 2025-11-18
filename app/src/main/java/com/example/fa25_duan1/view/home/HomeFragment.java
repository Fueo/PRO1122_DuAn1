package com.example.fa25_duan1.view.home;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.fa25_duan1.R;
import com.example.fa25_duan1.adapter.BannerAdapter;
import com.example.fa25_duan1.adapter.BookGridAdapter;
import com.example.fa25_duan1.adapter.BookHorizontalAdapter;
import com.example.fa25_duan1.adapter.CategoryAdapter;
import com.example.fa25_duan1.adapter.RankingBookAdapter;
import com.example.fa25_duan1.model.Banner;
import com.example.fa25_duan1.model.Book;
import com.example.fa25_duan1.model.Category;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private ViewPager2 viewPagerBanner;
    private LinearLayout layoutIndicators;
    private RecyclerView rvBooksGrid, rvCategories, rvBooksHorizontal, rvRankingCategories, rvRankingBooks;
    private List<Banner> mListBanners;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);


        viewPagerBanner = view.findViewById(R.id.viewPagerBanner);
        layoutIndicators = view.findViewById(R.id.layoutIndicators);
        rvBooksGrid = view.findViewById(R.id.rv_books_grid);
        rvCategories = view.findViewById(R.id.rv_categories);
        rvBooksHorizontal = view.findViewById(R.id.rv_books_horizontal);
        rvRankingCategories = view.findViewById(R.id.rv_ranking_categories);
        rvRankingBooks = view.findViewById(R.id.rv_ranking_books);


        setupBanner();

        setupBookGrid();
        setupCategories();
        setupHorizontalBooks();
        setupRankingBooks();

        return view;
    }

    private void setupBanner() {
        mListBanners = new ArrayList<>();
        mListBanners.add(new Banner("Giấc mơ Mỹ", "Huyền Chip", R.drawable.banner));
        mListBanners.add(new Banner("Đắc Nhân Tâm", "Dale Carnegie", R.drawable.banner));
        mListBanners.add(new Banner("Nhà Giả Kim", "Paulo Coelho", R.drawable.banner));
        mListBanners.add(new Banner("Tắt Đèn", "Ngô Tất Tố", R.drawable.banner));

        BannerAdapter adapter = new BannerAdapter(mListBanners);
        viewPagerBanner.setAdapter(adapter);

        setupIndicators(mListBanners.size());
        setCurrentIndicator(0);

        viewPagerBanner.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                setCurrentIndicator(position);
            }
        });
    }

    private void setupIndicators(int count) {
        ImageView[] indicators = new ImageView[count];
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(8, 0, 8, 0);

        for (int i = 0; i < indicators.length; i++) {
            indicators[i] = new ImageView(getContext());
            indicators[i].setImageDrawable(ContextCompat.getDrawable(
                    requireContext(), R.drawable.bg_indicator_inactive));
            indicators[i].setLayoutParams(layoutParams);
            layoutIndicators.addView(indicators[i]);
        }
    }

    private void setCurrentIndicator(int position) {
        int childCount = layoutIndicators.getChildCount();
        for (int i = 0; i < childCount; i++) {
            ImageView imageView = (ImageView) layoutIndicators.getChildAt(i);
            if (i == position) {
                imageView.setImageDrawable(ContextCompat.getDrawable(
                        requireContext(), R.drawable.bg_indicator_active));
            } else {
                imageView.setImageDrawable(ContextCompat.getDrawable(
                        requireContext(), R.drawable.bg_indicator_inactive));
            }
        }
    }


    private void setupBookGrid() {

        List<Book> books = getGridBooks();

        BookGridAdapter adapter = new BookGridAdapter(books);
        rvBooksGrid.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rvBooksGrid.setAdapter(adapter);
    }

    private void setupCategories() {
        List<Category> categories = new ArrayList<>();
        categories.add(new Category("Sách tham khảo", true));
        categories.add(new Category("Văn học", false));
        categories.add(new Category("Kinh tế", false));
        categories.add(new Category("Kỹ năng sống", false));

        CategoryAdapter adapter = new CategoryAdapter(categories);
        rvCategories.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvCategories.setAdapter(adapter);

        rvRankingCategories.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvRankingCategories.setAdapter(adapter);
    }

    private void setupHorizontalBooks() {

        List<Book> books = getDummyBooks();

        BookHorizontalAdapter adapter = new BookHorizontalAdapter(books);
        rvBooksHorizontal.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvBooksHorizontal.setAdapter(adapter);
    }

    private void setupRankingBooks() {

        List<Book> books = getDummyBooks();

        RankingBookAdapter adapter = new RankingBookAdapter(books);
        rvRankingBooks.setLayoutManager(new LinearLayoutManager(getContext()));
        rvRankingBooks.setAdapter(adapter);
    }


    private List<Book> getDummyBooks() {
        List<Book> list = new ArrayList<>();

        list.add(new Book("Thằng Huyện - Con Hầu", "Nguyễn Thế Hà", R.drawable.book_cover_placeholder, 32000, 50000, "-20%", 3636, 1200));
        list.add(new Book("Mắt Biếc", "Nguyễn Nhật Ánh", R.drawable.book_cover_placeholder, 80000, 100000, "-20%", 5000, 2300));
        list.add(new Book("Nhà Giả Kim", "Paulo Coelho", R.drawable.book_cover_placeholder, 65000, 75000, "-15%", 9000, 4500));
        list.add(new Book("Tắt Đèn", "Ngô Tất Tố", R.drawable.book_cover_placeholder, 40000, 45000, "-10%", 2000, 800));
        return list;
    }


    private List<Book> getGridBooks() {
        List<Book> list = new ArrayList<>();
        list.add(new Book("Thằng Huyện - Con Hầu", "Nguyễn Thế Hà", R.drawable.book_cover_placeholder, 32000, 50000, "-20%", 3636, 1200));
        list.add(new Book("Mắt Biếc", "Nguyễn Nhật Ánh", R.drawable.book_cover_placeholder, 80000, 100000, "-20%", 5000, 2300));
        list.add(new Book("Nhà Giả Kim", "Paulo Coelho", R.drawable.book_cover_placeholder, 65000, 75000, "-15%", 9000, 4500));
        list.add(new Book("Tắt Đèn", "Ngô Tất Tố", R.drawable.book_cover_placeholder, 40000, 45000, "-10%", 2000, 800));

        return list;
    }
}