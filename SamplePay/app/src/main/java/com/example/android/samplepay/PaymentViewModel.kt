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
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.example.android.samplepay.model.PaymentDetails

class PaymentViewModel : ViewModel() {

    private val _details = MutableLiveData<PaymentDetails>()
    private val _origin = MutableLiveData<String>()
    private val _merchantName = MutableLiveData<String>()

    val totalAmount = _details.map { it?.total?.amount }
    val origin: LiveData<String?>
        get() = _origin
    val merchantName: LiveData<String?>
        get() = _merchantName

    fun initialize(extras: Bundle) {
        val detailsJson = extras.getString("details")
        if (detailsJson == null) {

        } else {
            _details.value = PaymentDetails.parse(detailsJson)
        }
        _origin.value = extras.getString("origin")
        _merchantName.value = extras.getString("merchantName")
    }
}
