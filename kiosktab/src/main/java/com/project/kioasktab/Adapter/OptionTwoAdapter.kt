package com.project.kioasktab.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.project.kioasktab.Fragment.MenuTwoFragment
import com.project.kioasktab.databinding.RecyclerOptionBinding

open class OptionTwoAdapter(
    private val optionList: MutableList<MenuTwoFragment.OptionItem>,
    private val optionListener: onOptionItemClickListener,
    private val onItemCheckedChanged: (position: Int) -> Unit
) : RecyclerView.Adapter<OptionTwoAdapter.OptionTwoViewHolder>() {

    interface onOptionItemClickListener {
        fun onOptionItemClick(optionItem2: MenuTwoFragment.OptionItem)
    }

    inner class OptionTwoViewHolder(val binding: RecyclerOptionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(optionItem2: MenuTwoFragment.OptionItem) {
            binding.optionItemItem.text = optionItem2.optionItem2
            binding.optionItemPrice.text = "%,d원".format(optionItem2.optionPrice2)

            binding.optionItemCheck.setOnCheckedChangeListener(null)
            binding.optionItem.setOnClickListener(null)

            binding.optionItemCheck.isChecked = optionItem2.isChecked

            binding.optionItemCheck.setOnCheckedChangeListener { _, isChecked ->
                optionItem2.isChecked = isChecked
                onItemCheckedChanged(adapterPosition)
            }
            binding.optionItem.setOnClickListener {
                optionItem2.isChecked = !optionItem2.isChecked
                binding.optionItemCheck.isChecked = optionItem2.isChecked
                onItemCheckedChanged(adapterPosition)
                optionListener.onOptionItemClick(optionItem2)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): OptionTwoAdapter.OptionTwoViewHolder {
        val binding =
            RecyclerOptionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OptionTwoViewHolder(binding)
    }

    override fun getItemCount(): Int = optionList.size

    override fun onBindViewHolder(holder: OptionTwoAdapter.OptionTwoViewHolder, position: Int) {
        holder.bind(optionList[position])
    }

    fun getSelecetOptions(): List<MenuTwoFragment.OptionItem> {
        return optionList.filter { it.isChecked }
    }
}