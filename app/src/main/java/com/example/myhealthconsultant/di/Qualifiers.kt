package com.example.myhealthconsultant.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DefaultOkHttp

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class GlmOkHttp

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class SiliconFlowOkHttp

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DefaultRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class GlmRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class SiliconFlowRetrofit
