package com.example.speertestapp

import android.app.Application
import com.example.speertestapp.data.UserInfoCacheList

class MyApplication : Application() {


    companion object {
        var cacheList = UserInfoCacheList()
    }
}