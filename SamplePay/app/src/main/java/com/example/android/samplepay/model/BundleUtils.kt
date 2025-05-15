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

import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import org.json.JSONException

@Suppress("DEPRECATION", "UNCHECKED_CAST")
private fun Bundle.getBundleArray(key: String): List<Bundle>? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelableArray(key, Bundle::class.java)?.toList()
    } else {
        getParcelableArray(key)?.map {it as Bundle}
    }
}

internal fun Bundle.getPaymentAmount(key: String): PaymentAmount? {
    val s = getString(key)
    return if (s != null) {
        try {
            PaymentAmount.parse(s)
        } catch (e: JSONException) {
            e.printStackTrace()
            null
        }
    } else {
        null
    }
}

internal fun Bundle.getShippingOptions(key: String): List<ShippingOption> {
    return getBundleArray(key)?.map(ShippingOption::from) ?: emptyList()
}

internal fun Bundle.getMethodData(key: String): Map<String, String> {
    val b = getBundle(key) ?: return emptyMap()
    return b.keySet().associateWith { b.getString(it, "[]") }
}

internal fun SavedStateHandle.getMethodData(key: String): Map<String, String> {
    val b = get<Bundle>(key)
    return b?.keySet()?.associateWith { b.getString(it, "[]") } ?: emptyMap()
}

internal fun SavedStateHandle.getShippingOptions(key: String): List<ShippingOption> {
    return get<Array<Parcelable>>(key)?.map { ShippingOption.from(it as Bundle) } ?: emptyList()
}