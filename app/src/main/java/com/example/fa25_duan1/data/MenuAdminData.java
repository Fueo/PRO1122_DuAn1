package com.example.fa25_duan1.data;

import android.content.Context;

import com.example.fa25_duan1.R;
import com.example.fa25_duan1.model.MenuItem;

import java.util.ArrayList;

public class MenuAdminData {
    public static ArrayList<MenuItem> getListMenuAdminData() {
        ArrayList<MenuItem> listMenuManager = new ArrayList<>();
        listMenuManager.add(new MenuItem(0,R.drawable.ic_category, "Danh mục"));
        listMenuManager.add(new MenuItem(1, R.drawable.ic_product, "Sản phẩm"));
        listMenuManager.add(new MenuItem(2, R.drawable.ic_invoice, "Hóa đơn"));
        listMenuManager.add(new MenuItem(3, R.drawable.ic_sales, "Giảm giá"));
        listMenuManager.add(new MenuItem(4, R.drawable.ic_author, "Tác giả"));
        listMenuManager.add(new MenuItem(5, R.drawable.ic_account, "Tài khoản"));
        listMenuManager.add(new MenuItem(6, R.drawable.ic_statistic, "Thống kê"));
        return listMenuManager;
    }
}
