package com.binaryoptions.aisignal

import android.app.Application
import com.binaryoptions.aisignal.service.SignalBackgroundService

class BinarySignalApp : Application() {

    override fun onCreate() {
        super.onCreate()
        try {
            SignalBackgroundService.startService(this)
        } catch (e: Exception) {
            // Ignore during initial setup before permissions
        }
    }
}
