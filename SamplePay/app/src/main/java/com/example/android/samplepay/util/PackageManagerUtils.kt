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

package com.example.android.samplepay.util

import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Build

/**
 * Collects a list of signatures for a given package name based on the API version.
 *
 *  @param packageName the name of the package to gather signatures for.
 */
fun PackageManager.getApplicationSignatures(packageName: String): List<Signature> {
    try {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val signingInfo =
                getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES).signingInfo
            signingInfo?.let {
                if (it.hasMultipleSigners()) {
                    it.apkContentsSigners.toList()
                } else {
                    it.signingCertificateHistory.toList()
                }
            }.orEmpty()
        } else {
            @Suppress("DEPRECATION") val signatures =
                getPackageInfo(packageName, PackageManager.GET_SIGNATURES).signatures
            signatures?.toList().orEmpty()
        }
    } catch (_: Exception) {
        return emptyList()
    }
}