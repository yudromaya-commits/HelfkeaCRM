package com.example.repressales.api

import com.example.repressales.model.CreateTaskRequest
import com.example.repressales.model.CreateTaskResponse
import com.example.repressales.model.Task
import com.example.repressales.model.Contragent
import com.example.repressales.model.Product
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

interface TaskApiService {
    @POST("getTasks")
    suspend fun getTasks(): List<Task>  // ВОССТАНОВЛЕННЫЙ МЕТОД

    @POST("setTask")
    suspend fun createTask(@Body request: CreateTaskRequest): Response<CreateTaskResponse>

    @POST("getContragents")
    suspend fun getContragents(): List<Contragent>

    @GET("rmarket/getProductV2")
    suspend fun getProducts(): List<Product>
}

object TaskApi {
    private const val BASE_URL = "http://yo.serverworkdev.ru:8055/test_yo/hs/repressale/"

    // TODO: Замените на реальные credentials
    private const val USERNAME = "program" // Замените на реальные
    private const val PASSWORD = "11112" // Замените на реальные

    private val client = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .addInterceptor { chain ->
            try {
                val originalRequest = chain.request()

                // Basic Auth
                val credentials = android.util.Base64.encodeToString(
                    "$USERNAME:$PASSWORD".toByteArray(),
                    android.util.Base64.NO_WRAP
                )

                val request = originalRequest.newBuilder()
                    .addHeader("Authorization", "Basic $credentials")
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "application/json")
                    .build()

                chain.proceed(request)
            } catch (e: Exception) {
                throw e
            }
        }
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
        .build()

    val service: TaskApiService = retrofit.create(TaskApiService::class.java)
}