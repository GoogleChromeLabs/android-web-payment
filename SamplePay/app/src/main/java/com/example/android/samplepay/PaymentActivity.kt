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
    private lateinit var mErrorMessage: String
    private var mPromotionError: String = ""
    private lateinit var mParams: PaymentParams
    private lateinit var mShippingOptions: RadioGroup
    private lateinit var mShippingAddresses: RadioGroup
    private val viewModel: PaymentViewModel by viewModels()
    private lateinit var payButton: Button
    private lateinit var promotionButton: Button
    private var mAddresses: HashMap<Int, PaymentAddress> = HashMap()
    private lateinit var mSelectedShippingOptionId: String

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

        payButton = findViewById(R.id.pay)
        payButton.setOnClickListener { pay() }

        promotionButton = findViewById(R.id.promotion_button)
        promotionButton.setOnClickListener { applyPromotion() }


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
            mShippingOptions.isVisible = it
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
        mParams = PaymentParams.from(extras)
        if (mParams.paymentOptions.requestShipping) {
            createShippingOptions()
            setShippingOptionChangeListener()
            createShippingAddresses()
        }
        mErrorMessage = if (packageManager.authorizeCaller(callingPackage, application)) {
            ""
        } else {
            getString(R.string.error_caller_not_chrome)
        }
        viewModel.initialize(mParams, mErrorMessage, mPromotionError)
        return true
    }

    @SuppressLint("NewApi")
    private fun createShippingOptions() {
        findViewById<TextView>(R.id.option_title).text =
            "${mParams.paymentOptions.shippingType.capitalize()} Options:"

        mErrorMessage = ""
        mShippingOptions = findViewById(R.id.shipping_options)
        mShippingOptions.removeAllViews()
        mParams.shippingOptions?.forEach {
            val radioButton = RadioButton(this)
            val label = "${it.label}, ${it.amountCurrency} ${it.amountValue}"
            radioButton.text = label
            radioButton.tag = it.id
            radioButton.id = View.generateViewId()
            mShippingOptions.addView(radioButton)
            if (it.selected) {
                mShippingOptions.check(radioButton.id)
                mSelectedShippingOptionId = it.id
            }
        }
    }

    private fun setShippingOptionChangeListener() {
        mShippingOptions.setOnCheckedChangeListener { group, checkedId ->
            val selected: RadioButton = group.findViewById(checkedId)
            if (selected.tag.toString() != mSelectedShippingOptionId) {
                mSelectedShippingOptionId = selected.tag.toString()
                val shippingOptionChangeIntent =
                    Intent(this@PaymentActivity, PaymentDetailsUpdateActivity::class.java)
                shippingOptionChangeIntent.putExtra("selectedOptionId", mSelectedShippingOptionId)
                payButton.isEnabled = false
                startActivityForResult(
                    shippingOptionChangeIntent, UPDATE_DETAILS_ACTIVITY_REQUEST_CODE
                )
            }
        }
    }

    private fun createShippingAddresses() {
        findViewById<TextView>(R.id.address_title).text =
            "${mParams.paymentOptions.shippingType.capitalize()} Address:"

        // Todo: allow the user to add/edit address
        mAddresses[R.id.canada_address] = PaymentAddress(
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
            mAddresses[R.id.canada_address].toString()

        mAddresses[R.id.us_address] = PaymentAddress(
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
        (findViewById<RadioButton>(R.id.us_address)).text = mAddresses[R.id.us_address].toString()

        mAddresses[R.id.uk_address] = PaymentAddress(
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
        (findViewById<RadioButton>(R.id.uk_address)).text = mAddresses[R.id.uk_address].toString()

        mShippingAddresses = findViewById(R.id.shipping_addresses)
        mShippingAddresses.setOnCheckedChangeListener { _, checkedId ->
            val shippingAddressChangeIntent =
                Intent(this@PaymentActivity, PaymentDetailsUpdateActivity::class.java)
            shippingAddressChangeIntent.putExtra(
                "selectedAddress", mAddresses[checkedId]?.asBundle()
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
                        mParams.shippingOptions = updatedParams.shippingOptions
                        createShippingOptions()
                        logIfDebug("New shipping options:\t" + "${mParams.shippingOptions}")
                    }

                    if (updatedParams?.total != null) {
                        mParams.total = updatedParams.total
                        logIfDebug("New total:\t" + "${mParams.total}")
                    }

                    if (updatedParams?.error != null) {
                        mErrorMessage = updatedParams.error + "\n"
                        logIfDebug("New error:\t" + updatedParams.error)
                    }

                    if (updatedParams?.addressErrors != null) {
                        mErrorMessage += updatedParams.addressErrors.toString()
                        logIfDebug(
                            "New address errors:\t" + updatedParams.addressErrors.toString()
                        )
                    }

                    if (updatedParams?.stringifiedPaymentMethodErrors != null) {
                        mPromotionError = updatedParams.stringifiedPaymentMethodErrors
                        logIfDebug(
                            "New payment method error:\t" + updatedParams.stringifiedPaymentMethodErrors
                        )
                    }

                    viewModel.initialize(mParams, mErrorMessage, mPromotionError)
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
            putExtra("methodName", "https://sample-pay-web-app.firebaseapp.com")
            putExtra("details", "{\"token\": \"put-some-data-here\"}")
            populateRequestedPaymentOptions()
            if (BuildConfig.DEBUG) {
                Log.d(getString(R.string.payment_response), "${this.extras}")
            }
        })
        finish()
    }

    private fun Intent.populateRequestedPaymentOptions() {
        if (mParams.paymentOptions.requestPayerName) {
            val payerNameView = findViewById<EditText>(R.id.payer_name)
            val payerName: String = payerNameView.text.toString()
            putExtra("payerName", payerName)
        }
        if (mParams.paymentOptions.requestPayerPhone) {
            val payerPhoneView = findViewById<EditText>(R.id.payer_phone)
            val payerPhone: String = payerPhoneView.text.toString()
            putExtra("payerPhone", payerPhone)
        }
        if (mParams.paymentOptions.requestPayerEmail) {
            val payerEmailView = findViewById<EditText>(R.id.payer_email)
            val payerEmail: String = payerEmailView.text.toString()
            putExtra("payerEmail", payerEmail)
        }
        if (mParams.paymentOptions.requestShipping) {
            putExtra(
                "shippingAddress", mAddresses[mShippingAddresses.checkedRadioButtonId]?.asBundle()
            )
            putExtra("shippingOptionId", mSelectedShippingOptionId)
        }
    }

    private fun applyPromotion() {
        mPromotionError = ""
        val promotionCode = findViewById<EditText>(R.id.promotion_code).text.toString()
        val paymentMethodChangeIntent =
            Intent(this@PaymentActivity, PaymentDetailsUpdateActivity::class.java)
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
