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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.project.kioask.model.OrderLIst
import com.project.kioasktab.Adapter.MenuThreeAdapter
import com.project.kioasktab.Adapter.OptionThreeAdapter
import com.project.kioasktab.OrderActivity
import com.project.kioasktab.OrderData
import com.project.kioasktab.OrderSaved
import com.project.kioasktab.databinding.CustomDialogBinding
import com.project.kioasktab.databinding.FragmentMenuThreeBinding
import com.project.kioasktab.databinding.RecyclerMenuBinding
import com.project.kioasktab.databinding.RecyclerOptionBinding

class MenuThreeFragment : Fragment(), MenuThreeAdapter.OnItemClickListener,
    OptionThreeAdapter.onOptionItemClickLIstener {
    lateinit var binding3: FragmentMenuThreeBinding
    lateinit var menuBinding: RecyclerMenuBinding
    lateinit var optionBinding: RecyclerOptionBinding
    lateinit var customDialogBinding: CustomDialogBinding
    private var selectedItem: MenuItem? = null
    private var selectedOptionItem: MutableSet<OptionItem> = mutableSetOf()

    class menuData3(var item3: String = "", var price3: Int = 0)
    class optionData3(var optionItem3: String = "", var optionPrice3: Int = 0)


    private lateinit var menuAdapter: MenuThreeAdapter

    data class MenuItem(val item3: String, val price3: Int)

    private lateinit var optionAdapter: OptionThreeAdapter

    data class OptionItem(
        val optionItem3: String,
        val optionPrice3: Int,
        var isChecked: Boolean = false
    )

    lateinit var auth: FirebaseAuth
    lateinit var db: FirebaseFirestore
    var currentMenuPrice = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding3 = FragmentMenuThreeBinding.inflate(inflater, container, false)
        menuBinding = RecyclerMenuBinding.inflate(inflater, container, false)
        optionBinding = RecyclerOptionBinding.inflate(inflater, container, false)

        // DrawerLayout을 열기
        binding3.recyclerMenu.setOnClickListener {
            binding3.drawerMenu3.openDrawer(GravityCompat.END)
        }

        //firebase에서 자료를 불러옴
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        MenuThreeFetch()
        OptionThreeFetch()

        // 주문하기 버튼 클릭 리스너 설정
        binding3.orderMenu3Item.setOnClickListener { view ->

            customDialogBinding = CustomDialogBinding.inflate(layoutInflater)

            val selectedItem = this.selectedItem
            val selectOptions = optionAdapter.getSelecetOptions()
            val selectOptionItem = this.selectedOptionItem
            val optionSumPrice = selectOptions.sumBy { optionItem -> optionItem.optionPrice3 }

            if (selectedItem != null && selectedOptionItem.isNotEmpty()) {
                val item3 = selectedItem.item3
                val price3 = selectedItem.price3
                val optionItem3 = selectedOptionItem.map { it.optionItem3 }
                val optionPrice3 = selectOptionItem.map { it.optionPrice3 }

                var existingItem = OrderSaved.orders.firstOrNull {
                    it.item == item3 && it.optionItem.equals(optionItem3)
                }
                if (existingItem == null) {
                    OrderSaved.orders.add(
                        OrderData(
                            item3, price3 + optionSumPrice,
                            optionItem3.toString(), optionPrice3.size, 1
                        )
                    )
                } else if (selectedItem.equals(existingItem) && existingItem.optionItem.equals(
                        optionItem3
                    )
                ) {
                    existingItem.count += 1
                    existingItem.price += price3 + optionSumPrice
                }

                val intent = Intent(requireActivity(), OrderActivity::class.java)
                startActivity(intent)
            } else {
                dialog()
            }
        }
        return binding3.root
    }

    //firebase에서 자료 불러오기
    private fun MenuThreeFetch() {

        val itemListFilter = OrderLIst.order.filter { item ->
            item.code.startsWith("DRI") // 필터링 조건을 여기에 작성합니다
        }

        val menuList = mutableListOf<MenuThreeFragment.MenuItem>()

        for (item in itemListFilter) {
            val menuItem = MenuThreeFragment.MenuItem(item.name!!, item.price!!)
            menuList.add(menuItem)
        }

        val MenurecyclerView = binding3.recyclerMenu
        menuAdapter = MenuThreeAdapter(menuList, this, this)
        MenurecyclerView.adapter = menuAdapter
        MenurecyclerView.layoutManager = GridLayoutManager(context, 4)
    }

    private fun OptionThreeFetch() {

        db.collection("Option3")
            .get()
            .addOnSuccessListener { result ->
                val optionList = mutableListOf<OptionItem>()
                val OptionrecyclerView = binding3.recyclerOption

                for (document in result) {
                    val optionData = document.toObject(optionData3::class.java)
                    optionList.add(OptionItem(optionData.optionItem3, optionData.optionPrice3))
                }
                optionAdapter = OptionThreeAdapter(optionList, this) { position ->
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
        currentMenuPrice = menuItem.price3
        binding3.orderMenu3Total.text = "%,d원".format(currentMenuPrice)
        binding3.drawerMenu3.openDrawer(GravityCompat.END)
        binding3.option3ItemName.text = menuItem.item3
        binding3.option3ItemText.text = OrderLIst.order.find { it.name == menuItem.item3 }?.content
        Glide.with(this)
            .load(OrderLIst.order.find { it.name == menuItem.item3 }?.image)
            .into(binding3.option3ItemPic)
    }

    override fun onOptionItemClick(optionItem3: OptionItem, position: Int) {
        if (!selectedOptionItem.equals(optionItem3)) {
            selectedOptionItem.add(optionItem3)
        } else {
            selectedOptionItem.remove(optionItem3)
        }
    }

    private fun updatePrice(optionList: MutableList<OptionItem>) {
        var totalOptionPrice = 0

        for (option in optionList) {
            if (option.isChecked) {
                totalOptionPrice += option.optionPrice3
            }
        }
        val currentMenuPrice = "%,d".format(selectedItem?.price3 ?: 0).replace(",", "").toInt()
        binding3.orderMenu3Total.text = "%,d원".format(currentMenuPrice + totalOptionPrice)
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