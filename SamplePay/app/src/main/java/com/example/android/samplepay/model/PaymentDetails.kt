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

import android.os.Bundle
import org.json.JSONException
import org.json.JSONObject

data class PaymentAmount(
    val currency: String, val value: String
) {
    companion object {
        fun parse(json: String): PaymentAmount {
            try {
                val obj = JSONObject(json)
                return PaymentAmount(
                    obj.getString("currency"), obj.getString("value")
                )
            } catch (e: JSONException) {
                throw RuntimeException("Cannot parse JSON", e)
            }
        }

        fun from(extras: Bundle): PaymentAmount {
            return PaymentAmount(
                currency = extras.getString("currency")!!, value = extras.getString("value")!!
            )
        }
    }
}
