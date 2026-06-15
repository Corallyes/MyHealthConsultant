package com.example.myhealthconsultant.di

import android.content.Context
import com.example.myhealthconsultant.data.local.dao.*
import com.example.myhealthconsultant.data.local.db.AppDatabase
import com.example.myhealthconsultant.data.remote.api.GlmApiService
import com.example.myhealthconsultant.data.remote.api.OcrApiService
import com.example.myhealthconsultant.data.remote.api.SiliconFlowApiService
import com.example.myhealthconsultant.data.repository.*
import com.example.myhealthconsultant.domain.repository.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // Network
    @DefaultOkHttp
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    @DefaultRetrofit
    @Provides
    @Singleton
    fun provideRetrofit(@DefaultOkHttp okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("http://127.0.0.1:8000/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideOcrApiService(@DefaultRetrofit retrofit: Retrofit): OcrApiService {
        return retrofit.create(OcrApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideOcrRepository(ocrApiService: OcrApiService): OcrRepository {
        return OcrRepositoryImpl(ocrApiService)
    }

    // GLM AI API
    @GlmOkHttp
    @Provides
    @Singleton
    fun provideGlmOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val authInterceptor = okhttp3.Interceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer ${com.example.myhealthconsultant.BuildConfig.GLM_API_KEY}")
                .build()
            chain.proceed(request)
        }
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    @GlmRetrofit
    @Provides
    @Singleton
    fun provideGlmRetrofit(@GlmOkHttp glmOkHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://open.bigmodel.cn/api/")
            .client(glmOkHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideGlmApiService(@GlmRetrofit glmRetrofit: Retrofit): GlmApiService {
        return glmRetrofit.create(GlmApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideAiRepository(glmApiService: GlmApiService, siliconFlowApiService: SiliconFlowApiService): AiRepository {
        return AiRepositoryImpl(glmApiService, siliconFlowApiService)
    }

    // SiliconFlow API (Qwen)
    @SiliconFlowOkHttp
    @Provides
    @Singleton
    fun provideSiliconFlowOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val authInterceptor = okhttp3.Interceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer ${com.example.myhealthconsultant.BuildConfig.SILICONFLOW_API_KEY}")
                .build()
            chain.proceed(request)
        }
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(300, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    @SiliconFlowRetrofit
    @Provides
    @Singleton
    fun provideSiliconFlowRetrofit(@SiliconFlowOkHttp siliconFlowOkHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.siliconflow.cn/")
            .client(siliconFlowOkHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideSiliconFlowApiService(@SiliconFlowRetrofit siliconFlowRetrofit: Retrofit): SiliconFlowApiService {
        return siliconFlowRetrofit.create(SiliconFlowApiService::class.java)
    }

    // Database
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getInstance(context)
    }

    // DAOs
    @Provides
    @Singleton
    fun provideUserDao(database: AppDatabase): UserDao = database.userDao()

    @Provides
    @Singleton
    fun provideDrugDao(database: AppDatabase): DrugDao = database.drugDao()

    @Provides
    @Singleton
    fun provideMedicationPlanDao(database: AppDatabase): MedicationPlanDao = database.medicationPlanDao()

    @Provides
    @Singleton
    fun provideMedicationRecordDao(database: AppDatabase): MedicationRecordDao = database.medicationRecordDao()

    @Provides
    @Singleton
    fun provideChatHistoryDao(database: AppDatabase): ChatHistoryDao = database.chatHistoryDao()

    @Provides
    @Singleton
    fun provideScanHistoryDao(database: AppDatabase): ScanHistoryDao = database.scanHistoryDao()

    @Provides
    @Singleton
    fun provideCabinetMedicineDao(database: AppDatabase): CabinetMedicineDao = database.cabinetMedicineDao()

    @Provides
    @Singleton
    fun providePrescriptionDao(database: AppDatabase): PrescriptionDao = database.prescriptionDao()

    // Repositories
    @Provides
    @Singleton
    fun provideUserRepository(userDao: UserDao): UserRepository = UserRepositoryImpl(userDao)

    @Provides
    @Singleton
    fun provideDrugRepository(drugDao: DrugDao): DrugRepository = DrugRepositoryImpl(drugDao)

    @Provides
    @Singleton
    fun provideMedicationPlanRepository(dao: MedicationPlanDao): MedicationPlanRepository = MedicationPlanRepositoryImpl(dao)

    @Provides
    @Singleton
    fun provideMedicationRecordRepository(dao: MedicationRecordDao): MedicationRecordRepository = MedicationRecordRepositoryImpl(dao)

    @Provides
    @Singleton
    fun provideChatHistoryRepository(dao: ChatHistoryDao): ChatHistoryRepository = ChatHistoryRepositoryImpl(dao)

    @Provides
    @Singleton
    fun provideScanHistoryRepository(dao: ScanHistoryDao): ScanHistoryRepository = ScanHistoryRepositoryImpl(dao)

    @Provides
    @Singleton
    fun provideCabinetMedicineRepository(dao: CabinetMedicineDao): CabinetMedicineRepository = CabinetMedicineRepositoryImpl(dao)

    @Provides
    @Singleton
    fun providePrescriptionRepository(dao: PrescriptionDao): PrescriptionRepository = PrescriptionRepositoryImpl(dao)
}
