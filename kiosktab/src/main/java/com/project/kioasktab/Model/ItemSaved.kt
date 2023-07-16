package com.project.kioask.model

data class ItemModel(
    var code: String,
    var name: String?,
    var image: String?,
    var content: String?,
    var price: Int?,
    var state: Boolean = false

)

data class orderModel(
//    var orderid: String,
//    var ordername: String,
//    var totalprice: Int
    var orderid:String,
    var ordername: String,
    var totalprice: Int,
    var paymentkey: String,
    var orderstate: Int
)

object OrderLIst {
    val order: MutableList<ItemModel> = mutableListOf()
}