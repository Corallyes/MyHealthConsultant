package com.example.myhealthconsultant.presentation.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import android.content.Intent
import android.provider.AlarmClock
import android.widget.Toast
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myhealthconsultant.data.local.entity.MedicationPlan
import com.example.myhealthconsultant.presentation.components.DraggableFloatingButton
import com.example.myhealthconsultant.ui.theme.*

/**
 * 我的页面 - 极简设计
 * 日历式用药管理，视觉类似"极简课表"
 */
@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel = hiltViewModel(),
    onNavigateToProfile: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }

    // 获取选中日期的计划
    val selectedDayPlans = remember(uiState.selectedDayOfWeek, uiState.plans) {
        uiState.plans // 所有计划每天都执行
    }
    val isTodaySelected = remember(uiState.weekDates, uiState.selectedDayOfWeek) {
        uiState.weekDates.find { it.dayOfWeekInt == uiState.selectedDayOfWeek }?.isToday == true
    }
    // 选中日期的打卡记录（直接读 uiState 确保能触发重组）
    val checkedPlanIds = remember(uiState.weekRecords, uiState.selectedDayOfWeek) {
        (uiState.weekRecords[uiState.selectedDayOfWeek] ?: emptyList())
            .map { it.planId }.toSet()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // 用户信息区域 - 点击可进入设置
        UserHeader(
            nickname = uiState.user?.nickname ?: "未设置昵称",
            consecutiveDays = uiState.consecutiveDays,
            totalCheckIns = uiState.totalCheckIns,
            completionRate = uiState.completionRate,
            todayPlansCount = uiState.plans.size,
            onClick = onNavigateToProfile
        )

        // 周视图 - 带切换功能
        WeekView(
            weekDates = uiState.weekDates,
            weekOffset = uiState.weekOffset,
            onPreviousWeek = { viewModel.previousWeek() },
            onNextWeek = { viewModel.nextWeek() },
            onCurrentWeek = { viewModel.goToCurrentWeek() },
            onDaySelected = { viewModel.selectDay(it) }
        )

        // 选中日期的用药计划
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 标题
            item {
                val selectedDate = uiState.weekDates.find { it.dayOfWeekInt == uiState.selectedDayOfWeek }
                val titleText = if (selectedDate?.isToday == true) {
                    "今日用药"
                } else {
                    "${selectedDate?.date ?: ""}日用药"
                }
                Text(
                    text = titleText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // 上午
            item {
                TimeSlotSection(
                    title = "上午",
                    plans = selectedDayPlans.filter { it.timeSlot == "morning" },
                    checkedPlanIds = checkedPlanIds,
                    onCheckIn = viewModel::checkIn,
                    isToday = isTodaySelected
                )
            }

            // 下午
            item {
                TimeSlotSection(
                    title = "下午",
                    plans = selectedDayPlans.filter { it.timeSlot == "afternoon" },
                    checkedPlanIds = checkedPlanIds,
                    onCheckIn = viewModel::checkIn,
                    isToday = isTodaySelected
                )
            }

            // 晚上
            item {
                TimeSlotSection(
                    title = "晚上",
                    plans = selectedDayPlans.filter { it.timeSlot == "evening" },
                    checkedPlanIds = checkedPlanIds,
                    onCheckIn = viewModel::checkIn,
                    isToday = isTodaySelected
                )
            }
        }
    }

    // 添加按钮 - 可拖拽
    DraggableFloatingButton(
        onClick = { showAddDialog = true },
        onPositionChange = { x, y -> viewModel.saveFabPosition(x, y) },
        savedPosition = uiState.fabPosition
    ) {
        Icon(
            Icons.Default.Add,
            contentDescription = "添加用药",
            modifier = Modifier.size(24.dp)
        )
    }

    // 添加用药计划对话框
    if (showAddDialog) {
        AddMedicationPlanDialog(
            drugs = uiState.filteredDrugs,
            searchQuery = uiState.drugSearchQuery,
            onSearch = viewModel::searchDrugs,
            onDismiss = { showAddDialog = false },
            onConfirm = { drug, dosage, timeSlot, hour, minute, mealBasedTime, setSystemAlarm, notes ->
                viewModel.addPlan(drug, dosage, timeSlot, hour, minute, mealBasedTime, setSystemAlarm, notes)
                showAddDialog = false
                // 跳转系统闹钟应用
                if (setSystemAlarm && hour != null && minute != null) {
                    try {
                        val alarmIntent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                            putExtra(AlarmClock.EXTRA_HOUR, hour)
                            putExtra(AlarmClock.EXTRA_MINUTES, minute)
                            putExtra(AlarmClock.EXTRA_MESSAGE, "吃药: ${drug.name} $dosage")
                            putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                            putExtra(AlarmClock.EXTRA_VIBRATE, true)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(alarmIntent)
                    } catch (e: Exception) {
                        android.util.Log.e("CalendarScreen", "Failed to launch alarm", e)
                        Toast.makeText(context, "无法打开闹钟: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        )
    }
}

/**
 * 用户信息区域 - 可点击进入设置
 */
@Composable
private fun UserHeader(
    nickname: String,
    consecutiveDays: Int,
    totalCheckIns: Int,
    completionRate: Float,
    todayPlansCount: Int = 0,
    onClick: () -> Unit = {}
) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(24.dp)
        ) {
            // 头像和基本信息
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 头像
                Surface(
                    modifier = Modifier.size(64.dp),
                    shape = RoundedCornerShape(0.dp),
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = nickname,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "今日${todayPlansCount}项用药",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
                
                // 点击提示图标
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = "进入设置",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // 统计信息
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    value = "$consecutiveDays",
                    label = "连续天数"
                )
                StatItem(
                    value = "$totalCheckIns",
                    label = "总打卡"
                )
                StatItem(
                    value = "${(completionRate * 100).toInt()}%",
                    label = "完成率"
                )
            }
        }
    }
}

/**
 * 统计项 - 极简设计
 */
@Composable
private fun StatItem(
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
    }
}

