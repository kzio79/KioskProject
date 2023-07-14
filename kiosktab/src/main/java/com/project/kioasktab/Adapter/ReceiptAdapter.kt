package com.project.kioasktab.Adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.project.kioasktab.OrderData
import com.project.kioasktab.databinding.RecyclerReceiptBinding

class ReceiptAdapter(val dataList: MutableList<OrderData>) :
    RecyclerView.Adapter<ReceiptAdapter.CartViewHolder>() {


    inner class CartViewHolder(val binding: RecyclerReceiptBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(orderData: OrderData) {
            binding.itemOrder.text = "${orderData.item} ${orderData.optionItem}"
            binding.itemPrice.text = "%,d원".format(orderData.price)
            binding.itemCount.text = "${orderData.count}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding =
            RecyclerReceiptBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CartViewHolder(binding)
    }

    override fun getItemCount(): Int = dataList.size

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {

        val item = dataList[position]
        holder.bind(item)

    }
}



