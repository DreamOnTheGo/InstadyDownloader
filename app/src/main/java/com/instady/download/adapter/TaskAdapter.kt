package com.instady.download.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.os.FileUtils
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import com.arialyy.aria.core.Aria
import com.arialyy.aria.core.download.DownloadEntity
import com.bumptech.glide.Glide
import com.instady.download.BaseActivity
import com.instady.download.BuildConfig
import com.instady.download.R
import kotlinx.android.synthetic.main.fragment_download.*
import java.io.File
import java.io.FileInputStream
import java.io.OutputStream
import java.net.FileNameMap
import java.net.URLConnection


class TaskAdapter(private val taskList: ArrayList<DownloadEntity>) :
    RecyclerView.Adapter<TaskAdapter.ViewHolder>() {
    lateinit var context: Context
    lateinit var stateList: Array<String>
    var multipleState = false
    var checkedTaskList = ArrayList<DownloadEntity>()
    private var onItemClickListener: OnItemClickListener? = null

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val taskName: TextView = view.findViewById(R.id.name_tv)
        val taskProgress: ProgressBar = view.findViewById(R.id.progress_pb)
        val taskSpeed: TextView = view.findViewById(R.id.speed_tv)
        val taskState: TextView = view.findViewById(R.id.state_tv)
        val taskSize: TextView = view.findViewById(R.id.size_tv)
        val imageShow: ImageView = view.findViewById(R.id.image_iv)
        val taskStateImage: ImageView = view.findViewById(R.id.state_iv)
        val menuButton: ImageView = view.findViewById(R.id.menu_iv)
        val playImageView: ImageView = view.findViewById(R.id.play_iv)
        val checkBox: CheckBox = view.findViewById(R.id.checkBox)
    }

    interface OnItemClickListener {
        fun onItemLongClick(view: View?, position: Int, holder: ViewHolder)
        fun onItemClick(view: View?, position: Int, holder: ViewHolder)
    }

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener?) {
        this.onItemClickListener = onItemClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        stateList = arrayOf(
            context.resources.getString(R.string.fail),
            context.resources.getString(R.string.complete),
            context.resources.getString(R.string.stop),
            context.resources.getString(R.string.waiting),
            context.resources.getString(R.string.running),
            context.resources.getString(R.string.pretreatment),
            context.resources.getString(R.string.pretreatment_complete),
            context.resources.getString(R.string.delete),
        )
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val task = taskList[position]
        val state = task.state
        val type = task.fileName.split(".").last()
        when (type) { // 是否显示视频的三角图标
            "mp4" -> holder.playImageView.visibility = View.VISIBLE
            "jpg" -> holder.playImageView.visibility = View.GONE
        }
        Glide.with(context).load(task.url).placeholder(R.drawable.ic_image).into(holder.imageShow)
        holder.taskName.text = task.fileName
        holder.taskSize.text = task.convertFileSize
        holder.taskState.text = stateList[state]
        holder.checkBox.isChecked = false
        if (state == 1) {
            holder.taskProgress.progress = 100
            holder.taskSpeed.text = ""
        } else {
            holder.taskProgress.progress = task.percent % 100
            holder.taskSpeed.text = task.convertSpeed ?: "0kb/s"
        }
        if (onItemClickListener != null) {
            holder.itemView.setOnClickListener(View.OnClickListener { v ->
                onItemClickListener!!.onItemClick(v, position, holder)
            })
            holder.itemView.setOnLongClickListener { v ->
                onItemClickListener!!.onItemLongClick(v, position, holder)
                true
            }
        }
        if (state == 1 && multipleState) {
            holder.taskStateImage.visibility = View.GONE
            holder.menuButton.visibility = View.GONE
            holder.checkBox.visibility = View.VISIBLE
        } else {
            holder.taskStateImage.visibility = View.VISIBLE
            holder.menuButton.visibility = View.VISIBLE
            holder.checkBox.visibility = View.GONE
            val icon = when (state) {
                0 -> R.drawable.ic_fail
                1 -> R.drawable.ic_complete
                2 -> R.drawable.ic_pause
                4 -> R.drawable.ic_start
                else -> R.drawable.ic_wait
            }
            holder.taskStateImage.setImageResource(icon)
            holder.taskStateImage.setOnClickListener {
                when (state) {
                    4 -> Aria.download(this).load(task.id).stop()
                    2 -> Aria.download(this).load(task.id).resume()
                    0 -> Aria.download(this).load(task.id).resume()
                    1 -> {
                        AlertDialog.Builder(context).apply {
                            setTitle(context.getString(R.string.download_again_title))
                            setMessage(context.getString(R.string.download_again_message))
                            setCancelable(true)
                            setPositiveButton(context.resources.getString(R.string.ok)) { _, _ ->
                                run {
                                    Aria.download(this).load(task.id).resume()
                                }
                            }
                            setNegativeButton(context.resources.getString(R.string.cancel)) { _, _ ->
                                Log.d(
                                    "test", "cancel"
                                )
                            }
                            show()
                        }
                    }
                }
            }
            holder.menuButton.setOnClickListener {
                showPopup(holder.menuButton, task)
            }
            holder.imageShow.setOnClickListener {
                if (state == 1) {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    val dataUri = FileProvider.getUriForFile(
                        context, BuildConfig.APPLICATION_ID + ".fileprovider", File(
                            task.filePath
                        )
                    )
                    when (type) {
                        "mp4" -> intent.setDataAndType(dataUri, "video/*")
                        "jpg" -> intent.setDataAndType(dataUri, "image/*")
                    }
                    context.startActivity(intent)
                }
            }
        }
    }

    private fun deleteTask(task: DownloadEntity) {
        val builder = AlertDialog.Builder(context, R.style.AlertDialog)
        builder.setTitle(context.getString(R.string.delete_task_title))
        val items = arrayOf(context.getString(R.string.delete_task_message))
        val checkedItems = booleanArrayOf(true)
        builder.setMultiChoiceItems(items, checkedItems) { _, which, isChecked ->
            checkedItems[which] = isChecked
        }
        builder.setPositiveButton(context.getString(R.string.ok)) { _, _ ->
            Aria.download(this).load(task.id).cancel(checkedItems[0])
            (context as BaseActivity).showSnackBar(
                (context as Activity).findViewById(R.id.download_coordinator_layout),
                context.resources.getString(R.string.delete_success)
            )
        }
        builder.setNegativeButton(context.getString(R.string.cancel)) { _, _ -> }
        builder.show()
    }

    private fun saveTask(task: DownloadEntity) {
        try {
            val file = File(task.filePath)
            val type = getMimeType(file)
            val values = ContentValues()
            values.put(
                MediaStore.MediaColumns.DISPLAY_NAME,
                System.currentTimeMillis().toString() + file.name
            )
            values.put(MediaStore.MediaColumns.MIME_TYPE, type)
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM)
            val contentResolver = context.contentResolver
            val location = when (type) {
                "video/mp4" -> {
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                }
                "image/jpeg" -> {
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                }
                else -> {
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                }
            }
            val uri = contentResolver.insert(location, values) ?: return
            Log.d("uriTest", "uri: $uri")
            val out: OutputStream? = contentResolver.openOutputStream(uri)
            val fis = FileInputStream(file)
            if (out != null) {
                FileUtils.copy(fis, out)
            }
            fis.close()
            out?.close()
            (context as BaseActivity).showSnackBar(
                (context as Activity).findViewById(R.id.download_coordinator_layout),
                context.resources.getString(R.string.save_success),
            )
        } catch (e: Exception) {
            (context as BaseActivity).showSnackBar(
                (context as Activity).findViewById(R.id.download_coordinator_layout),
                context.resources.getString(R.string.save_fail)
            )
            e.printStackTrace()
        }
    }

    private fun shareTask(task: DownloadEntity) {
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(task.filePath))
        when (task.fileName.split(".").last()) {
            "mp4" -> intent.type = "video/*"
            "jpg" -> intent.type = "image/*"
        }
        context.startActivity(
            Intent.createChooser(
                intent,
                context.resources.getString(R.string.choose_app)
            )
        )
    }

    @SuppressLint("RestrictedApi")
    private fun showPopup(v: View, task: DownloadEntity) {
        val popup = PopupMenu(context, v)
        val inflater: MenuInflater = popup.menuInflater
        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.delete -> {
                    deleteTask(task)
                    true
                }
                R.id.save -> {
                    saveTask(task)
                    true
                }
                R.id.share -> {
                    shareTask(task)
                    true
                }
                else -> {
                    true
                }
            }
        }
        inflater.inflate(R.menu.menu_pop, popup.menu)
        if (task.state != 1) {
            popup.menu.setGroupVisible(R.id.complete_group, false)
        }
        popup.show()
    }

    private fun getMimeType(file: File): String? {
        val fileNameMap: FileNameMap = URLConnection.getFileNameMap()
        return fileNameMap.getContentTypeFor(file.name)
    }

    fun removeAllChecked() {
        val builder = AlertDialog.Builder(context, R.style.AlertDialog)
        builder.setTitle(context.getString(R.string.delete_mut_task_title, checkedTaskList.size))
        val items = arrayOf(context.getString(R.string.delete_task_message))
        val checkedItems = booleanArrayOf(true)
        builder.setMultiChoiceItems(items, checkedItems) { _, which, isChecked ->
            checkedItems[which] = isChecked
        }
        builder.setPositiveButton(context.getString(R.string.ok)) { _, _ ->
            for (task in checkedTaskList) {
                Aria.download(this).load(task.id).cancel(checkedItems[0])
            }
            (context as BaseActivity).showSnackBar(
                (context as Activity).findViewById(R.id.download_coordinator_layout),
                context.resources.getString(R.string.delete_success),
            )
            multipleState = false
            (context as Activity).bottom_tool_bar.visibility = View.GONE
            notifyDataSetChanged()
        }
        builder.setNegativeButton(context.getString(R.string.cancel)) { _, _ -> }
        builder.show()
    }

    fun saveAllChecked() {
        for (task in checkedTaskList) {
            val file = File(task.filePath)
            val type = getMimeType(file)
            val values = ContentValues()
            values.put(
                MediaStore.MediaColumns.DISPLAY_NAME,
                System.currentTimeMillis().toString() + file.name
            )
            values.put(MediaStore.MediaColumns.MIME_TYPE, type)
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM)
            val contentResolver = context.contentResolver
            val location = when (type) {
                "video/mp4" -> {
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                }
                "image/jpeg" -> {
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                }
                else -> {
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                }
            }
            val uri = contentResolver.insert(location, values)
            try {
                if (uri == null) {
                    throw Exception("uri is null")
                }
                val out: OutputStream? = contentResolver.openOutputStream(uri)
                val fis = FileInputStream(file)
                if (out != null) {
                    FileUtils.copy(fis, out)
                }
                fis.close()
                out?.close()
                (context as BaseActivity).showSnackBar(
                    (context as Activity).findViewById(R.id.download_coordinator_layout),
                    context.getString(R.string.save_mut_task_success, checkedTaskList.size)
                )
            } catch (e: Exception) {
                (context as BaseActivity).showSnackBar(
                    (context as Activity).findViewById(R.id.download_coordinator_layout),
                    context.getString(R.string.save_fail)
                )
                e.printStackTrace()
            } finally {
                multipleState = false
                (context as Activity).bottom_tool_bar.visibility = View.GONE
                notifyDataSetChanged()
            }
        }
    }

    fun shareAllChecked() {
        val intent = Intent()
        intent.action = Intent.ACTION_SEND_MULTIPLE
        val uriList: ArrayList<Uri> = ArrayList()
        val typeSet = HashSet<String>()
        for (task in checkedTaskList) {
            uriList.add(Uri.parse(task.filePath))
            val type = when (task.fileName.split(".").last()) {
                "mp4" -> "video/*"
                "jpg" -> "image/*"
                else -> "image/*"
            }
            typeSet.add(type)
        }
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uriList)
        intent.type = "*/*"
        intent.putExtra(Intent.EXTRA_MIME_TYPES, typeSet.toArray())
        context.startActivity(
            Intent.createChooser(
                intent,
                context.resources.getString(R.string.choose_app)
            )
        )
        multipleState = false
        (context as Activity).bottom_tool_bar.visibility = View.GONE
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return taskList.size
    }
}
