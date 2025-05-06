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
import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build
import java.security.MessageDigest

/**
 * Parse Keystore fingerprint as a [ByteArray].
 *
 * @param fingerprint The _SHA256_ fingerprint of the signing key. Bytes in 2-char hex expression
 * delimited with colons (e.g.
 * "F0:FD:6C:5B:41:0F:25:CB:25:C3:B5:33:46:C8:97:2F:AE:30:F8:EE:74:11:DF:91:04:80:AD:6B:2D:60:DB:83").
 */
fun parseFingerprint(fingerprint: String): ByteArray {
    return fingerprint.split(":").map { it.toInt(16).toByte() }.toByteArray()
}

/**
 * Checks if the application specified with the [packageName] has the matching signing
 * [certificates].
 *
 * @param packageName The package name.
 * @param certificates The signing certificates to be matched.
 */
fun PackageManager.hasSigningCertificates(
    packageName: String,
    certificates: Set<ByteArray>
): Boolean {
    return if (Build.VERSION.SDK_INT >= 28 && certificates.size == 1) {
        hasSigningCertificate(packageName, certificates.first(), PackageManager.CERT_INPUT_SHA256)
    } else {
        @SuppressLint("PackageManagerGetSignatures")
        @Suppress("DEPRECATION")
        val packageInfo =
            getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
        val sha256 = MessageDigest.getInstance("SHA-256")

        @Suppress("DEPRECATION")
        val signatures = packageInfo.signatures!!.map { sha256.digest(it.toByteArray()) }
        // All the certificates have to match in case the APK is signed with multiple keys.
        signatures.size == certificates.size &&
                signatures.all { s -> certificates.any { it.contentEquals(s) } }
    }
}

fun Application.authorizeCaller(packageName: String?): Boolean {
    return (packageName == "com.android.chrome" && packageManager.hasSigningCertificates(
        packageName, setOf(parseFingerprint(getString(R.string.chrome_stable_fingerprint)))
    )) || (packageName == "com.chrome.beta" && packageManager.hasSigningCertificates(
        packageName, setOf(parseFingerprint(getString(R.string.chrome_beta_fingerprint)))
    )) || (packageName == "com.chrome.dev" && packageManager.hasSigningCertificates(
        packageName, setOf(parseFingerprint(getString(R.string.chrome_dev_fingerprint)))
    )) || (packageName == "com.chrome.canary" && packageManager.hasSigningCertificates(
        packageName, setOf(parseFingerprint(getString(R.string.chrome_canary_fingerprint)))
    )) || (packageName == "org.chromium.chrome" && packageManager.hasSigningCertificates(
        packageName, setOf(parseFingerprint(getString(R.string.chromium_fingerprint)))
    ))
}

fun PackageManager.resolveServiceByFilter(intent: Intent): ResolveInfo? {
    return if (Build.VERSION.SDK_INT >= 33) {
        resolveService(
            intent,
            PackageManager.ResolveInfoFlags.of(PackageManager.GET_RESOLVED_FILTER.toLong())
        )
    } else {
        @Suppress("DEPRECATION")
        resolveService(intent, PackageManager.GET_RESOLVED_FILTER)
    }
}
