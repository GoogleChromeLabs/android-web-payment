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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.android.samplepay.R
import com.example.android.samplepay.util.overrideCloseTransition
import com.example.android.samplepay.util.overrideOpenTransition

class MainActivity : ComponentActivity() {

    private val viewModel: PaymentViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Refrain from taking action if the intent data is inappropriate
        if ("org.chromium.intent.action.PAY" == intent.action && intent.extras == null) {
            cancel()
        }

        // TODO CHECK IF THIS AUTHORIZATION STILL NEEDS TO BE MADE (consider doing in activity)
        /*
        _error.value = if (application.authorizeCaller(callingPackage)) {
            ""
        } else {
            application.getString(R.string.error_caller_not_chrome)
        }*/

        setContent {
            val payStatus by viewModel.paymentOperation.collectAsStateWithLifecycle()
            when (payStatus) {
                is PaymentOperation.ResultIntent -> {
                    setResult(RESULT_OK, (payStatus as PaymentOperation.ResultIntent).intent)
                    finish()
                }
                else -> PaymentApp(paymentStatus = payStatus)
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
}