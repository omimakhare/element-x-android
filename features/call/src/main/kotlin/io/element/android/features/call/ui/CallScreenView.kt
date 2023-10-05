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

package io.element.android.features.call.ui

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import io.element.android.features.call.R
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.designsystem.utils.CommonDrawables
import io.element.android.libraries.theme.ElementTheme

typealias RequestPermissionCallback = (Array<String>) -> Unit

interface CallScreenNavigator {
    fun close()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CallScreenView(
    state: CallScreenState,
    requestPermissions: (Array<String>, RequestPermissionCallback) -> Unit,
    modifier: Modifier = Modifier,
) {
    ElementTheme {
        Scaffold(
            modifier = modifier,
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.element_call)) },
                    navigationIcon = {
                        BackButton(
                            resourceId = CommonDrawables.ic_compound_close,
                            onClick = { state.eventSink(CallScreeEvents.Hangup) }
                        )
                    }
                )
            }
        ) { padding ->
            BackHandler {
                state.eventSink(CallScreeEvents.Hangup)
            }
            CallWebView(
                modifier = Modifier
                    .padding(padding)
                    .consumeWindowInsets(padding)
                    .fillMaxSize(),
                url = state.urlState,
                userAgent = state.userAgent,
                onPermissionsRequested = { request ->
                    val androidPermissions = mapWebkitPermissions(request.resources)
                    val callback: RequestPermissionCallback = { request.grant(it) }
                    requestPermissions(androidPermissions.toTypedArray(), callback)
                },
                onWebViewCreated = {
                    state.eventSink(CallScreeEvents.SetupMessageChannels(it))
                }
            )
        }
    }
}

@Composable
private fun CallWebView(
    url: Async<String>,
    userAgent: String,
    onPermissionsRequested: (PermissionRequest) -> Unit,
    onWebViewCreated: (WebView) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isInpectionMode = LocalInspectionMode.current
    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                setup(userAgent, onPermissionsRequested)
                when {
                    isInpectionMode -> Unit
                    url is Async.Success -> loadUrl(url.data)
                }

                onWebViewCreated(this)
            }
        },
        update = { webView ->
            when {
                isInpectionMode -> Unit
                url is Async.Success && webView.url != url.data -> webView.loadUrl(url.data)
            }
        },
        onRelease = { webView ->
            webView.destroy()
        }
    )
}

@SuppressLint("SetJavaScriptEnabled")
private fun WebView.setup(
    userAgent: String,
    onPermissionsRequested: (PermissionRequest) -> Unit,
) {
    layoutParams = ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT
    )

    with(settings) {
        javaScriptEnabled = true
        allowContentAccess = true
        allowFileAccess = true
        domStorageEnabled = true
        mediaPlaybackRequiresUserGesture = false
        databaseEnabled = true
        loadsImagesAutomatically = true
        userAgentString = userAgent
    }

    webChromeClient = object : WebChromeClient() {
        override fun onPermissionRequest(request: PermissionRequest) {
            onPermissionsRequested(request)
        }
    }
}

@PreviewsDayNight
@Composable
internal fun CallScreenViewPreview() {
    ElementTheme {
        CallScreenView(
            state = CallScreenState(
                urlState = Async.Success("https://call.element.io/some-actual-call?with=parameters"),
                isInWidgetMode = false,
                userAgent = "",
                eventSink = {},
            ),
            requestPermissions = { _, _ -> },
        )
    }
}
