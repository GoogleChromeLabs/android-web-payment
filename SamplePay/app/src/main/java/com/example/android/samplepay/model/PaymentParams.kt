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

package com.example.android.samplepay.model

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle

/**
 * Represents all the parameters passed to the activity for the PAY action.
 */
data class PaymentParams(
    /**
     * The names of the methods being used. The elements are the keys in the [methodData]
     * dictionary, and indicate the methods that the payment app supports.
     */
    val methodNames: List<String>,

    /**
     * A mapping from each [methodName][methodNames] to the
     * [methodData](https://w3c.github.io/payment-request/#declaring-multiple-ways-of-paying).
     */
    val methodData: Map<String, String>,

    /**
     * The contents of the `<title>` HTML tag of the top-level browsing context on the checkout web
     * page.
     */
    val merchantName: String,

    /**
     * The schemeless origin of the top-level browsing context. For example,
     * `https://mystore.com/checkout` will be passed as `mystore.com`.
     */
    val topLevelOrigin: String,

    /**
     * The schemeless origin of the iframe browsing context that invoked the `new
     * PaymentRequest(methodData, details, options)` constructor in Javascript. If the constructor
     * was invoked from the top-level context, then the value of this parameter equals the value of
     * [topLevelOrigin] parameter.
     */
    val paymentRequestOrigin: String,

    /**
     * The total amount of the checkout.
     */
    val total: PaymentAmount?,

    /**
     * The output of JSON.stringify(details.modifiers), where details.modifiers contain only
     * supportedMethods and total.
     */
    val modifiers: String,

    /**
     * The [PaymentRequest.id](https://w3c.github.io/payment-request/#id-attribute) field that
     * “push-payment” apps should associate with transaction state. Merchant websites will use
     * this field to query the “push-payment” apps for the state of transaction out of band.
     */
    val paymentRequestId: String,

    /**
     * The additional information requested by the merchant.
     */
    val paymentOptions: PaymentOptions,

    /**
     * Merchant specified shipping options; will be non-null whenever shipping is requested.
     */
    val shippingOptions: List<ShippingOption>
) {
    companion object {
        fun from(extras: Bundle) = PaymentParams(
                methodNames = extras.getStringArrayList("methodNames") ?: emptyList(),
                methodData = extras.getMethodData("methodData"),
                merchantName = extras.getString("merchantName", ""),
                topLevelOrigin = extras.getString("topLevelOrigin", ""),
                paymentRequestOrigin = extras.getString("paymentRequestOrigin", ""),
                total = extras.getPaymentAmount("total"),
                modifiers = extras.getString("modifiers", "[]"),
                paymentRequestId = extras.getString("paymentRequestId", ""),
                paymentOptions = PaymentOptions.from(extras.getBundle("paymentOptions")),
                shippingOptions = extras.getShippingOptions("shippingOptions")
            )

        fun from(state: SavedStateHandle) =
            PaymentParams(
                methodNames = state["methodNames"] ?: emptyList(),
                methodData = state.getMethodData("methodData"),
                merchantName = state["merchantName"] ?: "",
                topLevelOrigin = state["topLevelOrigin"] ?: "",
                paymentRequestOrigin = state["paymentRequestOrigin"] ?: "",
                total = state.get<String>("total")?.let(PaymentAmount::parse),
                modifiers = state["modifiers"] ?: "[]",
                paymentRequestId = state["paymentRequestId"] ?: "",
                paymentOptions = PaymentOptions.from(state["paymentOptions"]),
                shippingOptions = state.getShippingOptions("shippingOptions")
            )
    }
}
