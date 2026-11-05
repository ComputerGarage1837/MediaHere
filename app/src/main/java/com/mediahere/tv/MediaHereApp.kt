package com.mediahere.tv

import android.app.Application

class MediaHereApp : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
        com.mediahere.tv.util.AppLog.init(this)
        com.mediahere.tv.data.Format.init(this)
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, error ->
            try {
                com.mediahere.tv.util.AppLog.crash("Crash", "uncaught exception on ${thread.name}", error)
                com.mediahere.tv.data.Prefs(this).crashed = true
            } catch (_: Throwable) {}
            previous?.uncaughtException(thread, error)
        }
        com.mediahere.tv.data.EpgCache.init(this)
        if (android.os.Build.VERSION.SDK_INT < 25) {
            // Poster and logo downloads go through the shared client too, so the extra trusted roots apply to them.
            try {
                com.bumptech.glide.Glide.get(this).registry.replace(
                    com.bumptech.glide.load.model.GlideUrl::class.java, java.io.InputStream::class.java,
                    com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader.Factory(com.mediahere.tv.data.XtreamApi.client)
                )
            } catch (e: Exception) {
                com.mediahere.tv.util.AppLog.w("Tls", "Glide client not replaced: ${e.message}")
            }
        }
        com.mediahere.tv.data.WatchProgress.init(this)
        Thread { com.mediahere.tv.player.TimeshiftServer.cleanup(this) }.start()
    }

    companion object {
        lateinit var instance: MediaHereApp
            private set
    }
}
