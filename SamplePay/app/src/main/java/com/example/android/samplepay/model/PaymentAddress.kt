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

data class PaymentAddress(
    val id: String,
    val label: String,
    val addressLines: List<String>,
    val countryCode: String,
    val country: String,
    val city: String,
    val dependentLocality: String,
    val organization: String,
    val phone: String,
    val postalCode: String,
    val recipient: String,
    val region: String,
    val sortingCode: String
) {
    fun asBundle(): Bundle {
        val address = Bundle()
        address.putStringArray("addressLines", addressLines.toTypedArray())
        address.putString("countryCode", countryCode)
        address.putString("country", country)
        address.putString("city", city)
        address.putString("dependentLocality", dependentLocality)
        address.putString("organization", organization)
        address.putString("phone", phone)
        address.putString("postalCode", postalCode)
        address.putString("recipient", recipient)
        address.putString("region", region)
        address.putString("sortingCode", sortingCode)
        return address
    }

    override fun toString(): String {
        var address = "$recipient, $organization, "
        addressLines.forEach { addressLine -> address += ("$addressLine, ") }
        val cityLine = if (region.isNotEmpty()) {
            "$city, $region, $postalCode, $country\n"
        } else {
            "$dependentLocality, $city, $postalCode, $country\n"
        }
        address += cityLine
        return address
    }
}
