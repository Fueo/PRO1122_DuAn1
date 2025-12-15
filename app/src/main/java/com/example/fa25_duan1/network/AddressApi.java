package com.example.fa25_duan1.network;

import com.example.fa25_duan1.model.Address;
import com.example.fa25_duan1.model.ApiResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface AddressApi {

    // GET: Lấy danh sách địa chỉ của User đang login
    @GET("addresses/")
    Call<ApiResponse<List<Address>>> getMyAddresses();

    // GET: (Admin) Lấy tất cả địa chỉ hệ thống
    @GET("addresses/all-system")
    Call<ApiResponse<List<Address>>> getAllAddressesSystem();

    // POST: Thêm địa chỉ mới (Gửi JSON body)
    @POST("addresses/add")
    Call<ApiResponse<Address>> addAddress(@Body Address address);

    // PUT: Cập nhật địa chỉ
    @PUT("addresses/update/{id}")
    Call<ApiResponse<Address>> updateAddress(@Path("id") String id, @Body Address address);

    // DELETE: Xóa địa chỉ
    @DELETE("addresses/delete/{id}")
    Call<ApiResponse<Void>> deleteAddress(@Path("id") String id);
}