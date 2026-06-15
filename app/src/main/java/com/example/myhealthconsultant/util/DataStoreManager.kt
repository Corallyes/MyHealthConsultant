package com.example.myhealthconsultant.util

import android.content.Context
import androidx.datastore.core.DataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class DataStoreManager @Inject constructor(@param:ApplicationContext private val context: Context) {

    companion object {
        private val LOGGED_IN_USER_ID = stringPreferencesKey("logged_in_user_id")
        private val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        private val AI_PROVIDER = stringPreferencesKey("ai_provider")
        private val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        private val NOTIFICATION_ENABLED = booleanPreferencesKey("notification_enabled")
        private val REMINDER_ADVANCE_MINUTES = intPreferencesKey("reminder_advance_minutes")
        private val SAVED_PHONE = stringPreferencesKey("saved_phone")
        private val REMEMBER_PASSWORD = booleanPreferencesKey("remember_password")
        private val EXPIRY_THRESHOLD_DAYS = intPreferencesKey("expiry_threshold_days")
        private val FAB_CALENDAR_X = floatPreferencesKey("fab_calendar_x")
        private val FAB_CALENDAR_Y = floatPreferencesKey("fab_calendar_y")
        private val FAB_CABINET_X = floatPreferencesKey("fab_cabinet_x")
        private val FAB_CABINET_Y = floatPreferencesKey("fab_cabinet_y")
        private val FAB_CAMERA_X = floatPreferencesKey("fab_camera_x")
        private val FAB_CAMERA_Y = floatPreferencesKey("fab_camera_y")
    }

    // 登录状态
    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_LOGGED_IN] ?: false
    }

    suspend fun getLoggedInUserId(): String? {
        return context.dataStore.data.first()[LOGGED_IN_USER_ID]
    }

    suspend fun setLoggedInUserId(userId: String) {
        context.dataStore.edit { preferences ->
            preferences[LOGGED_IN_USER_ID] = userId
            preferences[IS_LOGGED_IN] = true
        }
    }

    suspend fun clearLoginState() {
        context.dataStore.edit { preferences ->
            preferences[IS_LOGGED_IN] = false
            preferences.remove(LOGGED_IN_USER_ID)
        }
    }

    // 深色模式
    val isDarkMode: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_DARK_MODE] ?: false
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_DARK_MODE] = enabled
        }
    }

    // AI服务提供商
    val aiProvider: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[AI_PROVIDER] ?: "GLM-4-Flash"
    }

    suspend fun setAiProvider(provider: String) {
        context.dataStore.edit { preferences ->
            preferences[AI_PROVIDER] = provider
        }
    }

    // 通知设置
    val isNotificationEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[NOTIFICATION_ENABLED] ?: true
    }

    suspend fun setNotificationEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[NOTIFICATION_ENABLED] = enabled
        }
    }

    val reminderAdvanceMinutes: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[REMINDER_ADVANCE_MINUTES] ?: 10
    }

    suspend fun setReminderAdvanceMinutes(minutes: Int) {
        context.dataStore.edit { preferences ->
            preferences[REMINDER_ADVANCE_MINUTES] = minutes
        }
    }

    // 记住手机号（不再存储密码）
    val savedCredentials: Flow<Pair<Boolean, String>> = context.dataStore.data.map { preferences ->
        val remember = preferences[REMEMBER_PASSWORD] ?: false
        val phone = preferences[SAVED_PHONE] ?: ""
        Pair(remember, phone)
    }

    suspend fun saveCredentials(phone: String) {
        context.dataStore.edit { preferences ->
            preferences[REMEMBER_PASSWORD] = true
            preferences[SAVED_PHONE] = phone
        }
    }

    suspend fun clearCredentials() {
        context.dataStore.edit { preferences ->
            preferences[REMEMBER_PASSWORD] = false
            preferences.remove(SAVED_PHONE)
        }
    }

    // 过期提醒阈值（天数）
    val expiryThresholdDays: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[EXPIRY_THRESHOLD_DAYS] ?: 7
    }

    suspend fun setExpiryThresholdDays(days: Int) {
        context.dataStore.edit { preferences ->
            preferences[EXPIRY_THRESHOLD_DAYS] = days
        }
    }

    // 浮动按钮位置
    suspend fun getFabPosition(key: String): Pair<Float, Float> {
        val prefs = context.dataStore.data.first()
        return when (key) {
            "calendar" -> Pair(prefs[FAB_CALENDAR_X] ?: Float.NaN, prefs[FAB_CALENDAR_Y] ?: Float.NaN)
            "cabinet" -> Pair(prefs[FAB_CABINET_X] ?: Float.NaN, prefs[FAB_CABINET_Y] ?: Float.NaN)
            "camera" -> Pair(prefs[FAB_CAMERA_X] ?: Float.NaN, prefs[FAB_CAMERA_Y] ?: Float.NaN)
            else -> Pair(Float.NaN, Float.NaN)
        }
    }

    suspend fun setFabPosition(key: String, x: Float, y: Float) {
        context.dataStore.edit { prefs ->
            when (key) {
                "calendar" -> {
                    prefs[FAB_CALENDAR_X] = x
                    prefs[FAB_CALENDAR_Y] = y
                }
                "cabinet" -> {
                    prefs[FAB_CABINET_X] = x
                    prefs[FAB_CABINET_Y] = y
                }
                "camera" -> {
                    prefs[FAB_CAMERA_X] = x
                    prefs[FAB_CAMERA_Y] = y
                }
            }
        }
    }
}
