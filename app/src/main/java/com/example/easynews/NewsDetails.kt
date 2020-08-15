package com.example.easynews

import android.annotation.SuppressLint
import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.activity_news_details.*

class NewsDetails : AppCompatActivity() {

    lateinit var dialog : AlertDialog

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news_details)

        dialog = SpotsDialog.Builder().setContext(this).build()
        dialog.show()

        //WebView
        webView.settings.javaScriptEnabled = true
        webView.webChromeClient = WebChromeClient()
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view : WebView?, url : String?) {
                dialog.dismiss()
            }
        }

        if (intent != null)
            if(intent.getStringExtra("webUrl")!!.isNotEmpty())
                webView.loadUrl(intent.getStringExtra("webUrl")!!)
    }
}