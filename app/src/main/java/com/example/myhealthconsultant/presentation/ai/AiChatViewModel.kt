package com.example.myhealthconsultant.presentation.ai

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myhealthconsultant.data.local.entity.ChatHistory
import com.example.myhealthconsultant.domain.repository.AiRepository
import com.example.myhealthconsultant.domain.repository.ChatHistoryRepository
import com.example.myhealthconsultant.domain.repository.ChatMessage
import com.example.myhealthconsultant.util.DataStoreManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

private const val TAG = "AiChatViewModel"

data class AiModel(
    val id: String,
    val displayName: String
)

val availableModels = listOf(
    AiModel(id = "glm-4-flash", displayName = "GLM-4-Flash"),
    AiModel(id = "qwen3-8b", displayName = "Qwen3-8B")
)

data class AiChatUiState(
    val messages: List<ChatHistory> = emptyList(),
    val streamingContent: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentSessionId: String = UUID.randomUUID().toString(),
    val selectedModel: AiModel = availableModels[0]
)

@HiltViewModel
class AiChatViewModel @Inject constructor(
    private val chatRepository: ChatHistoryRepository,
    private val aiRepository: AiRepository,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AiChatUiState())
    val uiState: StateFlow<AiChatUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
        loadAiProvider()
    }

    private fun loadAiProvider() {
        viewModelScope.launch {
            dataStoreManager.aiProvider.collect { provider ->
                val matched = availableModels.find { it.displayName == provider }
                if (matched != null && matched.id != _uiState.value.selectedModel.id) {
                    _uiState.update { it.copy(selectedModel = matched) }
                }
            }
        }
    }

    private fun loadHistory() {
        viewModelScope.launch {
            val userId = dataStoreManager.getLoggedInUserId() ?: "local_user"
            val sessionId = _uiState.value.currentSessionId
            chatRepository.getMessagesBySession(userId, sessionId).collect { messages ->
                _uiState.update { it.copy(messages = messages) }
            }
        }
    }

    private val systemPrompt = """
你是一款名为"青囊"的AI健康咨询顾问，专门为缺乏医学常识的普通用户提供专业、易懂的健康建议和用药指导。

## 角色定位
- 你是一位耐心、专业的健康顾问，用通俗易懂的语言解释医学问题
- 你的服务对象主要是年轻人和老年人，需要避免使用过于专业的术语
- 当必须使用专业术语时，请用括号附上通俗解释

## 核心原则
1. **安全第一**：对高风险症状必须立即建议就医，不得延误
2. **非处方优先**：优先推荐OTC药品，绝不推荐处方药，只能建议"请咨询医生后处方使用"
3. **结构化回答**：每次回答必须包含以下三个部分：
   - 用药建议（药品名称、用法用量）
   - 注意事项（禁忌、副作用、饮食禁忌等）
   - 就医判断（是否需要就医、什么情况下必须就医）
4. **个性化建议**：根据用户描述的症状严重程度和持续时间给出针对性建议

## 高风险症状（必须立即建议就医）
- 高烧超过39°C且持续不退
- 持续剧烈疼痛
- 呼吸困难或胸痛
- 意识模糊或精神状态异常
- 严重过敏反应（如喉头水肿、全身荨麻疹）
- 外伤出血不止
- 突发性剧烈头痛
- 腹痛伴发热或血便

## 回答格式规范
1. 使用清晰的分点说明，层次分明
2. 对高风险情况使用警告标识突出显示
3. 药品推荐时注明：药品名称、规格、用法用量
4. 提醒用户使用前核对药品说明书
5. 回答末尾必须附上免责声明

## 禁止事项
- 不能诊断疾病（只能提供参考建议）
- 不能开具处方
- 不能替代医生的专业诊断和治疗
- 不能推荐任何处方药或管制药品
- 不能对危急情况给出保守建议（必须立即建议就医）

## 特殊场景处理
- **用户描述模糊**：主动询问关键信息（症状持续时间、严重程度、既往病史等）
- **用户情绪焦虑**：先安抚情绪，再给出专业建议
- **儿童或孕妇相关**：格外谨慎，强烈建议就医
- **慢性病管理**：提醒定期复查，遵医嘱用药

免责声明：本回答仅供参考，不构成医疗建议。如有不适请及时就医。
    """.trimIndent()

    fun selectModel(model: AiModel) {
        _uiState.update { it.copy(selectedModel = model) }
        viewModelScope.launch {
            dataStoreManager.setAiProvider(model.displayName)
        }
    }

    fun sendMessage(content: String) {
        Log.d(TAG, "sendMessage called: content='$content', isLoading=${_uiState.value.isLoading}, isBlank=${content.isBlank()}")
        if (content.isBlank() || _uiState.value.isLoading) {
            Log.d(TAG, "sendMessage early return: content.isBlank()=${content.isBlank()}, isLoading=${_uiState.value.isLoading}")
            return
        }

        viewModelScope.launch {
            try {
                Log.d(TAG, "Coroutine started")

                val userId = dataStoreManager.getLoggedInUserId() ?: "local_user"
                Log.d(TAG, "userId=$userId")

                val sessionId = _uiState.value.currentSessionId
                val modelId = _uiState.value.selectedModel.id
                Log.d(TAG, "sessionId=$sessionId, modelId=$modelId")

                // 设置加载状态
                _uiState.update { it.copy(isLoading = true, error = null) }
                Log.d(TAG, "Loading state set to true")

                // 添加用户消息到数据库（loadHistory会自动更新UI）
                val userMessage = ChatHistory(
                    userId = userId,
                    sessionId = sessionId,
                    role = "user",
                    content = content
                )
                chatRepository.insertMessage(userMessage)
                Log.d(TAG, "User message saved to DB")

                // 构建历史上下文（最近10条消息）+ 当前用户消息
                val currentMessages = _uiState.value.messages
                val history = currentMessages.takeLast(10).map { msg ->
                    ChatMessage(role = msg.role, content = msg.content)
                }.toMutableList()
                // Flow可能还没更新，手动添加当前用户消息
                if (history.lastOrNull()?.content != content) {
                    history.add(ChatMessage(role = "user", content = content))
                }
                Log.d(TAG, "Calling AI API with ${history.size} messages")
                val aiResponse = aiRepository.chatWithHistory(systemPrompt, history, modelId)
                Log.d(TAG, "AI response received: ${aiResponse.take(100)}...")

                // 添加AI回复到数据库（loadHistory会自动更新UI）
                val aiMessage = ChatHistory(
                    userId = userId,
                    sessionId = sessionId,
                    role = "assistant",
                    content = aiResponse
                )
                chatRepository.insertMessage(aiMessage)
                Log.d(TAG, "AI message saved to DB")

                _uiState.update { it.copy(isLoading = false) }
                Log.d(TAG, "Loading state set to false")
            } catch (e: Exception) {
                Log.e(TAG, "sendMessage error", e)
                _uiState.update {
                    it.copy(
                        error = "请求失败: ${e.message ?: "未知错误"}",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun newSession() {
        val currentModel = _uiState.value.selectedModel
        _uiState.update {
            AiChatUiState(
                currentSessionId = UUID.randomUUID().toString(),
                selectedModel = currentModel
            )
        }
    }
}
