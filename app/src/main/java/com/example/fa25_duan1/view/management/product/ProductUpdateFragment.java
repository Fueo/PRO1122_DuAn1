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
import android.util.Log;

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

        // 3. Load dữ liệu cho Spinners từ LiveData
        setupSpinners();

        // 4. Kiểm tra chế độ "Thêm mới" hay "Cập nhật"
        if (getActivity() != null && getActivity().getIntent() != null) {
            productId = getActivity().getIntent().getStringExtra("Id");
        }

        if (productId != null) {
            btnSave.setText("Cập nhật");
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

    /**
     * Chuyển đổi chuỗi giá có định dạng (vd: "150.000đ", "150000") thành giá trị số (vd: "150000").
     */
    private String parsePrice(String formattedPrice) {
        if (formattedPrice == null || formattedPrice.isEmpty()) return "0";

        // Loại bỏ tất cả ký tự không phải số
        String finalPrice = formattedPrice.replaceAll("[^\\d]", "");

        return finalPrice.isEmpty() ? "0" : finalPrice;
    }


    /**
     * Khởi tạo dữ liệu cho các Spinner từ ViewModel
     */
    private void setupSpinners() {
        // --- 1. SPINNER TRẠNG THÁI (STATUS) ---
        List<String> statuses = new ArrayList<>(Arrays.asList("Đang bán", "Ngừng kinh doanh"));
        spStatus.attachDataSource(statuses);

        // --- 2. SPINNER TÁC GIẢ (AUTHOR) ---
        authorViewModel.getDisplayedAuthors().observe(getViewLifecycleOwner(), authors -> {
            if (authors != null) {
                List<String> authorNames = new ArrayList<>();
                authorMap.clear();

                for (Author author : authors) {
                    authorNames.add(author.getName());
                    authorMap.put(author.getName(), author.getAuthorID());
                }
                spAuthor.attachDataSource(authorNames);
                if (currentProduct != null) {
                    selectSpinnerItem(spAuthor, currentProduct.getAuthor().getName(), authorNames);
                }
            }
        });

        // --- 3. SPINNER DANH MỤC (CATEGORY) ---
        categoryViewModel.getDisplayedCategories().observe(getViewLifecycleOwner(), categories -> {
            if (categories != null) {
                List<String> categoryNames = new ArrayList<>();
                categoryMap.clear();

                categoryNames.add(NO_CATEGORY_OPTION);

                for (Category category : categories) {
                    categoryNames.add(category.getName());
                    categoryMap.put(category.getName(), category.get_id());
                }
                spCategory.attachDataSource(categoryNames);
                if (currentProduct != null) {
                    String currentCategoryName = currentProduct.getCategory() != null ? currentProduct.getCategory().getName() : NO_CATEGORY_OPTION;
                    selectSpinnerItem(spCategory, currentCategoryName, categoryNames);
                }
            }
        });
    }

    /**
     * Hàm tiện ích để chọn mục trong NiceSpinner
     */
    private void selectSpinnerItem(NiceSpinner spinner, String valueToSelect, List<String> dataSource) {
        int index = dataSource.indexOf(valueToSelect);
        if (index >= 0) {
            spinner.setSelectedIndex(index);
        }
    }

    /**
     * Hiển thị DatePickerDialog và cập nhật etDate
     */
    private void showDatePickerDialog() {
        final Calendar c = Calendar.getInstance();

        try {
            Date currentDisplayDate = displayFormat.parse(etDate.getText().toString());
            if (currentDisplayDate != null) {
                c.setTime(currentDisplayDate);
            }
        } catch (ParseException e) {
            // Dùng ngày hiện tại
        }

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


    /**
     * Load chi tiết Sản phẩm (Sách)
     */
    private void loadProductDetail(String id) {
        productViewModel.getProductByID(id).observe(getViewLifecycleOwner(), product -> {
            if (product != null) {
                this.currentProduct = product;

                // Điền thông tin sách
                etName.setText(product.getName());
                etDescription.setText(product.getDescription());
                etPages.setText(String.valueOf(product.getPages() > 0 ? product.getPages() : ""));
                // XỬ LÝ DATE
                if (product.getPublishDate() != null && !product.getPublishDate().isEmpty()) {
                    try {
                        SimpleDateFormat sourceFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
                        Date beDate = sourceFormat.parse(product.getPublishDate());
                        if (beDate != null) {
                            etDate.setText(displayFormat.format(beDate));
                        }
                    } catch (ParseException e) {
                        etDate.setText(product.getPublishDate());
                        e.printStackTrace();
                    }
                }

                // HIỂN THỊ GIÁ BÁN ĐÃ ĐỊNH DẠNG (SAU KHI LOAD)
                if (product.getPrice() > 0) {
                    String formattedPrice = currencyFormatter.format(product.getPrice()).replace(",", ".");
                    etPrice.setText(formattedPrice + "đ");
                } else {
                    etPrice.setText("");
                }

                etQuantity.setText(String.valueOf(product.getQuantity() > 0 ? product.getQuantity() : ""));

                // Load ảnh bìa/avatar
                if (product.getImage() != null && !product.getImage().isEmpty()) {
                    Glide.with(requireContext())
                            .load(product.getImage())
                            .placeholder(R.drawable.book_cover_placeholder)
                            .error(R.drawable.book_cover_placeholder)
                            .into(ivImage);
                }

                // Chọn trạng thái đúng cho Spinner (từ boolean của model sang chuỗi hiển thị)
                if (product.isStatus()) {
                    selectSpinnerItem(spStatus, "Đang bán", Arrays.asList("Đang bán", "Ngừng kinh doanh"));
                } else {
                    selectSpinnerItem(spStatus, "Ngừng kinh doanh", Arrays.asList("Đang bán", "Ngừng kinh doanh"));
                }

                setupSpinners();
            } else {
                Toast.makeText(getContext(), "Không tìm thấy thông tin sách", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Chuyển đổi ngày tháng hiển thị (dd/MM/yyyy) sang định dạng BE (yyyy-MM-dd)
     */
    private String convertToBackendDate(String displayDate) {
        if (displayDate.isEmpty()) return "";
        try {
            Date dateObj = displayFormat.parse(displayDate);
            if (dateObj != null) {
                return backendFormat.format(dateObj);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * Lấy ID Tác giả/Danh mục đã chọn, xử lý trường hợp "Chưa có danh mục" là null.
     */
    private String getSelectedId(NiceSpinner spinner, Map<String, String> idMap) {
        String selectedName = (String) spinner.getSelectedItem();

        if (selectedName == null || selectedName.equals(NO_CATEGORY_OPTION)) {
            // Trả về null nếu người dùng chọn "Chưa có danh mục" hoặc không chọn gì
            return null;
        }

        // Trả về ID từ Map. Nếu không tìm thấy, trả về null.
        return idMap.getOrDefault(selectedName, null);
    }

    /**
     * Chuyển đổi trạng thái hiển thị sang boolean string ("true" hoặc "false").
     */
    private String convertStatusToBooleanString(String statusName) {
        if ("Đang bán".equals(statusName)) {
            return "true";
        } else {
            return "false";
        }
    }


    /**
     * Kiểm tra dữ liệu nhập vào (Validate)
     */
    private boolean validateInput() {
        String name = etName.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String pages = etPages.getText().toString().trim();
        String price = etPrice.getText().toString().trim();
        String quantity = etQuantity.getText().toString().trim();
        String authorId = getSelectedId(spAuthor, authorMap); // Kiểm tra ID tác giả

        if (name.isEmpty()) {
            etName.setError("Tên sách không được để trống");
            etName.requestFocus();
            return false;
        }

        if (description.isEmpty()) {
            etDescription.setError("Mô tả không được để trống");
            etDescription.requestFocus();
            return false;
        }

        if (pages.isEmpty() || !pages.matches("\\d+")) {
            etPages.setError("Số trang không hợp lệ");
            etPages.requestFocus();
            return false;
        }

        if (price.isEmpty() || parsePrice(price).equals("0")) {
            etPrice.setError("Giá bán không được để trống");
            etPrice.requestFocus();
            return false;
        }

        if (authorId == null || authorId.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng chọn Tác giả cho sản phẩm.", Toast.LENGTH_LONG).show();
            return false;
        }

        if (quantity.isEmpty() || !quantity.matches("\\d+")) {
            etQuantity.setError("Số lượng không hợp lệ");
            etQuantity.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * Xử lý thêm Sản phẩm (Sách) mới
     */
    private void addProduct() {
        if (!validateInput()) return;

        // Chuẩn bị dữ liệu
        String dateForBE = convertToBackendDate(etDate.getText().toString().trim());
        String priceForBE = parsePrice(etPrice.getText().toString().trim());
        String authorId = getSelectedId(spAuthor, authorMap);
        String categoryId = getSelectedId(spCategory, categoryMap);
        String statusForBE = convertStatusToBooleanString((String) spStatus.getSelectedItem());

        // Thu thập các trường dữ liệu và tạo RequestBody
        RequestBody nameBody = toRequestBody(etName.getText().toString().trim());
        RequestBody descBody = toRequestBody(etDescription.getText().toString().trim());
        RequestBody pagesBody = toRequestBody(etPages.getText().toString().trim());
        RequestBody dateBody = toRequestBody(dateForBE);
        RequestBody statusBody = toRequestBody(statusForBE);
        RequestBody categoryBody = toRequestBody(categoryId);
        RequestBody authorBody = toRequestBody(authorId);
        RequestBody priceBody = toRequestBody(priceForBE);
        RequestBody quantityBody = toRequestBody(etQuantity.getText().toString().trim());


        MultipartBody.Part imagePart = prepareFilePart("image", selectedAvatarUri);

        // GỌI HÀM VIEWMODEL VỚI THỨ TỰ ĐÃ ĐỒNG BỘ:
        // name, description, pages, publishDate, status, categoryID, authorID, price, quantity, image
        productViewModel.addProductWithImage(
                nameBody, descBody, pagesBody, dateBody, statusBody, categoryBody, authorBody, priceBody, quantityBody,
                imagePart
        ).observe(getViewLifecycleOwner(), product -> {
            if (product != null) {
                Toast.makeText(getContext(), "Thêm sách thành công", Toast.LENGTH_SHORT).show();
                requireActivity().setResult(Activity.RESULT_OK);
                requireActivity().finish();
            } else {
                Toast.makeText(getContext(), "Thêm thất bại. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Xử lý cập nhật Sản phẩm (Sách)
     */
    private void updateProduct() {
        if (currentProduct == null) return;
        if (!validateInput()) return;

        // Chuẩn bị dữ liệu
        String dateForBE = convertToBackendDate(etDate.getText().toString().trim());
        String priceForBE = parsePrice(etPrice.getText().toString().trim());
        String authorId = getSelectedId(spAuthor, authorMap);
        String categoryId = getSelectedId(spCategory, categoryMap);
        String statusForBE = convertStatusToBooleanString((String) spStatus.getSelectedItem());

        // Thu thập các trường dữ liệu và tạo RequestBody
        RequestBody nameBody = toRequestBody(etName.getText().toString().trim());
        RequestBody descBody = toRequestBody(etDescription.getText().toString().trim());
        RequestBody pagesBody = toRequestBody(etPages.getText().toString().trim());
        RequestBody dateBody = toRequestBody(dateForBE);
        RequestBody statusBody = toRequestBody(statusForBE);
        RequestBody categoryBody = toRequestBody(categoryId);
        RequestBody authorBody = toRequestBody(authorId);
        RequestBody priceBody = toRequestBody(priceForBE);
        RequestBody quantityBody = toRequestBody(etQuantity.getText().toString().trim());

        MultipartBody.Part imagePart = prepareFilePart("image", selectedAvatarUri);

        // GỌI HÀM VIEWMODEL VỚI THỨ TỰ ĐÃ ĐỒNG BỘ:
        // productId, name, description, pages, publishDate, status, categoryID, authorID, price, quantity, image
        productViewModel.updateProductWithImage(
                productId, nameBody, descBody, pagesBody, dateBody, statusBody, categoryBody, authorBody, priceBody, quantityBody,
                imagePart
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
        // Chuyển null thành chuỗi rỗng để tránh crash khi tạo RequestBody
        if (value == null) value = "";
        return RequestBody.create(MediaType.parse("text/plain"), value);
    }
}