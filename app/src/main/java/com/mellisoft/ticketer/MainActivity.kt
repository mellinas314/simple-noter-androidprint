package com.mellisoft.ticketer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.webkit.WebView
import com.mellisoft.ticketer.helper.Callback
import com.mellisoft.ticketer.helper.JavascriptInterface
import com.mellisoft.ticketer.manager.WebViewManager
import hk.ucom.printer.UcomPrinterManager

private const val TAG = "MMainActivity"

class MainActivity : AppCompatActivity() {
    private lateinit var appWebView: WebView
    private lateinit var mPrinterManager: UcomPrinterManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mPrinterManager = UcomPrinterManager(UcomPrinterManager.PrinterModel.PU808USE)
        mPrinterManager.setManualSocketClose(true)

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

    override fun onBackPressed() {
        super.onBackPressed()
    }

    fun ignore(view: View) {
        Log.d(TAG, "Ignore click")
    }

    fun showLoading() {
        runOnUiThread {
            findViewById<View>(R.id.loading_indicator).visibility = View.VISIBLE
        }
    }

    fun hideLoading() {
        runOnUiThread {
            findViewById<View>(R.id.loading_indicator).visibility = View.GONE
        }
    }

    fun getPrinterManager(): UcomPrinterManager {
        return mPrinterManager;
    }

    override fun onDestroy() {
        super.onDestroy()
        mPrinterManager.closeConnection()
    }
}