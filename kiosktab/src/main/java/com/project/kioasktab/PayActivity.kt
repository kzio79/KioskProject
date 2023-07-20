package com.project.kioasktab

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import com.project.kioask.model.orderModel
import com.project.kioask.retrofit.NetworkService
import com.project.kioasktab.databinding.ActivityPayBinding
import com.project.kioasktab.databinding.CustomDialogBinding
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Hashtable

class PayActivity : AppCompatActivity() {
    lateinit var bindingpay: ActivityPayBinding
    lateinit var customDialogBinding: CustomDialogBinding
    var handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindingpay = ActivityPayBinding.inflate(layoutInflater)
        setContentView(bindingpay.root)


        val tokenSaved = getSharedPreferences("token", MODE_PRIVATE)
        val token = tokenSaved.getString("token", null)

        val orderid: String? = intent.getStringExtra("orderId")!!
        val ordername: String? = intent.getStringExtra("orderName")!!
        val totalprice = intent.getIntExtra("totalPrice", 0)
        val customerKey: String? = intent.getStringExtra("customerKey")!!

        val qrCodeWriter = QRCodeWriter()
        val paymentData = JSONObject()
        paymentData.put("orderId", orderid)
        paymentData.put("price", totalprice)
        paymentData.put("orderName", ordername)
        paymentData.put("customerKey", customerKey)

        try {

            customDialogBinding = CustomDialogBinding.inflate(layoutInflater)
            val hints = Hashtable<EncodeHintType, Any>()
            hints[EncodeHintType.CHARACTER_SET] = Charsets.UTF_8.toString()

            val bitMatrix =
                qrCodeWriter.encode(paymentData.toString(), BarcodeFormat.QR_CODE, 500, 500, hints)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
                }
            }
            bindingpay.payQrcode.setImageBitmap(bitmap)

            bindingpay.payChange.setOnClickListener {
                bindingpay.payQrcode.visibility = View.GONE
                bindingpay.payVan.visibility = View.VISIBLE
                bindingpay.payResult.visibility = View.VISIBLE
                bindingpay.payChange.visibility = View.GONE
                bindingpay.payQrtext.visibility = View.GONE
                bindingpay.payVantext.visibility = View.VISIBLE
                bindingpay.payCountDown.visibility = View.VISIBLE
            }

            handler.postDelayed({
                Thread {
                    val client = OkHttpClient()

                    val request = Request.Builder()
                        .url("https://api.tosspayments.com/v1/payments/orders/$orderid")
                        .get()
                        .addHeader(
                            "Authorization",
                            "Basic dGVzdF9za19aT1J6ZE1hcU4zd015a1p5bDdOcjVBa1lYUUd3Og=="
                        )
                        .build()

                    val response = client.newCall(request).execute()

                    if (response != null) {
                        val status = response
                        if (status.isSuccessful) {
                            runOnUiThread {
                                Log.w("zio", "결제인증 성공")
                                dialog("결제가 완료 되었습니다.", "결제요청 결과",
                                    customDialogBinding.dialogButton.setOnClickListener {
                                        val successIntent =
                                            Intent(this@PayActivity, ReceiptActivity::class.java)
                                        startActivity(successIntent)
                                    })
                            }
                        } else {
                            handler.postDelayed({
                                runOnUiThread {
                                    Log.w("zio", "결제인증 재 시작: $response, orderid: $orderid")
                                    dialog("Qr코드를 다시 스캔해 주세요.", "결제요청 결과",
                                        customDialogBinding.dialogButton.setOnClickListener {
                                            finishAfterTransition()
                                        })

                                }
                            }, 33_000)
                        }
                    } else {
                        handler.postDelayed({
                            runOnUiThread {
                                Log.w("zio", "결제 실패")
                                dialog("결제를 실패했습니다.", "결제요청 결과",
                                    customDialogBinding.dialogButton.setOnClickListener {
                                        intent = Intent(this@PayActivity, MainActivity::class.java)
                                        startActivity(intent)
                                    })
                            }
                        }, 40_000)
                    }
                    try {

                    } catch (e: Exception) {
                        e.printStackTrace()
                        Log.w("zio", "Thread error : ${e.message}")
                    }
                }.start()
            }, 33_000)


        } catch (e: WriterException) {
            e.printStackTrace()
        }


        //카드결제
        bindingpay.payResult.setOnClickListener {

            val retrofit = Retrofit.Builder()
                .baseUrl("https://api.format.kro.kr")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

            val orderstate = 1
            val paymentkey = "cardpay"

            val dto = orderModel(orderid!!,ordername!!,totalprice,paymentkey,orderstate)
            val service = retrofit.create(NetworkService::class.java)
            val postCall = service.doPostList(token, dto)

            postCall.enqueue(object : retrofit2.Callback<orderModel> {
                override fun onResponse(
                    call: retrofit2.Call<orderModel>,
                    response: retrofit2.Response<orderModel>
                ) {
                    if (response.isSuccessful) {
                        Log.w("zio", "보내기 성공!!")

                    } else {
                        Log.w("zio", "API 접속 실패 : ${response.code()}, ${response.message()}")
                    }
                }

                override fun onFailure(
                    call: retrofit2.Call<orderModel>,
                    t: Throwable
                ) {
                    Log.e("zio", "전송실패: ${t}")
                }
            })
            val intent = Intent(this, ReceiptActivity::class.java)
            startActivity(intent)
        }



        object : CountDownTimer(60_000, 1_000) {

            override fun onTick(millisUntilFinished: Long) {
                val sec = (millisUntilFinished / 1000).toInt()
                bindingpay.payCountDown.text = "$sec 초"
            }

            override fun onFinish() {
                bindingpay.payCountDown.text = "종료"
            }
        }.start()
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

