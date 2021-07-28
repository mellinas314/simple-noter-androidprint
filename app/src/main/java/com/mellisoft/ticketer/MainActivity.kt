package com.mellisoft.ticketer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.webkit.WebView
import com.mellisoft.ticketer.helper.Callback
import com.mellisoft.ticketer.manager.WebViewManager

private const val TAG = "MMainActivity"

class MainActivity : AppCompatActivity() {
    private lateinit var appWebView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        this.appWebView = findViewById(R.id.main_webview)
        this.showLoading()
        WebViewManager.onPageFinished = object: Callback {
            override fun onResult(result: Any?) {
                Handler(Looper.getMainLooper()).postDelayed({
                    this@MainActivity.hideLoading()
                }, 500)
            }
        }
        WebViewManager.mainActivity = this
        this.appWebView.webViewClient = WebViewManager.getWebViewClient()
        WebViewManager.initWebView(this.appWebView, this)
    }

    private fun showLoading() {
        findViewById<View>(R.id.loading_indicator).visibility = View.VISIBLE
    }

    private fun hideLoading() {
        findViewById<View>(R.id.loading_indicator).visibility = View.GONE
    }
}