package com.helfkea.crm.data.local.dao

import androidx.room.*
import com.helfkea.crm.data.local.entity.ProductEntity
import com.helfkea.crm.data.sync.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    
    // CRUD операции
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<ProductEntity>)
    
    @Update
    suspend fun updateProduct(product: ProductEntity)
    
    @Delete
    suspend fun deleteProduct(product: ProductEntity)
    
    @Query("DELETE FROM products")
    suspend fun deleteAll()
    
    // Запросы
    @Query("SELECT * FROM products ORDER BY name")
    fun getAllProducts(): Flow<List<ProductEntity>>
    
    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: String): ProductEntity?
    
    @Query("SELECT * FROM products WHERE serverId = :serverId")
    suspend fun getProductByServerId(serverId: String): ProductEntity?
    
    @Query("SELECT * FROM products WHERE category = :category ORDER BY name")
    fun getProductsByCategory(category: String): Flow<List<ProductEntity>>
    
    @Query("SELECT DISTINCT category FROM products ORDER BY category")
    fun getAllCategories(): Flow<List<String>>
    
    // Статистика синхронизации
    @Query("SELECT COUNT(*) FROM products WHERE syncStatus = 'PENDING'")
    suspend fun getPendingCount(): Int
    
    @Query("SELECT COUNT(*) FROM products WHERE syncStatus = 'FAILED'")
    suspend fun getFailedCount(): Int
    
    @Query("SELECT COUNT(*) FROM products WHERE syncStatus = 'SYNCED'")
    suspend fun getSyncedCount(): Int
    
    // Обновление статуса синхронизации
    @Query("UPDATE products SET syncStatus = :syncStatus, lastSyncTime = :lastSyncTime, syncError = :syncError WHERE id = :id")
    suspend fun updateProductSyncStatus(id: String, syncStatus: SyncStatus, lastSyncTime: Long?, syncError: String?)
    
    suspend fun updateProductSyncStatus(id: String, syncStatus: SyncStatus, lastSyncTime: java.util.Date?, syncError: String?) {
        updateProductSyncStatus(id, syncStatus, lastSyncTime?.time, syncError)
    }
    
    // Поиск
    @Query("SELECT * FROM products WHERE name LIKE '%' || :query || '%' OR article LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%' ORDER BY name")
    suspend fun searchProducts(query: String): List<ProductEntity>
}