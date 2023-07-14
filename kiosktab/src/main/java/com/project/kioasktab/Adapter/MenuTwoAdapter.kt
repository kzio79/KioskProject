package com.project.kioasktab.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.project.kioask.model.OrderLIst
import com.project.kioasktab.Fragment.MenuTwoFragment
import com.project.kioasktab.databinding.RecyclerMenuBinding

class MenuTwoAdapter(
    private val dataList: MutableList<MenuTwoFragment.MenuItem>,
    val changePrice: MenuTwoFragment,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<MenuTwoAdapter.MenuTwoViewHolder>() {

    interface OnItemClickListener {

        fun onItemClick(menuitem: MenuTwoFragment.MenuItem)
    }

    inner class MenuTwoViewHolder(val binding: RecyclerMenuBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item2: MenuTwoFragment.MenuItem) {
            binding.menuitemItem.text = item2.item2
            binding.menuitemPrice.text = "%,d원".format(item2.price2)

            val menuImage = OrderLIst.order.find { it.name == item2.item2 }?.image
            Glide.with(binding.root)
                .load(menuImage)
                .into(binding.menuitemImg)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuTwoViewHolder {
        val binding =
            RecyclerMenuBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MenuTwoViewHolder(binding)
    }

    override fun getItemCount(): Int = dataList.size

    override fun onBindViewHolder(holder: MenuTwoViewHolder, position: Int) {
        val menuItem = getItem(position)
        holder.bind(menuItem)

        holder.itemView.setOnClickListener {
            listener.onItemClick(menuItem)
        }
    }

    fun getItem(position: Int): MenuTwoFragment.MenuItem {
        return dataList[position]
    }
}