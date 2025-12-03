package com.example.fa25_duan1.network;

import android.content.Context;

import com.example.fa25_duan1.BuildConfig;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static RetrofitClient instance = null;

    // Các API Interface
    private final AuthApi authApi;
    private final UserApi userApi;
    private final AuthorApi authorApi;
    private final CategoryApi categoryApi;
    private final ProductApi productApi;
    private final CartApi cartApi;       // Khai báo biến
    private final FavoriteApi favoriteApi;
    private final DiscountApi discountApi;
    private final OrderApi orderApi;
    private final AddressApi addressApi;
    private final StatisticApi statisticApi;

    private static final String BASE_URL = BuildConfig.BASE_URL_ATHOME;

    // --- SỬA LẠI CONSTRUCTOR: Bỏ tham số CartApi thừa ---
    private RetrofitClient(Context context) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor(context)) // Thêm token vào header
                .authenticator(new TokenAuthenticator(context)) // Xử lý refresh token
                .addInterceptor(logging)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // Khởi tạo các API
        authApi = retrofit.create(AuthApi.class);
        userApi = retrofit.create(UserApi.class);
        authorApi = retrofit.create(AuthorApi.class);
        categoryApi = retrofit.create(CategoryApi.class);
        productApi = retrofit.create(ProductApi.class);
        favoriteApi = retrofit.create(FavoriteApi.class);
        discountApi = retrofit.create(DiscountApi.class);
        orderApi = retrofit.create(OrderApi.class);
        cartApi = retrofit.create(CartApi.class);
        addressApi = retrofit.create(AddressApi.class);
        statisticApi = retrofit.create(StatisticApi.class);
    }

    public static synchronized RetrofitClient getInstance(Context context) {
        if (instance == null) {
            instance = new RetrofitClient(context);
        }
        return instance;
    }

    // Getters
    public AuthApi getAuthApi() {
        return authApi;
    }

    public UserApi getUserApi() {
        return userApi;
    }

    public AuthorApi getAuthorApi() {
        return authorApi;
    }

    public CategoryApi getCategoryApi() {
        return categoryApi;
    }

    public ProductApi getProductApi() {
        return productApi;
    }

    public FavoriteApi getFavoriteApi() {
        return favoriteApi;
    }

    public CartApi getCartApi() {
        return cartApi;
    }

    public DiscountApi getDiscountApi() {return discountApi;}

    public OrderApi getOrderApi() {return orderApi;}

    public AddressApi getAddressApi() {return addressApi;}

    public StatisticApi getStatisticApi() {return statisticApi;}
}