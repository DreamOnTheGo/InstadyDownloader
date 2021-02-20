package com.instady.download.utils

import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.instady.download.R
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class WechatUtil(val context: Context) {
    fun wechatDonate(): Boolean {
        val bitmap = BitmapFactory.decodeResource(context.resources, R.mipmap.wechat_donate, null)
        saveMediaToStorage(bitmap)
        return openWechatScan()
    }

    private fun saveMediaToStorage(bitmap: Bitmap) {
        val filename = "instady_wechat.jpg"
        var fos: OutputStream? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            context.contentResolver?.also { resolver ->
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }
                val imageUri: Uri? =
                    resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                fos = imageUri?.let { resolver.openOutputStream(it) }
            }
        } else {
            val imagesDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val image = File(imagesDir, filename)
            fos = FileOutputStream(image)
        }
        fos?.use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
        }
    }

    private fun openWechatScan(): Boolean {
        val intent = Intent("com.tencent.mm.action.BIZSHORTCUT")
        intent.setPackage("com.tencent.mm")
        intent.putExtra("LauncherUI.From.Scaner.Shortcut", true)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        return try {
            context.startActivity(intent)
            true
        } catch (var3: ActivityNotFoundException) {
            false
        }
    }
}