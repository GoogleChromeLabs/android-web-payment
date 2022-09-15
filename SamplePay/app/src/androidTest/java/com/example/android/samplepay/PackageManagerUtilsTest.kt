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

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PackageManagerUtilsTest {

    @Test
    fun checkFingerprint() {
        val context: Context = ApplicationProvider.getApplicationContext()
        assertThat(
            context.packageManager.hasSigningCertificates(
                "com.android.chrome",
                setOf(parseFingerprint(context.getString(R.string.chrome_stable_fingerprint)))
            )
        ).isTrue()
        assertThat(
            context.packageManager.hasSigningCertificates(
                "com.example.android.samplepay",
                setOf(parseFingerprint(context.getString(R.string.chrome_stable_fingerprint)))
            )
        ).isFalse()
    }

    @Test
    fun checkFingerprint_multiple() {
        val context: Context = ApplicationProvider.getApplicationContext()
        assertThat(
            context.packageManager.hasSigningCertificates(
                "com.android.chrome",
                setOf(
                    parseFingerprint(context.getString(R.string.chrome_stable_fingerprint)),
                    parseFingerprint("4C:FC:14:C6:97:DE:66:4E:66:97:50:C0:24:CE:5F:27:00:92:EE:F3:7F:18:B3:DA:77:66:84:CD:9D:E9:D2:CB")
                )
            )
        ).isFalse()
    }
}
