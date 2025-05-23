/*
 * Copyright 2020 Google LLC
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

package com.example.android.samplepay.model

import android.os.Bundle

data class PaymentOptions(
    val requestPayerName: Boolean = false,
    val requestPayerPhone: Boolean = false,
    val requestPayerEmail: Boolean = false,
    val requestShipping: Boolean = false,
    val shippingType: String = "shipping"
) {
    companion object {
        fun from(extras: Bundle?): PaymentOptions {
            return extras?.let {
                PaymentOptions(requestPayerName = it.getBoolean("requestPayerName", false),
                    requestPayerPhone = it.getBoolean("requestPayerPhone", false),
                    requestPayerEmail = it.getBoolean("requestPayerEmail", false),
                    requestShipping = it.getBoolean("requestShipping", false),
                    shippingType = it.getString("shippingType", "shipping"))
            } ?: PaymentOptions()
        }
    }

    val requireContact: Boolean
        get() = requestPayerName || requestPayerPhone || requestPayerEmail
}
