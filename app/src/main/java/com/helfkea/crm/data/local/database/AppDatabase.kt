package com.helfkea.crm.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.helfkea.crm.data.local.dao.ContragentDao
import com.helfkea.crm.data.local.dao.IndividualDao
import com.helfkea.crm.data.local.dao.InteractionDao
import com.helfkea.crm.data.local.dao.OrderDao
import com.helfkea.crm.data.local.dao.ProductDao
import com.helfkea.crm.data.local.dao.TaskDao
import com.helfkea.crm.data.local.entity.ContragentEntity
import com.helfkea.crm.data.local.entity.IndividualEntity
import com.helfkea.crm.data.local.entity.InteractionEntity
import com.helfkea.crm.data.local.entity.OrderEntity
import com.helfkea.crm.data.local.entity.ProductEntity
import com.helfkea.crm.data.local.entity.TaskEntity

@Database(
    entities = [
        TaskEntity::class, 
        ContragentEntity::class,
        IndividualEntity::class,
        InteractionEntity::class,
        OrderEntity::class,
        ProductEntity::class
    ],
    version = 7,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun taskDao(): TaskDao
    abstract fun contragentDao(): ContragentDao
    abstract fun individualDao(): IndividualDao
    abstract fun interactionDao(): InteractionDao
    abstract fun orderDao(): OrderDao
    abstract fun productDao(): ProductDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "helfkea_crm.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}