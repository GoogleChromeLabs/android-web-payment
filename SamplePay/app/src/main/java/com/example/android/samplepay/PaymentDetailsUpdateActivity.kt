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
    private var mPromotionCode: String? = null
    private var mSelectedOptionId: String? = null
    private lateinit var mSelectedAddress: Bundle
    private var mUpdatedPaymentDetails: Bundle? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mPromotionCode = intent.getStringExtra("promotionCode")
        mSelectedOptionId = intent.getStringExtra("selectedOptionId")
        mSelectedAddress = intent.getBundleExtra("selectedAddress") ?: Bundle()
        logIfDebug("Payment details update activity started")
        bind()
    }

    private val mCallback: IPaymentDetailsUpdateServiceCallback =
        object : IPaymentDetailsUpdateServiceCallback.Stub() {
            override fun paymentDetailsNotUpdated() {
                logIfDebug("Payment details did not change.")
                unbind()
            }

            override fun updateWith(updatedPaymentDetails: Bundle) {
                mUpdatedPaymentDetails = updatedPaymentDetails
                unbind()
            }
        }

    private val mConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val paymentDetailsUpdateService = IPaymentDetailsUpdateService.Stub.asInterface(service)
            try {
                when {
                    mPromotionCode != null -> {
                        logIfDebug("changePaymentMethod called.")
                        val methodData = Bundle()
                        methodData.putString(
                            "methodName", "https://sample-pay-web-app.firebaseapp.com"
                        )
                        val details = JSONObject()
                        details.put("promotionCode", mPromotionCode)
                        methodData.putString("details", details.toString())
                        paymentDetailsUpdateService?.changePaymentMethod(methodData, mCallback)
                    }
                    mSelectedOptionId != null -> {
                        logIfDebug("changeShippingOption called.")
                        paymentDetailsUpdateService?.changeShippingOption(
                            mSelectedOptionId, mCallback
                        )
                    }
                    else -> {
                        logIfDebug("changeShippingAddress called.")
                        paymentDetailsUpdateService?.changeShippingAddress(
                            mSelectedAddress, mCallback
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
        val intent = Intent()
        intent.setClassName(
            "org.chromium.chrome", "org.chromium.components.payments.PaymentDetailsUpdateService"
        )
        intent.action = IPaymentDetailsUpdateService::class.java.name
        isBound = bindService(intent, mConnection, Context.BIND_AUTO_CREATE)
    }

    private fun unbind() {
        if (isBound) {
            unbindService(mConnection)
            isBound = false
        }
        setResult(RESULT_OK, Intent().apply {
            if (mUpdatedPaymentDetails != null) {
                putExtra("updatedPaymentParams", mUpdatedPaymentDetails)
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