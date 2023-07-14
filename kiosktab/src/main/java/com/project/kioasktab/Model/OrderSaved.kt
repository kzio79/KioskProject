package com.project.kioasktab

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class OrderData(
    val item: String,
    var price: Int,
    var optionItem: String,
    var optionPrice: Int,
    var count: Int
) : Parcelable

object OrderSaved {
    val orders: MutableList<OrderData> = mutableListOf()
}