/**
 * 周视图 - 带切换功能
 */
@Composable
private fun WeekView(
    weekDates: List<CalendarDate>,
    weekOffset: Int,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
    onCurrentWeek: () -> Unit,
    onDaySelected: (Int) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // 周标题和切换按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 上一周按钮
                IconButton(
                    onClick = onPreviousWeek,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.ChevronLeft,
                        contentDescription = "上一周",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // 当前周信息
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 显示月份
                    val firstDate = weekDates.firstOrNull()
                    val lastDate = weekDates.lastOrNull()
                    val monthText = if (firstDate != null && lastDate != null) {
                        if (firstDate.month == lastDate.month) {
                            "${firstDate.year}年${firstDate.month + 1}月"
                        } else {
                            "${firstDate.month + 1}月 - ${lastDate.month + 1}月"
                        }
                    } else {
                        ""
                    }

                    Text(
                        text = monthText,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // 当前周显示"本周"标签
                    if (weekOffset == 0) {
                        Surface(
                            shape = RoundedCornerShape(0.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "本周",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // 非当前周显示"回到本周"按钮
                    if (weekOffset != 0) {
                        Surface(
                            onClick = onCurrentWeek,
                            shape = RoundedCornerShape(0.dp),
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                text = "回到本周",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // 下一周按钮
                IconButton(
                    onClick = onNextWeek,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = "下一周",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 日期行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                weekDates.forEach { calendarDate ->
                    DayItem(
                        calendarDate = calendarDate,
                        onClick = { onDaySelected(calendarDate.dayOfWeekInt) }
                    )
                }
            }
        }
    }
}

/**
 * 日期项
 */
@Composable
private fun RowScope.DayItem(
    calendarDate: CalendarDate,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .weight(1f)
            .clickable { onClick() }
            .padding(vertical = 4.dp)
    ) {
        // 星期
        Text(
            text = calendarDate.dayOfWeek,
            style = MaterialTheme.typography.labelSmall,
            color = when {
                calendarDate.isSelected -> MaterialTheme.colorScheme.primary
                calendarDate.isToday -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
        )

        Spacer(modifier = Modifier.height(6.dp))

        // 日期数字 - 选中状态有圆形背景
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(32.dp)
                .background(
                    color = when {
                        calendarDate.isSelected -> MaterialTheme.colorScheme.primary
                        calendarDate.isToday -> MaterialTheme.colorScheme.primaryContainer
                        else -> Color.Transparent
                    },
                    shape = RoundedCornerShape(0.dp)
                )
        ) {
            Text(
                text = "${calendarDate.date}",
                style = MaterialTheme.typography.bodyMedium,
                color = when {
                    calendarDate.isSelected -> MaterialTheme.colorScheme.onPrimary
                    calendarDate.isToday -> MaterialTheme.colorScheme.onPrimaryContainer
                    else -> MaterialTheme.colorScheme.onSurface
                },
                fontWeight = if (calendarDate.isToday || calendarDate.isSelected) FontWeight.SemiBold else FontWeight.Normal
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 用药指示点
        if (calendarDate.hasMedication) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(0.dp)
                    )
            )
        } else {
            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}

/**
 * 时间段区块 - 极简设计
 */
@Composable
private fun TimeSlotSection(
    title: String,
    plans: List<MedicationPlan>,
    checkedPlanIds: Set<String>,
    onCheckIn: (MedicationPlan) -> Unit,
    isToday: Boolean = false
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (plans.isEmpty()) {
            Text(
                text = "暂无用药计划",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            plans.forEach { plan ->
                MedicationPlanItem(
                    plan = plan,
                    isCheckedIn = plan.id in checkedPlanIds,
                    onCheckIn = { onCheckIn(plan) },
                    isToday = isToday
                )
                if (plan != plans.last()) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

/**
 * 用药计划项 - 极简设计
 */
@Composable
private fun MedicationPlanItem(
    plan: MedicationPlan,
    isCheckedIn: Boolean,
    onCheckIn: () -> Unit,
    isToday: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(0.dp)
            )
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "%02d:%02d".format(plan.reminderHour ?: 8, plan.reminderMinute ?: 0),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(60.dp)
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = plan.drugName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = plan.dosage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (isCheckedIn && isToday) {
            // 今天已打卡 → 可点击取消
            IconButton(
                onClick = onCheckIn,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "取消打卡",
                    modifier = Modifier.size(24.dp),
                    tint = Success
                )
            }
        } else if (!isCheckedIn && isToday) {
            // 今天未打卡 → 可点击打卡
            IconButton(
                onClick = onCheckIn,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    Icons.Default.AddCircleOutline,
                    contentDescription = "打卡",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        } else if (isCheckedIn && !isToday) {
            // 非今天已打卡 → 只显示对号，不可操作
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = "已打卡",
                modifier = Modifier.size(24.dp),
                tint = Success.copy(alpha = 0.6f)
            )
        } else {
            // 非今天未打卡 → 小圆点，不可操作
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp)
                    )
            )
        }
    }
}

/**
 * 添加用药计划对话框
 */
@Composable
private fun AddMedicationPlanDialog(
    drugs: List<com.example.myhealthconsultant.data.local.entity.Drug>,
    searchQuery: String,
    onSearch: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (com.example.myhealthconsultant.data.local.entity.Drug, String, String, Int?, Int?, String?, Boolean, String?) -> Unit
) {
    var selectedDrug by remember { mutableStateOf<com.example.myhealthconsultant.data.local.entity.Drug?>(null) }
    var dosage by remember { mutableStateOf("") }
    var selectedTimeSlot by remember { mutableStateOf("morning") }
    var selectedMealTime by remember { mutableStateOf<String?>(null) }
    var reminderHour by remember { mutableStateOf<Int?>(null) }
    var reminderMinute by remember { mutableStateOf<Int?>(null) }
    var setSystemAlarm by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf("") }
    
    // 时间段选项
    val timeSlots = listOf(
        "morning" to "上午",
        "afternoon" to "下午",
        "evening" to "晚上"
    )
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(0.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // 顶部标题栏
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "添加用药计划",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "关闭",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                
                // 内容区域
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 未选药品时：显示搜索和药品列表
                    if (selectedDrug == null) {
                        // 药品搜索
                        item {
                            Column {
                                Text(
                                    text = "选择药品",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = onSearch,
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = {
                                        Text(
                                            "搜索药品名称...",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Search,
                                            contentDescription = "搜索",
                                            modifier = Modifier.size(20.dp),
                                            tint = if (searchQuery.isNotEmpty())
                                                MaterialTheme.colorScheme.primary
                                            else
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    },
                                    trailingIcon = {
                                        if (searchQuery.isNotEmpty()) {
                                            IconButton(onClick = { onSearch("") }) {
                                                Icon(
                                                    Icons.Default.Clear,
                                                    contentDescription = "清除",
                                                    modifier = Modifier.size(18.dp),
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                        focusedBorderColor = MaterialTheme.colorScheme.primary
                                    ),
                                    shape = RoundedCornerShape(0.dp)
                                )
                            }
                        }

                        // 药品列表
                        if (drugs.isNotEmpty()) {
                            item {
                                Column {
                                    drugs.forEach { drug ->
                                        Surface(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { selectedDrug = drug },
                                            shape = RoundedCornerShape(0.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(12.dp)
                                            ) {
                                                Text(
                                                    text = drug.name,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Medium,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                if (drug.genericName != null) {
                                                    Text(
                                                        text = drug.genericName,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                }
                            }
                        }
                    }

                    // 已选药品信息（选中后显示在顶部，带更换按钮）
                    if (selectedDrug != null) {
                        item {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "已选药品",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    TextButton(onClick = {
                                        selectedDrug = null
                                        onSearch("")
                                    }) {
                                        Text("更换", style = MaterialTheme.typography.labelMedium)
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(0.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp)
                                    ) {
                                        Text(
                                            text = selectedDrug!!.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Text(
                                            text = "分类: ${selectedDrug!!.category}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 剂量输入（仅选中药品后显示）
                    if (selectedDrug != null) {
                    item {
                        Column {
                            Text(
                                text = "剂量",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            
                            OutlinedTextField(
                                value = dosage,
                                onValueChange = { dosage = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = {
                                    Text(
                                        "例如: 1片, 2粒, 5ml",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                    focusedBorderColor = MaterialTheme.colorScheme.primary
                                ),
                                shape = RoundedCornerShape(0.dp)
                            )
                        }
                    }
                    
                    // 时间段选择
                    item {
                        Column {
                            Text(
                                text = "用药时间",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                timeSlots.forEach { (slot, label) ->
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { selectedTimeSlot = slot },
                                        shape = RoundedCornerShape(0.dp),
                                        color = if (selectedTimeSlot == slot)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelMedium,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 12.dp),
                                            textAlign = TextAlign.Center,
                                            color = if (selectedTimeSlot == slot)
                                                MaterialTheme.colorScheme.onPrimary
                                            else
                                                MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    // 提醒时间（可选）
                    item {
                        Column {
                            Text(
                                text = "提醒时间（可选）",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            // 常见餐后时间选项
                            Text(
                                text = "快速选择",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val mealOptions = listOf(
                                    "after_breakfast" to "早饭后",
                                    "after_lunch" to "午饭后",
                                    "after_dinner" to "晚饭后"
                                )
                                mealOptions.forEach { (key, label) ->
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable {
                                                selectedMealTime = if (selectedMealTime == key) null else key
                                                // 自动填充对应时间
                                                if (selectedMealTime == key) {
                                                    val time = com.example.myhealthconsultant.presentation.calendar.CalendarViewModel.MEAL_TIMES[key]
                                                    reminderHour = time?.first
                                                    reminderMinute = time?.second
                                                } else {
                                                    reminderHour = null
                                                    reminderMinute = null
                                                }
                                            },
                                        shape = RoundedCornerShape(0.dp),
                                        color = if (selectedMealTime == key)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelMedium,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 8.dp),
                                            textAlign = TextAlign.Center,
                                            color = if (selectedMealTime == key)
                                                MaterialTheme.colorScheme.onPrimary
                                            else
                                                MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // 自定义时间输入
                            Text(
                                text = "自定义时间",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // 小时选择
                                OutlinedTextField(
                                    value = reminderHour?.toString() ?: "",
                                    onValueChange = { value ->
                                        reminderHour = value.toIntOrNull()?.coerceIn(0, 23)
                                        selectedMealTime = null
                                    },
                                    modifier = Modifier.weight(1f),
                                    placeholder = {
                                        Text(
                                            "时",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                        focusedBorderColor = MaterialTheme.colorScheme.primary
                                    ),
                                    shape = RoundedCornerShape(0.dp)
                                )

                                Text(":", color = MaterialTheme.colorScheme.onSurface)

                                // 分钟选择
                                OutlinedTextField(
                                    value = reminderMinute?.toString() ?: "",
                                    onValueChange = { value ->
                                        reminderMinute = value.toIntOrNull()?.coerceIn(0, 59)
                                        selectedMealTime = null
                                    },
                                    modifier = Modifier.weight(1f),
                                    placeholder = {
                                        Text(
                                            "分",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                        focusedBorderColor = MaterialTheme.colorScheme.primary
                                    ),
                                    shape = RoundedCornerShape(0.dp)
                                )

                                // 清除按钮
                                if (reminderHour != null || reminderMinute != null) {
                                    IconButton(
                                        onClick = {
                                            reminderHour = null
                                            reminderMinute = null
                                            selectedMealTime = null
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "清除时间",
                                            modifier = Modifier.size(18.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            // 提示信息
                            if (reminderHour != null && reminderMinute != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(0.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                ) {
                                    Text(
                                        text = "将在 %02d:%02d 前后各30分钟内提醒（共3次）".format(reminderHour, reminderMinute),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }

                            // 系统闹钟选项
                            if (reminderHour != null && reminderMinute != null) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "同时设置系统闹钟",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "打开系统时钟应用设置闹钟",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Switch(
                                        checked = setSystemAlarm,
                                        onCheckedChange = { setSystemAlarm = it },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                                            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                                        )
                                    )
                                }
                            }
                        }
                    }
                    
                    // 备注（可选）
                    item {
                        Column {
                            Text(
                                text = "备注（可选）",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            
                            OutlinedTextField(
                                value = notes,
                                onValueChange = { notes = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp),
                                placeholder = {
                                    Text(
                                        "添加备注信息...",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                    focusedBorderColor = MaterialTheme.colorScheme.primary
                                ),
                                shape = RoundedCornerShape(0.dp)
                            )
                        }
                    }
                    } // end if (selectedDrug != null)

                    // 底部留白
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
                
                // 底部按钮
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 取消按钮
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(0.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text("取消")
                    }
                    
                    // 确定按钮
                    Button(
                        onClick = {
                            if (selectedDrug != null && dosage.isNotBlank()) {
                                onConfirm(
                                    selectedDrug!!,
                                    dosage,
                                    selectedTimeSlot,
                                    reminderHour,
                                    reminderMinute,
                                    selectedMealTime,
                                    setSystemAlarm,
                                    notes.ifBlank { null }
                                )
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = selectedDrug != null && dosage.isNotBlank(),
                        shape = RoundedCornerShape(0.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text("确定")
                    }
                }
            }
        }
    }
}
