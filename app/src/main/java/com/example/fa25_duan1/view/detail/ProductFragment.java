package com.example.fa25_duan1.view.detail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fa25_duan1.R;
import com.example.fa25_duan1.adapter.BookGridAdapter;
import com.example.fa25_duan1.model.Book;
import com.example.fa25_duan1.view.management.product.ProductFilterFragment;

import java.util.ArrayList;
import java.util.List;

public class ProductFragment extends Fragment {

    private RecyclerView rvProducts;
    private BookGridAdapter bookGridAdapter;
    private List<Book> mListBooks;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Sử dụng layout fragment_product (tên mới của account layout mà bạn đã sửa)
        View view = inflater.inflate(R.layout.fragment_product, container, false);

        rvProducts = view.findViewById(R.id.rv_products);
        if (savedInstanceState == null) {
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_filter, new ProductListFilterFragment())
                    .commit();
        }
        setupProductGrid();

        return view;
    }

    private void setupProductGrid() {
        mListBooks = getAllBooks();
        bookGridAdapter = new BookGridAdapter(mListBooks);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
        rvProducts.setLayoutManager(gridLayoutManager);
        rvProducts.setAdapter(bookGridAdapter);
    }

    private List<Book> getAllBooks() {
        List<Book> list = new ArrayList<>();
        // Sách Văn học
        list.add(new Book("Mắt Biếc", "Nguyễn Nhật Ánh", R.drawable.book_cover_placeholder, 80000, 100000, "-20%", 5000, 2300));
        list.add(new Book("Tắt Đèn", "Ngô Tất Tố", R.drawable.book_cover_placeholder, 40000, 45000, "-10%", 2000, 800));
        list.add(new Book("Số Đỏ", "Vũ Trọng Phụng", R.drawable.book_cover_placeholder, 50000, 60000, "-15%", 1000, 500));
        list.add(new Book("Thằng Huyện - Con Hầu", "Nguyễn Thế Hà", R.drawable.book_cover_placeholder, 32000, 50000, "-20%", 3636, 1200));

        // Sách Kinh tế
        list.add(new Book("Cha Giàu Cha Nghèo", "Robert Kiyosaki", R.drawable.book_cover_placeholder, 90000, 120000, "-25%", 8000, 4000));
        list.add(new Book("Chiến Tranh Tiền Tệ", "Song Hongbing", R.drawable.book_cover_placeholder, 150000, 200000, "-25%", 1200, 300));

        // Sách Kỹ năng
        list.add(new Book("Đắc Nhân Tâm", "Dale Carnegie", R.drawable.book_cover_placeholder, 70000, 85000, "-18%", 9999, 5000));
        list.add(new Book("Nhà Giả Kim", "Paulo Coelho", R.drawable.book_cover_placeholder, 65000, 75000, "-15%", 9000, 4500));

        // Sách Giáo khoa
        list.add(new Book("Giải Tích 12", "Bộ Giáo Dục", R.drawable.book_cover_placeholder, 15000, 15000, "0%", 5000, 100));
        list.add(new Book("Vật Lý Đại Cương", "Lương Duyên Bình", R.drawable.book_cover_placeholder, 45000, 50000, "-10%", 1500, 200));

        return list;
    }
}