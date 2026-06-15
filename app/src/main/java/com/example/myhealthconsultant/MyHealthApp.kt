package com.example.myhealthconsultant

import android.app.Application
import androidx.work.*
import com.example.myhealthconsultant.util.ExpiryCheckWorker
import com.example.myhealthconsultant.util.NotificationHelper
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

@HiltAndroidApp
class MyHealthApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannel(this)
        // WorkManager调度移到后台线程，避免阻塞主线程导致启动超时
        CoroutineScope(Dispatchers.IO).launch {
            scheduleExpiryCheck()
        }
    }

    private fun scheduleExpiryCheck() {
        val workRequest = PeriodicWorkRequestBuilder<ExpiryCheckWorker>(
            1, TimeUnit.DAYS
        )
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(true)
                    .build()
            )
            .addTag("expiry_check")
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "daily_expiry_check",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}
