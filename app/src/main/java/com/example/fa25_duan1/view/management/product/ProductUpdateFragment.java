package com.example.fa25_duan1.view.management.product;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.fa25_duan1.R;
import com.example.fa25_duan1.model.Author;
import com.example.fa25_duan1.model.Category;
import com.example.fa25_duan1.model.Product;
import com.example.fa25_duan1.utils.FileUtils;
import com.example.fa25_duan1.viewmodel.AuthorViewModel;
import com.example.fa25_duan1.viewmodel.CategoryViewModel;
import com.example.fa25_duan1.viewmodel.ProductViewModel;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class ProductUpdateFragment extends Fragment {

    // Views
    private ShapeableImageView ivImage;
    private ImageView btnChangeAvatar;
    private TextInputEditText etName, etDescription, etPages, etDate, etPrice, etQuantity;
    private AutoCompleteTextView acStatus, acCategory, acAuthor;
    private Button btnSave;

    // Logic variables
    private ProductViewModel productViewModel;
    private AuthorViewModel authorViewModel;
    private CategoryViewModel categoryViewModel;

    private Uri selectedAvatarUri = null;
    private String productId;
    private Product currentProduct;

    // Maps lưu trữ ID và Tên
    private Map<String, String> authorMap = new HashMap<>();
    private Map<String, String> categoryMap = new HashMap<>();
    private final String NO_CATEGORY_OPTION = "Chưa có danh mục";

    // Danh sách trạng thái
    private final String[] statusOptions = {"Đang kinh doanh", "Ngừng kinh doanh"};

    // Formatters
    private final SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
    private final SimpleDateFormat backendFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    // --- KHAI BÁO LAUNCHER ĐỂ CHỌN ẢNH (FIX LỖI PREVIEW) ---
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_productupdate, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Đăng ký Launcher chọn ảnh (Phải gọi trước khi người dùng click)
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        selectedAvatarUri = result.getData().getData();
                        // Dùng Glide để load ảnh preview cho mượt và tránh lỗi ContentProvider
                        Glide.with(this)
                                .load(selectedAvatarUri)
                                .centerCrop()
                                .into(ivImage);
                    }
                }
        );

        // 2. Khởi tạo ViewModel
        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);
        authorViewModel = new ViewModelProvider(this).get(AuthorViewModel.class);
        categoryViewModel = new ViewModelProvider(this).get(CategoryViewModel.class);

        initViews(view);
        setupStatusSpinner();
        setupDynamicSpinners();

        // 3. Kiểm tra Intent (Update hay Add)
        if (getActivity() != null && getActivity().getIntent() != null) {
            productId = getActivity().getIntent().getStringExtra("Id");
        }

        if (productId != null) {
            btnSave.setText("Cập nhật sản phẩm");
            loadProductDetail(productId);
        } else {
            btnSave.setText("Lưu sản phẩm");
        }

        // 4. Sự kiện Click
        btnChangeAvatar.setOnClickListener(v -> pickImageFromGallery());
        etDate.setOnClickListener(v -> showDatePickerDialog());

        btnSave.setOnClickListener(v -> {
            if (productId == null) addProduct();
            else updateProduct();
        });
    }

    private void initViews(View view) {
        ivImage = view.findViewById(R.id.ivImage);
        btnChangeAvatar = view.findViewById(R.id.btnChangeImage);

        etName = view.findViewById(R.id.etName);
        etDescription = view.findViewById(R.id.etDescription);
        etPages = view.findViewById(R.id.etPages);
        etDate = view.findViewById(R.id.etDate);
        etPrice = view.findViewById(R.id.etPrice);
        etQuantity = view.findViewById(R.id.etQuantity);

        acStatus = view.findViewById(R.id.acStatus);
        acCategory = view.findViewById(R.id.acCategory);
        acAuthor = view.findViewById(R.id.acAuthor);

        btnSave = view.findViewById(R.id.btnSave);
    }

    // --- XỬ LÝ ẢNH ---
    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        // Gọi launcher mới thay vì startActivityForResult
        imagePickerLauncher.launch(intent);
    }

    // --- SETUP SPINNERS ---
    private void setupStatusSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, statusOptions);
        acStatus.setAdapter(adapter);
        if (productId == null) {
            acStatus.setText(statusOptions[0], false);
        }
    }

    private void setupDynamicSpinners() {
        // --- AUTHOR ---
        authorViewModel.getDisplayedAuthors().observe(getViewLifecycleOwner(), authors -> {
            if (authors != null) {
                List<String> authorNames = new ArrayList<>();
                authorMap.clear();
                for (Author author : authors) {
                    authorNames.add(author.getName());
                    authorMap.put(author.getName(), author.getAuthorID());
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, authorNames);
                acAuthor.setAdapter(adapter);

                if (currentProduct != null && currentProduct.getAuthor() != null) {
                    acAuthor.setText(currentProduct.getAuthor().getName(), false);
                }
            }
        });

        // --- CATEGORY ---
        categoryViewModel.getDisplayedCategories().observe(getViewLifecycleOwner(), categories -> {
            if (categories != null) {
                List<String> categoryNames = new ArrayList<>();
                categoryMap.clear();
                categoryNames.add(NO_CATEGORY_OPTION);

                for (Category category : categories) {
                    categoryNames.add(category.getName());
                    categoryMap.put(category.getName(), category.get_id());
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, categoryNames);
                acCategory.setAdapter(adapter);

                if (currentProduct != null) {
                    String catName = currentProduct.getCategory() != null ? currentProduct.getCategory().getName() : NO_CATEGORY_OPTION;
                    acCategory.setText(catName, false);
                }
            }
        });
    }

    // --- LOAD DETAIL ---
    private void loadProductDetail(String id) {
        productViewModel.getProductByID(id).observe(getViewLifecycleOwner(), product -> {
            if (product != null) {
                this.currentProduct = product;

                etName.setText(product.getName());
                etDescription.setText(product.getDescription());
                etPages.setText(String.valueOf(product.getPages() > 0 ? product.getPages() : ""));
                etQuantity.setText(String.valueOf(product.getQuantity() > 0 ? product.getQuantity() : ""));

                if (product.getPublishDate() != null && !product.getPublishDate().isEmpty()) {
                    try {
                        SimpleDateFormat sourceFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
                        Date beDate = sourceFormat.parse(product.getPublishDate());
                        if (beDate != null) etDate.setText(displayFormat.format(beDate));
                    } catch (ParseException e) {
                        etDate.setText(product.getPublishDate());
                    }
                }

                if (product.getPrice() > 0) {
                    etPrice.setText(String.valueOf((long) product.getPrice()));
                }

                // Load ảnh từ server
                if (product.getImage() != null && !product.getImage().isEmpty()) {
                    Glide.with(requireContext()).load(product.getImage())
                            .placeholder(R.drawable.book_cover_placeholder)
                            .error(R.drawable.book_cover_placeholder)
                            .centerCrop()
                            .into(ivImage);
                }

                String statusText = product.isStatus() ? statusOptions[0] : statusOptions[1];
                acStatus.setText(statusText, false);

                setupDynamicSpinners();
            }
        });
    }

    // --- LOGIC ADD/UPDATE ---

    private void addProduct() {
        if (!validateInput()) return;

        String name = etName.getText().toString().trim();
        String desc = etDescription.getText().toString().trim();
        String pages = etPages.getText().toString().trim();
        String date = convertToBackendDate(etDate.getText().toString());
        String price = parsePrice(etPrice.getText().toString());
        String qty = etQuantity.getText().toString().trim();

        String authorId = getSelectedId(acAuthor, authorMap);
        String categoryId = getSelectedId(acCategory, categoryMap);
        String status = convertStatusToBooleanString(acStatus.getText().toString());

        productViewModel.addProductWithImage(
                toRequestBody(name), toRequestBody(desc), toRequestBody(pages),
                toRequestBody(date), toRequestBody(status), toRequestBody(categoryId),
                toRequestBody(authorId), toRequestBody(price), toRequestBody(qty),
                prepareFilePart("image", selectedAvatarUri)
        ).observe(getViewLifecycleOwner(), product -> {
            if (product != null) {
                Toast.makeText(getContext(), "Thêm thành công", Toast.LENGTH_SHORT).show();
                requireActivity().setResult(Activity.RESULT_OK);
                requireActivity().finish();
            } else {
                Toast.makeText(getContext(), "Thêm thất bại", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateProduct() {
        if (currentProduct == null || !validateInput()) return;

        String name = etName.getText().toString().trim();
        String desc = etDescription.getText().toString().trim();
        String pages = etPages.getText().toString().trim();
        String date = convertToBackendDate(etDate.getText().toString());
        String price = parsePrice(etPrice.getText().toString());
        String qty = etQuantity.getText().toString().trim();

        String authorId = getSelectedId(acAuthor, authorMap);
        String categoryId = getSelectedId(acCategory, categoryMap);
        String status = convertStatusToBooleanString(acStatus.getText().toString());

        productViewModel.updateProductWithImage(
                productId,
                toRequestBody(name), toRequestBody(desc), toRequestBody(pages),
                toRequestBody(date), toRequestBody(status), toRequestBody(categoryId),
                toRequestBody(authorId), toRequestBody(price), toRequestBody(qty),
                prepareFilePart("image", selectedAvatarUri)
        ).observe(getViewLifecycleOwner(), product -> {
            if (product != null) {
                Toast.makeText(getContext(), "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                requireActivity().setResult(Activity.RESULT_OK);
                requireActivity().finish();
            } else {
                Toast.makeText(getContext(), "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- HELPER FUNCTIONS ---

    private boolean validateInput() {
        if (isEmpty(etName)) { etName.setError("Nhập tên sách"); return false; }
        if (isEmpty(etPrice)) { etPrice.setError("Nhập giá bán"); return false; }
        if (isEmpty(etQuantity)) { etQuantity.setError("Nhập số lượng"); return false; }

        String authorId = getSelectedId(acAuthor, authorMap);
        if (authorId == null) {
            acAuthor.setError("Vui lòng chọn tác giả");
            Toast.makeText(getContext(), "Vui lòng chọn tác giả", Toast.LENGTH_SHORT).show();
            return false;
        }
        acAuthor.setError(null);
        return true;
    }

    private boolean isEmpty(TextInputEditText et) {
        return et.getText() == null || et.getText().toString().trim().isEmpty();
    }

    private String getSelectedId(AutoCompleteTextView ac, Map<String, String> map) {
        String text = ac.getText().toString();
        return map.getOrDefault(text, null);
    }

    private String convertStatusToBooleanString(String statusText) {
        return statusText.equals(statusOptions[0]) ? "true" : "false";
    }

    private String parsePrice(String rawPrice) {
        return rawPrice.replaceAll("[^\\d]", "");
    }

    private String convertToBackendDate(String displayDate) {
        try {
            Date date = displayFormat.parse(displayDate);
            return date != null ? backendFormat.format(date) : "";
        } catch (ParseException e) { return ""; }
    }

    private RequestBody toRequestBody(String value) {
        return value == null ? null : RequestBody.create(MediaType.parse("text/plain"), value);
    }

    private MultipartBody.Part prepareFilePart(String partName, Uri fileUri) {
        if (fileUri == null) return null;
        try {
            String filePath = FileUtils.getPath(getContext(), fileUri);
            if (filePath == null) return null;
            File file = new File(filePath);
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
            return MultipartBody.Part.createFormData(partName, file.getName(), requestFile);
        } catch (Exception e) { return null; }
    }

    private void showDatePickerDialog() {
        final Calendar c = Calendar.getInstance();
        try {
            if (!etDate.getText().toString().isEmpty()) {
                Date date = displayFormat.parse(etDate.getText().toString());
                if (date != null) c.setTime(date);
            }
        } catch (ParseException e) {}

        new DatePickerDialog(requireContext(), (view, y, m, d) -> {
            String strDate = String.format(Locale.US, "%02d/%02d/%d", d, m + 1, y);
            etDate.setText(strDate);
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (authorViewModel != null) authorViewModel.refreshData();
        if (categoryViewModel != null) categoryViewModel.refreshData();
    }
}