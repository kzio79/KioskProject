package com.project.kioasktab.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.project.kioasktab.Fragment.MenuOneFragment
import com.project.kioasktab.databinding.RecyclerOptionBinding

open class OptionOneAdapter(
    val optionList: MutableList<MenuOneFragment.OptionItem>,
    val optionListener: onOptionItemClickListener,
    val onItemCheckedChanged: (position: Int) -> Unit
)

    : RecyclerView.Adapter<OptionOneAdapter.OptionOneViewHolder>() {

    interface onOptionItemClickListener {
        fun onOptionItemClick(optionItem1: MenuOneFragment.OptionItem, position: Int)
    }

    inner class OptionOneViewHolder(val binding: RecyclerOptionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(optionItem: MenuOneFragment.OptionItem, position: Int) {
            binding.optionItemItem.text = optionItem.optionItem1
            binding.optionItemPrice.text = "%,d원".format(optionItem.optionPrice1)

            binding.optionItemCheck.setOnCheckedChangeListener(null)
            binding.optionItem.setOnClickListener(null)
            binding.optionItemCheck.isChecked = optionItem.isChecked

            binding.optionItemCheck.setOnCheckedChangeListener { _, isChecked ->
                optionItem.isChecked = isChecked
                onItemCheckedChanged(adapterPosition)
            }

            binding.optionItem.setOnClickListener {
                optionItem.isChecked = !optionItem.isChecked
                binding.optionItemCheck.isChecked = optionItem.isChecked
                onItemCheckedChanged(adapterPosition)
                optionListener.onOptionItemClick(optionItem, position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OptionOneViewHolder {
        val binding =
            RecyclerOptionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OptionOneViewHolder(binding)
    }

    override fun getItemCount(): Int = optionList.size

    override fun onBindViewHolder(holder: OptionOneAdapter.OptionOneViewHolder, position: Int) {
        holder.bind(optionList[position], position)
    }

    fun getSelecetOptions(): List<MenuOneFragment.OptionItem> {
        return optionList.filter { it.isChecked }
    }
}