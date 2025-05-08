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

import android.content.res.Configuration
import android.os.Parcelable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.android.samplepay.R
import com.example.android.samplepay.model.PaymentAddress
import com.example.android.samplepay.model.PaymentAmount
import com.example.android.samplepay.model.PaymentOptions
import com.example.android.samplepay.model.ShippingOption
import com.example.android.samplepay.ui.theme.Typography
import kotlinx.parcelize.Parcelize

@Composable
fun PaymentScreen(
    paymentStatus: PaymentOperation.Started,
    onAddPromoCode: (String) -> Unit,
    onShippingOptionChange: (String) -> Unit,
    onShippingAddressChange: (String) -> Unit,
    onPayButtonClicked: (PaymentFormInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    val (merchantName, merchantOrigin, errorText, promoCodeErrorText, amount, paymentOptions, shippingOptions, defaultShippingOptionId, paymentAddresses) = paymentStatus
    Surface {
        PaymentScaffold(
            merchantName = merchantName, merchantOrigin = merchantOrigin, modifier = modifier
        ) { innerPadding ->
            PaymentSummary(
                paymentOptions = paymentOptions,
                errorText = errorText,
                promotionCodeErrorText = promoCodeErrorText,
                amount = amount,
                onAddPromoCode = onAddPromoCode,
                shippingOptions = shippingOptions,
                defaultShippingOptionId = defaultShippingOptionId,
                onShippingOptionChange = onShippingOptionChange,
                paymentAddresses = paymentAddresses,
                onShippingAddressChange = onShippingAddressChange,
                onPayButtonClicked = onPayButtonClicked,
                modifier = modifier.padding(innerPadding)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaymentScaffold(
    merchantName: String?,
    merchantOrigin: String?,
    modifier: Modifier = Modifier,
    content: @Composable (innerPadding: PaddingValues) -> Unit
) {
    Box(
        modifier = modifier.background(
            Brush.linearGradient(
                colorStops = arrayOf(
                    0.0f to MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    0.5f to MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                    0.8f to MaterialTheme.colorScheme.tertiary.copy(alpha = 0.25f)
                )
            )
        )
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            merchantName?.let {
                                Text(
                                    text = it,
                                    style = Typography.titleLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            merchantOrigin?.let {
                                Text(
                                    text = it,
                                    style = Typography.labelMedium,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }, colors = TopAppBarDefaults.topAppBarColors(
                        titleContentColor = MaterialTheme.colorScheme.inversePrimary,
                        containerColor = Color.Transparent
                    )
                )
            }, containerColor = Color.Transparent, content = content
        )
    }
}

@Composable
private fun PaymentSummary(
    amount: PaymentAmount?,
    paymentOptions: PaymentOptions,
    onAddPromoCode: (String) -> Unit,
    shippingOptions: List<ShippingOption>,
    defaultShippingOptionId: String?,
    onShippingOptionChange: (String) -> Unit,
    paymentAddresses: List<PaymentAddress>,
    onShippingAddressChange: (String) -> Unit,
    onPayButtonClicked: (PaymentFormInfo) -> Unit,
    errorText: String? = null,
    promotionCodeErrorText: String? = null,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    var promoCode: String by rememberSaveable { mutableStateOf("") }
    var contactInfo: ContactInfo by rememberSaveable { mutableStateOf(ContactInfo()) }
    var shippingOptionSelection: String by rememberSaveable { mutableStateOf(defaultShippingOptionId.orEmpty()) }
    var shippingAddressSelection: String by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = modifier
            .padding(dimensionResource(R.dimen.spacing_medium))
            .fillMaxWidth()
            .verticalScroll(scrollState)
    ) {
        errorText?.let {
            Text(
                modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.spacing_medium)),
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))
        }
        amount?.let {
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_small)))
            AmountLabel(currency = it.currency, value = it.value)
        }
        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))
        PromoCodeTextField(onAddPromoCode = {
            promoCode = it
            onAddPromoCode(it)
        })
        promotionCodeErrorText?.let {
            Text(
                modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.spacing_medium)),
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
        if (paymentOptions.requireContact) {
            val (showNameInfo, showPhoneInfo, showEmailInfo, requestShipping, shippingType) = paymentOptions
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))
            ContactForm(
                showName = showNameInfo,
                onContactInfoChange = { contactInfo = it },
                showPhoneNumber = showPhoneInfo,
                showEmailAddress = showEmailInfo,
                shippingType = shippingType,
                showShippingInfo = requestShipping,
                shippingOptions = shippingOptions,
                shippingOptionSelection = shippingOptionSelection,
                onShippingOptionChange = {
                    shippingOptionSelection = it
                    onShippingOptionChange(it)
                },
                paymentAddresses = paymentAddresses,
                onShippingAddressChange = {
                    shippingAddressSelection = it
                    onShippingAddressChange(it)
                })
        }
        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))
        InfoText(message = stringResource(R.string.payment_explanation))
        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_small)))
        Button(
            onClick = {
                onPayButtonClicked(
                    PaymentFormInfo(
                        promotionCode = promoCode,
                        contactInfo = contactInfo,
                        shippingOption = shippingOptionSelection,
                        shippingAddress = shippingAddressSelection
                    )
                )
            },
            enabled = errorText == null,
            modifier = Modifier
                .padding(horizontal = dimensionResource(R.dimen.spacing_medium))
                .fillMaxWidth()
        ) {
            Text("Pay")
        }
    }
}

