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

data class AddressErrors(
    val addressLines: String?,
    val countryCode: String?,
    val city: String?,
    val dependentLocality: String?,
    val organization: String?,
    val phone: String?,
    val postalCode: String?,
    val recipient: String?,
    val region: String?,
    val sortingCode: String?
) {
    companion object {
        fun from(extras: Bundle): AddressErrors {
            return AddressErrors(
                addressLines = extras.getString("addressLines"),
                countryCode = extras.getString("countryCode"),
                city = extras.getString("city"),
                dependentLocality = extras.getString("dependentLocality"),
                organization = extras.getString("organization"),
                phone = extras.getString("phone"),
                postalCode = extras.getString("postalCode"),
                recipient = extras.getString("recipient"),
                region = extras.getString("region"),
                sortingCode = extras.getString("sortingCode")
            )
        }
    }

    override fun toString(): String {
        var error: String = ""
        if (!addressLines.isNullOrEmpty()) {
            error = addressLines + "\n"
        }
        if (!countryCode.isNullOrEmpty()) {
            error += countryCode + "\n"
        }
        if (!city.isNullOrEmpty()) {
            error += city + "\n"
        }
        if (!dependentLocality.isNullOrEmpty()) {
            error += dependentLocality + "\n"
        }
        if (!organization.isNullOrEmpty()) {
            error += organization + "\n"
        }
        if (!phone.isNullOrEmpty()) {
            error = phone + "\n"
        }
        if (!postalCode.isNullOrEmpty()) {
            error += postalCode + "\n"
        }
        if (!recipient.isNullOrEmpty()) {
            error += recipient + "\n"
        }
        if (!region.isNullOrEmpty()) {
            error += region + "\n"
        }
        if (!sortingCode.isNullOrEmpty()) {
            error += sortingCode + "\n"
        }
        return error
    }
}
