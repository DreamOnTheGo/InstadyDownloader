package com.instady.download.fragment

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.FileUtils
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.arialyy.annotations.Download
import com.arialyy.aria.core.Aria
import com.arialyy.aria.core.download.DownloadEntity
import com.arialyy.aria.core.task.DownloadTask
import com.instady.download.R
import com.instady.download.adapter.TaskAdapter
import kotlinx.android.synthetic.main.fragment_download.*
import java.io.File
import java.io.FileInputStream
import java.io.OutputStream
import java.net.FileNameMap
import java.net.URLConnection


class DownloadFragment : BaseFragment() {
    private var taskList = ArrayList<DownloadEntity>()
    private lateinit var adapter: TaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Aria.download(this).register()
        val prefs = requireActivity().getSharedPreferences("data", Context.MODE_PRIVATE)
        saveToAlbum = prefs.getBoolean("saveToAlbum", true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_download, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (Aria.download(this).taskList != null) {
            taskList = Aria.download(this).taskList as ArrayList<DownloadEntity>
        }
        recycleView.layoutManager = LinearLayoutManager(this.context)
        recycleView.itemAnimator = null
        adapter = TaskAdapter(taskList)
        recycleView.adapter = adapter
        adapter.setOnItemClickListener(object : TaskAdapter.OnItemClickListener {
            override fun onItemLongClick(
                view: View?,
                position: Int,
                holder: TaskAdapter.ViewHolder
            ) {
                Log.d(TAG, "long click: $position")
                adapter.checkedTaskList.clear()
                adapter.multipleState = !adapter.multipleState
                if (adapter.multipleState) {
                    bottom_tool_bar.visibility = View.VISIBLE
                } else {
                    bottom_tool_bar.visibility = View.GONE
                }
                adapter.notifyDataSetChanged()
            }

            override fun onItemClick(view: View?, position: Int, holder: TaskAdapter.ViewHolder) {
                if (adapter.multipleState) {
                    if (holder.checkBox.isChecked) {
                        adapter.checkedTaskList.remove(taskList[position])
                        holder.checkBox.isChecked = false
                    } else {
                        adapter.checkedTaskList.add(taskList[position])
                        holder.checkBox.isChecked = true
                    }
                }
            }
        })
        delete_btn.setOnClickListener {
            adapter.removeAllChecked()
        }
        return_btn.setOnClickListener {
            adapter.multipleState = false
            bottom_tool_bar.visibility = View.GONE
            adapter.notifyDataSetChanged()
        }
        save_btn.setOnClickListener {
            adapter.saveAllChecked()
        }
        share_btn.setOnClickListener {
            adapter.shareAllChecked()
        }
    }

    @Download.onPre
    fun onPre(task: DownloadTask) {
        Log.d(TAG, "onPre: ")
        updateTaskListForPre(task.downloadEntity)
    }

    @Download.onTaskStart
    fun taskStart(task: DownloadTask) {
        Log.d(TAG, "taskStart: ")
        updateTaskList(task.downloadEntity)
    }

    @Download.onTaskResume
    fun taskResume(task: DownloadTask) {
        Log.d(TAG, "taskResume: ")
        updateTaskList(task.downloadEntity)
    }

    @Download.onTaskRunning
    fun running(task: DownloadTask) {
        Log.d(TAG, "running: ")
        updateTaskList(task.downloadEntity)
    }

    @Download.onWait
    fun onWait(task: DownloadTask) {
        Log.d(TAG, "onWait: ")
        updateTaskList(task.downloadEntity)
    }

    @Download.onTaskStop
    fun taskStop(task: DownloadTask) {
        Log.d(TAG, "taskStop: ")
        updateTaskList(task.downloadEntity)
    }

    @Download.onTaskCancel
    fun taskCancel(task: DownloadTask) {
        Log.d(TAG, "taskCancel: ")
        deleteTask(task.downloadEntity)
    }

    @Download.onTaskFail
    fun taskFail(task: DownloadTask) {
        Log.d(TAG, "taskFail: ")
        updateTaskList(task.downloadEntity)
    }

    @Download.onTaskComplete
    fun taskComplete(task: DownloadTask) {
        Log.d(TAG, "taskComplete: ")
        updateTaskList(task.downloadEntity)
        if (saveToAlbum) {
            activity?.let { updatePhotoAlbum(it, File(task.filePath)) }
        }
    }

    private fun getPositionFromId(id: Long): Int {
        for (i in 0 until taskList.size) {
            Log.d(TAG, "id: $id, task.id: ${taskList[i].id}")
            if (taskList[i].id == id) {
                return i
            }
        }
        return -1
    }

    private fun updateTaskListForPre(task: DownloadEntity) {
        val position = getPositionFromId(task.id)
        if (position == -1) { // 第一次加入的任务
            taskList.add(task)
            adapter.notifyItemChanged(adapter.itemCount - 1)
        }
    }

    private fun updateTaskList(task: DownloadEntity) {
        val position = getPositionFromId(task.id)
        if (position >= 0) {
            taskList[position].percent = task.percent
            taskList[position].state = task.state
            taskList[position].convertSpeed = task.convertSpeed
            adapter.notifyItemChanged(position)
        }
    }

    private fun deleteTask(task: DownloadEntity) {
        val position = getPositionFromId(task.id)
        if (position >= 0) {
            taskList.removeAt(position)
            adapter.notifyItemRemoved(position)
            adapter.notifyDataSetChanged()
        }
    }

    private fun updatePhotoAlbum(mContext: Context, file: File) {
        val type = file.name.split(".").last()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues()
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, file.name)
            values.put(MediaStore.MediaColumns.MIME_TYPE, getMimeType(file))
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM)
            val contentResolver: ContentResolver = mContext.contentResolver
            val uri = when (type) {
                "jpg" -> {
                    contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                        ?: return
                }
                "mp4" -> {
                    contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
                        ?: return
                }
                else -> {
                    contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                        ?: return
                }
            }
            try {
                val out: OutputStream? = contentResolver.openOutputStream(uri)
                if (out != null) {
                    val fis = FileInputStream(file)
                    FileUtils.copy(fis, out)
                    fis.close()
                    out.close()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            MediaScannerConnection.scanFile(
                mContext.applicationContext,
                arrayOf(file.absolutePath),
                arrayOf(getMimeType(file))
            ) { _, _ -> }
        }
    }

    private fun getMimeType(file: File): String? {
        val fileNameMap: FileNameMap = URLConnection.getFileNameMap()
        return fileNameMap.getContentTypeFor(file.name)
    }


    companion object {
        private const val TAG = "instagram"
        var saveToAlbum: Boolean = true
    }
}