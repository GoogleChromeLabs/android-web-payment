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

@file:Suppress("DEPRECATION")

package com.example.android.samplepay.util

import android.annotation.SuppressLint
import android.app.Activity.OVERRIDE_TRANSITION_CLOSE
import android.app.Activity.OVERRIDE_TRANSITION_OPEN
import android.os.Build
import androidx.activity.ComponentActivity

@SuppressLint("InlinedApi")
fun ComponentActivity.overrideOpenTransition(enterAnim: Int, exitAnim: Int) =
    overrideTransition(OVERRIDE_TRANSITION_OPEN, enterAnim, exitAnim)

@SuppressLint("InlinedApi")
fun ComponentActivity.overrideCloseTransition(enterAnim: Int, exitAnim: Int) =
    overrideTransition(OVERRIDE_TRANSITION_CLOSE, enterAnim, exitAnim)

/**
 * Decides what flavor of `overrideTransition` to use based on the API version.
 */
private fun ComponentActivity.overrideTransition(overrideType: Int, enterAnim: Int, exitAnim: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        overrideActivityTransition(overrideType, enterAnim, exitAnim)
    } else {
        overridePendingTransition(enterAnim, exitAnim)
    }
}