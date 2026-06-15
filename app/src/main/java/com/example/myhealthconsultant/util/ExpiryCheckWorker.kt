package com.example.myhealthconsultant.util

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.myhealthconsultant.data.local.db.AppDatabase

class ExpiryCheckWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val db = AppDatabase.getInstance(applicationContext)
        val userDao = db.userDao()
        val cabinetDao = db.cabinetMedicineDao()

        // 获取所有用户
        val users = userDao.getAllUsersSync()
        val now = System.currentTimeMillis()
        val sevenDaysLater = now + 7L * 24 * 60 * 60 * 1000

        for (user in users) {
            // 查询即将过期的药品
            val expiringSoon = cabinetDao.getExpiringSoonSync(user.id, sevenDaysLater)
            val expired = cabinetDao.getExpiredSync(user.id, now)

            val expiringCount = expiringSoon.size
            val expiredCount = expired.size

            if (expiringCount > 0 || expiredCount > 0) {
                val message = buildString {
                    if (expiredCount > 0) append("${expiredCount}种药品已过期")
                    if (expiredCount > 0 && expiringCount > 0) append("，")
                    if (expiringCount > 0) append("${expiringCount}种药品即将过期")
                }

                NotificationHelper.sendExpiryNotification(
                    context = applicationContext,
                    notificationId = EXPIRY_NOTIFICATION_ID,
                    title = "药品过期提醒",
                    message = message
                )
            }
        }

        return Result.success()
    }

    companion object {
        const val EXPIRY_NOTIFICATION_ID = 9999
    }
}
