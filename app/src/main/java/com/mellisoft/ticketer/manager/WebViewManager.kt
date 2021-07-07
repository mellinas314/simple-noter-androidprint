package com.mellisoft.ticketer.manager

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.mellisoft.ticketer.BuildConfig
import com.mellisoft.ticketer.helper.Callback

private const val TAG = "WebViewManager"

@SuppressLint("StaticFieldLeak")
object WebViewManager {
    private var webView: WebView? = null
    var onPageFinished: Callback? = null
    var mainActivity: Activity? = null

    //region private initialization
    @SuppressLint("SetJavaScriptEnabled")
    private fun initialize() {
        Log.d(TAG, "initialize with path: ${BuildConfig.WEBVIEW_PATH}")

        WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG)

        if (this.webView != null) {
            this.webView!!.settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
            }
            this.webView!!.loadUrl(BuildConfig.WEBVIEW_PATH)
        }
    }
    //endregion

    fun initWebView(webView: WebView) {
        Log.d(TAG, "initWebView")
        this.webView = webView
        this.initialize()
    }

    fun getWebViewClient(): WebViewClient {
        return object: WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                this@WebViewManager.onPageFinished?.onResult(null)
                view?.loadUrl("javascript:document.body.setAttribute('data-is-ticketer-app', '1')")
                super.onPageFinished(view, url)
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                return if(request?.url == null || request.url.toString().startsWith(BuildConfig.WEBVIEW_PATH)) {
                    false
                }else {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse(request.url.toString())
                    mainActivity?.startActivity(intent)
                    true
                }
            }
        }
    }
}
