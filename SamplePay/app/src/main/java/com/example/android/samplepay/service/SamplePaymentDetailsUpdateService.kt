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


/** A service to handle the UPDATE_PAYMENT_DETAILS service connection from Chrome. */
class SamplePaymentDetailsUpdateService : Service() {

    /**
     * The service received from the caller to send payment updates back and update the
     * original checkout flow.
     */
    private var updateService: IPaymentDetailsUpdateService? = null

    /** A local binder to connect from the payment application and fetch the remote service. */
    private val binder = LocalBinder()

    inner class LocalBinder : IPaymentDetailsUpdateServiceCallback.Stub() {

        /**
         * The identity of the remote caller is registered at creation time, and verified against
         * identity of the Web application initiating the payment process.
         */
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

        /**
         * Retrieves the remote service so that the local payment app can use it as a channel to
         * report checkout changes in the Web application.
         *
         * @param appIdentity the identity that initiated the payment process.
         * @return the remote service to send updates back to the Web application.
         * @throws IllegalStateException if the identity that initiated the payment and delivered
         *     the service don't match.
         */
        fun getUpdateService(appIdentity: ApplicationIdentity): IPaymentDetailsUpdateService? {
            if (remoteCallerIdentity == appIdentity) {
                return updateService
            }

            throw IllegalStateException("""
                |Multiple callers are attempting to interact with this payment application.
                |The identities of the application initiating the payment and receiving updates
                |don't match.
            """.trimMargin()
            )
        }
    }

    override fun onBind(intent: Intent?) = binder
}