package com.project.kioasktab.Callback

import com.tosspayments.paymentsdk.model.TossPaymentResult

interface PaymentCallback {
    fun onPaymentSuccess(success: TossPaymentResult.Success)
    fun onPaymentFailed(failed: TossPaymentResult.Fail)
}