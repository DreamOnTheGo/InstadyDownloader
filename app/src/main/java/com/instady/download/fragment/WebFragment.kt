package com.instady.download.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import com.instady.download.BrowserActivity
import com.instady.download.R
import com.instady.download.utils.DownloadUtil
import com.instady.download.utils.ParseUrl
import kotlinx.android.synthetic.main.fragment_web.*


class WebFragment : BaseFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_web, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.d(TAG, "onActivityCreated: ")
        instagram_card.setOnClickListener {
            val intent = Intent(activity, BrowserActivity::class.java)
            intent.putExtra("url", "https://www.instagram.com")
            startActivity(intent)
        }
        tiktok_card.setOnClickListener {
            val intent = Intent(activity, BrowserActivity::class.java)
            intent.putExtra("url", "https://www.douyin.com")
            startActivity(intent)
        }
        web_url_edit.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                val input = web_url_edit.text.toString()
                val url = ParseUrl.getHttpUrl(input)
                val intent = Intent(activity, BrowserActivity::class.java)
                intent.putExtra("url", url)
                startActivity(intent)
                true
            } else {
                false
            }
        }
    }

    companion object {
        private const val TAG = "insWeb"
    }

}