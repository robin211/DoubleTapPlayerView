package com.github.vkay94.doubletapplayerviewexample

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

/**
 * Created by damai.subimawanto on 7/6/2020.
 */
class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    }
}