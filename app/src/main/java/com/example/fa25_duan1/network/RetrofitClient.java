package com.example.fa25_duan1.network;

import android.content.Context;

import com.example.fa25_duan1.BuildConfig;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static RetrofitClient instance = null;
    private final AuthApi authApi;
    private final UserApi userApi;
    private final AuthorApi authorApi;
    private final CategoryApi categoryApi;
    private final ProductApi productApi;
    private static final String BASE_URL = BuildConfig.BASE_URL_ATHOME;

    private RetrofitClient(Context context) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor(context)) // thêm token interceptor
                .authenticator(new TokenAuthenticator(context))
                .addInterceptor(logging)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        authApi = retrofit.create(AuthApi.class);
        userApi = retrofit.create(UserApi.class);
        authorApi = retrofit.create(AuthorApi.class);
        categoryApi = retrofit.create(CategoryApi.class);
        productApi = retrofit.create(ProductApi.class);
    }

    public static synchronized RetrofitClient getInstance(Context context) {
        if (instance == null) {
            instance = new RetrofitClient(context);
        }
        return instance;
    }

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
}