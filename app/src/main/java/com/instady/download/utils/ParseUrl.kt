package com.instady.download.utils

import android.os.Looper
import android.util.Log
import com.instady.download.data.DownloadUrl
import com.instady.download.data.HttpCallbackListener
import com.instady.download.data.OutResource
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.regex.Pattern
import kotlin.concurrent.thread

class ParseUrl {
    companion object {
        fun getHttpUrl(url: String): String {
            val regex = "(http:|https:)//[A-Za-z0-9._?%&+\\-=/#]*"
            val pattern = Pattern.compile(regex)
            val matcher = pattern.matcher(url)
            return if (matcher.find()) {
                matcher.group()
            } else {
                ""
            }
        }
        const val TAG = "instagram"
    }

    private val client = OkHttpClient()
    private fun getParseUrl(url: String): String {
        val index = url.indexOf("?")
        return if (index > 0)
            url.substring(0, index)
        else
            url
    }

    fun getDownloadUrl(url: String, listener: HttpCallbackListener) {
        when {
            url.contains("instagram") -> downloadFromInstagram(url, listener)
            url.contains("douyin") -> downloadFromDouyin(url, listener)
            else -> listener.onError(Exception("error in url"))
        }
    }

    private fun downloadFromDouyin(input: String, listener: HttpCallbackListener) {
        thread {
            Looper.prepare()
            try {
                val url = getHttpUrl(input)
                Log.d(TAG, "url: $url")
                var request = Request.Builder().url(url)
                    .addHeader(
                        "User-Agent",
                        "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.141 Mobile Safari/537.36"
                    ).build()
                var response = client.newCall(request).execute();
                val newUrl = response.request.url.toString()
                Log.d(TAG, "newUrl: $newUrl")
                val aweme_id = getAwemeId(newUrl)
                Log.d(TAG, "aweme_id:" + aweme_id.toString())
                val parseUrl =
                    "https://aweme-hl.snssdk.com/aweme/v1/aweme/detail/?aweme_id=$aweme_id&device_platform=ios&app_name=aweme&aid=1128"
                Log.d("xdy123", parseUrl)
                request = Request.Builder().url(parseUrl)
                    .addHeader(
                        "User-Agent",
                        "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.141 Mobile Safari/537.36"
                    ).build()
                response = client.newCall(request).execute()
                val responseData = response.body?.string()
                if (responseData != null) {
                    val jsonData = JSONObject(responseData)
                        .getJSONObject("aweme_detail")
                        .getJSONObject("video")
                        .getJSONObject("play_addr")
                    val urlList = jsonData.getJSONArray("url_list")
                    val fileName = "${aweme_id}_douyin.mp4"
                    val videoUrl = urlList[0]
                    val outResource = OutResource("video")
                    outResource.addUrl(DownloadUrl(fileName, videoUrl.toString()))
                    listener.onFinish(outResource)
                    Log.d("xdy", videoUrl.toString())
                } else {
                    throw Exception()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                listener.onError(e)
            }
            Looper.loop()
        }
    }

    private fun getAwemeId(url: String): String? {
        val start = url.indexOf("video/") + "video/".length
        val end = url.indexOf("/", start)
        Log.d(TAG, "url: $url, start: $start, end: $end")
        return if (start >= 0 && end >= 0) {
            url.substring(start, end)
        } else {
            null
        }
    }

    private fun downloadFromInstagram(url: String, listener: HttpCallbackListener) {
        thread {
            Looper.prepare()
            try {
                val parseUrl = getParseUrl(url)
                val request = Request.Builder()
                    .url("$parseUrl?__a=1")
                    .addHeader(
                        "User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)" +
                                " AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.141 Safari/537.36"
                    )
                    .build()
                val response = client.newCall(request).execute()
                val responseData = response.body?.string()
                if (responseData != null) {
                    val jsonData = JSONObject(responseData)
                        .getJSONObject("graphql")
                        .getJSONObject("shortcode_media")
                    when (jsonData.getString("__typename")) {
                        "GraphVideo" -> {
                            listener.onFinish(getVideoUrl(jsonData))
                        }
                        "GraphImage" -> {
                            listener.onFinish(getImageUrl(jsonData))
                        }
                        "GraphSidecar" -> {
                            listener.onFinish(getAllImageUrl(jsonData))
                        }
                        else -> {
                            throw Exception()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                listener.onError(e)
            }
            Looper.loop()
        }
    }

    private fun getVideoUrl(jsonData: JSONObject): OutResource {
        var outResorce = OutResource("video")
        val videoUrl = jsonData.getString("video_url")
        val fileName = videoUrl.split(".mp4")[0].split("/").last() + "_instagram.mp4"
        Log.d(TAG, "video结果：$videoUrl fileName: $fileName")
        outResorce.addUrl(DownloadUrl(fileName, videoUrl))
        return outResorce
    }

    private fun getImageUrl(jsonData: JSONObject): OutResource {
        var outResorce = OutResource("image")
        val imgUrl = jsonData.getString("display_url")
        val fileName = imgUrl.split(".jpg")[0].split("/").last() + "_instagram.jpg"
        Log.d(TAG, "img结果：$imgUrl fileName: $fileName")
        outResorce.addUrl(DownloadUrl(fileName, imgUrl))
        return outResorce
    }

    private fun getAllImageUrl(jsonData: JSONObject): OutResource {
        var outResource = OutResource("image")
        val imgGroup = jsonData.getJSONObject("edge_sidecar_to_children")
            .getJSONArray("edges")
        for (i in 0 until imgGroup.length()) {
            val imgUrl =
                JSONObject(imgGroup[i].toString()).getJSONObject("node").getString("display_url")
            val fileName = imgUrl.split(".jpg")[0].split("/").last() + "_instagram.jpg"
            Log.d(TAG, "img结果：$imgUrl fileName: $fileName")
            outResource.addUrl(DownloadUrl(fileName, imgUrl))
        }
        return outResource
    }

}