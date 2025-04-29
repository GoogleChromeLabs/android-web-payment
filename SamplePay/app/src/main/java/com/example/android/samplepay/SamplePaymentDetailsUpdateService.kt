/*
 * Copyright 2025 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.samplepay

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.util.Log
import com.example.android.samplepay.model.PaymentDetailsUpdate
import org.chromium.components.payments.IPaymentDetailsUpdateService
import org.chromium.components.payments.IPaymentDetailsUpdateServiceCallback

private const val TAG = "PaymentDetailsUpdateService"

/**
 * This service handles the UPDATE_PAYMENT_DETAILS service connection from Chrome.
 */
class SamplePaymentDetailsUpdateService : Service() {

    companion object {
      var connectedService: IPaymentDetailsUpdateService?
      var viewModel: PaymentViewModel?
    }

    private val binder = object : IPaymentDetailsUpdateServiceCallback.Stub() {
        override fun paymentDetailsNotUpdated() {
            Log.d("TAG", "Payment details did not change.")
        }

        override fun updateWith(newPaymentDetails: Bundle) {
            Log.d("TAG", "Payment details changed.")
            val update = PaymentDetailsUpdate.from(newPaymentDetails)
            runOnUiThread {
                update.shippingOptions?.let { viewModel.updateShippingOptions(it) }
                update.total?.let { viewModel.updateTotal(it) }
                viewModel.updateError(update.error ?: "")
                update.addressErrors?.let { viewModel.updateAddressErrors(it) }
                viewModel.updatePromotionError(update.stringifiedPaymentMethodErrors ?: "")
            }
        }

        override fun setPaymentDetailsUpdateService(service: IPaymentDetailsUpdateService) {
            Log.d("TAG", "Payment details service connected.")
            connectedService = service;
        }
    }

    override fun onBind(intent: Intent?) = binder
}
