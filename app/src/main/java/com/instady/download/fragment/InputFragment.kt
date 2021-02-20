package com.instady.download.fragment

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.instady.download.R
import com.instady.download.data.HttpCallbackListener
import com.instady.download.data.OutResource
import com.instady.download.utils.DownloadUtil
import com.instady.download.utils.ParseUrl
import kotlinx.android.synthetic.main.fragment_input.*


class InputFragment : BaseFragment() {
    private lateinit var downloadUtil: DownloadUtil

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_input, container, false)
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        downloadUtil = DownloadUtil(requireActivity().applicationContext)
        paste_btn.setOnClickListener {
            val pastContext = getPasteContent()
            url_edit.text = SpannableStringBuilder(pastContext)
            showSnackBar(constraint_layout, resources.getString(R.string.paste_success))
        }
        confirm_btn.setOnClickListener {
            showLoadingDialog()
            val url = url_edit.text.toString()
            ParseUrl().getDownloadUrl(url, object : HttpCallbackListener {
                override fun onFinish(resource: OutResource) {
                    dismissLoadingDialog()
                    AlertDialog.Builder(activity).apply {
                        setTitle(resources.getString(R.string.download_title))
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
                                    val isStart =
                                        downloadUtil.download(downloadUrl.url, downloadUrl.fileName)
                                    if (isStart) {
                                        success++
                                    } else {
                                        fail++
                                    }
                                }
                                showSnackBar(
                                    constraint_layout,
                                    resources.getString(R.string.toast_message, success, fail)
                                )
                            }
                        }
                        setNegativeButton(resources.getString(R.string.cancel)) { _, which ->
                            Log.d(TAG, "cancel$which")
                        }
                        show()
                    }
                }

                override fun onError(e: Exception) {
                    dismissLoadingDialog()
                    AlertDialog.Builder(activity).apply {
                        setTitle(resources.getString(R.string.download_error_title))
                        setMessage(resources.getString(R.string.download_error_message))
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

    private fun getPasteContent(): String {

        val clipboardManager: ClipboardManager = activity?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData: ClipData? = clipboardManager.primaryClip
        if (null != clipData && clipData.itemCount > 0) {
            val item = clipData.getItemAt(0)
            if (null != item.text) {
                return item.text.toString()
            }
        }
        return ""
    }

    companion object {
        private const val TAG = "ins"
    }
}