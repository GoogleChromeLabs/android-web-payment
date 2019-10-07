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

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.android.samplepay.model.PaymentAmount
import com.example.android.samplepay.model.PaymentParams

private const val TAG = "PaymentViewModel"

class PaymentViewModel : ViewModel() {

    private val _origin = MutableLiveData<String>()
    private val _merchantName = MutableLiveData<String>()
    private val _total = MutableLiveData<PaymentAmount?>()

    val total: LiveData<PaymentAmount?> = _total
    val origin: LiveData<String?> = _origin
    val merchantName: LiveData<String?> = _merchantName

    fun initialize(extras: Bundle) {
        val params = PaymentParams.from(extras)
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "$params")
        }
        _total.value = params.total
        _origin.value = params.topLevelOrigin
        _merchantName.value = params.merchantName
    }
}
