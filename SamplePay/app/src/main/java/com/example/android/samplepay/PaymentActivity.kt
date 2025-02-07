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

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.RadioButton
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatRadioButton
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import com.example.android.samplepay.databinding.PaymentActivityBinding
import com.example.android.samplepay.model.PaymentDetailsUpdate
import com.example.android.samplepay.model.PaymentParams
import org.chromium.components.payments.IPaymentDetailsUpdateService
import org.chromium.components.payments.IPaymentDetailsUpdateServiceCallback
import org.json.JSONObject
import java.util.*
import kotlin.math.roundToInt

private const val TAG = "PaymentActivity"

/**
 * This activity handles the PAY action from Chrome.
 *
 * It [returns][setResult] [#RESULT_OK] when the user clicks on the "Pay" button. The "Pay" button
 * is disabled unless the calling app is Chrome.
 */
class PaymentActivity : AppCompatActivity(R.layout.payment_activity) {

    private val viewModel: PaymentViewModel by viewModels()
    private val binding by viewBindings(PaymentActivityBinding::bind)

    private var shippingOptionsListenerEnabled = false
    private var shippingAddressListenerEnabled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.payment.layoutParams.width =
            (resources.displayMetrics.widthPixels * 0.90).roundToInt()
        setSupportActionBar(findViewById(R.id.toolbar))

        SamplePaymentDetailsUpdateService.viewModel = viewModel;

        // Bind values from ViewModel to views.
        viewModel.merchantName.observe(this) { merchantName ->
            binding.merchantName.isVisible = merchantName != null
            binding.merchantName.text = merchantName
        }
        viewModel.origin.observe(this) { origin ->
            binding.origin.isVisible = origin != null
            binding.origin.text = origin
        }
        viewModel.error.observe(this) { error ->
            if (error.isEmpty()) { // No error
                binding.error.isVisible = false
                binding.error.text = null
                binding.pay.isEnabled = true
            } else {
                binding.error.isVisible = true
                binding.error.text = error
                binding.pay.isEnabled = false
            }
        }
        viewModel.promotionError.observe(this) { promotionError ->
            if (promotionError.isEmpty()) { // No promotion error
                binding.promotionError.isVisible = false
                binding.promotionError.text = null
            } else {
                binding.promotionError.isVisible = true
                binding.promotionError.text = promotionError
            }
        }
        viewModel.total.observe(this) { total ->
            binding.total.isVisible = total != null
            binding.total.text = if (total != null) {
                getString(R.string.total_format, total.currency, total.value)
            } else {
                null
            }
        }
        viewModel.paymentOptions.observe(this) { paymentOptions ->
            binding.payerName.isVisible = paymentOptions.requestPayerName
            binding.payerPhone.isVisible = paymentOptions.requestPayerPhone
            binding.payerEmail.isVisible = paymentOptions.requestPayerEmail
            binding.contactTitle.isVisible = paymentOptions.requireContact
            binding.shippingOptions.isVisible = paymentOptions.requestShipping
            binding.shippingAddresses.isVisible = paymentOptions.requestShipping
            binding.optionTitle.text = formatTitle(
                R.string.option_title_format,
                paymentOptions.shippingType
            )
            binding.addressTitle.text = formatTitle(
                R.string.address_title_format,
                paymentOptions.shippingType
            )
        }
        viewModel.shippingOptions.observe(this) { shippingOptions ->
            shippingOptionsListenerEnabled = false
            binding.shippingOptions.removeAllViews()
            var selectedId = 0
            for (option in shippingOptions) {
                binding.shippingOptions.addView(
                    AppCompatRadioButton(this).apply {
                        text = getString(
                            R.string.option_format,
                            option.label,
                            option.amountCurrency,
                            option.amountValue
                        )
                        tag = option.id
                        id = ViewCompat.generateViewId()
                        if (option.selected) {
                            selectedId = id
                        }
                    }
                )
            }
            if (selectedId != 0) {
                binding.shippingOptions.check(selectedId)
            }
            shippingOptionsListenerEnabled = true
        }
        viewModel.paymentAddresses.observe(this) { addresses ->
            shippingAddressListenerEnabled = false
            binding.canadaAddress.text = addresses[R.id.canada_address].toString()
            binding.usAddress.text = addresses[R.id.us_address].toString()
            binding.ukAddress.text = addresses[R.id.uk_address].toString()
            shippingAddressListenerEnabled = true
        }

        // Handle UI events.
        binding.promotionButton.setOnClickListener {
            val promotionCode = binding.promotionCode.text.toString()
            SamplePaymentDetailsUpdateService.connectedService?.changePaymentMethod(
                Bundle().apply {
                    putString("methodName", BuildConfig.SAMPLE_PAY_METHOD_NAME)
                    putString("details", JSONObject().apply {
                        put("promotionCode", promotionCode)
                    }.toString())
                },
                callback
            )
        }
        binding.shippingOptions.setOnCheckedChangeListener { group, checkedId ->
            if (shippingOptionsListenerEnabled) {
                group.findViewById<RadioButton>(checkedId)?.let { button ->
                    val shippingOptionId = button.tag as String
                    SamplePaymentDetailsUpdateService.connectedService?.changeShippingOption(shippingOptionId, callback)
                }
            }
        }
        binding.shippingAddresses.setOnCheckedChangeListener { _, checkedId ->
            if (shippingAddressListenerEnabled) {
                viewModel.paymentAddresses.value?.get(checkedId)?.let { address ->
                    SamplePaymentDetailsUpdateService.connectedService?.changeShippingAddress(address.asBundle(), callback)
                }
            }
        }
        binding.pay.setOnClickListener { pay() }

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
        viewModel.initialize(PaymentParams.from(extras), callingPackage)
        return true
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
        unbindService(serviceConnection)
    }

    private fun cancel() {
        setResult(RESULT_CANCELED)
        finish()
    }

    private fun pay() {
        setResult(RESULT_OK, Intent().apply {
            putExtra("methodName", BuildConfig.SAMPLE_PAY_METHOD_NAME)
            putExtra("details", "{\"token\": \"put-some-data-here\"}")
            val paymentOptions = viewModel.paymentOptions.value
            if (paymentOptions == null) {
                cancel()
                return
            }
            if (paymentOptions.requestPayerName) {
                putExtra("payerName", binding.payerName.text.toString())
            }
            if (paymentOptions.requestPayerPhone) {
                putExtra("payerPhone", binding.payerPhone.text.toString())
            }
            if (paymentOptions.requestPayerEmail) {
                putExtra("payerEmail", binding.payerEmail.text.toString())
            }
            if (paymentOptions.requestShipping) {
                val addresses = viewModel.paymentAddresses.value!!
                putExtra(
                    "shippingAddress",
                    addresses[binding.shippingAddresses.checkedRadioButtonId]?.asBundle()
                )
                val optionId = binding.shippingOptions.findViewById<RadioButton>(
                    binding.shippingOptions.checkedRadioButtonId
                ).tag as String
                putExtra("shippingOptionId", optionId)
            }
            if (BuildConfig.DEBUG) {
                Log.d(getString(R.string.payment_response), "${this.extras}")
            }
        })
        finish()
    }

    private fun formatTitle(@StringRes id: Int, label: String): String {
        return getString(
            id,
            label.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
            }
        )
    }
}
