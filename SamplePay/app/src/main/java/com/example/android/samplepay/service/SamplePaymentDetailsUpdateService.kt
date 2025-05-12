/*
 * Copyright 2025 Google LLC
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

package com.example.android.samplepay.service

import android.app.Service
import android.content.Intent
import android.os.Bundle
import com.example.android.samplepay.ui.ApplicationIdentity
import com.example.android.samplepay.util.getApplicationSignatures
import org.chromium.components.payments.IPaymentDetailsUpdateService
import org.chromium.components.payments.IPaymentDetailsUpdateServiceCallback


/**
 * This service handles the UPDATE_PAYMENT_DETAILS service connection from Chrome.
 */
class SamplePaymentDetailsUpdateService : Service() {

    private var updateService: IPaymentDetailsUpdateService? = null

    private val binder = LocalBinder()

    inner class LocalBinder : IPaymentDetailsUpdateServiceCallback.Stub() {

        private var remoteCallerIdentity: ApplicationIdentity? = null

        override fun updateWith(updatedPaymentDetails: Bundle?) {
            // No-op
        }

        override fun paymentDetailsNotUpdated() {
            // No-op
        }

        override fun setPaymentDetailsUpdateService(service: IPaymentDetailsUpdateService) {
            val callingAppId = packageManager.getNameForUid(getCallingUid())
            remoteCallerIdentity = callingAppId?.let { appId ->
                ApplicationIdentity(appId, packageManager.getApplicationSignatures(appId))
            }

            updateService = service
        }

        fun getUpdateService(appIdentity: ApplicationIdentity): IPaymentDetailsUpdateService? {
            if (remoteCallerIdentity == appIdentity) {
                return updateService
            }

            throw IllegalStateException("""
                Multiple callers are attempting to interact with this payment application.
                The identities of the application initiating the payment and receiving updates don't match.
            """)
        }
    }

    override fun onBind(intent: Intent?) = binder
}