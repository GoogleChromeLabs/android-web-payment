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
import androidx.lifecycle.MutableLiveData
import com.example.android.samplepay.model.PaymentAmount
import com.example.android.samplepay.model.PaymentParams

private const val TAG = "PaymentViewModel"

class PaymentViewModel(application: Application) : AndroidViewModel(application) {

    private val _origin = MutableLiveData<String>()
    private val _merchantName = MutableLiveData<String>()
    private val _error = MutableLiveData<String>()
    private val _promotionError = MutableLiveData<String>()
    private val _total = MutableLiveData<PaymentAmount?>()
    private val _payerName = MutableLiveData<Boolean>()
    private val _payerPhone = MutableLiveData<Boolean>()
    private val _payerEmail = MutableLiveData<Boolean>()
    private val _shipping = MutableLiveData<Boolean>()

    val origin: LiveData<String?> = _origin
    val merchantName: LiveData<String?> = _merchantName
    val error: LiveData<String> = _error
    val promotionError: LiveData<String> = _promotionError
    val total: LiveData<PaymentAmount?> = _total
    var payerName: MutableLiveData<Boolean> = _payerName
    var payerPhone: MutableLiveData<Boolean> = _payerPhone
    var payerEmail: MutableLiveData<Boolean> = _payerEmail
    var shipping: MutableLiveData<Boolean> = _shipping

    fun initialize(params: PaymentParams, error: String?, promotionError: String) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "$params")
        }
        _error.value = error
        _promotionError.value = promotionError
        _total.value = params.total
        _origin.value = params.topLevelOrigin
        _merchantName.value = params.merchantName
        _payerName.value = params.paymentOptions.requestPayerName
        _payerPhone.value = params.paymentOptions.requestPayerPhone
        _payerEmail.value = params.paymentOptions.requestPayerEmail
        _shipping.value = params.paymentOptions.requestShipping
    }
}
