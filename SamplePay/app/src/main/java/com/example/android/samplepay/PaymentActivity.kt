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

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.observe

class PaymentActivity : AppCompatActivity() {

    private val viewModel: PaymentViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.payment_activity)
        setSupportActionBar(findViewById(R.id.toolbar))

        val merchantName: TextView = findViewById(R.id.merchant_name)
        val origin: TextView = findViewById(R.id.origin)
        val error: TextView = findViewById(R.id.error)
        val total: TextView = findViewById(R.id.total)
        val pay: Button = findViewById(R.id.pay)
        pay.setOnClickListener { pay() }
        viewModel.merchantName.observe(this) { name ->
            merchantName.isVisible = name != null
            merchantName.text = name
        }
        viewModel.origin.observe(this) {
            origin.isVisible = it != null
            origin.text = it
        }
        viewModel.error.observe(this) { e ->
            if (e == 0) { // No error
                error.isVisible = false
                error.text = null
                pay.isEnabled = true
            } else {
                error.isVisible = true
                error.setText(e)
                pay.isEnabled = false
            }
        }
        viewModel.total.observe(this) { t ->
            total.isVisible = t != null
            total.text = if (t != null) {
                getString(R.string.total_format, t.currency, t.value)
            } else {
                null
            }
        }

        if (savedInstanceState == null) {
            val result = handleIntent()
            if (!result) cancel()
        }
    }

    private fun handleIntent(): Boolean {
        val intent = intent ?: return false
        if (intent.action != "org.chromium.intent.action.PAY") {
            return false
        }
        val extras = intent.extras ?: return false
        viewModel.initialize(extras, callingPackage)
        return true
    }

    private fun cancel() {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    private fun pay() {
        setResult(Activity.RESULT_OK, Intent().apply {
            putExtra("methodName", "https://sample-pay-e6bb3.firebaseapp.com")
            putExtra("details", "{\"token\": \"put-some-data-here\"}")
        })
        finish()
    }
}
