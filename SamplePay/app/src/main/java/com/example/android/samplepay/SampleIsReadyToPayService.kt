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

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.os.RemoteException
import android.util.Log
import com.example.android.samplepay.model.IsReadyToPayParams
import org.chromium.IsReadyToPayService
import org.chromium.IsReadyToPayServiceCallback

private const val TAG = "IsReadyToPayService"

/**
 * This service handles the IS_READY_TO_PAY action from Chrome.
 */
class SampleIsReadyToPayService : Service() {

    private val binder = object : IsReadyToPayService.Stub() {
        override fun isReadyToPay(callback: IsReadyToPayServiceCallback?) {
            try {
                val callingPackage: String? = packageManager.getNameForUid(Binder.getCallingUid())
                if (packageManager.authorizeCaller(callingPackage, application)) {
                    Log.d(TAG, "The caller is Chrome")
                    callback?.handleIsReadyToPay(true)
                } else {
                    Log.d(TAG, "The caller is not Chrome")
                    callback?.handleIsReadyToPay(false)
                }
            } catch (e: RemoteException) {
                // Ignore
            }
        }
    }

    /**
     * This binder simply returns false for any IS_READY_TO_PAY inquiry.
     */
    private val rejectingBinder = object : IsReadyToPayService.Stub() {
        override fun isReadyToPay(callback: IsReadyToPayServiceCallback?) {
            try {
                val callingPackage = packageManager.getNameForUid(Binder.getCallingUid())
                Log.d(TAG, "Rejecting the call from $callingPackage")
                callback?.handleIsReadyToPay(false)
            } catch (e: RemoteException) {
                // Ignore
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder {
        val extras = intent?.extras ?: return rejectingBinder
        val params = IsReadyToPayParams.from(extras)
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "$params")
        }
        return if (areParametersValid(params)) {
            binder
        } else {
            // Something was wrong in the parameters. We return the binder that always rejects the
            // subsequent inquiry.
            rejectingBinder
        }
    }

    /**
     * @return Whether the provided parameters are valid.
     */
    private fun areParametersValid(params: IsReadyToPayParams): Boolean {
        // Here, you can add more checks to `params` based on your criteria.
        return params.methodNames.size == 1 &&
                params.methodNames[0] == BuildConfig.SAMPLE_PAY_METHOD_NAME
    }
}

