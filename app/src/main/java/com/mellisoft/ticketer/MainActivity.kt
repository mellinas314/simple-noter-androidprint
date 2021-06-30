package com.mellisoft.ticketer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebView
import com.mellisoft.ticketer.manager.WebViewManager

class MainActivity : AppCompatActivity() {
    private lateinit var appWebView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        this.appWebView = findViewById(R.id.main_webview)
        WebViewManager.initWebView(this.appWebView)
    }
}