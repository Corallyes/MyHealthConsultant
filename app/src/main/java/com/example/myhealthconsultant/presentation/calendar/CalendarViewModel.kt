package com.example.myhealthconsultant.presentation.calendar

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.provider.AlarmClock
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import androidx.work.OneTimeWorkRequestBuilder
import com.example.myhealthconsultant.data.local.entity.Drug
import com.example.myhealthconsultant.data.local.entity.MedicationPlan
import com.example.myhealthconsultant.data.local.entity.MedicationRecord
import com.example.myhealthconsultant.data.local.entity.User
import com.example.myhealthconsultant.domain.repository.DrugRepository
import com.example.myhealthconsultant.domain.repository.MedicationPlanRepository
import com.example.myhealthconsultant.domain.repository.MedicationRecordRepository
import com.example.myhealthconsultant.domain.repository.UserRepository
import com.example.myhealthconsultant.util.DataStoreManager
import com.example.myhealthconsultant.util.MedicationReminderWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private const val TAG = "CalendarViewModel"

data class CalendarUiState(
    val user: User? = null,
    val plans: List<MedicationPlan> = emptyList(),
    val todayRecords: List<MedicationRecord> = emptyList(),
    val weekPlans: Map<Int, List<MedicationPlan>> = emptyMap(), // 一周每天的用药计划
    val weekRecords: Map<Int, List<MedicationRecord>> = emptyMap(), // 一周每天的用药记录
    val selectedDayOfWeek: Int = Calendar.getInstance().get(Calendar.DAY_OF_WEEK), // 选中的星期几
    val consecutiveDays: Int = 0,
    val totalCheckIns: Int = 0,
    val completionRate: Float = 0f,
    val weekDates: List<CalendarDate> = emptyList(),
    val weekOffset: Int = 0, // 周偏移量，0=本周，-1=上周，1=下周
    val isLoading: Boolean = false,
    val error: String? = null,
    val drugs: List<Drug> = emptyList(),
    val filteredDrugs: List<Drug> = emptyList(),
    val drugSearchQuery: String = "",
    val fabPosition: Pair<Float, Float> = Pair(Float.NaN, Float.NaN)
)

data class CalendarDate(
    val dayOfWeek: String,
    val date: Int,
    val month: Int,
    val year: Int,
    val isToday: Boolean,
    val isSelected: Boolean,
    val hasMedication: Boolean,
    val dayOfWeekInt: Int // Calendar.DAY_OF_WEEK 值
)

