package com.project.kioasktab

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.project.kioasktab.Callback.PaymentCallback
import com.project.kioasktab.databinding.ActivityPaymentBinding
import com.project.kioasktab.databinding.CustomDialogBinding
import com.tosspayments.paymentsdk.PaymentWidget
import com.tosspayments.paymentsdk.model.TossPaymentResult
import com.tosspayments.paymentsdk.view.Agreement
import com.tosspayments.paymentsdk.view.PaymentMethod
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException


class PaymentActivity : AppCompatActivity() {
    lateinit var binding: ActivityPaymentBinding
    private lateinit var paymentWidget: PaymentWidget
    lateinit var customDialogBinding: CustomDialogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val orderId: String = intent.getStringExtra("orderId")!!
        val price = intent.getIntExtra("totalPrice", 0)
        val customerKey: String = intent.getStringExtra("customerKey")!!
        val orderItem: ArrayList<OrderData>? =
            intent.getParcelableArrayListExtra<OrderData>("orderItem")

        Log.d("zio", "orderId 확인: $orderId")
        Log.d("zio", "price 확인: $price")
        Log.d("zio", "customerKey 확인: $customerKey")
        Log.d("zio", "orderItem 확인: $orderItem")


        //위젯 생성하기 위해 생성
        val methodWidget = binding.paymentWidget
        val agreement = binding.agreementWidget

        if (orderId == null || price == 0) {
            val intent = Intent(this, OrderActivity::class.java)
            startActivity(intent)
        }

        //위젯에 값전달
        paymentWidget = PaymentWidget(
            activity = this,
            clientKey = getString(R.string.payment_ClientKey),
            customerKey = customerKey //PaymentWidget.ANONYMOUS
        )

        //가격과 주문번호
        paymentWidget.renderPaymentMethods(
            method = methodWidget,
            amount = price
        )

        //이용약관 위젯
        paymentWidget.renderAgreement(agreement)
        //결제위젯 view
        PaymentMethod(this, null)
        //이용약관 view
        Agreement(this, null)

        binding.payAccess.setOnClickListener {
            customDialogBinding = CustomDialogBinding.inflate(layoutInflater)
            paymentWidget.requestPayment(
                paymentInfo = PaymentMethod.PaymentInfo(
                    orderId = orderId,
                    orderName = orderItem.toString()
                ),
                paymentCallback = object : PaymentCallback,
                    com.tosspayments.paymentsdk.model.PaymentCallback {

                    override fun onPaymentSuccess(success: TossPaymentResult.Success) {

                        val client = OkHttpClient()

                        val mediaType = "application/json".toMediaTypeOrNull()
                        val requestBody =
                            "{\"paymentKey\":\"${success.paymentKey}\",\"amount\":$price,\"orderId\":\"$orderId\",\"customerName\":\"테스트고객\",\"orderItem\":\"테스트상품\"}".toRequestBody(
                                mediaType
                            )
                        val qrData =
                            "{\"paymentKey\":\"${success.paymentKey}\",\"amount\":$price,\"orderId\":\"$orderId\",\"customerName\":\"테스트고객\",\"orderItem\":\"테스트상품\"}".toRequestBody(
                                mediaType
                            )
                        Log.w("zio", "paymentKey: ${success.paymentKey}")

                        val request = Request.Builder()
                            .url("https://api.tosspayments.com/v1/payments/confirm")
                            .header(
                                "Authorization",
                                "Basic dGVzdF9za19aT1J6ZE1hcU4zd015a1p5bDdOcjVBa1lYUUd3Og=="
                            )
                            .header("Content-Type", "application/json")
                            .post(requestBody)
                            .build()

                        client.newCall(request).enqueue(object : Callback {

                            override fun onResponse(call: Call, response: Response) {
                                runOnUiThread {
                                    if (response.isSuccessful) {
                                        Log.w("zio", "결제인증 성공")
                                        dialog(
                                            "결제가 되었습니다.",
                                            "결제요청 결과",
                                            customDialogBinding.dialogButton.setOnClickListener {
                                                val successIntent =
                                                    Intent(
                                                        this@PaymentActivity,
                                                        ReceiptActivity::class.java
                                                    )
                                                startActivity(successIntent)
                                            })
                                    } else {
                                        Log.w("zio", "결제인증 재 시작: $response, orderId: $orderId")
                                    }
                                }
                            }

                            override fun onFailure(call: Call, e: IOException) {
                                runOnUiThread {
                                    Log.w("zio", "결제인증 실패")
                                    dialog(
                                        "결제가 실패돼었습니다..",
                                        "결제요청 결과",
                                        customDialogBinding.dialogButton.setOnClickListener {
                                            val failIntent =
                                                Intent(
                                                    this@PaymentActivity,
                                                    PaymentActivity::class.java
                                                )
                                            startActivity(failIntent)
                                        })
                                }
                            }
                        })
                    }

                    override fun onPaymentFailed(fail: TossPaymentResult.Fail) {
                        Log.d("zio", "결제 실패 !")
                        dialog(
                            "결제가 실패돼었습니다..",
                            "결제요청 결과",
                            customDialogBinding.dialogButton.setOnClickListener {
                                val failIntent =
                                    Intent(this@PaymentActivity, PaymentActivity::class.java)
                                startActivity(failIntent)
                            })
                    }
                }
            )
        }
    }


    //dialog
    fun dialog(message: String, title: String, OnClickListener: Unit) {

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
