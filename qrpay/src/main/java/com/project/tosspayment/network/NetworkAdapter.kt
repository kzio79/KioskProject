package com.project.tosspayment.network

import retrofit2.CallAdapter
import retrofit2.Retrofit
import java.lang.reflect.Type

class NetworkAdapter: CallAdapter.Factory() {
    override fun get(
        returnType: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): CallAdapter<*, *>? {
        TODO("Not yet implemented")
    }
}