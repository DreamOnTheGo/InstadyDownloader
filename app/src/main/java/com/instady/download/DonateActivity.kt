package com.instady.download

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.instady.download.utils.WechatUtil
import kotlinx.android.synthetic.main.activity_donate.*


class DonateActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_donate)
        val wechatUtil = WechatUtil(this)
        wechat_card.setOnClickListener {
            if (!wechatUtil.wechatDonate()) {
                showSnackBar(constraint_layout, getString(R.string.not_found_wecaht))
            }
        }

        alipay_card.setOnClickListener {
            val uri = Uri.parse("https://qr.alipay.com/fkx19724nhk4hrzalcpvmd4")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }
        paypal_card.setOnClickListener {
            val uri = Uri.parse("https://www.paypal.com/paypalme/xxddyy1225")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }
    }
}