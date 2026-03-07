package com.helfkea.crm.repository

import android.util.Log
import com.helfkea.crm.api.TaskApi
import com.helfkea.crm.data.local.dao.ProductDao
import com.helfkea.crm.data.local.mapper.ProductMapper
import com.helfkea.crm.data.network.NetworkMonitor
import com.helfkea.crm.data.sync.SyncStatus
import com.helfkea.crm.model.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class OfflineProductRepository(
    private val productDao: ProductDao,
    private val taskApi: TaskApi,
    private val networkMonitor: NetworkMonitor
) {
    
    companion object {
        private const val TAG = "OfflineProductRepo"
    }
    
    // Получение данных
    fun getAllProducts(): Flow<List<Product>> {
        return productDao.getAllProducts().map { entities ->
            ProductMapper.toDomainList(entities)
        }
    }
    
    fun getProductsByCategory(category: String): Flow<List<Product>> {
        return productDao.getProductsByCategory(category).map { entities ->
            ProductMapper.toDomainList(entities)
        }
    }
    
    fun getAllCategories(): Flow<List<String>> {
        return productDao.getAllCategories()
    }
    
    suspend fun getProductById(id: String): Product? {
        val entity = productDao.getProductById(id)
        return entity?.let { ProductMapper.toDomain(it) }
    }
    
    suspend fun getProductByServerId(serverId: String): Product? {
        val entity = productDao.getProductByServerId(serverId)
        return entity?.let { ProductMapper.toDomain(it) }
    }
    
    // Синхронизация - только загрузка с сервера (продукты только для чтения)
    suspend fun syncPendingProducts(): Boolean {
        // Для продуктов только загрузка с сервера, нет создания/обновления
        return loadFromServer()
    }
    
    // Загрузка данных с сервера
    suspend fun loadFromServer(): Boolean {
        return try {
            Log.d(TAG, "Загружаем продукты с сервера...")
            
            if (!networkMonitor.isConnected()) {
                Log.d(TAG, "Нет интернета, пропускаем загрузку продуктов")
                return false
            }
            
            val serverProducts = taskApi.service.getProducts()
            Log.d(TAG, "Получено ${serverProducts.size} продуктов с сервера")
            
            // Конвертируем в Entity с статусом SYNCED
            val entities = ProductMapper.toEntityList(serverProducts, SyncStatus.SYNCED)
            
            // Сохраняем в БД
            productDao.deleteAll()  // Очищаем старые данные
            productDao.insertAll(entities)
            
            Log.d(TAG, "Продукты успешно загружены и сохранены")
            true
            
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка загрузки продуктов с сервера: ${e.message}", e)
            false
        }
    }
    
    // Статистика синхронизации
    suspend fun getPendingCount(): Int = productDao.getPendingCount()
    suspend fun getFailedCount(): Int = productDao.getFailedCount()
    suspend fun getSyncedCount(): Int = productDao.getSyncedCount()
    
    // Поиск
    suspend fun searchProducts(query: String): List<Product> {
        val entities = productDao.searchProducts(query)
        return ProductMapper.toDomainList(entities)
    }
}