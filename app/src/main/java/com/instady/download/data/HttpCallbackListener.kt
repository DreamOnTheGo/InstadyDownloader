package com.instady.download.data

interface HttpCallbackListener {
    fun onFinish(resource: OutResource)
    fun onError(e: Exception)
}