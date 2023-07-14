package com.project.kioasktab.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.project.kioasktab.Fragment.CartFragment
import com.project.kioasktab.OrderData
import com.project.kioasktab.databinding.RecyclerCartBinding

class CartAdapter(val dataList: MutableList<OrderData>, val changePrice: CartFragment) :
    RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    inner class CartViewHolder(val binding: RecyclerCartBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(orderData: OrderData) {
            binding.itemOrder.text = "${orderData.item} ${orderData.optionItem}"
            binding.itemPrice.text = "%,d원".format(orderData.price)
            binding.itemCount.text = "${orderData.count}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding =
            RecyclerCartBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CartViewHolder(binding)
    }

    override fun getItemCount(): Int = dataList.size

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {

        val item = dataList[position]
        holder.bind(item)

        //삭제버튼 누를시 recyclerView의 항목 삭제와 합계 금액 동시에 변경
        holder.binding.btnItemDelete.setOnClickListener {
            dataList.remove(item)
            notifyDataSetChanged()
            updatePrice()
        }
    }

    //합계 금액 변경 함수
    fun updatePrice(): Int {
        val totalPrice = dataList.sumOf { it.price }
        changePrice.cartbinding.totalPrice.text = "%,d원".format(totalPrice)
        return totalPrice
    }
}