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
import android.os.RemoteException
import android.util.Log
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
                if (application.authorizeCaller(callingPackage)) {
                    Log.d(TAG, "The caller is Chrome")
                } else {
                    Log.d(TAG, "The caller is not Chrome")
                }
                // Allow non-Chrome callers.
                callback?.handleIsReadyToPay(true)
            } catch (e: RemoteException) {
                // Ignore
            }
        }
    }

    override fun onBind(intent: Intent?) = binder
}
