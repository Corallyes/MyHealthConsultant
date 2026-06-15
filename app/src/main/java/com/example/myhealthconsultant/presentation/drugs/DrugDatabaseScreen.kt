package com.example.myhealthconsultant.presentation.drugs

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.myhealthconsultant.R
import com.example.myhealthconsultant.data.local.entity.Drug
import com.example.myhealthconsultant.ui.theme.*

/**
 * 药品库页面 - 微扁平化设计
 * 卡片边缘锐利（2-4dp圆角），细微投影增加层次感
 * 深青色与白色对比配色，严谨整洁
 */
@Composable
fun DrugDatabaseScreen(
    viewModel: DrugViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedDrug by remember { mutableStateOf<Drug?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // 搜索栏 - 微扁平化
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = viewModel::onSearchQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = {
                Text(
                    "搜索药品名称...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "搜索",
                    modifier = Modifier.size(20.dp),
                    tint = if (uiState.searchQuery.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            },
            trailingIcon = {
                if (uiState.searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = "清除",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedBorderColor = MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(0.dp)  // 锐利边缘
        )

        // 分类筛选
        CategoryFilter(
            categories = uiState.categories,
            selectedCategory = uiState.selectedCategory,
            showOnlyFavorites = uiState.showOnlyFavorites,
            onCategorySelected = viewModel::onCategorySelected,
            onFavoritesSelected = viewModel::onFavoritesSelected
        )

        // 药品列表
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 判断是否显示推荐药品（搜索框为空且未选择分类且未选择收藏）
            val isShowingRecommendations = uiState.searchQuery.isEmpty() && uiState.selectedCategory == null && !uiState.showOnlyFavorites

            if (isShowingRecommendations && uiState.recommendedDrugs.isNotEmpty()) {
                // 显示推荐药品标题和刷新按钮
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "为你推荐",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        IconButton(onClick = { viewModel.refreshRecommendations() }) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "换一批",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                items(uiState.recommendedDrugs) { drug ->
                    DrugCard(
                        drug = drug,
                        onFavoriteClick = { viewModel.toggleFavorite(drug.id) },
                        onClick = { selectedDrug = drug }
                    )
                }
            } else {
                items(uiState.filteredDrugs) { drug ->
                    DrugCard(
                        drug = drug,
                        onFavoriteClick = { viewModel.toggleFavorite(drug.id) },
                        onClick = { selectedDrug = drug }
                    )
                }

                if (uiState.filteredDrugs.isEmpty()) {
                    item {
                        if (uiState.showOnlyFavorites) {
                            FavoritesEmptyState()
                        } else {
                            EmptyState()
                        }
                    }
                }
            }
        }
    }

    // 药品详情弹窗
    selectedDrug?.let { drug ->
        DrugDetailDialog(
            drug = drug,
            onDismiss = { selectedDrug = null },
            onFavoriteClick = { viewModel.toggleFavorite(drug.id) }
        )
    }
}

/**
 * 分类筛选 - 横向滚动标签
 */
@Composable
private fun CategoryFilter(
    categories: List<String>,
    selectedCategory: String?,
    showOnlyFavorites: Boolean,
    onCategorySelected: (String?) -> Unit,
    onFavoritesSelected: () -> Unit
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CategoryChip(
            text = "全部",
            isSelected = selectedCategory == null && !showOnlyFavorites,
            onClick = { onCategorySelected(null) }
        )

        CategoryChip(
            text = "收藏",
            isSelected = showOnlyFavorites,
            onClick = onFavoritesSelected,
            icon = if (showOnlyFavorites) Icons.Default.Favorite else Icons.Default.FavoriteBorder
        )

        categories.forEach { category ->
            CategoryChip(
                text = category,
                isSelected = selectedCategory == category,
                onClick = { onCategorySelected(category) }
            )
        }
    }
}

/**
 * 分类标签 - 微扁平化，锐利边缘，沉稳色彩
 */
@Composable
private fun CategoryChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(0.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(0.dp),  // 锐利边缘 2dp
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        contentColor = if (isSelected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurface
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

/**
 * 药品卡片 - 微扁平化设计
 * 2-4dp圆角，细微投影，深青色配色
 */
@Composable
private fun DrugCard(
    drug: Drug,
    onFavoriteClick: () -> Unit,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(0.dp),  // 4dp圆角，边缘锐利
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 药品图片/图标
                DrugImage(
                    imageResId = drug.imageResId,
                    imageUrl = drug.imageUrl,
                    category = drug.category,
                    modifier = Modifier.size(48.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = drug.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
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

                IconButton(onClick = onFavoriteClick) {
                    Icon(
                        if (drug.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (drug.isFavorite) "取消收藏" else "收藏",
                        modifier = Modifier.size(20.dp),
                        tint = if (drug.isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 分类标签 - 实心色块，沉稳色彩
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(0.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = drug.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Surface(
                    shape = RoundedCornerShape(0.dp),
                    color = if (drug.type.contains("OTC") || drug.type.contains("非处方"))
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        Warning.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = drug.type,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (drug.type.contains("OTC") || drug.type.contains("非处方"))
                            MaterialTheme.colorScheme.primary
                        else
                            Warning,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 功能描述 - 标准系统字体，字号较小但清晰
            Text(
                text = drug.indications,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 空状态 - 极简设计
 */
@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(64.dp),
                shape = RoundedCornerShape(0.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.SearchOff,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "暂无相关药品",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "请尝试其他搜索关键词",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

/**
 * 收藏空状态 - 极简设计
 */
@Composable
private fun FavoritesEmptyState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(64.dp),
                shape = RoundedCornerShape(0.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "暂无收藏药品",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "点击药品卡片上的爱心图标即可收藏",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

/**
 * 药品详情弹窗 - 微扁平化设计
 */
@Composable
private fun DrugDetailDialog(
    drug: Drug,
    onDismiss: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(0.dp),  // 锐利边缘
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // 顶部栏
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "药品详情",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row {
                        IconButton(onClick = onFavoriteClick) {
                            Icon(
                                if (drug.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = if (drug.isFavorite) "取消收藏" else "收藏",
                                tint = if (drug.isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "关闭",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)

                // 内容
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 药品名称
                    item {
                        Column {
                            Text(
                                text = drug.name,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (drug.genericName != null) {
                                Text(
                                    text = drug.genericName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // 药品大图
                            Spacer(modifier = Modifier.height(12.dp))
                            DrugImage(
                                imageResId = drug.imageResId,
                                imageUrl = drug.imageUrl,
                                category = drug.category,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Surface(
                                    shape = RoundedCornerShape(0.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = drug.category,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(0.dp),
                                    color = if (drug.type.contains("OTC") || drug.type.contains("非处方"))
                                        MaterialTheme.colorScheme.primaryContainer
                                    else
                                        Warning.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = drug.type,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (drug.type.contains("OTC") || drug.type.contains("非处方"))
                                            PrimaryDark
                                        else
                                            Warning,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    item { DetailSection(title = "成分", content = drug.ingredients) }
                    item { DetailSection(title = "适应症", content = drug.indications) }
                    item { DetailSection(title = "用法用量", content = drug.dosage) }
                    item { DetailSection(title = "不良反应", content = drug.sideEffects) }
                    item { DetailSection(title = "禁忌", content = drug.contraindications) }

                    drug.precautions?.let { precautions ->
                        item { DetailSection(title = "注意事项", content = precautions) }
                    }
                }
            }
        }
    }
}

/**
 * 药品图片组件
 * 优先：本地 drawable → 网络图片 → 分类兜底图 → 默认图标
 */
@Composable
private fun DrugImage(
    imageResId: Int?,
    imageUrl: String?,
    category: String = "",
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(0.dp),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        when {
            imageResId != null -> {
                Image(
                    painter = painterResource(id = imageResId),
                    contentDescription = "药品图片",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            !imageUrl.isNullOrBlank() -> {
                var loadFailed by remember { mutableStateOf(false) }
                if (!loadFailed) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "药品图片",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        onError = { loadFailed = true }
                    )
                } else {
                    CategoryFallbackImage(category)
                }
            }
            else -> {
                CategoryFallbackImage(category)
            }
        }
    }
}

/**
 * 分类兜底图 - 同分类复用同一张代表图
 */
@Composable
private fun CategoryFallbackImage(category: String) {
    val fallbackResId = when (category) {
        "解热镇痛" -> R.drawable.drug_paracetamol
        "感冒用药" -> R.drawable.drug_lianqingwen
        "胃肠用药" -> R.drawable.drug_mengtuoshi
        "抗过敏药" -> R.drawable.drug_loratadine
        "外用药" -> R.drawable.drug_huoxiang
        "维生素补充" -> R.drawable.drug_vitamin_c
        "中药" -> R.drawable.drug_banlangen
        "心血管用药" -> R.drawable.drug_aspirin
        else -> null
    }
    if (fallbackResId != null) {
        Image(
            painter = painterResource(id = fallbackResId),
            contentDescription = "药品图片",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.6f
        )
    } else {
        DrugIcon()
    }
}

@Composable
private fun DrugIcon() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Default.Medication,
            contentDescription = null,
            modifier = Modifier.size(26.dp),
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

/**
 * 详情区块
 */
@Composable
private fun DetailSection(
    title: String,
    content: String
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
