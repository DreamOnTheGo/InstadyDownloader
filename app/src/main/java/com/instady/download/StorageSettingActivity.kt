package com.instady.download

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.instady.download.fragment.DownloadFragment
import com.instady.download.utils.DownloadUtil
import kotlinx.android.synthetic.main.activity_storage_setting.*


class StorageSettingActivity : BaseActivity() {
    private lateinit var downloadUtil: DownloadUtil
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_storage_setting)
        setSupportActionBar(toolbar)
        downloadUtil = DownloadUtil(this)
        val uri = downloadUtil.getFilePath()
        position_text.text = uri.path

        val prefs = getSharedPreferences("data", Context.MODE_PRIVATE)
        album_storage_switch.isChecked = prefs.getBoolean("saveToAlbum", true)
        storage_position_card.setOnClickListener {
            openASpecificFolderInFileManager(this, uri.path)
        }
        album_storage_switch.setOnCheckedChangeListener { _, isChecked ->
            run {
                val editor = getSharedPreferences("data", Context.MODE_PRIVATE).edit()
                editor.putBoolean("saveToAlbum", isChecked)
                editor.apply()
                DownloadFragment.saveToAlbum = isChecked
            }
        }
    }
    private fun openASpecificFolderInFileManager(context: Context, path: String?) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(Uri.parse(path), "resource/folder")
        if (intent.resolveActivityInfo(context.packageManager, 0) != null) {
            context.startActivity(Intent.createChooser(intent, "Open with"))
        } else {
            showSnackBar(icon_constraint_layout, getString(R.string.file_manager_not_found))
        }
    }
}