package com.project.tosspayment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.JsonObject
import com.project.kioask.retrofit.NetworkService
import com.project.tosspayment.databinding.ActivityMainBinding
import com.project.tosspayment.databinding.CustomDialogBinding
import com.tosspayments.paymentsdk.PaymentWidget
import com.tosspayments.paymentsdk.model.TossPaymentResult
import com.tosspayments.paymentsdk.view.Agreement
import com.tosspayments.paymentsdk.view.PaymentMethod
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class PayActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    lateinit var customDialogBinding: CustomDialogBinding
    private lateinit var paymentWidget: PaymentWidget

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

            val orderid  = intent.getStringExtra("orderId")
            val totalprice = intent.getIntExtra("price",0)
            val ordername : String? = intent.getStringExtra("orderName")
            val customerKey: String? = intent.getStringExtra("customerKey")

            //위젯 생성하기 위해 생성
            val methodWidget = binding.paymentWidget
            val agreement = binding.agreementWidget

            paymentWidget = PaymentWidget(this,getString(R.string.payment_ClientKey), customerKey.toString())

            //가격과 주문번호
            paymentWidget.renderPaymentMethods(
                method = methodWidget,
                amount = totalprice
            )

            //이용약관 위젯
            paymentWidget.renderAgreement(agreement)
            //결제위젯 view
            PaymentMethod(this, null)
            //이용약관 view
            Agreement(this, null)

            binding.payAccess.setOnClickListener {

                    if (orderid != null && ordername != null) {
                        customDialogBinding =
                            CustomDialogBinding.inflate(LayoutInflater.from(this@PayActivity))

                        paymentWidget.requestPayment(
                            paymentInfo = PaymentMethod.PaymentInfo(
                                orderId = orderid.toString(),
                                orderName = ordername.toString()

                            ),
                            paymentCallback = object : 
                                com.tosspayments.paymentsdk.model.PaymentCallback {

                                override fun onPaymentSuccess(success: TossPaymentResult.Success) {

                                Thread{
                                        val client = OkHttpClient()
                                        val mediaType = MediaType.parse("application/json")
                                        val qrBody = RequestBody.create(
                                            mediaType,
                                            "{\"paymentKey\":\"${success.paymentKey}\",\"amount\":$totalprice,\"orderId\":\"$orderid\",\"customerName\":\"$customerKey\",\"orderName\":\"$ordername\"}"
                                        )

                                        val request = Request.Builder()
                                            .url("https://api.tosspayments.com/v1/payments/confirm")
                                            .header(
                                                "Authorization",
                                                "Basic dGVzdF9za19aT1J6ZE1hcU4zd015a1p5bDdOcjVBa1lYUUd3Og=="
                                            )
                                            .header("Content-Type", "application/json")
                                            .post(qrBody)
                                            .build()

                                        val response = client.newCall(request).execute()

                                        val status = response
                                    try {
                                        if(status.isSuccessful){
                                            customDialogBinding =
                                                CustomDialogBinding.inflate(LayoutInflater.from(this@PayActivity))

                                            val paymentkey = success.paymentKey
                                            val orderstate = 1

                                            val tokenSaved = getSharedPreferences("token", MODE_PRIVATE)
                                            val token = tokenSaved.getString("token",null)

                                            val payRetrofit = Retrofit.Builder()
                                                .baseUrl("https://api.format.kro.kr")
                                                .addConverterFactory(GsonConverterFactory.create())
                                                .build();

                                            val dto = JsonObject()
                                            dto.addProperty("orderid",orderid)
                                            dto.addProperty("ordername",ordername)
                                            dto.addProperty("totalprice",totalprice)
                                            dto.addProperty("paymentkey",paymentkey)
                                            dto.addProperty("orderstate",orderstate)

                                            val payService = payRetrofit.create(NetworkService::class.java)
                                            val postCall = payService.doPostList(token ,dto)

                                            postCall.enqueue(object : retrofit2.Callback<JsonObject> {
                                                    override fun onResponse(
                                                        call: retrofit2.Call<JsonObject>,
                                                        response: retrofit2.Response<JsonObject>
                                                    ) {
                                                        if(response.isSuccessful){
                                                            Log.w("zio","보내기 성공!!")

                                                        }else{
                                                            Log.w("zio","API 접속 실패 : ${response.code()}")
                                                        }
                                                    }

                                                    override fun onFailure(
                                                        call: retrofit2.Call<JsonObject>,t: Throwable) {
                                                        Log.w("zio", "전송실패: ${t.message}")
                                                    }
                                                })

                                                runOnUiThread {
                                                    customDialogBinding =
                                                        CustomDialogBinding.inflate(LayoutInflater.from(this@PayActivity))
                                                dialog("결제가 완료 되었습니다.", "결제요청 결과",
                                                    customDialogBinding.dialogButton.setOnClickListener {
                                                        finishAffinity()
                                                    })
                                                }
                                        }else {

                                                Log.w("zio","결제인증 재 시작: $response, orderId: $orderid")

                                        }
                                    } catch (e:Exception){
                                        e.printStackTrace()
                                        Log.w("zio","무엇이 error 인가!!!! : ${e.message}")
                                    }
                                }.start()

                                }

                                override fun onPaymentFailed(fail: TossPaymentResult.Fail) {
                                    Log.d("zio", "결제 실패 ! : $fail")

                                    runOnUiThread {
                                    customDialogBinding =
                                        CustomDialogBinding.inflate(LayoutInflater.from(this@PayActivity))
                                    dialog(
                                        "결제를 실패 하였습니다.",
                                        "결제요청 결과",
                                        customDialogBinding.dialogButton.setOnClickListener {
                                            val failIntent =
                                                Intent(this@PayActivity, MainActivity::class.java)
                                            startActivity(failIntent)
                                        })
                                    }
                                }
                            }
                        )

                    }else{
                        Toast.makeText(this, "주문을 확인해 주세요", Toast.LENGTH_LONG).show()
                    }
            }
    }

    private fun dialog(message:String, title:String, OnClickListener: Unit) {

            val alertDialog = AlertDialog.Builder(this)
                .setView(customDialogBinding.customDialog)
                .create()

            customDialogBinding.dialogTitle.text = title
            customDialogBinding.dialogMessage.text = message
            customDialogBinding.dialogButton.text = "확인"
            customDialogBinding.dialogMessage.visibility = View.VISIBLE
            customDialogBinding.dialogButton.visibility = View.VISIBLE

            alertDialog.show()
    }
}
