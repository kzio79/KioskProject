package com.project.kioasktab

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.project.kioasktab.Adapter.ReceiptAdapter
import com.project.kioasktab.databinding.ActivityReceiptBinding

class ReceiptActivity : AppCompatActivity() {
    lateinit var binding: ActivityReceiptBinding
    lateinit var receiptAdapter: ReceiptAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReceiptBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.receiptRecycler.layoutManager = LinearLayoutManager(this)
        receiptAdapter = ReceiptAdapter(OrderSaved.orders)
        binding.receiptRecycler.adapter = receiptAdapter

        var totalPrice = OrderSaved.orders.sumOf { it.price }
        binding.receiptPrice.text = "%,d원".format(totalPrice)

        binding.receiptResult.setOnClickListener {
            intent = Intent(this, MainActivity::class.java)
            OrderSaved.orders.clear()
            startActivity(intent)
        }
    }
}