@Composable
private fun AmountLabel(currency: String, value: String, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier.fillMaxWidth()
    ) {
        Text(text = "Total price:", style = Typography.labelLarge)
        Text(
            text = stringResource(R.string.amount_label, currency, value),
            style = Typography.displayLarge.copy(
                brush = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.inversePrimary
                    )
                )
            )
        )
    }
}

@Composable
private fun PaymentFormTextField(
    value: String?,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    textStyle: TextStyle = LocalTextStyle.current,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    TextField(
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
        ),
        placeholder = placeholder,
        label = label,
        leadingIcon = leadingIcon,
        textStyle = textStyle,
        onValueChange = onValueChange,
        modifier = modifier.height(54.dp),
        shape = RoundedCornerShape(16.dp),
        value = value.orEmpty(),
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Done,
        ),
        keyboardActions = keyboardActions,
        maxLines = 1,
        singleLine = true,
    )
}

@Composable
private fun PromoCodeTextField(
    value: String = "", onAddPromoCode: (String) -> Unit, modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    var internalPromoCode: String by rememberSaveable { mutableStateOf(value) }

    val onAddPromoCodeTriggered = {
        keyboardController?.hide()
        onAddPromoCode(internalPromoCode)
    }

    PaymentFormTextField(
        value = internalPromoCode,
        onValueChange = {
            internalPromoCode = it
        },
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .testTag("promoCodeTextField")
            .onKeyEvent {
                if (it.key == Key.Enter) {
                    if (internalPromoCode.isBlank()) return@onKeyEvent false
                    onAddPromoCodeTriggered()
                    true
                } else {
                    false
                }
            },
        placeholder = {
            Text(text = stringResource(R.string.promotion_code))
        },
        label = {
            Text(text = stringResource(R.string.promotion_title))
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Rounded.AddCircle,
                contentDescription = stringResource(
                    id = R.string.promotion_code,
                ),
                tint = MaterialTheme.colorScheme.onSurface,
            )
        },
        textStyle = MaterialTheme.typography.labelSmall,
        keyboardActions = KeyboardActions(
            onDone = {
                if (internalPromoCode.isBlank()) return@KeyboardActions
                onAddPromoCodeTriggered()
            })
    )
}

@Composable
fun ContactForm(
    showName: Boolean,
    showPhoneNumber: Boolean,
    showEmailAddress: Boolean,
    showShippingInfo: Boolean,
    onContactInfoChange: (ContactInfo) -> Unit,
    shippingType: String,
    shippingOptions: List<ShippingOption>,
    shippingOptionSelection: String?,
    onShippingOptionChange: (String) -> Unit,
    paymentAddresses: List<PaymentAddress>,
    onShippingAddressChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var contactInfo: ContactInfo by rememberSaveable { mutableStateOf(ContactInfo()) }

    Column(modifier = modifier.padding(horizontal = dimensionResource(R.dimen.spacing_medium))) {
        Text(
            text = "Contact information",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.secondary
        )

        if (showName) {
            Spacer(modifier = modifier.height(dimensionResource(R.dimen.spacing_small)))
            PaymentFormTextField(value = contactInfo.name, label = {
                Text("Name")
            }, onValueChange = {
                contactInfo = contactInfo.copy(name = it)
                onContactInfoChange(contactInfo)
            })
        }

        if (showPhoneNumber) {
            Spacer(modifier = modifier.height(dimensionResource(R.dimen.spacing_small)))
            PaymentFormTextField(value = contactInfo.phoneNumber, label = {
                Text("Phone number")
            }, onValueChange = {
                contactInfo = contactInfo.copy(phoneNumber = it)
                onContactInfoChange(contactInfo)
            })
        }

        if (showEmailAddress) {
            Spacer(modifier = modifier.height(dimensionResource(R.dimen.spacing_small)))
            PaymentFormTextField(value = contactInfo.emailAddress, label = {
                Text("Email address")
            }, onValueChange = {
                contactInfo = contactInfo.copy(emailAddress = it)
                onContactInfoChange(contactInfo)
            })
        }

        if (showShippingInfo) {
            Spacer(modifier = modifier.height(dimensionResource(R.dimen.spacing_medium)))
            Text(
                text = stringResource(R.string.option_title_format, shippingType),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = modifier.height(dimensionResource(R.dimen.spacing_tiny)))
            ShippingSelector(
                shippingOptions = shippingOptions,
                selectedOption = shippingOptionSelection,
                onValueChange = onShippingOptionChange
            )
            Spacer(modifier = modifier.height(dimensionResource(R.dimen.spacing_small)))
            Text(
                text = stringResource(R.string.address_title_format, shippingType),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = modifier.height(dimensionResource(R.dimen.spacing_tiny)))
            SingleChoiceSegmentedButton(
                options = paymentAddresses, onValueChange = onShippingAddressChange
            )
        }
    }
}

