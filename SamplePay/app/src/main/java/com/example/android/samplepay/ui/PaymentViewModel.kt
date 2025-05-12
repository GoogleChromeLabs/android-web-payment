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

package com.example.android.samplepay.ui

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.Signature
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.android.samplepay.BuildConfig
import com.example.android.samplepay.model.PaymentAddress
import com.example.android.samplepay.model.PaymentAmount
import com.example.android.samplepay.model.PaymentDetailsUpdate
import com.example.android.samplepay.model.PaymentOptions
import com.example.android.samplepay.model.PaymentParams
import com.example.android.samplepay.model.ShippingOption
import com.example.android.samplepay.service.SamplePaymentDetailsUpdateService
import com.example.android.samplepay.util.getApplicationSignatures
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.chromium.components.payments.IPaymentDetailsUpdateService
import org.chromium.components.payments.IPaymentDetailsUpdateServiceCallback
import org.json.JSONObject

private const val TAG = "PaymentViewModel"

class PaymentViewModel(
    private val application: Application,
    private val state: SavedStateHandle,
    private val callingPackage: String?
) : AndroidViewModel(application) {

    // Define view model factory to include the calling package
    companion object {
        val CALLING_PACKAGE_KEY = object : CreationExtras.Key<String?> {}
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as Application
                val callingPackage = this[CALLING_PACKAGE_KEY]
                PaymentViewModel(
                    application = application,
                    state = createSavedStateHandle(),
                    callingPackage = callingPackage
                )
            }
        }
    }

    private val _paymentIntent: MutableStateFlow<PaymentIntent> =
        MutableStateFlow(PaymentIntent.None)
    val paymentIntent: StateFlow<PaymentIntent> = _paymentIntent.asStateFlow()

    private val _paymentResult: MutableStateFlow<PaymentResult> =
        MutableStateFlow(PaymentResult.None)
    val paymentResult: StateFlow<PaymentResult> = _paymentResult.asStateFlow()

    private var paymentDetailsUpdateService: IPaymentDetailsUpdateService? = null

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            if (_paymentIntent.value is PaymentIntent.Started) {
                val binder = service as SamplePaymentDetailsUpdateService.LocalBinder
                try {
                    paymentDetailsUpdateService =
                        binder.getUpdateService((_paymentIntent.value as PaymentIntent.Started).callingIdentity)

                } catch (ise: IllegalStateException) {
                    _paymentResult.update { PaymentResult.Error(ise) }
                }
            }
        }

        override fun onServiceDisconnected(className: ComponentName?) {
            paymentDetailsUpdateService = null
        }
    }

    private val updatePaymentCallback = object : IPaymentDetailsUpdateServiceCallback.Stub() {
        override fun paymentDetailsNotUpdated() {
            Log.d(TAG, "Payment details did not change.")
        }

        override fun updateWith(newPaymentDetails: Bundle) {
            Log.d(TAG, "Payment details changed.")

            viewModelScope.launch {
                if (_paymentIntent.value is PaymentIntent.Started) {
                    val updatedDetails = PaymentDetailsUpdate.from(newPaymentDetails)
                    _paymentIntent.update {
                        (it as PaymentIntent.Started).copy(
                            shippingOptions = updatedDetails.shippingOptions!!,
                            amount = updatedDetails.total,
                            promotionCodeErrorText = updatedDetails.stringifiedPaymentMethodErrors,
                            errorText = updatedDetails.error?.let { e ->
                                buildString {
                                    append(e)
                                    updatedDetails.addressErrors?.let { ae ->
                                        appendLine()
                                        append(ae.toString())
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }

        override fun setPaymentDetailsUpdateService(service: IPaymentDetailsUpdateService?) {
            // No-op
        }
    }

    init {
        // Analyze the contents of the intent and initiate the payment process
        if (callingPackage != null && state.contains("paymentOptions")) {
            val (_, _, merchantName, merchantOrigin, _, amount, _, _, paymentOptions, shippingOptions) = PaymentParams.from(
                state
            )

            val callingIdentity = callingPackage.let {
                val signatures = application.packageManager.getApplicationSignatures(it)
                ApplicationIdentity(it, signatures)
            }

            _paymentIntent.update {
                PaymentIntent.Started(
                    callingIdentity = callingIdentity,
                    merchantName = merchantName,
                    merchantOrigin = merchantOrigin,
                    amount = amount,
                    paymentOptions = paymentOptions,
                    shippingOptions = shippingOptions,
                    defaultShippingOptionId = shippingOptions.find { it.selected }?.id,
                    paymentAddresses = paymentAddresses,
                )
            }
        }

        // Start service to listen to payment update changes
        Intent(application, SamplePaymentDetailsUpdateService::class.java).also { intent ->
            application.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    fun applyPromotionCode(promotionCode: String) {
        val bundle = Bundle().apply {
            putString("methodName", BuildConfig.SAMPLE_PAY_METHOD_NAME)
            putString("details", JSONObject().apply {
                put("promotionCode", promotionCode)
            }.toString())
        }
        paymentDetailsUpdateService?.changePaymentMethod(bundle, updatePaymentCallback)
    }

    fun updateShippingOption(shippingOptionId: String) {
        paymentDetailsUpdateService?.changeShippingOption(shippingOptionId, updatePaymentCallback)
    }

    fun updateShippingAddress(shippingAddressId: String) {
        paymentDetailsUpdateService?.changeShippingAddress(
            paymentAddresses.find { it.id == shippingAddressId }!!.asBundle(), updatePaymentCallback
        )
    }

    fun pay(paymentInfo: PaymentFormInfo) {
        val paymentOptions = (_paymentIntent.value as PaymentIntent.Started).paymentOptions

        _paymentResult.update {
            PaymentResult.ResultIntent(Intent().apply {
                putExtra("methodName", BuildConfig.SAMPLE_PAY_METHOD_NAME)
                putExtra("details", "{\"token\": \"put-some-data-here\"}")

                val (name, phoneNumber, emailAddress) = paymentInfo.contactInfo
                if (paymentOptions.requestPayerName) {
                    putExtra("payerName", name)
                }

                if (paymentOptions.requestPayerPhone) {
                    putExtra("payerPhone", phoneNumber)
                }

                if (paymentOptions.requestPayerEmail) {
                    putExtra("payerEmail", emailAddress)
                }

                if (paymentOptions.requestShipping) {
                    val shippingAddress =
                        paymentAddresses.find { it.id == paymentInfo.shippingAddress }?.asBundle()
                    putExtra("shippingAddress", shippingAddress)
                    putExtra("shippingOptionId", paymentInfo.shippingOption)
                }

                putExtra("promoCode", paymentInfo.promotionCode)

                if (BuildConfig.DEBUG) {
                    Log.d(TAG, extras.toString())
                }
            })
        }
    }

    override fun onCleared() {
        if (paymentDetailsUpdateService != null) {
            application.applicationContext.unbindService(connection)
        }
    }
}

abstract class PaymentIntent {
    data object None : PaymentIntent()
    data class Started(
        val callingIdentity: ApplicationIdentity,
        val merchantName: String?,
        val merchantOrigin: String?,
        val errorText: String? = null,
        val promotionCodeErrorText: String? = null,
        val amount: PaymentAmount?,
        val paymentOptions: PaymentOptions,
        val shippingOptions: List<ShippingOption>,
        val defaultShippingOptionId: String?,
        val paymentAddresses: List<PaymentAddress>
    ) : PaymentIntent()
}

sealed class PaymentResult {
    data object None : PaymentResult()
    data class Error(val exception: Exception) : PaymentResult()
    data class ResultIntent(val intent: Intent) : PaymentResult()
}

data class ApplicationIdentity(
    val packageName: String,
    val signatures: List<Signature>
)

val paymentAddresses: List<PaymentAddress> = listOf(
    PaymentAddress(
        "canada_address",
        "Canada",
        listOf("111 Richmond st. West #12"),
        "CA",
        "Canada",
        "Toronto",
        "",
        "Google",
        "+14169158200",
        "M5H2G4",
        "John Smith",
        "Ontario",
        ""

    ), PaymentAddress(
        "us_address",
        "US",
        listOf("1875 Explorer St #1000"),
        "US",
        "United States",
        "Reston",
        "",
        "Google",
        "+12023705600",
        "20190",
        "John Smith",
        "Virginia",
        ""

    ), PaymentAddress(
        "uk_address",
        "UK",
        listOf("1-13 St Giles High St"),
        "UK",
        "United Kingdom",
        "London",
        "West End",
        "Google",
        "+442070313000",
        "WC2H 8AG",
        "John Smith",
        "",
        ""
    )
)