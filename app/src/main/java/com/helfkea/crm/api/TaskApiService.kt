package com.helfkea.crm.api

import com.helfkea.crm.model.CreateOrderResponse
import com.helfkea.crm.model.CreateTaskRequest
import com.helfkea.crm.model.CreateTaskResponse
import com.helfkea.crm.model.Task
import com.helfkea.crm.model.Individual
import com.helfkea.crm.model.Contragent
import com.helfkea.crm.model.Product
import com.helfkea.crm.model.Order
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import java.util.concurrent.TimeUnit
import com.helfkea.crm.model.SnapContragentRequest
import com.helfkea.crm.model.SnapContragentResponse
import com.helfkea.crm.model.BasicContragent
import com.helfkea.crm.model.OrderResponse
import com.helfkea.crm.model.OrderCreateRequest
import com.helfkea.crm.model.OrderCreateResponse
import com.helfkea.crm.model.CreateInteractionRequest
import com.helfkea.crm.model.CreateInteractionResponse
import com.helfkea.crm.model.ContactType
import com.helfkea.crm.model.CreateContragentRequest
import com.helfkea.crm.model.CreateContragentResponse
import com.helfkea.crm.model.UploadFileResponse
import com.helfkea.crm.model.AttachmentRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Multipart  // ★ ДОБАВИТЬ ЭТОТ ИМПОРТ
import retrofit2.http.Part  // ★ ДОБАВИТЬ ЭТОТ ИМПОРТ
import com.helfkea.crm.model.User
import com.helfkea.crm.model.UpdateTaskRequest
import com.helfkea.crm.model.AddCommentRequest
import com.helfkea.crm.model.InnDataRequest
import com.helfkea.crm.model.InnDataResponse
import com.helfkea.crm.model.GetContragentByIdRequest
import com.helfkea.crm.model.GetContragentByIdFullResponse
import com.helfkea.crm.model.AuthResponse


interface TaskApiService {

    @POST("getTasksMe")  // Задачи мне (все задачи)
    suspend fun getTasksMe(): List<Task>

    @POST("getTasksMy")  // Мои задачи (где я исполнитель)
    suspend fun getTasksMy(): List<Task>

    @POST("getUsers")
    suspend fun getUsers(): List<User>

    @POST("snapContragent")
    suspend fun snapContragent(@Body request: SnapContragentRequest): Response<SnapContragentResponse>
    @POST("setTask")
    suspend fun createTask(@Body request: CreateTaskRequest): Response<CreateTaskResponse>

    @POST("getContragents")
    suspend fun getContragents(): List<Contragent>

    @POST("getAllContragents")
    suspend fun getAllContragents(): List<BasicContragent>

    @POST("setInteraction")
    suspend fun createInteraction(@Body request: CreateInteractionRequest): Response<CreateInteractionResponse>

    @POST("getTypeContact")
    suspend fun getContactTypes(): List<ContactType>

    @POST("createContragent")
    suspend fun createContragent(@Body request: CreateContragentRequest): Response<CreateContragentResponse>


    // Добавить в TaskApiService
    @POST("getOrders")
    suspend fun getOrders(): Response<List<OrderResponse>>

    @POST("setOrder")
    suspend fun createOrderV2(@Body request: OrderCreateRequest): Response<OrderCreateResponse>

    @POST("Individual")
    suspend fun getIndividuals(): List<Individual>
    @POST("getProductV2")
    suspend fun getProducts(): List<Product>

    // ДОБАВЛЯЕМ НОВЫЙ МЕТОД ДЛЯ СОЗДАНИЯ ЗАКАЗА
    @POST("createOrder")
    suspend fun createOrder(@Body order: Order): Response<CreateOrderResponse>

    // НОВЫЙ МЕТОД для загрузки файла
    @Multipart
    @POST("uploadAttachment")
    suspend fun uploadAttachment(
        @Part("taskId") taskId: RequestBody?,
        @Part file: MultipartBody.Part
    ): Response<UploadFileResponse>

    // ДОБАВЛЯЕМ НОВЫЙ МЕТОД ДЛЯ РЕДАКТИРОВАНИЯ ЗАДАЧ
    @POST("updateTask")
    suspend fun updateTask(@Body request: UpdateTaskRequest): Response<CreateTaskResponse>

    // ДОБАВЛЯЕМ НОВЫЙ МЕТОД ДЛЯ ДОБАВЛЕНИЯ КОММЕНТАРИЯ
    @POST("addComment")
    suspend fun addComment(@Body request: AddCommentRequest): Response<CreateTaskResponse>

    // НОВЫЙ МЕТОД для получения данных по ИНН из 1С
    @POST("writeINN")
    suspend fun getInnData(@Body request: InnDataRequest): Response<InnDataResponse>

    // НОВЫЙ МЕТОД для получения контрагента по ID
    @POST("getContragentById")
    suspend fun getContragentById(@Body request: GetContragentByIdRequest): Response<GetContragentByIdFullResponse>
    
    @POST("auth")
    suspend fun auth(): Response<AuthResponse>

}


object TaskApi {
    private const val BASE_URL = "http://yo.serverworkdev.ru:8055/test_yo/hs/repressale/"

    // Динамические учетные данные (будут устанавливаться после авторизации)
    private var currentUsername: String? = null
    private var currentPassword: String? = null

    fun setCredentials(username: String, password: String) {
        currentUsername = username
        currentPassword = password
    }

    fun clearCredentials() {
        currentUsername = null
        currentPassword = null
    }

    fun hasCredentials(): Boolean {
        return currentUsername != null && currentPassword != null
    }

    private val client: OkHttpClient
        get() = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .addInterceptor { chain ->
                try {
                    val originalRequest = chain.request()

                    // Basic Auth с динамическими учетными данными
                    val credentials = if (currentUsername != null && currentPassword != null) {
                        android.util.Base64.encodeToString(
                            "$currentUsername:$currentPassword".toByteArray(),
                            android.util.Base64.NO_WRAP
                        )
                    } else {
                        // Fallback на старые учетные данные для обратной совместимости
                        android.util.Base64.encodeToString(
                            "program:11112".toByteArray(),
                            android.util.Base64.NO_WRAP
                        )
                    }

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

    private val retrofit: Retrofit
        get() = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
            .build()

    val service: TaskApiService
        get() = retrofit.create(TaskApiService::class.java)
}
