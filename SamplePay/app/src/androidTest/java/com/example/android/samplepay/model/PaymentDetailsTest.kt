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

package com.example.android.samplepay.model

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith

private const val SAMPLE_TOTAL = "{\"currency\":\"USD\",\"value\":\"25.00\"}"

@RunWith(AndroidJUnit4::class)
class PaymentDetailsTest {

    @Test
    fun parseTotal() {
        val total = PaymentAmount.parse(SAMPLE_TOTAL)
        assertThat(total.currency).isEqualTo("$")
        assertThat(total.value).isEqualTo("25.00")
    }
}
