package com.project.kioasktab.Fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.project.kioasktab.Adapter.CartAdapter
import com.project.kioasktab.OrderSaved
import com.project.kioasktab.PayActivity
import com.project.kioasktab.databinding.CustomDialogBinding
import com.project.kioasktab.databinding.FragmentCartBinding
import kotlin.random.Random

class CartFragment : Fragment() {
    lateinit var cartAdapter: CartAdapter
    lateinit var cartbinding: FragmentCartBinding
    lateinit var customDialogBinding: CustomDialogBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        cartbinding = FragmentCartBinding.inflate(layoutInflater)

        //Adapter를 사용하여 리퀘스트뷰에 목록 활성화
        cartAdapter = CartAdapter(OrderSaved.orders, this)
        cartbinding.cartRecycler.adapter = cartAdapter
        cartbinding.cartRecycler.layoutManager = LinearLayoutManager(context)

        var totalPrice = OrderSaved.orders.sumOf { it.price }

        cartbinding.totalPrice.text = "%,d원".format(totalPrice)

        //결제하기 버튼 클릭
        cartbinding.payment.setOnClickListener {

            customDialogBinding = CustomDialogBinding.inflate(layoutInflater)
            if (totalPrice <= 0) {
                dialog()
                return@setOnClickListener
            } else if (cartAdapter.updatePrice() <= 0) {
                dialog()
                return@setOnClickListener
            } else {

                val intent = Intent(requireActivity(), PayActivity::class.java)
                intent.putExtra("totalPrice", totalPrice)
                intent.putExtra("orderId", orderid())
                intent.putExtra("customerKey", "ANONYMOUS")
                intent.putExtra("orderName", orderItems())
                startActivity(intent)
            }
        }
        return cartbinding.root
    }

    fun dialog() {

        if (customDialogBinding != null) {
            val alertDialog = context?.let {
                AlertDialog.Builder(it)
                    .setView(customDialogBinding.customDialog)
                    .create()
            }

            val dialogTitle = customDialogBinding.dialogTitle
            val dialogMessage = customDialogBinding.dialogMessage
            val dialogBtn = customDialogBinding.dialogButton

            dialogTitle.text = "주문금액을 확인해 주세요"
            dialogMessage.visibility = View.GONE
            dialogBtn.visibility = View.GONE

            alertDialog?.show()
        }
    }

    //payment에 들어갈 주문번호
    fun orderid(): String {
        var charset = ('0'..'9') + ('a'..'z') + ('A'..'Z')
        val randomset = List(10) { charset[Random.nextInt(charset.size)] }.joinToString("")
        return randomset
    }

    private fun orderItems(): String {
        val itemList = mutableListOf<String>()

        OrderSaved.orders.forEach { orderData ->
            val orderItem = orderData.item
            val optionItem = orderData.optionItem
            itemList.add("$orderItem $optionItem")
        }
        return itemList.joinToString(separator = ",")
    }
}