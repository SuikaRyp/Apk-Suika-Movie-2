package com.suikamovie.app.ui.screens.detail

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

/**
 * Bungkus android.webkit.WebView jadi Composable. Ini satu-satunya WebView
 * yang tersisa di app - server streaming pihak ketiga (vidlink.pro dkk)
 * adalah halaman web lengkap sama JS player-nya sendiri, jadi cuma bisa
 * diputer lewat WebView, bukan player video native (ExoPlayer dkk) yang
 * butuh link file video langsung.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun PlayerWebView(url: String, modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                )
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.mediaPlaybackRequiresUserGesture = false
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                // WebChromeClient default nggak nge-handle fullscreen HTML5
                // <video> (onShowCustomView). Kontrol fullscreen/rotasi di app
                // ini SENGAJA dipegang sendiri lewat tombol custom di
                // DetailScreen (lihat lockLandscape()/unlockOrientation() di
                // MainActivity), bukan mengandalkan fullscreen bawaan video-nya.
                webChromeClient = WebChromeClient()
                loadUrl(url)
            }
        },
        update = { webView ->
            if (webView.url != url) {
                webView.loadUrl(url)
            }
        },
    )
}
