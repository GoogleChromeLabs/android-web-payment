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

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.MutableCreationExtras
import com.example.android.samplepay.R
import com.example.android.samplepay.util.overrideCloseTransition
import com.example.android.samplepay.util.overrideOpenTransition

class MainActivity : ComponentActivity() {

    private val viewModel: PaymentViewModel by viewModels(
        factoryProducer = { PaymentViewModel.Factory },
        extrasProducer = {
            MutableCreationExtras(defaultViewModelCreationExtras).apply {
                set(PaymentViewModel.CALLING_PACKAGE_KEY, callingPackage)
            }
        })

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Refrain from taking action if the intent data is inappropriate
        if ("org.chromium.intent.action.PAY" == intent.action && intent.extras == null) {
            cancel()
        }

        setContent {
            val payIntent by viewModel.paymentIntent.collectAsStateWithLifecycle()
            val payResult by viewModel.paymentResult.collectAsStateWithLifecycle()
            val openErrorDialog = rememberSaveable { mutableStateOf(false) }

            PaymentApp(
                payIntent = payIntent,
                openErrorDialog = openErrorDialog.value,
                errorMessage = messageResourceForResult(payResult)
                    ?.let { stringResource(it) }
                    .orEmpty(),
                onErrorDismissed = {
                    openErrorDialog.value = false
                    cancel()
                },
                onAddPromoCode = viewModel::applyPromotionCode,
                onShippingOptionChange = viewModel::updateShippingOption,
                onShippingAddressChange = viewModel::updateShippingAddress,
                onPayButtonClicked = viewModel::pay
            )

            when (payResult) {
                PaymentResult.None -> {}
                is PaymentResult.ResultIntent -> {
                    setResult(RESULT_OK, (payResult as PaymentResult.ResultIntent).intent)
                    finish()
                }

                is PaymentResult.Error, PaymentResult.UnsupportedCaller -> {
                    openErrorDialog.value = true
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        overrideOpenTransition(R.anim.slide_from_bottom, R.anim.slide_to_bottom_20)
    }

    override fun onPause() {
        super.onPause()
        overrideCloseTransition(R.anim.slide_from_bottom_20, R.anim.slide_to_bottom)
    }

    private fun cancel() {
        setResult(RESULT_CANCELED)
        finish()
    }

    private fun messageResourceForResult(result: PaymentResult) = when (result) {
        is PaymentResult.Error -> R.string.error_multiple_callers
        is PaymentResult.UnsupportedCaller -> R.string.error_caller_not_supported
        else -> null
    }
}