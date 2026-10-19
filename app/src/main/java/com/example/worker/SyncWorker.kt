package com.example.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.firebase.FirestoreSyncRepository
import com.example.data.local.AppDatabase
import com.example.data.model.NotificationType
import com.example.data.model.SystemNotification
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class SyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val currentUser = Firebase.auth.currentUser
            if (currentUser != null) {
                val db = AppDatabase.getInstance(applicationContext)
                val dao = db.netCardsDao()
                val syncRepo = FirestoreSyncRepository(applicationContext)

                // Push all local Room records to Firestore for offline sync recovery
                val distributors = dao.getAllDistributors().first()
                val categories = dao.getAllCategories().first()
                val transactions = dao.getAllTransactions().first()
                val users = dao.getAllUsersDirect()
                val settings = dao.getSettingsDirect()

                distributors.forEach { syncRepo.syncDistributor(currentUser.uid, it) }
                categories.forEach { syncRepo.syncCategory(currentUser.uid, it) }
                transactions.takeLast(200).forEach { syncRepo.syncTransaction(currentUser.uid, it) }
                users.forEach { syncRepo.syncAppUser(currentUser.uid, it) }
                settings?.let { syncRepo.syncSettings(currentUser.uid, it) }

                // Add a notification upon successful sync to show sync count/status badge on notification icon
                val notification = SystemNotification(
                    title = "مزامنة سحابية ناجحة",
                    message = "تمت مزامنة العمليات المعلقة (بيع، شراء، سندات) بنجاح مع السحابة عند عودة الاتصال بالإنترنت.",
                    type = NotificationType.GENERAL,
                    date = System.currentTimeMillis(),
                    isRead = false
                )
                dao.insertNotifications(listOf(notification))
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
