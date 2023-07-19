package com.project.tosspayment

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.gson.JsonObject
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import com.project.kioask.retrofit.NetworkService
import com.project.tosspayment.databinding.ActivityMainBinding
import com.project.tosspayment.databinding.CustomDialogBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.regex.Pattern

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    lateinit var customDialogBinding: CustomDialogBinding
    private var backPressTime:Long = 0

    private val REQUEST_CAMERA_PERMISSION = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        customDialogBinding =
            CustomDialogBinding.inflate(LayoutInflater.from(this@MainActivity))

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA),REQUEST_CAMERA_PERMISSION)
        } else {
            openScanner()
        }

        val passkey = getString(R.string.backend_secretKey)

        val tokenRetrofit = Retrofit.Builder()
            .baseUrl("https://api.format.kro.kr")
            .addConverterFactory(GsonConverterFactory.create())
            .build();

        val jsonObject = JsonObject()
        jsonObject.addProperty("passkey",passkey)

        val tokenService = tokenRetrofit.create(NetworkService::class.java)
        val tokenCall: Call<JsonObject> = tokenService.doPostToken(passkey)
        try {

            tokenCall.enqueue(object : Callback<JsonObject> {
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    if(response.isSuccessful){
                        val token = response.body()?.get("token")?.asString

                        val tokenSaved = getSharedPreferences("token", MODE_PRIVATE)
                        val tokenEdit: SharedPreferences.Editor = tokenSaved.edit()
                        tokenEdit.putString("token",token)
                        tokenEdit.apply()

                        Log.w("zio","token: $token")

                    }else{
                        Log.w("zio","인증실패 : ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                    Log.w("zio","접속실패 : ${t.message}")
                }
            })
        }catch (e: Exception){
            e.printStackTrace()
            Log.w("zio","error : ${e.message}")
        }
    }

    private fun openScanner(){
        val QrScan = IntentIntegrator(this)
        QrScan.setCameraId(0) //후방카메라 사용
        QrScan.setBeepEnabled(false) //스캔할때 소리 끄기
        QrScan.addExtra("PROMPT_MESSAGE","QR코드를 스캔해주세요")
        QrScan.setOrientationLocked(false)
        QrScan.initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        customDialogBinding =
            CustomDialogBinding.inflate(LayoutInflater.from(this@MainActivity))

        val result: IntentResult =
            IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        val scannerData = result.contents

        Log.w("zio","scannerData: $scannerData")
        if (scannerData == null) {
            dialog("QR코드 확인","QR코드를 다시 스캔해 주세요",
                customDialogBinding.dialogButton.setOnClickListener {
                    intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                })
            return
        }

        if (requestCode == IntentIntegrator.REQUEST_CODE && resultCode == RESULT_OK) {
            val pattern =
                Pattern.compile("\\{\"orderId\":\"([A-Za-z0-9]+)\",\"price\":(\\d+),\"orderName\":\"([^\"]+)\",\"customerKey\":\"([A-Za-z0-9]+)\"\\}")

            val matcher = pattern.matcher(scannerData ?: "")

            if (matcher.matches()) {
                val orderid = matcher.group(1)
                val price = matcher.group(2)?.toInt()
                val ordername = matcher.group(3)
                val customerKey = matcher.group(4)

//                if(scannerData == orderid){
//                    intent = Intent(this, PayActivity::class.java)
//                    intent.putExtra("orderId", orderid)
//                    intent.putExtra("price", price)
//                    intent.putExtra("orderName", ordername)
//                    intent.putExtra("customerKey", customerKey)
//
//                    startActivity(intent)
//                    Log.w("zio","qr코드 ok")
//                }else {
//                    dialog("잘못된 QR코드 입니다.","QR코드를 확인해 주세요",
//                        customDialogBinding.dialogButton.setOnClickListener {
//                            intent = Intent(this, MainActivity::class.java)
//                            startActivity(intent)
//                        })
//                    Log.w("zio","잘못된 qr코드")
//                }
                    intent = Intent(this, PayActivity::class.java)
                    intent.putExtra("orderId", orderid)
                    intent.putExtra("price", price)
                    intent.putExtra("orderName", ordername)
                    intent.putExtra("customerKey", customerKey)

                    startActivity(intent)
            }
            super.onActivityResult(requestCode, resultCode, data)
        }else{
            dialog("QR코드 확인","잘못된 QR코드 입니다.",
            customDialogBinding.dialogButton.setOnClickListener {
                intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            })
        }
    }

    override fun onBackPressed() {
        if(System.currentTimeMillis() - backPressTime < 2_000){
            finish()
        }else{
            Toast.makeText(this,"한번 더 누르면 앱이 종료됩니다.",Toast.LENGTH_SHORT).show()
            backPressTime = System.currentTimeMillis()
        }
    }

    fun dialog(title:String,message:String, OnClickListener: Unit) {

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