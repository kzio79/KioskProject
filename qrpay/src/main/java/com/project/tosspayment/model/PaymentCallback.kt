package com.project.tosspayment.model

import com.tosspayments.paymentsdk.model.TossPaymentResult

interface PaymentCallback {
    fun onPaymentSuccess(success: TossPaymentResult.Success)
    fun onPaymentFailed(failed: TossPaymentResult.Fail)
}