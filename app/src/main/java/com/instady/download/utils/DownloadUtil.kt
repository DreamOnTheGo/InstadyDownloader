package com.instady.download.utils

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import com.arialyy.aria.core.Aria
import com.arialyy.aria.core.common.HttpOption


class DownloadUtil(private val context: Context) {
    init {
        Aria.download(context).register()
    }

    fun download(url: String, fileName: String): Boolean {
        val list = Aria.download(this).taskList
        val filePath = getFilePath().path + "/" + fileName
        Log.d(TAG, "filePath:$filePath")
        val option = HttpOption().useServerFileName(true)
        if (list == null) {
            Aria.download(this)
                .load(url)
                .option(option)
                .setFilePath(filePath)
                .ignoreFilePathOccupy()
                .create()
        } else if (list.all { it.url != url }) {
            Aria.download(this)
                .load(url)
                .option(option)
                .setFilePath(filePath)
                .ignoreFilePathOccupy()
                .create()
        } else {
            return false
        }
        return true
    }

    fun getFilePath(): Uri {
        return Uri.fromFile(context.getExternalFilesDir(Environment.DIRECTORY_DCIM)?:context.filesDir)
    }

    companion object {
        const val TAG = "downloadTest"
    }
}