@Composable
private fun InfoText(message: String) {
    val warningContainerColor = MaterialTheme.colorScheme.inversePrimary
    val warningTextColor = MaterialTheme.colorScheme.primary
    Text(
        text = message,
        color = warningTextColor,
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier
            .padding(horizontal = dimensionResource(R.dimen.spacing_medium))
            .drawWithCache {
                onDrawBehind {
                    drawRoundRect(
                        warningContainerColor,
                        cornerRadius = CornerRadius(15.dp.toPx()),
                    )
                    drawRoundRect(
                        warningTextColor,
                        cornerRadius = CornerRadius(15.dp.toPx()),
                        style = Stroke(width = 0.5.dp.toPx())
                    )
                }
            }
            .padding(
                horizontal = dimensionResource(R.dimen.spacing_medium),
                vertical = dimensionResource(R.dimen.spacing_small)
            ))
}

@Composable
fun ShippingSelector(
    shippingOptions: List<ShippingOption>,
    selectedOption: String?,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.selectableGroup()) {
        shippingOptions.forEach { option ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(35.dp)
                    .selectable(
                        selected = (option.id == selectedOption),
                        onClick = { onValueChange(option.id) },
                        role = Role.RadioButton
                    )
                    .padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (option.id == selectedOption), onClick = null
                )
                Text(
                    text = stringResource(
                        R.string.option_format,
                        option.label,
                        option.amountCurrency,
                        option.amountValue
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }
    }
}

@Composable
fun SingleChoiceSegmentedButton(
    options: List<PaymentAddress>, onValueChange: (String) -> Unit, modifier: Modifier = Modifier
) {
    var selectedOption: String? by rememberSaveable { mutableStateOf(null) }

    SingleChoiceSegmentedButtonRow(modifier = modifier.fillMaxWidth()) {
        options.forEachIndexed { index, paymentAddress ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = options.size,
                ),
                colors = SegmentedButtonDefaults.colors(
                    inactiveContainerColor = Color.Transparent
                ),
                onClick = {
                    selectedOption = paymentAddress.id
                    onValueChange(paymentAddress.id)
                },
                selected = paymentAddress.id == selectedOption,
                label = { Text(paymentAddress.label) },
            )
        }
    }
}

@Preview("Default")
@Preview("Dark theme", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewPaymentScreen() {
    Surface {
        PaymentScaffold(
            merchantName = "Sample Merchant", merchantOrigin = "https://example.com"
        ) { innerPadding ->
            PaymentSummary(
                modifier = Modifier.padding(innerPadding),
                amount = PaymentAmount(currency = "$", value = "30"),
                onAddPromoCode = {},
                paymentOptions = PaymentOptions(
                    requestPayerName = true,
                    requestPayerEmail = true,
                    requestPayerPhone = true,
                    requestShipping = true
                ),
                shippingOptions = listOf(
                    ShippingOption(
                        id = "US",
                        label = "Express national",
                        amountCurrency = "USD",
                        amountValue = "12",
                        selected = true,
                    )
                ),
                defaultShippingOptionId = "US",
                onShippingOptionChange = {},
                paymentAddresses = paymentAddresses,
                onShippingAddressChange = {},
                onPayButtonClicked = {})
        }
    }
}

@Parcelize
data class PaymentFormInfo(
    val promotionCode: String? = null,
    val contactInfo: ContactInfo,
    val shippingOption: String? = null,
    val shippingAddress: String? = null
) : Parcelable

@Parcelize
data class ContactInfo(
    val name: String? = null, val phoneNumber: String? = null, val emailAddress: String? = null
) : Parcelable