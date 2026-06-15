package com.example.myhealthconsultant.util

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class MedicationReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val drugName = inputData.getString(KEY_DRUG_NAME) ?: return Result.failure()
        val dosage = inputData.getString(KEY_DOSAGE) ?: return Result.failure()
        val notificationId = inputData.getInt(KEY_NOTIFICATION_ID, 0)

        NotificationHelper.sendMedicationReminder(
            context = applicationContext,
            notificationId = notificationId,
            drugName = drugName,
            dosage = dosage
        )

        return Result.success()
    }

    companion object {
        const val KEY_DRUG_NAME = "drug_name"
        const val KEY_DOSAGE = "dosage"
        const val KEY_NOTIFICATION_ID = "notification_id"
    }
}
