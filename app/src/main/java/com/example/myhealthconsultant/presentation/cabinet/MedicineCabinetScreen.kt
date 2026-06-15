package com.example.myhealthconsultant.presentation.cabinet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myhealthconsultant.data.local.entity.CabinetMedicine
import com.example.myhealthconsultant.data.local.entity.Prescription
import com.example.myhealthconsultant.presentation.components.DraggableFloatingButton
import com.example.myhealthconsultant.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineCabinetScreen(
    viewModel: MedicineCabinetViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var showPrescriptionDialog by remember { mutableStateOf(false) }
    var selectedMedicine by remember { mutableStateOf<CabinetMedicine?>(null) }
    var showThresholdDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            kotlinx.coroutines.delay(2000)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("我的医药箱", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回",
                            tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                actions = {
                    IconButton(onClick = { showPrescriptionDialog = true }) {
                        Icon(Icons.Default.Description, contentDescription = "医嘱管理",
                            tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 统计卡片
                item {
                    StatsRow(
                        total = uiState.totalCount,
                        expiringSoon = uiState.expiringSoonCount,
                        expired = uiState.expiredCount,
                        thresholdDays = uiState.expiryThresholdDays,
                        onThresholdClick = { showThresholdDialog = true }
                    )
                }

                // 搜索栏
                item {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.search(it) },
                        placeholder = { Text("搜索药品名称", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                        },
                        trailingIcon = {
                            if (uiState.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.search("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "清除", modifier = Modifier.size(18.dp))
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(0.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            cursorColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                // 排序选项
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Sort, contentDescription = null, modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        SortType.entries.forEach { sortType ->
                            FilterChip(
                                selected = uiState.sortType == sortType,
                                onClick = { viewModel.setSortType(sortType) },
                                label = { Text(sortType.label, style = MaterialTheme.typography.labelSmall) },
                                shape = RoundedCornerShape(0.dp)
                            )
                        }
                    }
                }

                // 药品列表
                if (uiState.filteredMedicines.isEmpty()) {
                    item {
                        EmptyState(searchQuery = uiState.searchQuery)
                    }
                } else {
                    items(uiState.filteredMedicines, key = { it.id }) { medicine ->
                        MedicineCard(
                            medicine = medicine,
                            thresholdDays = uiState.expiryThresholdDays,
                            onClick = { selectedMedicine = medicine }
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }

            // 消息提示
            uiState.message?.let { message ->
                Snackbar(
                    modifier = Modifier.padding(16.dp).align(Alignment.BottomCenter),
                    containerColor = if (message.contains("成功") || message.contains("已移除")) Success else MaterialTheme.colorScheme.error
                ) { Text(message) }
            }

            // 可拖拽的添加按钮
            DraggableFloatingButton(
                onClick = { showAddDialog = true },
                onPositionChange = { x, y -> viewModel.saveFabPosition(x, y) },
                savedPosition = uiState.fabPosition
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加药品")
            }
        }
    }

    // 添加药品对话框
    if (showAddDialog) {
        AddMedicineDialog(
            onConfirm = { name, genericName, category, spec, qty, unit, expiry, location, notes ->
                viewModel.addMedicine(name, genericName, category, spec, qty, unit, expiry, location, notes)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }

    // 药品详情对话框
    selectedMedicine?.let { medicine ->
        MedicineDetailDialog(
            medicine = medicine,
            prescriptions = uiState.prescriptions,
            thresholdDays = uiState.expiryThresholdDays,
            onUpdate = { viewModel.updateMedicine(it) },
            onDelete = { viewModel.deleteMedicine(it) },
            onLinkPrescription = { medId, presId -> viewModel.linkPrescription(medId, presId) },
            onDismiss = { selectedMedicine = null }
        )
    }

    // 医嘱管理对话框
    if (showPrescriptionDialog) {
        PrescriptionManagementDialog(
            prescriptions = uiState.prescriptions,
            onAdd = { doctor, hospital, diagnosis, notes ->
                viewModel.addPrescription(doctor, hospital, diagnosis, notes)
            },
            onDismiss = { showPrescriptionDialog = false }
        )
    }

    // 过期阈值设置对话框
    if (showThresholdDialog) {
        ThresholdSettingDialog(
            currentDays = uiState.expiryThresholdDays,
            onConfirm = { days ->
                viewModel.setExpiryThresholdDays(days)
                showThresholdDialog = false
            },
            onDismiss = { showThresholdDialog = false }
        )
    }
}

@Composable
private fun StatsRow(total: Int, expiringSoon: Int, expired: Int, thresholdDays: Int = 7, onThresholdClick: () -> Unit = {}) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatCard("药品总数", "$total", MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.primary, Modifier.weight(1f))
        StatCard(
            label = "${thresholdDays}天内到期",
            value = "$expiringSoon",
            bgColor = WarningContainer,
            textColor = Warning,
            modifier = Modifier.weight(1f),
            onClick = onThresholdClick,
            showSettings = true
        )
        StatCard("已过期", "$expired", MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.error, Modifier.weight(1f))
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    bgColor: androidx.compose.ui.graphics.Color,
    textColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier,
    onClick: (() -> Unit)? = null,
    showSettings: Boolean = false
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(0.dp),
        color = bgColor,
        onClick = onClick ?: {}
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = textColor)
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(label, style = MaterialTheme.typography.bodySmall, color = textColor.copy(alpha = 0.8f))
                if (showSettings) {
                    Icon(Icons.Default.Tune, contentDescription = "设置阈值", modifier = Modifier.size(14.dp).padding(start = 2.dp),
                        tint = textColor.copy(alpha = 0.6f))
                }
            }
        }
    }
}

@Composable
private fun EmptyState(searchQuery: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.Inventory2, contentDescription = null, modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            if (searchQuery.isEmpty()) "医药箱空空如也" else "未找到匹配药品",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
        if (searchQuery.isEmpty()) {
            Text("点击右下角 + 添加药品", style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
        }
    }
}

@Composable
private fun MedicineCard(medicine: CabinetMedicine, thresholdDays: Int = 7, onClick: () -> Unit) {
    val now = System.currentTimeMillis()
    val isExpired = medicine.expiryDate != null && medicine.expiryDate < now
    val isExpiringSoon = medicine.expiryDate != null && !isExpired && medicine.expiryDate < now + thresholdDays.toLong() * 24 * 60 * 60 * 1000
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(0.dp),
        color = MaterialTheme.colorScheme.surface,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 图标
            Surface(
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(0.dp),
                color = when {
                    isExpired -> MaterialTheme.colorScheme.errorContainer
                    isExpiringSoon -> WarningContainer
                    else -> MaterialTheme.colorScheme.primaryContainer
                }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        when {
                            isExpired -> Icons.Default.ErrorOutline
                            isExpiringSoon -> Icons.Default.Schedule
                            else -> Icons.Default.Medication
                        },
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = when {
                            isExpired -> MaterialTheme.colorScheme.error
                            isExpiringSoon -> Warning
                            else -> MaterialTheme.colorScheme.primary
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 信息
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    medicine.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!medicine.genericName.isNullOrBlank()) {
                    Text(
                        medicine.genericName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${medicine.quantity}${medicine.unit}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (medicine.specification.isNotBlank()) {
                        Text(
                            " · ${medicine.specification}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // 过期状态标签
            if (isExpired) {
                StatusChip("已过期", MaterialTheme.colorScheme.error, MaterialTheme.colorScheme.errorContainer)
            } else if (isExpiringSoon) {
                StatusChip("即将过期", Warning, WarningContainer)
            } else if (medicine.expiryDate != null) {
                Text(
                    dateFormat.format(Date(medicine.expiryDate)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun StatusChip(text: String, textColor: androidx.compose.ui.graphics.Color, bgColor: androidx.compose.ui.graphics.Color) {
    Surface(shape = RoundedCornerShape(0.dp), color = bgColor) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ======================== 对话框 ========================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddMedicineDialog(
    onConfirm: (name: String, genericName: String?, category: String, spec: String, qty: Int, unit: String, expiry: Long?, location: String, notes: String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var genericName by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("其他") }
    var specification by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }
    var unit by remember { mutableStateOf("盒") }
    var expiryDateMillis by remember { mutableStateOf<Long?>(null) }
    var storageLocation by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var expandedCategory by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    val categories = listOf("感冒类", "消炎类", "肠胃类", "止痛类", "皮肤类", "维生素", "慢性病", "外用药", "其他")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加药品", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    placeholder = { Text("药品名称 *", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)) },
                    singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(0.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedContainerColor = MaterialTheme.colorScheme.surface, unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )
                OutlinedTextField(
                    value = genericName, onValueChange = { genericName = it },
                    placeholder = { Text("通用名 (选填)", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)) },
                    singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(0.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedContainerColor = MaterialTheme.colorScheme.surface, unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )

                // 分类下拉
                ExposedDropdownMenuBox(expanded = expandedCategory, onExpandedChange = { expandedCategory = it }) {
                    OutlinedTextField(
                        value = category, onValueChange = {}, readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(), shape = RoundedCornerShape(0.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedContainerColor = MaterialTheme.colorScheme.surface, unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                    ExposedDropdownMenu(expanded = expandedCategory, onDismissRequest = { expandedCategory = false }) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = { category = cat; expandedCategory = false }
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = specification, onValueChange = { specification = it },
                        placeholder = { Text("规格", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)) },
                        singleLine = true, modifier = Modifier.weight(1f), shape = RoundedCornerShape(0.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedContainerColor = MaterialTheme.colorScheme.surface, unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            cursorColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    OutlinedTextField(
                        value = quantity, onValueChange = { quantity = it.filter { c -> c.isDigit() } },
                        placeholder = { Text("数量", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true, modifier = Modifier.width(80.dp), shape = RoundedCornerShape(0.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedContainerColor = MaterialTheme.colorScheme.surface, unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            cursorColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    OutlinedTextField(
                        value = unit, onValueChange = { unit = it },
                        placeholder = { Text("单位", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)) },
                        singleLine = true, modifier = Modifier.width(64.dp), shape = RoundedCornerShape(0.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedContainerColor = MaterialTheme.colorScheme.surface, unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            cursorColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                // 有效期 - 日期选择器
                OutlinedTextField(
                    value = expiryDateMillis?.let { dateFormat.format(Date(it)) } ?: "",
                    onValueChange = {},
                    readOnly = true,
                    placeholder = { Text("点击选择有效期", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)) },
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = "选择日期",
                                tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(0.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedContainerColor = MaterialTheme.colorScheme.surface, unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )

                OutlinedTextField(
                    value = storageLocation, onValueChange = { storageLocation = it },
                    placeholder = { Text("存放位置 (如 家中药箱)", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)) },
                    singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(0.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedContainerColor = MaterialTheme.colorScheme.surface, unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )

                OutlinedTextField(
                    value = notes, onValueChange = { notes = it },
                    placeholder = { Text("个人备注 (选填)", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)) },
                    maxLines = 3, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(0.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedContainerColor = MaterialTheme.colorScheme.surface, unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(name, genericName.ifBlank { null }, category, specification, quantity.toIntOrNull() ?: 1, unit, expiryDateMillis, storageLocation, notes)
                },
                enabled = name.isNotBlank(),
                shape = RoundedCornerShape(0.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) { Text("添加") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(0.dp)
    )

    // 日期选择器
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = expiryDateMillis
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    expiryDateMillis = datePickerState.selectedDateMillis
                    showDatePicker = false
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("取消") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun MedicineDetailDialog(
    medicine: CabinetMedicine,
    prescriptions: List<Prescription>,
    thresholdDays: Int = 7,
    onUpdate: (CabinetMedicine) -> Unit,
    onDelete: (CabinetMedicine) -> Unit,
    onLinkPrescription: (String, String?) -> Unit,
    onDismiss: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showLinkDialog by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }
    var editNotes by remember { mutableStateOf(medicine.notes) }
    var editQuantity by remember { mutableStateOf(medicine.quantity.toString()) }

    val now = System.currentTimeMillis()
    val isExpired = medicine.expiryDate != null && medicine.expiryDate < now
    val isExpiringSoon = medicine.expiryDate != null && !isExpired && medicine.expiryDate < now + thresholdDays.toLong() * 24 * 60 * 60 * 1000

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(medicine.name, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                if (isExpired) StatusChip("已过期", MaterialTheme.colorScheme.error, MaterialTheme.colorScheme.errorContainer)
                else if (isExpiringSoon) StatusChip("即将过期", Warning, WarningContainer)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!medicine.genericName.isNullOrBlank()) {
                    DetailRow("通用名", medicine.genericName)
                }
                DetailRow("分类", medicine.category)
                if (medicine.specification.isNotBlank()) DetailRow("规格", medicine.specification)

                if (isEditing) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("库存:", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        OutlinedTextField(
                            value = editQuantity, onValueChange = { editQuantity = it.filter { c -> c.isDigit() } },
                            singleLine = true, modifier = Modifier.width(80.dp), shape = RoundedCornerShape(0.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                focusedContainerColor = MaterialTheme.colorScheme.surface, unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                cursorColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        Text(medicine.unit, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    DetailRow("库存", "${medicine.quantity}${medicine.unit}")
                }

                if (medicine.expiryDate != null) {
                    DetailRow("有效期至", dateFormat.format(Date(medicine.expiryDate)))
                }
                if (medicine.storageLocation.isNotBlank()) DetailRow("存放位置", medicine.storageLocation)

                // 医嘱关联
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("关联医嘱", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f))
                    TextButton(onClick = { showLinkDialog = true }) {
                        Text(if (medicine.prescriptionId != null) "更换" else "关联", style = MaterialTheme.typography.bodySmall)
                    }
                }
                if (medicine.prescriptionId != null) {
                    val linkedPrescription = prescriptions.find { it.id == medicine.prescriptionId }
                    if (linkedPrescription != null) {
                        Surface(
                            shape = RoundedCornerShape(0.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                if (linkedPrescription.doctorName.isNotBlank())
                                    Text("${linkedPrescription.doctorName}医生", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                if (linkedPrescription.diagnosis.isNotBlank())
                                    Text(linkedPrescription.diagnosis, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                } else {
                    Text("未关联医嘱", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                }

                // 备注
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Text("个人备注", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                if (isEditing) {
                    OutlinedTextField(
                        value = editNotes, onValueChange = { editNotes = it },
                        placeholder = { Text("添加备注...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)) },
                        maxLines = 3, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(0.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedContainerColor = MaterialTheme.colorScheme.surface, unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            cursorColor = MaterialTheme.colorScheme.primary
                        )
                    )
                } else {
                    Text(
                        if (medicine.notes.isBlank()) "暂无备注" else medicine.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (medicine.notes.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            Row {
                TextButton(onClick = { showDeleteConfirm = true }) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
                Spacer(modifier = Modifier.weight(1f))
                if (isEditing) {
                    TextButton(onClick = {
                        onUpdate(medicine.copy(quantity = editQuantity.toIntOrNull() ?: medicine.quantity, notes = editNotes))
                        isEditing = false
                    }) { Text("保存") }
                } else {
                    TextButton(onClick = { isEditing = true }) { Text("编辑") }
                }
                TextButton(onClick = onDismiss) { Text("关闭") }
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(0.dp)
    )

    // 删除确认
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("确认删除") },
            text = { Text("确定要将 ${medicine.name} 从医药箱中移除吗？") },
            confirmButton = {
                TextButton(onClick = { onDelete(medicine); showDeleteConfirm = false; onDismiss() }) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("取消") } },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(0.dp)
        )
    }

    // 关联医嘱选择
    if (showLinkDialog) {
        AlertDialog(
            onDismissRequest = { showLinkDialog = false },
            title = { Text("选择医嘱") },
            text = {
                Column {
                    if (prescriptions.isEmpty()) {
                        Text("暂无医嘱，请先在医嘱管理中添加", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        prescriptions.forEach { prescription ->
                            Surface(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                shape = RoundedCornerShape(0.dp),
                                color = if (prescription.id == medicine.prescriptionId) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                onClick = {
                                    onLinkPrescription(medicine.id, if (prescription.id == medicine.prescriptionId) null else prescription.id)
                                    showLinkDialog = false
                                }
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    if (prescription.doctorName.isNotBlank())
                                        Text("${prescription.doctorName}医生", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                                    if (prescription.diagnosis.isNotBlank())
                                        Text(prescription.diagnosis, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    if (prescription.id == medicine.prescriptionId)
                                        Text("已关联", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showLinkDialog = false }) { Text("关闭") } },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(0.dp)
        )
    }
}

@Composable
private fun PrescriptionManagementDialog(
    prescriptions: List<Prescription>,
    onAdd: (String, String, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var showAddForm by remember { mutableStateOf(false) }
    var doctorName by remember { mutableStateOf("") }
    var hospitalName by remember { mutableStateOf("") }
    var diagnosis by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("医嘱管理", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                if (!showAddForm) {
                    IconButton(onClick = { showAddForm = true }) {
                        Icon(Icons.Default.Add, contentDescription = "添加医嘱")
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (showAddForm) {
                    OutlinedTextField(
                        value = doctorName, onValueChange = { doctorName = it },
                        placeholder = { Text("医生姓名", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)) },
                        singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(0.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedContainerColor = MaterialTheme.colorScheme.surface, unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            cursorColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    OutlinedTextField(
                        value = hospitalName, onValueChange = { hospitalName = it },
                        placeholder = { Text("医院名称", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)) },
                        singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(0.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedContainerColor = MaterialTheme.colorScheme.surface, unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            cursorColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    OutlinedTextField(
                        value = diagnosis, onValueChange = { diagnosis = it },
                        placeholder = { Text("诊断结果", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)) },
                        singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(0.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedContainerColor = MaterialTheme.colorScheme.surface, unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            cursorColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    OutlinedTextField(
                        value = notes, onValueChange = { notes = it },
                        placeholder = { Text("医嘱备注", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)) },
                        maxLines = 3, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(0.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedContainerColor = MaterialTheme.colorScheme.surface, unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            cursorColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    Button(
                        onClick = {
                            onAdd(doctorName, hospitalName, diagnosis, notes)
                            doctorName = ""; hospitalName = ""; diagnosis = ""; notes = ""
                            showAddForm = false
                        },
                        enabled = doctorName.isNotBlank() || diagnosis.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(0.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) { Text("保存医嘱") }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                }

                if (prescriptions.isEmpty() && !showAddForm) {
                    Text("暂无医嘱记录", style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp))
                }

                prescriptions.forEach { prescription ->
                    Surface(
                        shape = RoundedCornerShape(0.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            if (prescription.doctorName.isNotBlank())
                                Text("${prescription.doctorName}医生", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                            if (prescription.hospitalName.isNotBlank())
                                Text(prescription.hospitalName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (prescription.diagnosis.isNotBlank())
                                Text("诊断: ${prescription.diagnosis}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (prescription.notes.isNotBlank())
                                Text(prescription.notes, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("关闭") } },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(0.dp)
    )
}

@Composable
private fun ThresholdSettingDialog(
    currentDays: Int,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val options = listOf(7, 15, 30, 60, 90)
    var selected by remember { mutableIntStateOf(currentDays) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("过期提醒阈值", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("设置提前多少天提醒药品即将过期",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))
                options.forEach { days ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(0.dp),
                        color = if (selected == days) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        onClick = { selected = days }
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selected == days,
                                onClick = { selected = days },
                                colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "${days}天",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (selected == days) FontWeight.SemiBold else FontWeight.Normal
                            )
                            if (days == 7) {
                                Text(" (默认)", style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selected) },
                shape = RoundedCornerShape(0.dp)
            ) { Text("确定") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(0.dp)
    )
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row {
        Text("$label: ", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
    }
}
