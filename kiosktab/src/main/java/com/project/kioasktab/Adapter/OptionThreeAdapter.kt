package com.project.kioasktab.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.project.kioasktab.Fragment.MenuThreeFragment
import com.project.kioasktab.databinding.RecyclerOptionBinding


open class OptionThreeAdapter(
    private val optionList: MutableList<MenuThreeFragment.OptionItem>,
    private val optionListener: onOptionItemClickLIstener,
    private val onItemCheckedChanged: (position: Int) -> Unit
) : RecyclerView.Adapter<OptionThreeAdapter.OptionThreeViewHolder>() {

    interface onOptionItemClickLIstener {
        fun onOptionItemClick(optionItem3: MenuThreeFragment.OptionItem, position:Int)
    }

    inner class OptionThreeViewHolder(val binding: RecyclerOptionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(optionItem3: MenuThreeFragment.OptionItem, position: Int) {
            binding.optionItemItem.text = optionItem3.optionItem3
            binding.optionItemPrice.text = "%,d원".format(optionItem3.optionPrice3)

            binding.optionItemCheck.setOnCheckedChangeListener(null)
            binding.optionItem.setOnClickListener(null)

            binding.optionItemCheck.isChecked = optionItem3.isChecked

            binding.optionItemCheck.setOnCheckedChangeListener { _, isChecked ->
                optionItem3.isChecked = isChecked
                onItemCheckedChanged(adapterPosition)
            }

            binding.optionItem.setOnClickListener {
                optionItem3.isChecked = !optionItem3.isChecked
                binding.optionItemCheck.isChecked = optionItem3.isChecked
                onItemCheckedChanged(adapterPosition)
                optionListener.onOptionItemClick(optionItem3, position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OptionThreeViewHolder {
        val binding =
            RecyclerOptionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OptionThreeViewHolder(binding)
    }

    override fun getItemCount(): Int = optionList.size

    override fun onBindViewHolder(holder: OptionThreeViewHolder, position: Int) {
        holder.bind(optionList[position], position)

    }

    fun getSelecetOptions(): List<MenuThreeFragment.OptionItem> {
        return optionList.filter { it.isChecked }
    }
}