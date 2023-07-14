package com.project.kioask.retrofit

import com.google.gson.JsonObject
import com.project.kioask.model.ItemModel
import com.project.kioask.model.orderModel
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface NetworkService {


    @GET("/item/soldgetlist")
    fun doGetList(@Header("Authorization") token: String?): Call<List<ItemModel>>

    @POST("/order/additem")
    fun doPostList(
        @Header("Authorization") token: String?,
        @Body dto: orderModel?
    ): Call<orderModel>

    @Headers("Content-Type:application/json")
    @POST("/user/signpass")
    fun doPostToken(@Body passkey: JsonObject): Call<JsonObject>
}
