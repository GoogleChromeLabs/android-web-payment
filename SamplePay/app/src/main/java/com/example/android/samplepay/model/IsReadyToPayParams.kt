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

data class IsReadyToPayParams(
    /**
     * The names of the methods being queried. The elements are the keys in [methodData] dictionary.
     */
    val methodNames: List<String>,

    /**
     * A mapping from each [methodName][methodNames] to the
     * [methodData](https://w3c.github.io/payment-request/#declaring-multiple-ways-of-paying).
     */
    val methodData: Map<String, String>,

    /**
     * The schemeless origin of the top-level browsing context. For example,
     * `https://mystore.com/checkout` will be passed as `mystore.com`.
     */
    val topLevelOrigin: String,

    /**
     * The certificate chain of the top-level browsing context. Null for localhost and file on disk,
     * which are both secure contexts without SSL certificates. The certificate chain is necessary
     * because a payment app might have different trust requirements for websites.
     */
    val topLevelCertificateChain: List<ByteArray>,

    /**
     * The schemeless origin of the iframe browsing context that invoked the `new
     * PaymentRequest(methodData, details, options)` constructor in Javascript. If the constructor
     * was invoked from the top-level context, then the value of this parameter equals the value of
     * [topLevelOrigin] parameter.
     */
    val paymentRequestOrigin: String
) {
    companion object {
        fun from(extras: Bundle): IsReadyToPayParams {
            return IsReadyToPayParams(
                methodNames = extras.getStringArrayList("methodNames") ?: emptyList(),
                methodData = extras.getMethodData("methodData"),
                topLevelOrigin = extras.getString("topLevelOrigin", ""),
                topLevelCertificateChain = extras.getCertificateChain("topLevelCertificateChain"),
                paymentRequestOrigin = extras.getString("paymentRequestOrigin", "")
            )
        }
    }
}
