package com.example.myhealthconsultant.presentation.camera

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myhealthconsultant.data.local.entity.ScanHistory
import com.example.myhealthconsultant.domain.repository.AiRepository
import com.example.myhealthconsultant.domain.repository.DrugRepository
import com.example.myhealthconsultant.domain.repository.OcrRepository
import com.example.myhealthconsultant.domain.repository.ScanHistoryRepository
import com.example.myhealthconsultant.util.DataStoreManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import javax.inject.Inject

data class DrugRecognitionResult(
    val drugName: String,
    val confidence: Float,
    val category: String,
    val indications: String,
    val dosage: String,
    val contraindications: String,
    val isBlurry: Boolean = false,
    val source: String = "database", // "database" 或 "ai"
    val ocrText: String = "" // 原始OCR文字，供AI分析用
)

data class CameraUiState(
    val capturedImageUri: Uri? = null,
    val result: DrugRecognitionResult? = null,
    val showDetail: Boolean = false, // 是否显示详细信息
    val isDetailLoading: Boolean = false, // 详细信息是否正在加载
    val isLoading: Boolean = false,
    val error: String? = null,
    val blurWarning: String? = null,
    val fabPosition: Pair<Float, Float> = Pair(Float.NaN, Float.NaN)
)

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val application: Application,
    private val ocrRepository: OcrRepository,
    private val drugRepository: DrugRepository,
    private val aiRepository: AiRepository,
    private val scanHistoryRepository: ScanHistoryRepository,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    init {
        loadFabPosition()
    }

    companion object {
        private const val BLUR_THRESHOLD = 100.0  // 低于此值判定为模糊
        private const val PREPROCESS_SIZE = 512   // 检测/预处理时缩放到此尺寸
    }

    fun recognizeDrug(imageUri: Uri) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    capturedImageUri = imageUri,
                    isLoading = true,
                    result = null,
                    showDetail = false,
                    isDetailLoading = false,
                    error = null,
                    blurWarning = null
                )
            }

            try {
                // 1. 读取图片
                val originalBitmap = loadBitmap(imageUri)
                if (originalBitmap == null) {
                    _uiState.update { it.copy(error = "无法读取图片", isLoading = false) }
                    return@launch
                }

                // 2. 模糊检测
                val blurScore = detectBlur(originalBitmap)
                val isBlurry = blurScore < BLUR_THRESHOLD

                if (isBlurry) {
                    _uiState.update { it.copy(blurWarning = "检测到图片较模糊，已自动增强处理") }
                }

                // 3. 根据模糊程度决定预处理策略
                val processedBytes = if (isBlurry) {
                    // 模糊图片：锐化 + 对比度增强后再压缩
                    val sharpened = sharpenImage(originalBitmap)
                    val enhanced = enhanceContrast(sharpened)
                    val bytes = bitmapToBytes(enhanced, 95)
                    enhanced.recycle()
                    bytes
                } else {
                    // 清晰图片：直接压缩
                    val bytes = bitmapToBytes(originalBitmap, 85)
                    originalBitmap.recycle()
                    bytes
                }

                // 4. 调用 OCR
                val ocrResponse = ocrRepository.recognizeImage(processedBytes)

                if (!ocrResponse.success) {
                    _uiState.update {
                        it.copy(error = ocrResponse.error ?: "OCR识别失败", isLoading = false)
                    }
                    return@launch
                }

                if (ocrResponse.texts.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            error = if (isBlurry)
                                "图片模糊且未能识别到文字，请尝试重新拍摄，保持光线充足、镜头对焦"
                            else
                                "未能识别到文字，请拍摄更清晰的药品图片",
                            isLoading = false
                        )
                    }
                    return@launch
                }

                // 5. 匹配药品库
                val allDrugs = drugRepository.getAllDrugs().first()
                val ocrText = ocrResponse.texts.joinToString(" ")
                android.util.Log.d("CameraViewModel", "OCR texts: ${ocrResponse.texts}")
                android.util.Log.d("CameraViewModel", "OCR scores: ${ocrResponse.scores}")
                val avgScore = if (ocrResponse.scores.isNotEmpty()) {
                    ocrResponse.scores.average().toFloat()
                } else 0.5f

                // 模糊图片降低置信度
                val adjustedConfidence = if (isBlurry) avgScore * 0.8f else avgScore

                val matchedDrug = allDrugs.find { drug ->
                    ocrText.contains(drug.name) ||
                    (drug.genericName != null && ocrText.contains(drug.genericName))
                }

                val result = if (matchedDrug != null) {
                    DrugRecognitionResult(
                        drugName = matchedDrug.name,
                        confidence = adjustedConfidence,
                        category = "${matchedDrug.category} (${matchedDrug.type})",
                        indications = matchedDrug.indications,
                        dosage = matchedDrug.dosage,
                        contraindications = matchedDrug.contraindications,
                        isBlurry = isBlurry,
                        source = "database"
                    )
                } else {
                    // 药品库未匹配，先本地提取药品名，再用AI
                    val localName = extractDrugNameLocally(ocrResponse.texts)
                    val drugName = localName ?: extractDrugNameWithAi(ocrText) ?: ocrText.take(10)
                    android.util.Log.d("CameraViewModel", "Drug name: local='$localName', final='$drugName'")
                    DrugRecognitionResult(
                        drugName = drugName,
                        confidence = adjustedConfidence,
                        category = "",
                        indications = "",
                        dosage = "",
                        contraindications = "",
                        isBlurry = isBlurry,
                        source = "ai",
                        ocrText = ocrText
                    )
                }

                _uiState.update { it.copy(result = result, isLoading = false, showDetail = false) }

                // 保存扫描历史
                saveScanHistory(imageUri, result)

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "识别失败: ${e.message}", isLoading = false)
                }
            }
        }
    }

    /**
     * 从OCR文字中本地提取药品名称
     * 策略1：正则匹配"XX片/胶囊/颗粒"模式
     * 策略2：从功效描述中提取疾病关键词 + 剂型后缀重建药名
     */
    private fun extractDrugNameLocally(ocrTexts: List<String>): String? {
        // 常见药品名称后缀（按长度降序）
        val suffixes = listOf(
            "缓释胶囊", "肠溶胶囊", "软胶囊", "分散片", "泡腾片",
            "缓释片", "肠溶片", "咀嚼片", "喷雾剂", "气雾剂",
            "口服液", "滴眼液", "滴鼻液", "注射液",
            "胶囊", "颗粒", "糖浆", "软膏", "贴膏", "含片",
            "片", "丸", "散", "滴丸"
        )

        // 排除词
        val excludeKeywords = listOf(
            "有限公司", "集团公司", "药业", "制药", "药厂", "生物",
            "科技", "集团", "公司", "工厂",
            "国药准字", "批准文号", "生产日期", "有效期", "批号",
            "省", "市", "区", "路", "号", "街", "村",
            "每盒", "每瓶", "每袋", "每片", "每粒", "每支",
            "装量", "规格", "含量", "成份", "辅料"
        )

        // 策略1：直接正则匹配
        val candidates = mutableListOf<Pair<String, Int>>()
        for (text in ocrTexts) {
            val trimmed = text.trim()
            if (trimmed.length < 2) continue
            if (excludeKeywords.any { trimmed.contains(it) }) continue

            for (suffix in suffixes) {
                val pattern = "([\\u4e00-\\u9fa5]{2,8}$suffix)".toRegex()
                val match = pattern.find(trimmed)
                if (match != null) {
                    val name = match.groupValues[1]
                    candidates.add(name to (100 - name.length))
                }
            }
        }
        if (candidates.isNotEmpty()) {
            return candidates.maxByOrNull { it.second }?.first
        }

        // 策略2：从功效描述推断药名
        // 查找"功能主治"或"用于"后面的疾病关键词，结合剂型后缀
        val fullText = ocrTexts.joinToString(" ")

        // 常见疾病/症状关键词 → 对应药名映射
        val diseaseToDrug = mapOf(
            "鼻炎" to "鼻炎片",
            "感冒" to "感冒清热颗粒",
            "咳嗽" to "止咳片",
            "咽喉" to "咽炎片",
            "胃痛" to "胃痛片",
            "腹泻" to "止泻颗粒",
            "头痛" to "头痛片",
            "风湿" to "风湿片",
            "跌打" to "跌打丸",
            "活血" to "活血片",
            "清热" to "清热片",
            "解毒" to "解毒片"
        )

        // 检查功效描述中的关键词
        for ((keyword, drugName) in diseaseToDrug) {
            if (fullText.contains(keyword)) {
                // 进一步确认：找到匹配的剂型后缀
                for (suffix in suffixes) {
                    // 在所有文本段中查找包含该关键词+后缀的组合
                    for (text in ocrTexts) {
                        if (text.contains(keyword) && text.contains(suffix)) {
                            return "${keyword}${suffix}"
                        }
                    }
                }
                // 没找到精确组合，但有明确的疾病关键词，返回映射的药名
                return drugName
            }
        }

        return null
    }

    /**
     * 仅提取药品名称（快速，不加载详细信息）
     */
    private suspend fun extractDrugNameWithAi(ocrText: String): String? {
        return try {
            val systemPrompt = """你是药品识别助手。用户拍摄了药品包装，OCR识别出了以下文字。
请从中提取药品的通用名称（如"感冒清热颗粒"、"阿莫西林胶囊"），只返回药品名，不要其他文字。
注意：不要返回"国药准字"、规格、厂家等信息，只返回药品名称。如果无法判断，返回"无法识别"。"""
            val response = aiRepository.chat(systemPrompt, ocrText.take(300), "glm-4-flash")
            val name = response.trim().removeSurrounding("\"").removeSurrounding("'")
            android.util.Log.d("CameraViewModel", "AI extracted name: '$name' from ocrText: '${ocrText.take(100)}'")
            if (name.isEmpty() || name == "无法识别") null else name
        } catch (e: Exception) {
            android.util.Log.e("CameraViewModel", "AI name extraction failed", e)
            null
        }
    }

    /**
     * 显示药品详细信息（点击后触发）
     */
    fun showDrugDetail() {
        val current = _uiState.value.result ?: return

        // 数据库药品已有完整信息，直接显示
        if (current.source == "database") {
            _uiState.update { it.copy(showDetail = true) }
            return
        }

        // AI药品需要加载详细信息
        _uiState.update { it.copy(showDetail = true, isDetailLoading = true) }
        viewModelScope.launch {
            try {
                val systemPrompt = """你是一个专业的药品助手。请根据以下药品名称，提供详细用药信息，用JSON格式返回：
{
  "category": "药品类型（如中成药、西药等）",
  "indications": "适应症/功效主治",
  "dosage": "用法用量",
  "contraindications": "禁忌/注意事项"
}
只返回JSON，不要其他文字。"""

                val response = aiRepository.chat(systemPrompt, current.drugName, "glm-4-flash")

                val jsonStr = response.trim().let {
                    val start = it.indexOf('{')
                    val end = it.lastIndexOf('}')
                    if (start >= 0 && end > start) it.substring(start, end + 1) else it
                }

                val json = org.json.JSONObject(jsonStr)
                _uiState.update { state ->
                    state.copy(
                        result = state.result?.copy(
                            category = json.optString("category", "AI识别"),
                            indications = json.optString("indications", ""),
                            dosage = json.optString("dosage", ""),
                            contraindications = json.optString("contraindications", "")
                        ),
                        isDetailLoading = false
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("CameraViewModel", "AI detail loading failed", e)
                _uiState.update { state ->
                    state.copy(
                        result = state.result?.copy(
                            category = "AI识别",
                            indications = "暂无详细信息，请查看药品包装",
                            dosage = "请查看药品包装上的用法用量",
                            contraindications = "请查看药品包装上的禁忌信息"
                        ),
                        isDetailLoading = false
                    )
                }
            }
        }
    }

    fun hideDrugDetail() {
        _uiState.update { it.copy(showDetail = false) }
    }

    /**
     * 从 URI 加载 Bitmap，自动处理 EXIF 旋转
     */
    private fun loadBitmap(uri: Uri): Bitmap? {
        return try {
            val inputStream = application.contentResolver.openInputStream(uri) ?: return null
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            // 读取 EXIF 旋转信息
            val exifStream = application.contentResolver.openInputStream(uri)
            val orientation = if (exifStream != null) {
                try {
                    val exif = androidx.exifinterface.media.ExifInterface(exifStream)
                    exif.getAttributeInt(
                        androidx.exifinterface.media.ExifInterface.TAG_ORIENTATION,
                        androidx.exifinterface.media.ExifInterface.ORIENTATION_NORMAL
                    )
                } catch (_: Exception) {
                    androidx.exifinterface.media.ExifInterface.ORIENTATION_NORMAL
                } finally {
                    exifStream.close()
                }
            } else {
                androidx.exifinterface.media.ExifInterface.ORIENTATION_NORMAL
            }

            // 根据 EXIF 旋转
            val matrix = Matrix()
            when (orientation) {
                androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            }

            val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            if (rotated !== bitmap) bitmap.recycle()
            rotated
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 模糊检测 - 拉普拉斯方差法
     * 方差越大越清晰，越小越模糊
     */
    private fun detectBlur(bitmap: Bitmap): Double {
        // 缩小图片加速计算
        val scale = PREPROCESS_SIZE.toFloat() / maxOf(bitmap.width, bitmap.height)
        val small = if (scale < 1f) {
            Bitmap.createScaledBitmap(
                bitmap,
                (bitmap.width * scale).toInt(),
                (bitmap.height * scale).toInt(),
                true
            )
        } else bitmap

        val width = small.width
        val height = small.height
        val pixels = IntArray(width * height)
        small.getPixels(pixels, 0, width, 0, 0, width, height)

        // 转灰度并计算拉普拉斯
        val laplacianValues = mutableListOf<Double>()
        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                val idx = y * width + x
                val center = Color.red(pixels[idx]).toDouble()
                val top = Color.red(pixels[(y - 1) * width + x]).toDouble()
                val bottom = Color.red(pixels[(y + 1) * width + x]).toDouble()
                val left = Color.red(pixels[y * width + (x - 1)]).toDouble()
                val right = Color.red(pixels[y * width + (x + 1)]).toDouble()

                // 3x3 拉普拉斯核: [0,1,0; 1,-4,1; 0,1,0]
                val laplacian = top + bottom + left + right - 4 * center
                laplacianValues.add(laplacian)
            }
        }

        if (small !== bitmap) small.recycle()

        if (laplacianValues.isEmpty()) return 0.0

        // 计算方差
        val mean = laplacianValues.average()
        val variance = laplacianValues.map { (it - mean) * (it - mean) }.average()
        return variance
    }

    /**
     * 图像锐化 - 使用锐化卷积核
     */
    private fun sharpenImage(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        val output = IntArray(width * height)

        // 锐化核: [0,-1,0; -1,5,-1; 0,-1,0]
        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                val idx = y * width + x

                val centerR = Color.red(pixels[idx])
                val centerG = Color.green(pixels[idx])
                val centerB = Color.blue(pixels[idx])

                val top = pixels[(y - 1) * width + x]
                val bottom = pixels[(y + 1) * width + x]
                val left = pixels[y * width + (x - 1)]
                val right = pixels[y * width + (x + 1)]

                val newR = (5 * centerR - Color.red(top) - Color.red(bottom) -
                        Color.red(left) - Color.red(right)).coerceIn(0, 255)
                val newG = (5 * centerG - Color.green(top) - Color.green(bottom) -
                        Color.green(left) - Color.green(right)).coerceIn(0, 255)
                val newB = (5 * centerB - Color.blue(top) - Color.blue(bottom) -
                        Color.blue(left) - Color.blue(right)).coerceIn(0, 255)

                output[idx] = Color.rgb(newR, newG, newB)
            }
        }

        // 边缘像素保持原值
        for (y in 0 until height) {
            for (x in 0 until width) {
                if (y == 0 || y == height - 1 || x == 0 || x == width - 1) {
                    output[y * width + x] = pixels[y * width + x]
                }
            }
        }

        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        result.setPixels(output, 0, width, 0, 0, width, height)
        if (result !== bitmap) bitmap.recycle()
        return result
    }

    /**
     * 对比度增强 - 直方图拉伸
     */
    private fun enhanceContrast(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        // 找到亮度的 2% 和 98% 分位点
        val brightnesses = pixels.map { Color.red(it) * 0.299 + Color.green(it) * 0.587 + Color.blue(it) * 0.114 }
        val sorted = brightnesses.sorted()
        val low = sorted[(sorted.size * 0.02).toInt()].toInt()
        val high = sorted[(sorted.size * 0.98).toInt()].toInt()
        val range = (high - low).coerceAtLeast(1)

        val output = IntArray(width * height)
        for (i in pixels.indices) {
            val r = ((Color.red(pixels[i]) - low) * 255.0 / range).coerceIn(0.0, 255.0).toInt()
            val g = ((Color.green(pixels[i]) - low) * 255.0 / range).coerceIn(0.0, 255.0).toInt()
            val b = ((Color.blue(pixels[i]) - low) * 255.0 / range).coerceIn(0.0, 255.0).toInt()
            output[i] = Color.rgb(r, g, b)
        }

        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        result.setPixels(output, 0, width, 0, 0, width, height)
        if (result !== bitmap) bitmap.recycle()
        return result
    }

    /**
     * Bitmap 转 JPEG 字节
     */
    private fun bitmapToBytes(bitmap: Bitmap, quality: Int): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
        val bytes = stream.toByteArray()
        stream.close()
        return bytes
    }

    private fun saveScanHistory(imageUri: Uri, result: DrugRecognitionResult) {
        viewModelScope.launch {
            try {
                val userId = dataStoreManager.getLoggedInUserId() ?: return@launch
                val scan = ScanHistory(
                    userId = userId,
                    imageUrl = imageUri.toString(),
                    recognizedDrugName = result.drugName,
                    confidence = result.confidence,
                    drugDetails = if (result.source == "database") result.category else null
                )
                scanHistoryRepository.insertScan(scan)
            } catch (e: Exception) {
                android.util.Log.e("CameraViewModel", "Failed to save scan history", e)
            }
        }
    }

    fun saveFabPosition(x: Float, y: Float) {
        viewModelScope.launch {
            dataStoreManager.setFabPosition("camera", x, y)
            _uiState.update { it.copy(fabPosition = Pair(x, y)) }
        }
    }

    private fun loadFabPosition() {
        viewModelScope.launch {
            val pos = dataStoreManager.getFabPosition("camera")
            _uiState.update { it.copy(fabPosition = pos) }
        }
    }

    fun clearResult() {
        _uiState.update {
            it.copy(
                capturedImageUri = null,
                result = null,
                showDetail = false,
                isDetailLoading = false,
                error = null,
                blurWarning = null
            )
        }
    }
}
