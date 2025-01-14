/*
 * Copyright (c) 2023 New Vector Ltd
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

package io.element.android.tests.konsist

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.modifierprovider.withoutOverrideModifier
import com.lemonappdev.konsist.api.ext.list.withFunction
import com.lemonappdev.konsist.api.ext.list.withReturnType
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.Test

class KonsistTestTest {
    @Test
    fun `Classes name containing @Test must end with 'Test''`() {
        Konsist
            .scopeFromTest()
            .classes()
            .withFunction { it.hasAnnotationOf(Test::class) }
            .assertTrue { it.name.endsWith("Test") }
    }

    @Test
    fun `Function which creates Presenter in test MUST be named 'createPresenterName'`() {
        Konsist
            .scopeFromTest()
            .functions()
            .withReturnType { it.name.endsWith("Presenter") }
            .withoutOverrideModifier()
            .assertTrue { functionDeclaration ->
                functionDeclaration.name == "create${functionDeclaration.returnType?.name}"
            }
    }
}
