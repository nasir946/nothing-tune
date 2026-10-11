package com.nothing.tune

import android.app.Application
import android.util.Log

class NothingTuneApp : Application() {
    override fun onCreate() {
        super.onCreate()
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("NothingTune", "Uncaught exception on thread ${thread.name}", throwable)
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }
}