@HiltViewModel
class CalendarViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val planRepository: MedicationPlanRepository,
    private val recordRepository: MedicationRecordRepository,
    private val userRepository: UserRepository,
    private val drugRepository: DrugRepository,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        Log.e(TAG, "CalendarViewModel init")
        loadUserInfo()
        loadPlans()
        loadWeekData()
        loadDrugs()
        loadFabPosition()
    }

    private fun loadUserInfo() {
        viewModelScope.launch {
            val userId = dataStoreManager.getLoggedInUserId()
            if (userId != null) {
                userRepository.getUserById(userId).collect { user ->
                    _uiState.update { it.copy(user = user) }
                }
            }
        }
    }

    private fun loadPlans() {
        viewModelScope.launch {
            val userId = dataStoreManager.getLoggedInUserId() ?: "local_user"
            planRepository.getActivePlans(userId).collect { plans ->
                _uiState.update {
                    it.copy(plans = plans)
                }
                // 计划变化后重新计算统计数据
                computeStats(userId)
            }
        }
    }

    /**
     * 加载一周数据（日期、计划、记录）
     * 日期计算是纯同步操作，不需要在协程中执行
     */
    private fun loadWeekData() {
        val weekOffset = _uiState.value.weekOffset
        val weekDates = computeWeekDates(weekOffset)

        _uiState.update { it.copy(weekDates = weekDates) }

        // 异步加载用药记录数据
        viewModelScope.launch {
            try {
                val userId = dataStoreManager.getLoggedInUserId() ?: "local_user"
                loadMedicationData(userId)
            } catch (e: Exception) {
                Log.e(TAG, "Error loading medication data", e)
            }
        }
    }

    private fun computeWeekDates(weekOffset: Int): List<CalendarDate> {
        val calendar = Calendar.getInstance()
        val today = calendar.get(Calendar.DAY_OF_MONTH)
        val todayCalendar = Calendar.getInstance()

        // 调整到本周一
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val mondayOffset = if (dayOfWeek == Calendar.SUNDAY) -6 else Calendar.MONDAY - dayOfWeek
        calendar.add(Calendar.DAY_OF_MONTH, mondayOffset)

        // 应用周偏移
        calendar.add(Calendar.WEEK_OF_YEAR, weekOffset)

        val weekDays = listOf("一", "二", "三", "四", "五", "六", "日")
        val weekDates = mutableListOf<CalendarDate>()
        val isCurrentWeek = weekOffset == 0

        for (i in 0 until 7) {
            val date = calendar.get(Calendar.DAY_OF_MONTH)
            val month = calendar.get(Calendar.MONTH)
            val year = calendar.get(Calendar.YEAR)
            val dayOfWeekInt = calendar.get(Calendar.DAY_OF_WEEK)

            val isToday = isCurrentWeek && (date == today &&
                    month == todayCalendar.get(Calendar.MONTH) &&
                    year == todayCalendar.get(Calendar.YEAR))

            // 只有当前周才标记选中状态
            val isSelected = isCurrentWeek && dayOfWeekInt == _uiState.value.selectedDayOfWeek

            weekDates.add(
                CalendarDate(
                    dayOfWeek = weekDays[i],
                    date = date,
                    month = month,
                    year = year,
                    isToday = isToday,
                    isSelected = isSelected,
                    hasMedication = false,
                    dayOfWeekInt = dayOfWeekInt
                )
            )

            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        return weekDates
    }

    private suspend fun loadMedicationData(userId: String) {
        val weekDates = _uiState.value.weekDates
        val weekRecords = mutableMapOf<Int, List<MedicationRecord>>()
        val updatedDates = mutableListOf<CalendarDate>()

        for (calDate in weekDates) {
            // 计算当天零点时间戳
            val dayCal = Calendar.getInstance().apply {
                set(Calendar.YEAR, calDate.year)
                set(Calendar.MONTH, calDate.month)
                set(Calendar.DAY_OF_MONTH, calDate.date)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val dayTimestamp = dayCal.timeInMillis

            // 从数据库加载当天的记录
            val records = recordRepository.getRecordsByDate(userId, dayTimestamp).first()
            weekRecords[calDate.dayOfWeekInt] = records
            if (records.isNotEmpty()) {
                Log.d(TAG, "loadMedicationData: day=${calDate.date}/${calDate.month} dow=${calDate.dayOfWeekInt} records=${records.size}")
            }

            updatedDates.add(calDate.copy(hasMedication = records.isNotEmpty()))
        }

        Log.d(TAG, "loadMedicationData: total weekRecords keys=${weekRecords.keys}, non-empty=${weekRecords.filter { it.value.isNotEmpty() }.keys}")
        _uiState.update {
            it.copy(
                weekRecords = weekRecords,
                weekDates = updatedDates
            )
        }

        // 计算统计数据
        computeStats(userId)
    }

    private suspend fun computeStats(userId: String) {
        val plans = _uiState.value.plans
        if (plans.isEmpty()) {
            _uiState.update { it.copy(consecutiveDays = 0, totalCheckIns = 0, completionRate = 0f) }
            return
        }

        // 获取最近60天的记录
        val allRecords = recordRepository.getRecentRecords(userId, limit = 500).first()

        // 总打卡数 = 去重后的记录数（每个plan每天只算一次）
        val distinctRecords = allRecords.distinctBy { "${it.planId}_${it.takenDate}" }
        val totalCheckIns = distinctRecords.size

        // 连续天数：从今天往回数，每天所有计划都打卡才算连续
        val todayCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val planIds = plans.map { it.id }.toSet()
        val recordsByDate = distinctRecords.groupBy { record ->
            val cal = Calendar.getInstance().apply { timeInMillis = record.takenDate }
            cal.get(Calendar.YEAR) * 10000 + (cal.get(Calendar.MONTH) + 1) * 100 + cal.get(Calendar.DAY_OF_MONTH)
        }

        var consecutiveDays = 0
        val checkCal = todayCal.clone() as Calendar
        for (i in 0 until 60) {
            val dateKey = checkCal.get(Calendar.YEAR) * 10000 +
                    (checkCal.get(Calendar.MONTH) + 1) * 100 +
                    checkCal.get(Calendar.DAY_OF_MONTH)
            val dayRecords = recordsByDate[dateKey] ?: emptyList()
            val completedPlanIds = dayRecords.map { it.planId }.toSet()

            if (completedPlanIds.containsAll(planIds)) {
                consecutiveDays++
            } else {
                // 今天还没全部打卡不算中断，继续往回检查
                if (i == 0 && completedPlanIds.isNotEmpty()) {
                    // 今天部分打卡，跳过不中断
                } else {
                    break
                }
            }
            checkCal.add(Calendar.DAY_OF_MONTH, -1)
        }

        // 完成率：最近7天的完成情况
        val weekCal = todayCal.clone() as Calendar
        weekCal.add(Calendar.DAY_OF_MONTH, -6)
        var expectedCount = 0
        var actualCount = 0
        for (i in 0 until 7) {
            val dateKey = weekCal.get(Calendar.YEAR) * 10000 +
                    (weekCal.get(Calendar.MONTH) + 1) * 100 +
                    weekCal.get(Calendar.DAY_OF_MONTH)
            expectedCount += plans.size
            val dayRecords = recordsByDate[dateKey] ?: emptyList()
            actualCount += dayRecords.distinctBy { it.planId }.size
            weekCal.add(Calendar.DAY_OF_MONTH, 1)
        }
        val completionRate = if (expectedCount > 0) actualCount.toFloat() / expectedCount else 0f

        Log.d(TAG, "computeStats: totalCheckIns=$totalCheckIns, consecutiveDays=$consecutiveDays, completionRate=$completionRate")
        _uiState.update {
            it.copy(
                consecutiveDays = consecutiveDays,
                totalCheckIns = totalCheckIns,
                completionRate = completionRate
            )
        }
    }

    /**
     * 切换到上一周
     */
    fun previousWeek() {
        _uiState.update { it.copy(weekOffset = it.weekOffset - 1) }
        loadWeekData()
    }

    /**
     * 切换到下一周
     */
    fun nextWeek() {
        _uiState.update { it.copy(weekOffset = it.weekOffset + 1) }
        loadWeekData()
    }

    /**
     * 回到本周
     */
    fun goToCurrentWeek() {
        _uiState.update {
            it.copy(
                weekOffset = 0,
                selectedDayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
            )
        }
        loadWeekData()
    }

    /**
     * 选择某一天
     */
    fun selectDay(dayOfWeekInt: Int) {
        _uiState.update { it.copy(selectedDayOfWeek = dayOfWeekInt) }
        loadWeekData()
    }

    /**
     * 获取选中日期的计划
     */
    fun getSelectedDayPlans(): List<MedicationPlan> {
        val selectedDay = _uiState.value.selectedDayOfWeek
        return _uiState.value.weekPlans[selectedDay] ?: _uiState.value.plans
    }

    /**
     * 获取选中日期的记录
     */
    fun getSelectedDayRecords(): List<MedicationRecord> {
        val selectedDay = _uiState.value.selectedDayOfWeek
        return _uiState.value.weekRecords[selectedDay] ?: emptyList()
    }

    fun checkIn(plan: MedicationPlan) {
        Log.d(TAG, "checkIn called: plan=${plan.id}, drug=${plan.drugName}")
        viewModelScope.launch {
            val userId = dataStoreManager.getLoggedInUserId() ?: "local_user"
            val selectedDay = _uiState.value.selectedDayOfWeek
            val selectedDate = _uiState.value.weekDates.find { it.dayOfWeekInt == selectedDay }

            val calendar = Calendar.getInstance().apply {
                if (selectedDate != null) {
                    set(Calendar.YEAR, selectedDate.year)
                    set(Calendar.MONTH, selectedDate.month)
                    set(Calendar.DAY_OF_MONTH, selectedDate.date)
                }
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val dayTimestamp = calendar.timeInMillis

            // 用当前 UI 状态判断，避免并发导致重复插入
            val alreadyChecked = isCheckedIn(plan.id)

            if (alreadyChecked) {
                // 已打卡 → 查出记录并删除
                val existingRecord = recordRepository.getRecordByPlanAndDate(userId, plan.id, dayTimestamp)
                if (existingRecord != null) {
                    try {
                        recordRepository.deleteRecord(existingRecord)
                        Log.d(TAG, "checkIn: record deleted (uncheck)")
                    } catch (e: Exception) {
                        Log.e(TAG, "checkIn: delete failed", e)
                        return@launch
                    }
                    // 即时更新 UI，移除该记录
                    _uiState.update { state ->
                        val updatedRecords = state.weekRecords.toMutableMap()
                        val dayList = (updatedRecords[selectedDay] ?: emptyList()).toMutableList()
                        dayList.removeAll { it.planId == plan.id }
                        updatedRecords[selectedDay] = dayList
                        state.copy(weekRecords = updatedRecords)
                    }
                }
            } else {
                // 未打卡 → 插入记录
                val now = Calendar.getInstance()
                val timeStr = "%02d:%02d".format(
                    now.get(Calendar.HOUR_OF_DAY),
                    now.get(Calendar.MINUTE)
                )

                val record = MedicationRecord(
                    userId = userId,
                    planId = plan.id,
                    drugName = plan.drugName,
                    dosage = plan.dosage,
                    takenDate = dayTimestamp,
                    takenTime = timeStr,
                    isTaken = true,
                    notes = null
                )

                try {
                    recordRepository.insertRecord(record)
                    Log.d(TAG, "checkIn: record inserted")
                } catch (e: Exception) {
                    Log.e(TAG, "checkIn: insert failed", e)
                    return@launch
                }
                // 即时更新 UI，添加该记录
                _uiState.update { state ->
                    val updatedRecords = state.weekRecords.toMutableMap()
                    val dayList = (updatedRecords[selectedDay] ?: emptyList()).toMutableList()
                    dayList.add(record)
                    updatedRecords[selectedDay] = dayList
                    state.copy(weekRecords = updatedRecords)
                }
            }

            // 后台刷新数据（同步统计信息等）
            loadMedicationData(userId)
        }
    }

    fun isCheckedIn(planId: String): Boolean {
        val selectedDay = _uiState.value.selectedDayOfWeek
        val records = _uiState.value.weekRecords[selectedDay] ?: emptyList()
        return records.any { it.planId == planId }
    }

    private fun getTodayTimestamp(): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

    /**
     * 加载药品库数据
     */
    private fun loadDrugs() {
        viewModelScope.launch {
            drugRepository.getAllDrugs().collect { drugs ->
                _uiState.update {
                    it.copy(
                        drugs = drugs,
                        filteredDrugs = drugs
                    )
                }
            }
        }
    }

    private fun loadFabPosition() {
        viewModelScope.launch {
            val pos = dataStoreManager.getFabPosition("calendar")
            _uiState.update { it.copy(fabPosition = pos) }
        }
    }

    fun saveFabPosition(x: Float, y: Float) {
        viewModelScope.launch {
            dataStoreManager.setFabPosition("calendar", x, y)
            _uiState.update { it.copy(fabPosition = Pair(x, y)) }
        }
    }

    /**
     * 搜索药品
     */
    fun searchDrugs(query: String) {
        _uiState.update { state ->
            state.copy(
                drugSearchQuery = query,
                filteredDrugs = if (query.isEmpty()) {
                    state.drugs
                } else {
                    state.drugs.filter { drug ->
                        drug.name.contains(query, ignoreCase = true) ||
                        drug.genericName?.contains(query, ignoreCase = true) == true
                    }
                }
            )
        }
    }

    /**
     * 添加用药计划
     */
    fun addPlan(
        drug: Drug,
        dosage: String,
        timeSlot: String,
        reminderHour: Int?,
        reminderMinute: Int?,
        mealBasedTime: String?,
        setSystemAlarm: Boolean,
        notes: String?
    ) {
        viewModelScope.launch {
            val userId = dataStoreManager.getLoggedInUserId() ?: "local_user"
            val today = getTodayTimestamp()

            val plan = MedicationPlan(
                userId = userId,
                drugId = drug.id,
                drugName = drug.name,
                dosage = dosage,
                frequency = "每日1次",
                timeSlot = timeSlot,
                startDate = today,
                endDate = null,
                reminderEnabled = reminderHour != null,
                reminderHour = reminderHour,
                reminderMinute = reminderMinute,
                mealBasedTime = mealBasedTime,
                setSystemAlarm = setSystemAlarm,
                notes = notes
            )
            planRepository.insertPlan(plan)

            // 如果设置了提醒时间，调度提醒
            if (reminderHour != null && reminderMinute != null) {
                scheduleReminder(plan)
            }
        }
    }

    companion object {
        // 餐后时间预设
        val MEAL_TIMES = mapOf(
            "after_breakfast" to Pair(8, 0),   // 早饭后 8:00
            "after_lunch" to Pair(12, 30),      // 午饭后 12:30
            "after_dinner" to Pair(18, 30)      // 晚饭后 18:30
        )
    }

    private fun scheduleReminder(plan: MedicationPlan) {
        val hour = plan.reminderHour ?: return
        val minute = plan.reminderMinute ?: return

        // 调度3个通知：-30分钟、准时、+30分钟
        scheduleNotificationAtOffset(plan, hour, minute, -30, "提前")
        scheduleNotificationAtOffset(plan, hour, minute, 0, "")
        scheduleNotificationAtOffset(plan, hour, minute, 30, "补漏")

        // 如果设置了系统闹钟
        if (plan.setSystemAlarm) {
            setSystemAlarm(plan.drugName, plan.dosage, hour, minute)
        }
    }

    private fun scheduleNotificationAtOffset(
        plan: MedicationPlan,
        hour: Int,
        minute: Int,
        offsetMinutes: Int,
        label: String
    ) {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.MINUTE, offsetMinutes)
        }
        if (target.before(now)) {
            target.add(Calendar.DAY_OF_MONTH, 1)
        }
        val delay = target.timeInMillis - now.timeInMillis

        val suffix = if (label.isNotEmpty()) " ($label)" else ""
        val inputData = workDataOf(
            MedicationReminderWorker.KEY_DRUG_NAME to plan.drugName,
            MedicationReminderWorker.KEY_DOSAGE to plan.dosage,
            MedicationReminderWorker.KEY_NOTIFICATION_ID to (plan.id.hashCode() + offsetMinutes + 1000)
        )

        val workRequest = OneTimeWorkRequestBuilder<MedicationReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .addTag("reminder_${plan.id}_$offsetMinutes")
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "medication_reminder_${plan.id}_$offsetMinutes",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    /**
     * 设置系统闹钟
     */
    private fun setSystemAlarm(drugName: String, dosage: String, hour: Int, minute: Int) {
        try {
            val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                putExtra(AlarmClock.EXTRA_HOUR, hour)
                putExtra(AlarmClock.EXTRA_MINUTES, minute)
                putExtra(AlarmClock.EXTRA_MESSAGE, "吃药: $drugName $dosage")
                putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                putExtra(AlarmClock.EXTRA_VIBRATE, true)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // 设备不支持闹钟应用
        }
    }

    fun cancelReminder(planId: String) {
        WorkManager.getInstance(context).cancelUniqueWork("medication_reminder_${planId}_-30")
        WorkManager.getInstance(context).cancelUniqueWork("medication_reminder_${planId}_0")
        WorkManager.getInstance(context).cancelUniqueWork("medication_reminder_${planId}_30")
    }
}
