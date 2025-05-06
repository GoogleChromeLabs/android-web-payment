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

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.android.samplepay.R
import com.example.android.samplepay.ui.theme.AppTheme
import kotlinx.serialization.Serializable

@Serializable
object Default

@Serializable
object Payment

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun PaymentApp(viewModel: PaymentViewModel = viewModel(), paymentStatus: PaymentOperation) {
    AppTheme {
        val navController = rememberNavController()
        NavHost(
            navController = navController,
            startDestination = if (paymentStatus is PaymentOperation.Started) Payment else Default
        ) {
            composable<Default> { DefaultScreen() }
            composable<Payment> {
                PaymentScreen(
                    paymentStatus = paymentStatus as PaymentOperation.Started,
                    onAddPromoCode = viewModel::applyPromotionCode,
                    onShippingOptionChange = viewModel::updateShippingOption,
                    onShippingAddressChange = viewModel::updateShippingAddress,
                    onPayButtonClicked = viewModel::pay
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefaultScreen(modifier: Modifier = Modifier) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.app_name))
                }, colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.inversePrimary
                )
            )
        }) { innerPadding ->
        Text(
            text = stringResource(R.string.home_explanation),
            color = Color.Black,
            modifier = modifier
                .padding(innerPadding)
                .padding(dimensionResource(R.dimen.spacing_medium))
        )
    }
}