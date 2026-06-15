package com.example.myhealthconsultant.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.myhealthconsultant.data.local.dao.*
import com.example.myhealthconsultant.data.local.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Database(
    entities = [
        User::class,
        Drug::class,
        MedicationPlan::class,
        MedicationRecord::class,
        ChatHistory::class,
        ScanHistory::class,
        CabinetMedicine::class,
        Prescription::class
    ],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun drugDao(): DrugDao
    abstract fun medicationPlanDao(): MedicationPlanDao
    abstract fun medicationRecordDao(): MedicationRecordDao
    abstract fun chatHistoryDao(): ChatHistoryDao
    abstract fun scanHistoryDao(): ScanHistoryDao
    abstract fun cabinetMedicineDao(): CabinetMedicineDao
    abstract fun prescriptionDao(): PrescriptionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private suspend fun seedSampleUserData(database: AppDatabase) {
            val userId = "test-user-001"

            // 检查是否已有用药计划，避免重复插入
            val planDao = database.medicationPlanDao()
            val existingPlans = planDao.getActivePlans(userId).first()
            if (existingPlans.isNotEmpty()) return // 已有数据，跳过

            // 插入处方
            val prescriptionDao = database.prescriptionDao()
            for (rx in SampleUserData.getPrescriptions()) {
                prescriptionDao.insert(rx)
            }

            // 插入用药计划
            for (plan in SampleUserData.getMedicationPlans()) {
                planDao.insertPlan(plan)
            }

            // 插入用药记录
            val recordDao = database.medicationRecordDao()
            for (record in SampleUserData.getMedicationRecords()) {
                recordDao.insertRecord(record)
            }

            // 插入医药箱药品
            val cabinetDao = database.cabinetMedicineDao()
            for (med in SampleUserData.getCabinetMedicines()) {
                cabinetDao.insert(med)
            }

            // 插入AI对话历史
            val chatDao = database.chatHistoryDao()
            for (msg in SampleUserData.getChatHistory()) {
                chatDao.insertMessage(msg)
            }

            // 插入扫描历史
            val scanDao = database.scanHistoryDao()
            for (scan in SampleUserData.getScanHistory()) {
                scanDao.insertScan(scan)
            }

            // 标记收藏药品
            val drugDao = database.drugDao()
            for (drugId in SampleUserData.getFavoriteDrugIds()) {
                drugDao.toggleFavorite(drugId, true)
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "myhealthconsultant.db"
                )
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // 插入测试用户 - 使用固定的salt确保密码验证一致
                        val salt = "testSalt"
                        val hash = java.security.MessageDigest.getInstance("SHA-256")
                            .digest("${salt}123456".toByteArray())
                            .joinToString("") { "%02x".format(it) }
                        val passwordHash = "$salt:$hash"
                        val now: Long = System.currentTimeMillis()

                        db.execSQL(
                            "INSERT OR REPLACE INTO users (id, phone, passwordHash, nickname, avatarUrl, wechatOpenId, createdAt, updatedAt) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                            arrayOf<Any?>("test-user-001", "13972293217", passwordHash, "测试用户", null, null, now, now)
                        )

                        // 插入药品示例数据
                        CoroutineScope(Dispatchers.IO).launch {
                            INSTANCE?.let { database ->
                                val drugDao = database.drugDao()
                                drugDao.insertDrugs(SampleDrugData.getSampleDrugs())
                            }
                        }
                    }

                    override fun onOpen(db: SupportSQLiteDatabase) {
                        super.onOpen(db)
                        // 每次打开数据库时检查并确保测试用户存在
                        val salt = "testSalt"
                        val hash = java.security.MessageDigest.getInstance("SHA-256")
                            .digest("${salt}123456".toByteArray())
                            .joinToString("") { "%02x".format(it) }
                        val passwordHash = "$salt:$hash"
                        val now: Long = System.currentTimeMillis()

                        db.execSQL(
                            "INSERT OR IGNORE INTO users (id, phone, passwordHash, nickname, avatarUrl, wechatOpenId, createdAt, updatedAt) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                            arrayOf<Any?>("test-user-001", "13972293217", passwordHash, "测试用户", null, null, now, now)
                        )

                        // 检查药品表是否为空，如果为空则插入示例数据
                        CoroutineScope(Dispatchers.IO).launch {
                            INSTANCE?.let { database ->
                                val drugDao = database.drugDao()
                                val count = drugDao.getDrugCount()
                                if (count == 0) {
                                    drugDao.insertDrugs(SampleDrugData.getSampleDrugs())
                                }

                                // 为 13972293217 账号生成丰富的样例数据
                                seedSampleUserData(database)
                            }
                        }
                    }
                })
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
