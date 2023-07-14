package com.project.kioasktab.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.project.kioask.model.OrderLIst
import com.project.kioasktab.Fragment.MenuOneFragment
import com.project.kioasktab.databinding.RecyclerMenuBinding

class MenuOneAdapter(
    private val itemList: MutableList<MenuOneFragment.MenuItem>,
    val changePrice: MenuOneFragment,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<MenuOneAdapter.MenuOneViewHolder>() {

    interface OnItemClickListener {

        fun onItemClick(menuitem: MenuOneFragment.MenuItem)
    }

    inner class MenuOneViewHolder(val binding: RecyclerMenuBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MenuOneFragment.MenuItem) {
            binding.menuitemItem.text = item.item1
            binding.menuitemPrice.text = "%,d원".format(item.price1)
            val menuImage = OrderLIst.order.find { it.name == item.item1 }?.image
            Glide.with(binding.root)
                .load(menuImage)
                .into(binding.menuitemImg)

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuOneViewHolder {
        val binding =
            RecyclerMenuBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MenuOneViewHolder(binding)
    }

    override fun getItemCount(): Int = itemList.size

    override fun onBindViewHolder(holder: MenuOneViewHolder, position: Int) {
        val menuItem = getItem(position)
        holder.bind(menuItem)


        holder.itemView.setOnClickListener {
            listener.onItemClick(menuItem)
        }
    }

    fun getItem(position: Int): MenuOneFragment.MenuItem {
        return itemList[position]
    }
}