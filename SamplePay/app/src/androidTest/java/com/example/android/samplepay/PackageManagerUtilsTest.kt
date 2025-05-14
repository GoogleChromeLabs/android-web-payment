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

package com.example.android.samplepay

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.android.samplepay.util.getApplicationSignatures
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PackageManagerUtilsTest {

    @Test
    fun checkArbitrarySignatures() {
        val context: Context = ApplicationProvider.getApplicationContext()
        val chromeSignatures = context.packageManager.getApplicationSignatures("com.android.chrome")
        assertThat(chromeSignatures).isNotEmpty()

        val appSignatures = context.packageManager.getApplicationSignatures("com.example.android.samplepay")
        assertThat(appSignatures).isNotEmpty()

        assertThat(chromeSignatures).isNotEqualTo(appSignatures)
    }
}
