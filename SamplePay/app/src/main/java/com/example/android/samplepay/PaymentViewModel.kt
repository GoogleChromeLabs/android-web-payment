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

package com.example.android.samplepay

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.example.android.samplepay.model.*

private const val TAG = "PaymentViewModel"

class PaymentViewModel(application: Application) : AndroidViewModel(application) {

    private val _origin = MutableLiveData<String>()
    private val _merchantName = MutableLiveData<String>()
    private val _error = MutableLiveData<String>()
    private val _addressErrors = MutableLiveData<AddressErrors>()
    private val _promotionError = MutableLiveData<String>()
    private val _total = MutableLiveData<PaymentAmount?>()
    private val _paymentOptions = MutableLiveData<PaymentOptions>()
    private val _shippingOptions = MutableLiveData<List<ShippingOption>>()

    val origin: LiveData<String?> = _origin
    val merchantName: LiveData<String?> = _merchantName

    val error: LiveData<String> = combine(_error, _addressErrors) { e, ae ->
        buildString {
            append(e)
            if (ae != null) {
                appendLine()
                append(ae.toString())
            }
        }
    }

    val promotionError: LiveData<String> = _promotionError
    val total: LiveData<PaymentAmount?> = _total
    val paymentOptions: LiveData<PaymentOptions> = _paymentOptions
    val shippingOptions: LiveData<List<ShippingOption>> = _shippingOptions
    val paymentAddresses: LiveData<Map<Int, PaymentAddress>> = MutableLiveData(
        mapOf(
            R.id.canada_address to
                    PaymentAddress(
                        listOf("111 Richmond st. West #12"),
                        "CA",
                        "Canada",
                        "Toronto",
                        "",
                        "Google",
                        "+14169158200",
                        "M5H2G4",
                        "John Smith",
                        "Ontario",
                        ""
                    ),
            R.id.us_address to
                    PaymentAddress(
                        listOf("1875 Explorer St #1000"),
                        "US",
                        "United States",
                        "Reston",
                        "",
                        "Google",
                        "+12023705600",
                        "20190",
                        "John Smith",
                        "Virginia",
                        ""
                    ),
            R.id.uk_address to
                    PaymentAddress(
                        listOf("1-13 St Giles High St"),
                        "UK",
                        "United Kingdom",
                        "London",
                        "West End",
                        "Google",
                        "+442070313000",
                        "WC2H 8AG",
                        "John Smith",
                        "",
                        ""
                    )
        )
    )

    fun initialize(params: PaymentParams, callingPackage: String?) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "$params")
        }
        val application: Application = getApplication()
        _error.value = if (application.authorizeCaller(callingPackage)) {
            ""
        } else {
            application.getString(R.string.error_caller_not_chrome)
        }
        _total.value = params.total
        _origin.value = params.topLevelOrigin
        _merchantName.value = params.merchantName
        _paymentOptions.value = params.paymentOptions
        _shippingOptions.value = params.shippingOptions
    }

    fun updateShippingOptions(shippingOptions: List<ShippingOption>) {
        _shippingOptions.value = shippingOptions
    }

    fun updateTotal(total: PaymentAmount) {
        _total.value = total
    }

    fun updateError(error: String) {
        _error.value = error
    }

    fun updateAddressErrors(addressErrors: AddressErrors) {
        _addressErrors.value = addressErrors
    }

    fun updatePromotionError(promotionError: String) {
        _promotionError.value = promotionError
    }
}
