package com.project.kioasktab.Fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.project.kioask.model.OrderLIst
import com.project.kioasktab.Adapter.MenuOneAdapter
import com.project.kioasktab.Adapter.OptionOneAdapter
import com.project.kioasktab.OrderActivity
import com.project.kioasktab.OrderData
import com.project.kioasktab.OrderSaved
import com.project.kioasktab.databinding.CustomDialogBinding
import com.project.kioasktab.databinding.FragmentMemuOneBinding
import com.project.kioasktab.databinding.RecyclerMenuBinding
import com.project.kioasktab.databinding.RecyclerOptionBinding

class MenuOneFragment : Fragment(), MenuOneAdapter.OnItemClickListener,
    OptionOneAdapter.onOptionItemClickListener {
    lateinit var binding1: FragmentMemuOneBinding
    lateinit var menuBinding: RecyclerMenuBinding
    lateinit var optionBinding: RecyclerOptionBinding
    lateinit var customDialogBinding: CustomDialogBinding
    private var selectedItem: MenuItem? = null
    private var selectedOptionItem: MutableSet<OptionItem> = mutableSetOf()

    class menuData1(var item1: String = "", var price1: Int = 0)
    class optionData1(var optionItem1: String = "", var optionPrice1: Int = 0)

    private lateinit var menuAdapter: MenuOneAdapter

    data class MenuItem(val item1: String, val price1: Int)

    private lateinit var optionAdapter: OptionOneAdapter

    data class OptionItem(
        val optionItem1: String,
        val optionPrice1: Int,
        var isChecked: Boolean = false
    )

    lateinit var db: FirebaseFirestore
    var currentMenuPrice = 0


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding1 = FragmentMemuOneBinding.inflate(inflater, container, false)
        menuBinding = RecyclerMenuBinding.inflate(inflater, container, false)
        optionBinding = RecyclerOptionBinding.inflate(inflater, container, false)


        // DrawerLayout을 열기
        binding1.recyclerMenu.setOnClickListener {
            binding1.drawerMenu1.openDrawer(GravityCompat.END)
        }

        //firebase에서 자료를 불러옴
        db = FirebaseFirestore.getInstance()

        MenuOneFetch()
        OptionOneFetch()

        // 주문하기 버튼 클릭 리스너 설정
        binding1.orderMenu1Item.setOnClickListener {

            customDialogBinding = CustomDialogBinding.inflate(layoutInflater)
            val selectedItem = this.selectedItem
            val selectedOptionItem = this.selectedOptionItem
            val selectedOptions = optionAdapter.getSelecetOptions()
            val optionSumPrice = selectedOptions.sumBy { optionItem -> optionItem.optionPrice1 }

            if (selectedItem != null && selectedOptionItem.isNotEmpty()) {
                val item1 = selectedItem.item1
                val price1 = selectedItem.price1
                val optionItem1 = selectedOptionItem.map { it.optionItem1 }
                val optionPrice1 = selectedOptionItem.map { it.optionPrice1 }

                var existingItem = OrderSaved.orders.firstOrNull {
                    it.item == item1 && it.optionItem.equals(optionItem1)
                }

                if (existingItem == null) {
                    OrderSaved.orders.add(
                        OrderData(
                            item1, price1 + optionSumPrice, optionItem1.toString(),
                            optionPrice1.size, 1
                        )
                    )

                } else if (selectedItem.equals(existingItem) && existingItem.optionItem.equals(optionItem1)) {
                    existingItem.count++
                    existingItem.price += price1 + optionSumPrice
                    Log.w("zio","동작하니?")
                }
                val intent = Intent(requireActivity(), OrderActivity::class.java)
                startActivity(intent)
            } else {
                dialog()
            }
        }
        return binding1.root
    }

    private fun MenuOneFetch() {

        val itemListFilter = OrderLIst.order.filter { item ->
            item.code.startsWith("BUG") // 필터링 조건을 여기에 작성합니다
        }

        val menuList = mutableListOf<MenuItem>()

        for (item in itemListFilter) {
            val menuItem = MenuItem(item.name!!, item.price!!)
            menuList.add(menuItem)
        }

        val MenurecyclerView = binding1.recyclerMenu

        menuAdapter = MenuOneAdapter(menuList, this, this)
        MenurecyclerView.adapter = menuAdapter
        MenurecyclerView.layoutManager = GridLayoutManager(context, 4)
        MenurecyclerView.adapter = menuAdapter
    }

    //firebase에서 자료 불러오기
    private fun OptionOneFetch() {

        db.collection("Option1")
            .get()
            .addOnSuccessListener { result ->
                val optionList = mutableListOf<OptionItem>()
                val OptionrecyclerView = binding1.recyclerOption

                for (document in result) {
                    val optionData = document.toObject(MenuOneFragment.optionData1::class.java)
                    optionList.add(OptionItem(optionData.optionItem1, optionData.optionPrice1))
                }
                optionAdapter = OptionOneAdapter(optionList, this) { position ->
                    optionList[position].isChecked = !optionList[position].isChecked
                    optionAdapter.notifyItemChanged(position)

                    selectedOptionItem.clear()
                    selectedOptionItem.addAll(optionList.filter { it.isChecked })
                    updatePrice(optionList)
                }
                OptionrecyclerView.adapter = optionAdapter
                OptionrecyclerView.layoutManager = LinearLayoutManager(context)
            }
            .addOnFailureListener { e ->
                Log.w("zio", "Error get document", e)
            }
    }

    //리사이클러 뷰 클릭시 drawer활성화
    override fun onItemClick(menuItem: MenuItem) {

        selectedItem = menuItem
        currentMenuPrice = menuItem.price1
        binding1.orderMenu1Total.text = "%,d원".format(currentMenuPrice)
        binding1.drawerMenu1.openDrawer(GravityCompat.END)
        binding1.option1ItemName.text = menuItem.item1
        binding1.option1ItemText.text = OrderLIst.order.find { it.name == menuItem.item1 }?.content
        Glide.with(this)
            .load(OrderLIst.order.find { it.name == menuItem.item1 }?.image)
            .into(binding1.option1ItemPic)
    }

    override fun onOptionItemClick(optionItem1: OptionItem, position: Int) {
        if (!selectedOptionItem.equals(optionItem1)) {
            selectedOptionItem.add(optionItem1)
        } else {
            selectedOptionItem.remove(optionItem1)
        }
    }

    fun updatePrice(optionList: List<OptionItem>) {
        var totalOptionPrice = 0

        for (option in optionList) {
            if (option.isChecked) {
                totalOptionPrice += option.optionPrice1
            }
        }
        val currentMenuItemPrice = "%,d".format(selectedItem?.price1 ?: 0).replace(",", "").toInt()
        binding1.orderMenu1Total.text = "%,d원".format(currentMenuItemPrice + totalOptionPrice)

    }

    fun dialog() {
        val alertDialog =
            AlertDialog.Builder(requireContext())
                .setView(customDialogBinding.customDialog)
                .create()

        customDialogBinding.dialogTitle.text = "옵션을 1개이상 선택하세요"
        customDialogBinding.dialogTitle.visibility = View.VISIBLE
        alertDialog.show()
    }
}

