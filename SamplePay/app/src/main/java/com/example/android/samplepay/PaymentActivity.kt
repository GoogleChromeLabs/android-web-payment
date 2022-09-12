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

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.observe
import com.example.android.samplepay.model.PaymentAddress
import com.example.android.samplepay.model.PaymentDetailsUpdate
import com.example.android.samplepay.model.PaymentParams
import kotlin.math.roundToInt

private const val UPDATE_DETAILS_ACTIVITY_REQUEST_CODE = 0
private const val TAG = "PaymentActivity"

/**
 * This activity handles the PAY action from Chrome.
 *
 * It [returns][setResult] [#RESULT_OK] when the user clicks on the "Pay" button. The "Pay" button
 * is disabled unless the calling app is Chrome.
 */
class PaymentActivity : AppCompatActivity() {
    private lateinit var paymentParams: PaymentParams
    private lateinit var shippingOptions: RadioGroup
    private lateinit var shippingAddresses: RadioGroup
    private lateinit var payButton: Button
    private lateinit var promotionButton: Button
    private lateinit var selectedShippingOptionId: String
    private val viewModel: PaymentViewModel by viewModels()
    private var errorMessage: String = ""
    private var promotionErrorMessage: String = ""
    private var addresses: HashMap<Int, PaymentAddress> = HashMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.payment_activity)
        val contentView: View = findViewById(R.id.payment)
        contentView.layoutParams.width = (resources.displayMetrics.widthPixels * 0.90).roundToInt()
        setSupportActionBar(findViewById(R.id.toolbar))

        // View references.
        val merchantName: TextView = findViewById(R.id.merchant_name)
        val origin: TextView = findViewById(R.id.origin)
        val error: TextView = findViewById(R.id.error)
        val promotionError: TextView = findViewById(R.id.promotion_error)
        val total: TextView = findViewById(R.id.total)
        val payerName: View = findViewById(R.id.payer_name)
        val payerPhone: View = findViewById(R.id.payer_phone)
        val payerEmail: View = findViewById(R.id.payer_email)
        val contact: TextView = findViewById(R.id.contact_title)

        payButton = findViewById(R.id.pay)
        payButton.setOnClickListener { pay() }

        promotionButton = findViewById(R.id.promotion_button)
        promotionButton.setOnClickListener { applyPromotion() }

        shippingOptions = findViewById(R.id.shipping_options)
        shippingAddresses = findViewById(R.id.shipping_addresses)

        // Bind values from ViewModel to views.
        viewModel.merchantName.observe(this) { name ->
            merchantName.isVisible = name != null
            merchantName.text = name
        }
        viewModel.origin.observe(this) {
            origin.isVisible = it != null
            origin.text = it
        }
        viewModel.error.observe(this) { e ->
            if (e.isEmpty()) { // No error
                error.isVisible = false
                error.text = null
                payButton.isEnabled = true
            } else {
                error.isVisible = true
                error.text = e
                payButton.isEnabled = false
            }
        }
        viewModel.promotionError.observe(this) { e ->
            if (e.isEmpty()) { // No promotion error
                promotionError.isVisible = false
                promotionError.text = null
            } else {
                promotionError.isVisible = true
                promotionError.text = e
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
        viewModel.shipping.observe(this) {
            shippingOptions.isVisible = it
            shippingAddresses.isVisible = it
        }

        viewModel.payerName.observe(this) {
            payerName.isVisible = it
        }

        viewModel.payerPhone.observe(this) {
            payerPhone.isVisible = it
        }

        viewModel.payerEmail.observe(this) {
            payerEmail.isVisible = it
        }

        viewModel.contact.observe(this) {
            contact.isVisible = it
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
        paymentParams = PaymentParams.from(extras)
        if (paymentParams.paymentOptions.requestShipping) {
            createShippingOptions()
            setShippingOptionChangeListener()
            createShippingAddresses()
        }
        errorMessage = if (packageManager.authorizeCaller(callingPackage, application)) {
            ""
        } else {
            getString(R.string.error_caller_not_chrome)
        }
        viewModel.initialize(paymentParams, errorMessage, promotionErrorMessage)
        return true
    }

    @SuppressLint("NewApi")
    private fun createShippingOptions() {
        findViewById<TextView>(R.id.option_title).text =
            "${paymentParams.paymentOptions.shippingType.capitalize()} Options:"

        errorMessage = ""
        shippingOptions.removeAllViews()
        paymentParams.shippingOptions?.forEach {
            val radioButton = RadioButton(this)
            val label = "${it.label}, ${it.amountCurrency} ${it.amountValue}"
            radioButton.text = label
            radioButton.tag = it.id
            radioButton.id = View.generateViewId()
            shippingOptions.addView(radioButton)
            if (it.selected) {
                shippingOptions.check(radioButton.id)
                selectedShippingOptionId = it.id
            }
        }
    }

    private fun setShippingOptionChangeListener() {
        shippingOptions.setOnCheckedChangeListener { group, checkedId ->
            val selected: RadioButton = group.findViewById(checkedId)
            if (selected.tag.toString() != selectedShippingOptionId) {
                selectedShippingOptionId = selected.tag.toString()
                val shippingOptionChangeIntent =
                    Intent(this@PaymentActivity, PaymentDetailsUpdateActivity::class.java)
                shippingOptionChangeIntent.putExtra("callingBrowserPackage", callingPackage)
                shippingOptionChangeIntent.putExtra("selectedOptionId", selectedShippingOptionId)
                payButton.isEnabled = false
                startActivityForResult(
                    shippingOptionChangeIntent, UPDATE_DETAILS_ACTIVITY_REQUEST_CODE
                )
            }
        }
    }

    private fun createShippingAddresses() {
        findViewById<TextView>(R.id.address_title).text =
            "${paymentParams.paymentOptions.shippingType.capitalize()} Address:"

        // Todo: allow the user to add/edit address
        addresses[R.id.canada_address] = PaymentAddress(
            arrayOf("111 Richmond st. West #12"),
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
        )
        (findViewById<RadioButton>(R.id.canada_address)).text =
            addresses[R.id.canada_address].toString()

        addresses[R.id.us_address] = PaymentAddress(
            arrayOf("1875 Explorer St #1000"),
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
        )
        (findViewById<RadioButton>(R.id.us_address)).text = addresses[R.id.us_address].toString()

        addresses[R.id.uk_address] = PaymentAddress(
            arrayOf("1-13 St Giles High St"),
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
        (findViewById<RadioButton>(R.id.uk_address)).text = addresses[R.id.uk_address].toString()

        shippingAddresses.setOnCheckedChangeListener { _, checkedId ->
            val shippingAddressChangeIntent =
                Intent(this@PaymentActivity, PaymentDetailsUpdateActivity::class.java)
            shippingAddressChangeIntent.putExtra("callingBrowserPackage", callingPackage)
            shippingAddressChangeIntent.putExtra(
                "selectedAddress", addresses[checkedId]?.asBundle()
            )
            payButton.isEnabled = false
            startActivityForResult(
                shippingAddressChangeIntent, UPDATE_DETAILS_ACTIVITY_REQUEST_CODE
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            UPDATE_DETAILS_ACTIVITY_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val updatedParams = data.extras?.getBundle("updatedPaymentParams")?.let {
                        PaymentDetailsUpdate.from(it)
                    }

                    if (updatedParams?.shippingOptions != null) {
                        paymentParams.shippingOptions = updatedParams.shippingOptions
                        createShippingOptions()
                        logIfDebug("New shipping options:\t" + "${paymentParams.shippingOptions}")
                    }

                    if (updatedParams?.total != null) {
                        paymentParams.total = updatedParams.total
                        logIfDebug("New total:\t" + "${paymentParams.total}")
                    }

                    if (updatedParams?.error != null) {
                        errorMessage = updatedParams.error + "\n"
                        logIfDebug("New error:\t" + updatedParams.error)
                    }

                    if (updatedParams?.addressErrors != null) {
                        errorMessage += updatedParams.addressErrors.toString()
                        logIfDebug(
                            "New address errors:\t" + updatedParams.addressErrors.toString()
                        )
                    }

                    if (updatedParams?.stringifiedPaymentMethodErrors != null) {
                        promotionErrorMessage = updatedParams.stringifiedPaymentMethodErrors
                        logIfDebug(
                            "New payment method error:\t" + updatedParams.stringifiedPaymentMethodErrors
                        )
                    }

                    viewModel.initialize(paymentParams, errorMessage, promotionErrorMessage)
                }
            }
        }
    }

    private fun cancel() {
        setResult(RESULT_CANCELED)
        finish()
    }

    private fun pay() {
        setResult(RESULT_OK, Intent().apply {
            putExtra("methodName", "https://sample-pay-e6bb3.firebaseapp.com")
            putExtra("details", "{\"token\": \"put-some-data-here\"}")
            populateRequestedPaymentOptions()
            if (BuildConfig.DEBUG) {
                Log.d(getString(R.string.payment_response), "${this.extras}")
            }
        })
        finish()
    }

    private fun Intent.populateRequestedPaymentOptions() {
        if (paymentParams.paymentOptions.requestPayerName) {
            val payerNameView = findViewById<EditText>(R.id.payer_name)
            val payerName: String = payerNameView.text.toString()
            putExtra("payerName", payerName)
        }
        if (paymentParams.paymentOptions.requestPayerPhone) {
            val payerPhoneView = findViewById<EditText>(R.id.payer_phone)
            val payerPhone: String = payerPhoneView.text.toString()
            putExtra("payerPhone", payerPhone)
        }
        if (paymentParams.paymentOptions.requestPayerEmail) {
            val payerEmailView = findViewById<EditText>(R.id.payer_email)
            val payerEmail: String = payerEmailView.text.toString()
            putExtra("payerEmail", payerEmail)
        }
        if (paymentParams.paymentOptions.requestShipping) {
            putExtra(
                "shippingAddress", addresses[shippingAddresses.checkedRadioButtonId]?.asBundle()
            )
            putExtra("shippingOptionId", selectedShippingOptionId)
        }
    }

    private fun applyPromotion() {
        promotionErrorMessage = ""
        val promotionCode = findViewById<EditText>(R.id.promotion_code).text.toString()
        val paymentMethodChangeIntent =
            Intent(this@PaymentActivity, PaymentDetailsUpdateActivity::class.java)
        paymentMethodChangeIntent.putExtra("callingBrowserPackage", callingPackage)
        paymentMethodChangeIntent.putExtra("promotionCode", promotionCode)
        payButton.isEnabled = false
        startActivityForResult(
            paymentMethodChangeIntent, UPDATE_DETAILS_ACTIVITY_REQUEST_CODE
        )
    }

    private fun logIfDebug(message: String) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, message)
        }
    }
}
