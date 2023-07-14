package com.project.kioasktab

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.JsonObject
import com.project.kioask.model.ItemModel
import com.project.kioask.model.OrderLIst
import com.project.kioask.retrofit.NetworkService
import com.project.kioasktab.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val passkey = getString(R.string.backend_secretKey)

        val tokenRetrofit = Retrofit.Builder()
            .baseUrl("https://api.format.kro.kr")
            .addConverterFactory(GsonConverterFactory.create())
            .build();

        val jsonObject = JsonObject()
        jsonObject.addProperty("passkey", passkey)

        val tokenService = tokenRetrofit.create(NetworkService::class.java)
        val tokenCall: Call<JsonObject> = tokenService.doPostToken(jsonObject)

        val orderRetrofit = Retrofit.Builder()
            .baseUrl("https://api.format.kro.kr")
            .addConverterFactory(GsonConverterFactory.create())
            .build();

        val orderService = orderRetrofit.create(NetworkService::class.java)

        tokenCall?.enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                if (response.isSuccessful) {
                    val token = response.body()?.get("token")?.asString

                    val tokenSaved = getSharedPreferences("token", MODE_PRIVATE)
                    val tokenEdit: SharedPreferences.Editor = tokenSaved.edit()
                    tokenEdit.putString("token", token)
                    tokenEdit.commit()

                    val orderCall: Call<List<ItemModel>> = orderService.doGetList("Bearer $token")
                    orderCall?.enqueue(object : Callback<List<ItemModel>> {
                        override fun onResponse(
                            call: Call<List<ItemModel>>,
                            response: Response<List<ItemModel>>
                        ) {
                            if (response.isSuccessful) {
                                val list = response?.body()
                                for (i in 0 until list!!.size) {

                                    val itemList = ItemModel(
                                        list.get(i).code,
                                        list.get(i).name,
                                        list.get(i).image,
                                        list.get(i).content,
                                        list.get(i).price
                                    )
                                    var existingItem = OrderLIst.order.firstOrNull {
                                        it.name == itemList.name
                                    }
                                    if (existingItem == null) {
                                        OrderLIst.order.add(itemList)
                                    } else {
                                        OrderLIst.order.clear()
                                        OrderLIst.order.add(itemList)
                                        Log.w("zio", "이미 존재하는 값")
                                    }
                                }
                            } else {
                                Log.w("zio", "API호출 실패 : ${response.code()}")
                            }
                        }

                        override fun onFailure(call: Call<List<ItemModel>>, t: Throwable) {
                            Log.e("zio", "에러!@@@@@@@@@@@: ${t.message}")
                        }
                    })

                } else {
                    Log.w("zio", "인증실패 : ${response.code()}")
                }
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Log.w("zio", "접속실패 : ${t.message}")
            }

        })

        //메인 화면 클릭시 주문 화면으로 이동
        binding.main.setOnClickListener {
            val intent = Intent(this, OrderActivity::class.java)
            startActivity(intent)
        }
    }
}

