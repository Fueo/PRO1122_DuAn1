package com.example.fa25_duan1.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.fa25_duan1.model.Discount;
import com.example.fa25_duan1.repository.DiscountRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DiscountViewModel extends AndroidViewModel {

    private final DiscountRepository repository;
    private MutableLiveData<List<Discount>> displayedDiscounts = new MutableLiveData<>();
    private List<Discount> originalList = new ArrayList<>();

    public DiscountViewModel(@NonNull Application application) {
        super(application);
        repository = new DiscountRepository(application.getApplicationContext());
        refreshData();
    }

    public MutableLiveData<List<Discount>> getDisplayedDiscounts() {
        return displayedDiscounts;
    }

    public void refreshData() {
        repository.getAllDiscounts().observeForever(discounts -> {
            if (discounts != null) {
                originalList = discounts;
                displayedDiscounts.setValue(originalList);
            } else {
                displayedDiscounts.setValue(new ArrayList<>());
            }
        });
    }

    public MutableLiveData<Boolean> addDiscount(Discount discount) {
        return repository.addDiscount(discount);
    }

    public MutableLiveData<Boolean> updateDiscount(String id, Discount discount) {
        return repository.updateDiscount(id, discount);
    }

    public MutableLiveData<Boolean> deleteDiscount(String id) {
        return repository.deleteDiscount(id);
    }

    public MutableLiveData<Discount> getDiscountByID(String id) {
        return repository.getDiscountById(id);
    }

    public void searchDiscounts(String query) {
        if (query == null || query.isEmpty()) {
            displayedDiscounts.setValue(originalList);
        } else {
            List<Discount> filtered = new ArrayList<>();
            for (Discount d : originalList) {
                if (d.getDiscountName() != null && d.getDiscountName().toLowerCase().contains(query.toLowerCase())) {
                    filtered.add(d);
                }
            }
            displayedDiscounts.setValue(filtered);
        }
    }

    public void sortDiscounts(int position) {
        List<Discount> currentList = new ArrayList<>(displayedDiscounts.getValue() != null ? displayedDiscounts.getValue() : originalList);

        switch (position) {
            case 0: // Mới nhất
                Collections.sort(currentList, (o1, o2) -> {
                    if (o1.getCreateAt() == null || o2.getCreateAt() == null) return 0;
                    return o2.getCreateAt().compareTo(o1.getCreateAt());
                });
                break;
            case 1: // Cũ nhất
                Collections.sort(currentList, (o1, o2) -> {
                    if (o1.getCreateAt() == null || o2.getCreateAt() == null) return 0;
                    return o1.getCreateAt().compareTo(o2.getCreateAt());
                });
                break;
            case 2: // A-Z
                Collections.sort(currentList, (o1, o2) -> {
                    String name1 = o1.getDiscountName() != null ? o1.getDiscountName() : "";
                    String name2 = o2.getDiscountName() != null ? o2.getDiscountName() : "";
                    return name1.compareToIgnoreCase(name2);
                });
                break;
        }
        displayedDiscounts.setValue(currentList);
    }
}