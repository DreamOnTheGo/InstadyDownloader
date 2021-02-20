package com.instady.download

import android.app.AlertDialog
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.MenuItem
import android.webkit.WebView
import android.webkit.WebViewClient
import com.instady.download.data.HttpCallbackListener
import com.instady.download.data.OutResource
import com.instady.download.utils.DownloadUtil
import com.instady.download.utils.ParseUrl
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_browser.*
import kotlinx.android.synthetic.main.fragment_input.*

class BrowserActivity : BaseActivity() {
    private lateinit var downloadUtil: DownloadUtil
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack();// 返回前一个页面
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_browser)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        downloadUtil = DownloadUtil(this.applicationContext)

        val url = intent.getStringExtra("url")
        webView.settings.javaScriptEnabled = true
        if (url != null) {
            webView.loadUrl(url)
        }
        webView.webViewClient = object: WebViewClient(){
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                Log.d("xdy", url.toString())
            }
            
        }
        download_btn.setOnClickListener {
            showLoadingDialog()
            val currentUrl = webView.url
            if (currentUrl != null) {
                ParseUrl().getDownloadUrl(currentUrl, object : HttpCallbackListener {
                    override fun onFinish(resource: OutResource) {
                        dismissLoadingDialog()
                        AlertDialog.Builder(this@BrowserActivity).apply {
                            setTitle(context.resources.getString(R.string.download_title))
                            val num = resource.getResourceList().size
                            var type = resource.getType()
                            var message = resources.getString(R.string.download_message, num, type)
                            setMessage(message)
                            setCancelable(false)
                            setPositiveButton(resources.getString(R.string.ok)) { _, which ->
                                run {
                                    Log.d(TAG, "ok$which")
                                    var success = 0
                                    var fail = 0
                                    for (downloadUrl in resource.getResourceList()) {
                                        val isStart = downloadUtil.download(
                                            downloadUrl.url,
                                            downloadUrl.fileName
                                        )
                                        if (isStart) {
                                            success++
                                        } else {
                                            fail++
                                        }
                                    }
                                    Snackbar.make(
                                        browser_coordinator_layout,
                                        context.getString(R.string.toast_message, success, fail),
                                        Snackbar.LENGTH_SHORT
                                    ).show()
                                }
                            }
                            setNegativeButton(resources.getString(R.string.cancel)) { _, which ->
                                run {
                                    Log.d(TAG, "cancel$which")
                                }
                            }
                            show()
                        }
                    }

                    override fun onError(e: Exception) {
                        dismissLoadingDialog()
                        Log.d(TAG, "获取失败")
                        AlertDialog.Builder(this@BrowserActivity).apply {
                            setTitle(resources.getString(R.string.download_error_title_2))
                            setMessage(resources.getString(R.string.download_error_message_2))
                            setCancelable(false)
                            setPositiveButton(resources.getString(R.string.ok)) { _, _ ->
                                run { Log.d(TAG, "ok") }
                            }
                            show()
                        }
                    }

                })
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        const val TAG = "xdyTest"
    }
}