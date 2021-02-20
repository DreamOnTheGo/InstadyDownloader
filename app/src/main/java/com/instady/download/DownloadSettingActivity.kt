package com.instady.download

import android.os.Bundle
import android.util.Log
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.app.AppCompatActivity
import com.arialyy.aria.core.Aria
import kotlinx.android.synthetic.main.activity_download_setting.*
import kotlinx.android.synthetic.main.activity_main.*


class DownloadSettingActivity : BaseActivity() {
    private var taskNum = 0
    private var threadNum = 0
    private var speed = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_download_setting)
        Aria.download(this).register()
        initTaskNum()
        initThreadNum()
        initSpeed()
    }

    private fun initTaskNum() {
        taskNum = Aria.get(this).downloadConfig.maxTaskNum
        task_num.text = taskNum.toString()
        task_num_seek.progress = taskNum
        task_num_seek.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                taskNum = progress
                task_num.text = taskNum.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                Aria.get(this@DownloadSettingActivity).downloadConfig.maxTaskNum = taskNum
            }
        })
    }

    private fun initThreadNum() {
        threadNum = Aria.get(this).downloadConfig.threadNum
        thread_num.text = threadNum.toString()
        thread_num_seek.progress = threadNum
        thread_num_seek.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                threadNum = progress
                thread_num.text = threadNum.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                Aria.get(this@DownloadSettingActivity).downloadConfig.threadNum = threadNum
            }
        })
    }

    private fun initSpeed() {
        speed = Aria.get(this).downloadConfig.maxSpeed
        speed_seek.progress = if (speed == 0) {
            100
        } else {
            speed / 128
        }
        speed_num.text = getSpeedString(speed)
        speed_seek.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                speed = if (progress == 100) {
                    0
                } else {
                    progress * 128
                }
                speed_num.text = getSpeedString(speed)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                Aria.get(this@DownloadSettingActivity).downloadConfig.maxSpeed = speed
            }
        })
    }

    private fun getSpeedString(speed: Int): String {
        return when {
            speed == 0 -> "max"
            speed >= 1024 -> String.format("%.2f", speed / 1024F) + "mb/s"
            else -> "${speed}kb/s"
        }
    }
}