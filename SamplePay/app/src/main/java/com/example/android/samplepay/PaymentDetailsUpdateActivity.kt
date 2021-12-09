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

package com.example.android.samplepay

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import android.os.RemoteException
import android.util.Log
import org.chromium.components.payments.IPaymentDetailsUpdateService
import org.chromium.components.payments.IPaymentDetailsUpdateServiceCallback
import org.json.JSONObject

private const val TAG = "PaymentDetailsUpdate"

class PaymentDetailsUpdateActivity : Activity() {
    private var isBound: Boolean = false
    private lateinit var callingBrowserPackage: String
    private var promotionCode: String? = null
    private var selectedOptionId: String? = null
    private lateinit var selectedAddress: Bundle
    private var updatedPaymentDetails: Bundle? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        callingBrowserPackage = intent.getStringExtra("callingBrowserPackage")
        promotionCode = intent.getStringExtra("promotionCode")
        selectedOptionId = intent.getStringExtra("selectedOptionId")
        selectedAddress = intent.getBundleExtra("selectedAddress") ?: Bundle()
        logIfDebug("Payment details update activity started")
        bind()
    }

    private val callback: IPaymentDetailsUpdateServiceCallback =
        object : IPaymentDetailsUpdateServiceCallback.Stub() {
            override fun paymentDetailsNotUpdated() {
                logIfDebug("Payment details did not change.")
                unbind()
            }

            override fun updateWith(newPaymentDetails: Bundle) {
                updatedPaymentDetails = newPaymentDetails
                unbind()
            }
        }

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val paymentDetailsUpdateService = IPaymentDetailsUpdateService.Stub.asInterface(service)
            try {
                when {
                    promotionCode != null -> {
                        logIfDebug("changePaymentMethod called.")
                        val methodData = Bundle()
                        methodData.putString(
                            "methodName", "https://sample-pay-web-app.firebaseapp.com"
                        )
                        val details = JSONObject()
                        details.put("promotionCode", promotionCode)
                        methodData.putString("details", details.toString())
                        paymentDetailsUpdateService?.changePaymentMethod(methodData, callback)
                    }
                    selectedOptionId != null -> {
                        logIfDebug("changeShippingOption called.")
                        paymentDetailsUpdateService?.changeShippingOption(
                            selectedOptionId, callback
                        )
                    }
                    else -> {
                        logIfDebug("changeShippingAddress called.")
                        paymentDetailsUpdateService?.changeShippingAddress(
                            selectedAddress, callback
                        )
                    }
                }
            } catch (e: RemoteException) {
                logIfDebug(e.toString())
            }
        }

        override fun onServiceDisconnected(className: ComponentName) {
            logIfDebug("PaymentDetailsUpdateService unexpectedly disconnected.")
        }
    }

    private fun bind() {
        // The action is introduced in Chrome version 92, which supports the service in Chrome
        // and other browsers (e.g., WebLayer).
        val newIntent = Intent("org.chromium.intent.action.UPDATE_PAYMENT_DETAILS")
            .setPackage(callingBrowserPackage)
        if (packageManager.resolveService(newIntent, PackageManager.GET_RESOLVED_FILTER) == null) {
            // Fallback to Chrome-only approach.
            newIntent.setClassName(
                callingBrowserPackage,
                "org.chromium.components.payments.PaymentDetailsUpdateService")
            newIntent.action = IPaymentDetailsUpdateService::class.java.name
        }
        isBound = bindService(newIntent, connection, Context.BIND_AUTO_CREATE)
    }

    private fun unbind() {
        if (isBound) {
            unbindService(connection)
            isBound = false
        }
        setResult(RESULT_OK, Intent().apply {
            if (updatedPaymentDetails != null) {
                putExtra("updatedPaymentParams", updatedPaymentDetails)
            }
        })
        finish()
    }

    private fun logIfDebug(message: String) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, message)
        }
    }
}
