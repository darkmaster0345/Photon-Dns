package com.photondns.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PhotonApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        // Initialize any application-level components here
    }
}
