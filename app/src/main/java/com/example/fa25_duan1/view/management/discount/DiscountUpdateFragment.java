package com.example.fa25_duan1.view.management.discount;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fa25_duan1.R;
import com.example.fa25_duan1.adapter.ProductSelectionAdapter;
import com.example.fa25_duan1.model.Discount;
import com.example.fa25_duan1.model.Product;
import com.example.fa25_duan1.view.management.ManageActivity;
import com.example.fa25_duan1.viewmodel.DiscountViewModel;
import com.example.fa25_duan1.viewmodel.ProductViewModel;
import com.shashank.sony.fancytoastlib.FancyToast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import io.github.cutelibs.cutedialog.CuteDialog;

public class DiscountUpdateFragment extends Fragment {

    private EditText edtCode, edtPercent, edtStartDate, edtEndDate;
    private Button btnSave;
    private RadioGroup rgScope;
    private LinearLayout layoutSelectedProducts;
    private TextView tvSelectedCount, btnOpenProductSelector;
    private RecyclerView rvSelectedProducts;

    private DiscountViewModel discountViewModel;
    private ProductViewModel productViewModel;
    private ProductSelectionAdapter previewAdapter;
    private String discountId;

    private final Calendar startCalendar = Calendar.getInstance();
    private final Calendar endCalendar = Calendar.getInstance();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    private ArrayList<String> selectedProductIds = new ArrayList<>();
    private List<Product> previewProductList = new ArrayList<>();

