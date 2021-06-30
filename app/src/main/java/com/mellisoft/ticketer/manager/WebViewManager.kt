package com.mellisoft.ticketer.manager

import android.annotation.SuppressLint
import android.util.Log
import android.webkit.WebView
import com.mellisoft.ticketer.BuildConfig

private const val TAG = "WebViewManager"

@SuppressLint("StaticFieldLeak")
object WebViewManager {
    private var webView: WebView? = null

    fun initWebView(webView: WebView) {
        Log.d(TAG, "initWebView")
        this.webView = webView
        this.initialize()
    }

    //region private initialization
    @SuppressLint("SetJavaScriptEnabled")
    private fun initialize() {
        Log.d(TAG, "initialize with path: ${BuildConfig.WEBVIEW_PATH}")
        if (this.webView != null) {
            this.webView!!.settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
            }
            this.webView!!.loadUrl(BuildConfig.WEBVIEW_PATH)
        }
    }
    //endregion
}