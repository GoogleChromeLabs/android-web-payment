/*
 * Copyright 2019 Google LLC
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

import org.json.JSONException
import org.json.JSONObject

data class PaymentDetails(
    val total: PaymentItem,
    val displayItems: List<PaymentItem>
) {
    companion object {
        fun parse(json: String): PaymentDetails {
            try {
                val builder = Builder()
                val obj = JSONObject(json)
                builder.total = PaymentItem.parse(obj.getJSONObject("total"))
                // We parse this "displayItems" field, but it is usually empty for privacy.
                obj.optJSONArray("displayItems")?.let { displayItems ->
                    for (i in 0 until displayItems.length()) {
                        builder.displayItems.add(PaymentItem.parse(displayItems.getJSONObject(i)))
                    }
                }
                return builder.build()
            } catch (e: JSONException) {
                throw RuntimeException("Cannot parse JSON", e)
            }
        }

    }

    internal class Builder {
        var total: PaymentItem? = null
        val displayItems = mutableListOf<PaymentItem>()
        fun build() = PaymentDetails(total!!, displayItems)
    }
}

data class PaymentItem(
    val label: String,
    val amount: PaymentAmount
) {
    companion object {
        internal fun parse(obj: JSONObject): PaymentItem {
            return PaymentItem(
                obj.getString("label"),
                PaymentAmount.parse(obj.getJSONObject("amount"))
            )
        }
    }
}

data class PaymentAmount(
    val currency: String,
    val value: String
) {
    companion object {
        fun parse(json: String): PaymentAmount {
            try {
                return parse(JSONObject(json))
            } catch (e: JSONException) {
                throw RuntimeException("Cannot parse JSON", e)
            }
        }

        internal fun parse(obj: JSONObject): PaymentAmount {
            return PaymentAmount(
                obj.getString("currency"),
                obj.getString("value")
            )
        }
    }
}