    private final ActivityResultLauncher<Intent> selectProductLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    ArrayList<String> results = result.getData().getStringArrayListExtra("selected_ids");
                    if (results != null) {
                        this.selectedProductIds = results;
                        updateSelectedCountUI();
                        loadSelectedProductsDetails();
                    }
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_discount_update, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);

        discountViewModel = new ViewModelProvider(this).get(DiscountViewModel.class);
        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);

        setupRecyclerView();

        if (getActivity().getIntent() != null) {
            discountId = getActivity().getIntent().getStringExtra("Id");
        }

        setupDatePickers();
        setupScopeEvents();

        btnOpenProductSelector.setOnClickListener(v -> openSelectProductActivity());

        if (discountId != null) {
            btnSave.setText("Cập nhật");
            loadDiscountData(discountId);
        } else {
            btnSave.setText("Thêm mới");
            rgScope.check(R.id.rbAllProducts);
            layoutSelectedProducts.setVisibility(View.GONE);
        }

        btnSave.setOnClickListener(v -> handleSave());
    }

    private void initViews(View view) {
        edtCode = view.findViewById(R.id.edtCode);
        edtPercent = view.findViewById(R.id.edtPercent);
        edtStartDate = view.findViewById(R.id.edtStartDate);
        edtEndDate = view.findViewById(R.id.edtEndDate);
        btnSave = view.findViewById(R.id.btnSaveDiscount);
        rgScope = view.findViewById(R.id.rgScope);
        layoutSelectedProducts = view.findViewById(R.id.layoutSelectedProducts);
        tvSelectedCount = view.findViewById(R.id.tvSelectedCount);
        btnOpenProductSelector = view.findViewById(R.id.btnOpenProductSelector);
        rvSelectedProducts = view.findViewById(R.id.rvSelectedProducts);
    }

    private void setupRecyclerView() {
        previewAdapter = new ProductSelectionAdapter(getContext(), (count, selectedIds) -> {
            this.selectedProductIds = new ArrayList<>(selectedIds);
            updateSelectedCountUI();
        });
        rvSelectedProducts.setLayoutManager(new LinearLayoutManager(getContext()));
        rvSelectedProducts.setAdapter(previewAdapter);
    }

    private void setupScopeEvents() {
        rgScope.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbSpecificProducts) {
                layoutSelectedProducts.setVisibility(View.VISIBLE);
                if (previewProductList.isEmpty() && !selectedProductIds.isEmpty()) {
                    loadSelectedProductsDetails();
                }
            } else {
                layoutSelectedProducts.setVisibility(View.GONE);
            }
        });
    }

    private void loadSelectedProductsDetails() {
        if (selectedProductIds == null || selectedProductIds.isEmpty()) {
            previewProductList.clear();
            previewAdapter.setList(new ArrayList<>());
            return;
        }
        previewProductList.clear();
        previewAdapter.setList(new ArrayList<>());
        previewAdapter.setSelectedIds(selectedProductIds);

        for (String id : selectedProductIds) {
            // [SỬA] Xử lý ApiResponse<Product>
            productViewModel.getProductByID(id).observe(getViewLifecycleOwner(), apiResponse -> {
                if (apiResponse != null && apiResponse.isStatus()) {
                    Product product = apiResponse.getData();
                    if (product != null) {
                        boolean exists = false;
                        for (Product p : previewProductList) {
                            if (p.getId().equals(product.getId())) {
                                exists = true; break;
                            }
                        }
                        if (!exists) {
                            previewProductList.add(product);
                            previewAdapter.setList(new ArrayList<>(previewProductList));
                        }
                    }
                }
            });
        }
    }

    private void openSelectProductActivity() {
        Intent intent = new Intent(getContext(), ManageActivity.class);
        intent.putExtra(ManageActivity.EXTRA_CONTENT_FRAGMENT, "productdiscount");
        intent.putStringArrayListExtra("current_selected_ids", selectedProductIds);
        selectProductLauncher.launch(intent);
    }

    private void updateSelectedCountUI() {
        int count = selectedProductIds != null ? selectedProductIds.size() : 0;
        tvSelectedCount.setText("Đã chọn: " + count + " sản phẩm");
    }

    private void loadDiscountData(String id) {
        // [SỬA] Xử lý ApiResponse<Discount>
        discountViewModel.getDiscountByID(id).observe(getViewLifecycleOwner(), apiResponse -> {
            if (apiResponse != null && apiResponse.isStatus()) {
                Discount discount = apiResponse.getData();
                if (discount != null) {
                    edtCode.setText(discount.getDiscountName());
                    edtPercent.setText(String.valueOf((int)discount.getDiscountRate()));
                    edtStartDate.setText(formatDateForDisplay(discount.getStartDate()));
                    edtEndDate.setText(formatDateForDisplay(discount.getEndDate()));

                    List<Product> applied = discount.getAppliedProducts();

                    if (applied != null && !applied.isEmpty()) {
                        rgScope.check(R.id.rbSpecificProducts);
                        selectedProductIds.clear();
                        previewProductList.clear();
                        for (Product p : applied) {
                            selectedProductIds.add(p.getId());
                            previewProductList.add(p);
                        }
                        updateSelectedCountUI();
                        previewAdapter.setList(previewProductList);
                        previewAdapter.setSelectedIds(selectedProductIds);
                    } else {
                        rgScope.check(R.id.rbAllProducts);
                        layoutSelectedProducts.setVisibility(View.GONE);
                    }
                }
            } else {
                String msg = (apiResponse != null) ? apiResponse.getMessage() : "Lỗi tải dữ liệu";
                FancyToast.makeText(getContext(), msg, FancyToast.LENGTH_SHORT, FancyToast.ERROR, true).show();
            }
        });
    }

    private void handleSave() {
        String name = edtCode.getText().toString().trim();
        String rateStr = edtPercent.getText().toString().trim();
        String start = edtStartDate.getText().toString().trim();
        String end = edtEndDate.getText().toString().trim();

        if (name.isEmpty() || rateStr.isEmpty() || start.isEmpty() || end.isEmpty()) {
            FancyToast.makeText(getContext(), "Vui lòng nhập đủ thông tin", FancyToast.LENGTH_SHORT, FancyToast.WARNING, true).show();
            return;
        }

        boolean isSpecific = (rgScope.getCheckedRadioButtonId() == R.id.rbSpecificProducts);

        if (isSpecific && (selectedProductIds == null || selectedProductIds.isEmpty())) {
            FancyToast.makeText(getContext(), "Vui lòng chọn ít nhất 1 sản phẩm", FancyToast.LENGTH_SHORT, FancyToast.WARNING, true).show();
            return;
        }

        double rate;
        try {
            rate = Double.parseDouble(rateStr);
            if (rate < 0 || rate > 100) { edtPercent.setError("0-100"); return; }
        } catch (NumberFormatException e) { edtPercent.setError("Lỗi số"); return; }

        Discount discount = new Discount(name, rate, start, end);

        if (isSpecific) {
            discount.setProductIds(selectedProductIds);
        } else {
            discount.setProductIds(new ArrayList<>());
        }

        // [SỬA] Xử lý ApiResponse<Discount> cho Add/Update
        if (discountId == null) {
            discountViewModel.addDiscount(discount).observe(getViewLifecycleOwner(), apiResponse -> {
                if (apiResponse != null && apiResponse.isStatus()) {
                    showSuccessDialog("Thêm mã giảm giá thành công!");
                } else {
                    String msg = (apiResponse != null) ? apiResponse.getMessage() : "Thêm thất bại";
                    FancyToast.makeText(getContext(), msg, FancyToast.LENGTH_SHORT, FancyToast.ERROR, true).show();
                }
            });
        } else {
            discountViewModel.updateDiscount(discountId, discount).observe(getViewLifecycleOwner(), apiResponse -> {
                if (apiResponse != null && apiResponse.isStatus()) {
                    showSuccessDialog("Cập nhật thành công!");
                } else {
                    String msg = (apiResponse != null) ? apiResponse.getMessage() : "Cập nhật thất bại";
                    FancyToast.makeText(getContext(), msg, FancyToast.LENGTH_SHORT, FancyToast.ERROR, true).show();
                }
            });
        }
    }

    private void setupDatePickers() {
        edtStartDate.setOnClickListener(v -> showDatePicker(startCalendar, edtStartDate));
        edtEndDate.setOnClickListener(v -> showDatePicker(endCalendar, edtEndDate));
    }

    private void showDatePicker(Calendar calendar, EditText editText) {
        new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            editText.setText(dateFormat.format(calendar.getTime()));
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private String formatDateForDisplay(String serverDate) {
        if (serverDate != null && serverDate.contains("T")) {
            return serverDate.split("T")[0];
        }
        return serverDate;
    }

    private void showSuccessDialog(String message) {
        new CuteDialog.withIcon(requireActivity())
                .setIcon(R.drawable.ic_dialog_success)
                .setTitle("Thành công")
                .setDescription(message)
                .setPrimaryColor(R.color.blue)
                .setPositiveButtonColor(R.color.blue)
                .setPositiveButtonText("Đóng", v -> finishActivity())
                .hideNegativeButton(true)
                .show();
    }

    private void finishActivity() {
        if (getActivity() != null) {
            getActivity().setResult(Activity.RESULT_OK);
            getActivity().finish();
        }
    }
}