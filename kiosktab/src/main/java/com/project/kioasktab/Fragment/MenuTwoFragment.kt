package com.project.kioasktab.Fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
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
import com.project.kioasktab.Adapter.MenuTwoAdapter
import com.project.kioasktab.Adapter.OptionTwoAdapter
import com.project.kioasktab.OrderActivity
import com.project.kioasktab.OrderData
import com.project.kioasktab.OrderSaved
import com.project.kioasktab.databinding.CustomDialogBinding
import com.project.kioasktab.databinding.FragmentMenuTwoBinding
import com.project.kioasktab.databinding.RecyclerMenuBinding
import com.project.kioasktab.databinding.RecyclerOptionBinding

class MenuTwoFragment : Fragment(), MenuTwoAdapter.OnItemClickListener,
    OptionTwoAdapter.onOptionItemClickListener {
    lateinit var binding2: FragmentMenuTwoBinding
    lateinit var menuBinding: RecyclerMenuBinding
    lateinit var optionBinding: RecyclerOptionBinding
    lateinit var customDialogBinding: CustomDialogBinding
    private var selectedItem: MenuItem? = null
    private var selectedOptionItem: MutableSet<OptionItem> = mutableSetOf()

    class menuData2(var item2: String = "", var price2: Int = 0)
    class optionData2(var optionItem2: String = "", var optionPrice2: Int = 0)

    private lateinit var menuAdapter: MenuTwoAdapter

    data class MenuItem(val item2: String, val price2: Int)

    private lateinit var optionAdapter: OptionTwoAdapter

    data class OptionItem(
        val optionItem2: String,
        val optionPrice2: Int,
        var isChecked: Boolean = false
    )

    lateinit var db: FirebaseFirestore
    var currentMenuPrice = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding2 = FragmentMenuTwoBinding.inflate(inflater, container, false)
        menuBinding = RecyclerMenuBinding.inflate(inflater, container, false)
        optionBinding = RecyclerOptionBinding.inflate(inflater, container, false)

        // DrawerLayout을 열기
        binding2.recyclerMenu.setOnClickListener {
            binding2.drawerMenu2.openDrawer(GravityCompat.END)
        }

        //firebase에서 자료를 불러옴
        db = FirebaseFirestore.getInstance()

        MenuTwoFetch()
        OptionTwoFetch()

        // 주문하기 버튼 클릭 리스너 설정
        binding2.orderMenu2Item.setOnClickListener {

            customDialogBinding = CustomDialogBinding.inflate(layoutInflater)

            val selectedItem = this.selectedItem
            val selectedOptionItem = this.selectedOptionItem
            val selectedOptions = optionAdapter.getSelecetOptions()
            val optionSumPrice = selectedOptions.sumBy { optionItem -> optionItem.optionPrice2 }

            if (selectedItem != null && selectedOptionItem.isNotEmpty()) {
                val item2 = selectedItem.item2
                val price2 = selectedItem.price2
                val optionItem2 = selectedOptionItem.map { it.optionItem2 }
                val optionPrice2 = selectedOptionItem.map { it.optionPrice2 }

                var existingItem = OrderSaved.orders.firstOrNull {
                    it.item == item2 && it.optionItem.equals(optionItem2)
                }

                if (existingItem == null) {
                    OrderSaved.orders.add(
                        OrderData(
                            item2, price2 + optionSumPrice,
                            optionItem2.toString(), optionPrice2.size, 1
                        )
                    )

                } else if (selectedItem.equals(existingItem) && existingItem.optionItem.equals(
                        optionItem2
                    )
                ) {
                    existingItem.count += 1
                    existingItem.price += price2 + optionSumPrice
                }

                val intent = Intent(requireActivity(), OrderActivity::class.java)
                startActivity(intent)
            } else {
                dialog()
            }
        }
        return binding2.root
    }

    //firebase에서 자료 불러오기
    private fun MenuTwoFetch() {

        val itemListFilter = OrderLIst.order.filter { item ->
            item.code.startsWith("SIDE") // 필터링 조건을 여기에 작성합니다
        }

        val menuList = mutableListOf<MenuTwoFragment.MenuItem>()

        for (item in itemListFilter) {
            val menuItem = MenuTwoFragment.MenuItem(item.name!!, item.price!!)
            menuList.add(menuItem)
        }

        val MenurecyclerView = binding2.recyclerMenu
        menuAdapter = MenuTwoAdapter(menuList, this, this)
        MenurecyclerView.adapter = menuAdapter
        MenurecyclerView.layoutManager = GridLayoutManager(context, 4)
    }

    private fun OptionTwoFetch() {

        db.collection("Option2")
            .get()
            .addOnSuccessListener { result ->
                val optionList = mutableListOf<OptionItem>()
                val OptionrecyclerView = binding2.recyclerOption

                for (document in result) {
                    val optionData = document.toObject(optionData2::class.java)
                    optionList.add(OptionItem(optionData.optionItem2, optionData.optionPrice2))
                }
                optionAdapter = OptionTwoAdapter(optionList, this) { position ->

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
        currentMenuPrice = menuItem.price2
        binding2.orderMenu2Total.text = "%,d원".format(currentMenuPrice)
        binding2.drawerMenu2.openDrawer(GravityCompat.END)
        binding2.option2ItemName.text = menuItem.item2
        binding2.option2ItemText.text = OrderLIst.order.find { it.name == menuItem.item2 }?.content
        Glide.with(this)
            .load(OrderLIst.order.find { it.name == menuItem.item2 }?.image)
            .into(binding2.option2ItemPic)
    }

    override fun onOptionItemClick(optionItem2: OptionItem) {

        if (!selectedOptionItem.equals(optionItem2)) {
            selectedOptionItem.add(optionItem2)
        } else {
            selectedOptionItem.remove(optionItem2)
        }
    }

    private fun updatePrice(optionList: MutableList<OptionItem>) {
        var totalOptionPrice = 0

        for (option in optionList) {
            if (option.isChecked) {
                totalOptionPrice += option.optionPrice2
            }
        }
        val currentMenuPrice = "%,d".format(selectedItem?.price2 ?: 0).replace(",", "").toInt()
        binding2.orderMenu2Total.text = "%,d원".format(currentMenuPrice + totalOptionPrice)
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