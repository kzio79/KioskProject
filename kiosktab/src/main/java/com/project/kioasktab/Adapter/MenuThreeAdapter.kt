package com.project.kioasktab.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.project.kioask.model.OrderLIst
import com.project.kioasktab.Fragment.MenuThreeFragment
import com.project.kioasktab.databinding.RecyclerMenuBinding

class MenuThreeAdapter(
    private val dataList: MutableList<MenuThreeFragment.MenuItem>,
    val changePrice: MenuThreeFragment,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<MenuThreeAdapter.MenuThreeViewHolder>() {

    interface OnItemClickListener {

        fun onItemClick(menuitem: MenuThreeFragment.MenuItem)
    }

    inner class MenuThreeViewHolder(val binding: RecyclerMenuBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private val menu3ItemTextView: TextView = binding.menuitemItem
        private val menu3PriceTextView: TextView = binding.menuitemPrice

        fun bind(item3: MenuThreeFragment.MenuItem) {
            menu3ItemTextView.text = item3.item3
            menu3PriceTextView.text = "%,d원".format(item3.price3)

            val menuImage = OrderLIst.order.find { it.name == item3.item3 }?.image
            Glide.with(binding.root)
                .load(menuImage)
                .into(binding.menuitemImg)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuThreeViewHolder {
        val binding =
            RecyclerMenuBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MenuThreeViewHolder(binding)
    }

    override fun getItemCount(): Int = dataList.size

    override fun onBindViewHolder(holder: MenuThreeViewHolder, position: Int) {
        val menuItem = getItem(position)
        holder.bind(menuItem)

        holder.itemView.setOnClickListener {
            listener.onItemClick(menuItem)
        }
    }

    fun getItem(position: Int): MenuThreeFragment.MenuItem {
        return dataList[position]
    }
}