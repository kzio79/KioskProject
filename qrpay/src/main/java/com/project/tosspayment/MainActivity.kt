package com.project.tosspayment

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.gson.JsonObject
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import com.project.kioask.retrofit.NetworkService
import com.project.tosspayment.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.regex.Pattern

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    private var backPressTime:Long = 0

    private val REQUEST_CAMERA_PERMISSION = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
        val tokenCall: Call<JsonObject> = tokenService.doPostToken(jsonObject)

        tokenCall?.enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                if(response.isSuccessful){
                    val token = response.body()?.get("token")?.asString

                    val tokenSaved = getSharedPreferences("token", MODE_PRIVATE)
                    val tokenEdit: SharedPreferences.Editor = tokenSaved.edit()
                    tokenEdit.putString("token",token)
                    tokenEdit.commit()

                    Log.w("zio","token: $token")

                }else{
                    Log.w("zio","인증실패 : ${response.code()}")
                }
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Log.w("zio","접속실패 : ${t.message}")
            }

        })

    }

    private fun openScanner(){
        val QrScan = IntentIntegrator(this)
        QrScan.setCameraId(0) //후방카메라 사용
        QrScan.setBeepEnabled(false) //스캔할때 소리 끄기
        QrScan.addExtra("PROMPT_MESSAGE","QR코드를 스캔해주세요")
        QrScan.setOrientationLocked(false)
        QrScan.initiateScan()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            REQUEST_CAMERA_PERMISSION -> {
                if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    openScanner()
                }else{
                    Toast.makeText(this,"카메라를 허용해 주세요",Toast.LENGTH_LONG).show()
                }
                return
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        val result: IntentResult =
            IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        val scannerData = result.contents

        if (scannerData == null) {
            Toast.makeText(this, "QR코드를 다시 스캔해주세요!!!!!!!!!", Toast.LENGTH_LONG).show()
            return
        }

        if (requestCode == IntentIntegrator.REQUEST_CODE && resultCode == RESULT_OK) {
            val pattern =
                Pattern.compile("\\{\"orderId\":\"([A-Za-z0-9]+)\",\"price\":(\\d+),\"orderName\":\"([^\"]+)\",\"customerKey\":\"([A-Za-z0-9]+)\"\\}")

            val matcher = pattern.matcher(scannerData ?: "")

            if (matcher.find()) {
                val orderid = matcher.group(1)
                val price = matcher.group(2)?.toInt()
                val ordername = matcher.group(3)
                val customerKey = matcher.group(4)

                    intent = Intent(this, PayActivity::class.java)
                    intent.putExtra("orderId", orderid)
                    intent.putExtra("price", price)
                    intent.putExtra("orderName", ordername)
                    intent.putExtra("customerKey", customerKey)

                    startActivity(intent)
            }
            super.onActivityResult(requestCode, resultCode, data)
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
}