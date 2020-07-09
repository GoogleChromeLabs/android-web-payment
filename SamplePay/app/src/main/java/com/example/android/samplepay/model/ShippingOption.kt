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

data class ShippingOption(
    val id: String,
    val label: String,
    var amountCurrency: String,
    var amountValue: String,
    val selected: Boolean
) {
    companion object {
        fun from(extras: Bundle): ShippingOption {
            return ShippingOption(
                id = extras.getString("id", ""),
                label = extras.getString("label", ""),
                amountCurrency = extras.getBundle("amount")!!.getString("currency", ""),
                amountValue = extras.getBundle("amount")!!.getString("value", ""),
                selected = extras.getBoolean("selected", false)
            )
        }
    }
}
