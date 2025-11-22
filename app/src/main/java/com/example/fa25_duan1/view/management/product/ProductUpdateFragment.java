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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

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

import org.angmarch.views.NiceSpinner;

import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class ProductUpdateFragment extends Fragment {

    // Khai báo Views
    private CircleImageView ivImage;
    private ImageView btnChangeAvatar;
    private EditText etName, etDescription, etPages, etDate, etPrice, etQuantity;
    private NiceSpinner spAuthor, spCategory, spStatus;
    private Button btnSave;

    // Logic variables
    private ProductViewModel productViewModel;
    private AuthorViewModel authorViewModel;
    private CategoryViewModel categoryViewModel;

    private Uri selectedAvatarUri = null;
    private String productId;
    private Product currentProduct;

    // Maps để lưu trữ ID và Tên cho Spinner
    private Map<String, String> authorMap = new HashMap<>();
    private Map<String, String> categoryMap = new HashMap<>();
    private final String NO_CATEGORY_OPTION = "Chưa có danh mục";

    // --- SỬA 1: Khai báo danh sách Status cố định ---
    private final List<String> statusList = new LinkedList<>(Arrays.asList("Đang bán", "Ngừng kinh doanh"));

    private static final int PICK_IMAGE_REQUEST = 1005;

    // Định dạng ngày tháng
    private final SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
    private final SimpleDateFormat backendFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    // Định dạng tiền tệ
    private final NumberFormat currencyFormatter = new DecimalFormat("#,###");

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_productupdate, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Khởi tạo ViewModels
        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);
        authorViewModel = new ViewModelProvider(this).get(AuthorViewModel.class);
        categoryViewModel = new ViewModelProvider(this).get(CategoryViewModel.class);

        // 2. Ánh xạ View
        ivImage = view.findViewById(R.id.ivImage);
        btnChangeAvatar = view.findViewById(R.id.btnChangeAvatar);
        etName = view.findViewById(R.id.etName);
        etDescription = view.findViewById(R.id.etDescription);
        etPages = view.findViewById(R.id.etPages);
        etDate = view.findViewById(R.id.etDate);
        etPrice = view.findViewById(R.id.etPrice);
        etQuantity = view.findViewById(R.id.etQuantity);

        spAuthor = view.findViewById(R.id.spAuthor);
        spCategory = view.findViewById(R.id.spCategory);
        spStatus = view.findViewById(R.id.spStatus);

        btnSave = view.findViewById(R.id.btnSave);

        etDate.setFocusable(false);
        etDate.setClickable(true);

        // --- SỬA 2: Khởi tạo Spinner Status 1 lần duy nhất tại đây ---
        spStatus.attachDataSource(statusList);

        // 3. Load dữ liệu cho các Spinner khác (Author, Category)
        setupSpinners();

        // 4. Kiểm tra chế độ "Thêm mới" hay "Cập nhật"
        if (getActivity() != null && getActivity().getIntent() != null) {
            productId = getActivity().getIntent().getStringExtra("Id");
        }

        if (productId != null) {
            btnSave.setText("Cập nhật");
            // Load chi tiết sản phẩm để hiển thị lên form
            loadProductDetail(productId);
        } else {
            btnSave.setText("Thêm mới");
        }

        // 5. Sự kiện Click
        btnChangeAvatar.setOnClickListener(v -> pickImageFromGallery());
        etDate.setOnClickListener(v -> showDatePickerDialog());

        btnSave.setOnClickListener(v -> {
            if (productId == null) {
                addProduct();
            } else {
                updateProduct();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (authorViewModel != null) authorViewModel.refreshData();
        if (categoryViewModel != null) categoryViewModel.refreshData();
    }

    private String parsePrice(String formattedPrice) {
        if (formattedPrice == null || formattedPrice.isEmpty()) return "0";
        String finalPrice = formattedPrice.replaceAll("[^\\d]", "");
        return finalPrice.isEmpty() ? "0" : finalPrice;
    }

    /**
     * Khởi tạo dữ liệu cho các Spinner Author và Category
     * (Đã xóa phần Status ở đây để tránh reset)
     */
    private void setupSpinners() {
        // --- SỬA 3: Xóa phần khởi tạo Status Spinner ở đây ---

        // --- AUTHOR SPINNER ---
        authorViewModel.getDisplayedAuthors().observe(getViewLifecycleOwner(), authors -> {
            if (authors != null && !authors.isEmpty()) {
                List<String> authorNames = new ArrayList<>();
                authorMap.clear();

                for (Author author : authors) {
                    authorNames.add(author.getName());
                    authorMap.put(author.getName(), author.getAuthorID());
                }

                if (authors.size() == 1) {
                    authorNames.add("");
                    spAuthor.attachDataSource(authorNames);
                    spAuthor.setSelectedIndex(0);
                    spAuthor.setEnabled(false);
                } else {
                    spAuthor.attachDataSource(authorNames);
                    spAuthor.setEnabled(true);
                }

                // Logic chọn lại Author khi đang update
                if (productId != null && currentProduct != null && currentProduct.getAuthor() != null) {
                    selectSpinnerItem(spAuthor, currentProduct.getAuthor().getName(), authorNames);
                }
            }
        });

        // --- CATEGORY SPINNER ---
        categoryViewModel.getDisplayedCategories().observe(getViewLifecycleOwner(), categories -> {
            if (categories != null) {
                List<String> categoryNames = new ArrayList<>();
                categoryMap.clear();

                // Luôn thêm tùy chọn "Chưa có danh mục" đầu tiên
                categoryNames.add(NO_CATEGORY_OPTION);

                for (Category category : categories) {
                    categoryNames.add(category.getName());
                    categoryMap.put(category.getName(), category.get_id());
                }
                spCategory.attachDataSource(categoryNames);

                // Logic chọn lại Category khi đang update
                if (currentProduct != null) {
                    String currentCategoryName = currentProduct.getCategory() != null ? currentProduct.getCategory().getName() : NO_CATEGORY_OPTION;
                    selectSpinnerItem(spCategory, currentCategoryName, categoryNames);
                }
            }
        });
    }

    private void selectSpinnerItem(NiceSpinner spinner, String valueToSelect, List<String> dataSource) {
        int index = dataSource.indexOf(valueToSelect);
        if (index >= 0) {
            spinner.setSelectedIndex(index);
        }
    }

    private void showDatePickerDialog() {
        final Calendar c = Calendar.getInstance();
        try {
            Date currentDisplayDate = displayFormat.parse(etDate.getText().toString());
            if (currentDisplayDate != null) c.setTime(currentDisplayDate);
        } catch (ParseException e) { }

        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String date = String.format(Locale.getDefault(), "%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear);
                    etDate.setText(date);
                },
                year, month, day);
        datePickerDialog.show();
    }

    private void loadProductDetail(String id) {
        productViewModel.getProductByID(id).observe(getViewLifecycleOwner(), product -> {
            if (product != null) {
                this.currentProduct = product;
                etName.setText(product.getName());
                etDescription.setText(product.getDescription());
                etPages.setText(String.valueOf(product.getPages() > 0 ? product.getPages() : ""));

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
                    String formattedPrice = currencyFormatter.format(product.getPrice()).replace(",", ".");
                    etPrice.setText(formattedPrice + "đ");
                } else { etPrice.setText(""); }

                etQuantity.setText(String.valueOf(product.getQuantity() > 0 ? product.getQuantity() : ""));

                if (product.getImage() != null && !product.getImage().isEmpty()) {
                    Glide.with(requireContext()).load(product.getImage())
                            .placeholder(R.drawable.book_cover_placeholder).error(R.drawable.book_cover_placeholder).into(ivImage);
                }

                // --- SỬA 4: Logic chọn Status đơn giản và chính xác ---
                // "Đang bán" (index 0) tương ứng true
                // "Ngừng kinh doanh" (index 1) tương ứng false
                if (product.isStatus()) {
                    spStatus.setSelectedIndex(0); // Đang bán
                } else {
                    spStatus.setSelectedIndex(1); // Ngừng kinh doanh
                }

                // Gọi setupSpinners để cập nhật lựa chọn cho Author và Category
                // Vì setupSpinners không còn chứa logic reset status nên status sẽ được giữ nguyên
                setupSpinners();

            } else {
                Toast.makeText(getContext(), "Không tìm thấy thông tin sách", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String convertToBackendDate(String displayDate) {
        if (displayDate.isEmpty()) return "";
        try {
            Date dateObj = displayFormat.parse(displayDate);
            if (dateObj != null) return backendFormat.format(dateObj);
        } catch (ParseException e) { e.printStackTrace(); }
        return "";
    }

    private String getSelectedId(NiceSpinner spinner, Map<String, String> idMap) {
        String selectedName = (String) spinner.getSelectedItem();
        if (selectedName == null || selectedName.equals(NO_CATEGORY_OPTION) || selectedName.equals("")) {
            return null;
        }
        return idMap.getOrDefault(selectedName, null);
    }

    private String convertStatusToBooleanString(String statusName) {
        return "Đang bán".equals(statusName) ? "true" : "false";
    }

    private boolean validateInput() {
        String name = etName.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String pages = etPages.getText().toString().trim();
        String price = etPrice.getText().toString().trim();
        String quantity = etQuantity.getText().toString().trim();
        String authorId = getSelectedId(spAuthor, authorMap);

        if (name.isEmpty()) { etName.setError("Tên sách không được để trống"); etName.requestFocus(); return false; }
        if (description.isEmpty()) { etDescription.setError("Mô tả không được để trống"); etDescription.requestFocus(); return false; }
        if (pages.isEmpty() || !pages.matches("\\d+")) { etPages.setError("Số trang không hợp lệ"); etPages.requestFocus(); return false; }
        if (price.isEmpty() || parsePrice(price).equals("0")) { etPrice.setError("Giá bán không được để trống"); etPrice.requestFocus(); return false; }

        if (authorId == null || authorId.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng chọn Tác giả cho sản phẩm.", Toast.LENGTH_LONG).show();
            return false;
        }
        if (quantity.isEmpty() || !quantity.matches("\\d+")) { etQuantity.setError("Số lượng không hợp lệ"); etQuantity.requestFocus(); return false; }

        return true;
    }

    private void addProduct() {
        if (!validateInput()) return;

        String dateForBE = convertToBackendDate(etDate.getText().toString().trim());
        String priceForBE = parsePrice(etPrice.getText().toString().trim());
        String authorId = getSelectedId(spAuthor, authorMap);
        String categoryId = getSelectedId(spCategory, categoryMap);
        String statusForBE = convertStatusToBooleanString((String) spStatus.getSelectedItem());

        productViewModel.addProductWithImage(
                toRequestBody(etName.getText().toString().trim()),
                toRequestBody(etDescription.getText().toString().trim()),
                toRequestBody(etPages.getText().toString().trim()),
                toRequestBody(dateForBE),
                toRequestBody(statusForBE),
                toRequestBody(categoryId),
                toRequestBody(authorId),
                toRequestBody(priceForBE),
                toRequestBody(etQuantity.getText().toString().trim()),
                prepareFilePart("image", selectedAvatarUri)
        ).observe(getViewLifecycleOwner(), product -> {
            if (product != null) {
                Toast.makeText(getContext(), "Thêm sách thành công", Toast.LENGTH_SHORT).show();
                requireActivity().setResult(Activity.RESULT_OK);
                requireActivity().finish();
            } else {
                Toast.makeText(getContext(), "Thêm thất bại.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateProduct() {
        if (currentProduct == null) return;
        if (!validateInput()) return;

        String dateForBE = convertToBackendDate(etDate.getText().toString().trim());
        String priceForBE = parsePrice(etPrice.getText().toString().trim());
        String authorId = getSelectedId(spAuthor, authorMap);
        String categoryId = getSelectedId(spCategory, categoryMap);
        String statusForBE = convertStatusToBooleanString((String) spStatus.getSelectedItem());

        productViewModel.updateProductWithImage(
                productId,
                toRequestBody(etName.getText().toString().trim()),
                toRequestBody(etDescription.getText().toString().trim()),
                toRequestBody(etPages.getText().toString().trim()),
                toRequestBody(dateForBE),
                toRequestBody(statusForBE),
                toRequestBody(categoryId),
                toRequestBody(authorId),
                toRequestBody(priceForBE),
                toRequestBody(etQuantity.getText().toString().trim()),
                prepareFilePart("image", selectedAvatarUri)
        ).observe(getViewLifecycleOwner(), product -> {
            if (product != null) {
                Toast.makeText(getContext(), "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                requireActivity().setResult(Activity.RESULT_OK);
                requireActivity().finish();
            } else {
                Toast.makeText(getContext(), "Cập nhật thất bại.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedAvatarUri = data.getData();
            ivImage.setImageURI(selectedAvatarUri);
        }
    }

    private MultipartBody.Part prepareFilePart(String partName, Uri fileUri) {
        if (fileUri == null) return null;
        try {
            String filePath = FileUtils.getPath(getContext(), fileUri);
            if (filePath == null) return null;
            File file = new File(filePath);
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
            return MultipartBody.Part.createFormData(partName, file.getName(), requestFile);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private RequestBody toRequestBody(String value) {
        if (value == null) {
            return null;
        }
        return RequestBody.create(MediaType.parse("text/plain"), value);
    }
}