package com.instady.download.data

class OutResource(type: String) {
    private var type = type
    private var resourceList = ArrayList<DownloadUrl>()

    public fun addUrl(downloadUrl: DownloadUrl) {
        resourceList.add(downloadUrl)
    }

    public fun getResourceList(): ArrayList<DownloadUrl> {
        return resourceList
    }

    public fun getType(): String {
        return type
    }
}