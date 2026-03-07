package com.helfkea.crm.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.helfkea.crm.data.sync.SyncStatus
import java.util.Date

/**
 * Entity для хранения контрагентов в локальной БД
 */
@Entity(tableName = "contragents")
data class ContragentEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    
    @ColumnInfo(name = "server_id")
    val serverId: String?,
    
    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "full_name")
    val fullName: String?,
    
    @ColumnInfo(name = "inn")
    val inn: String?,
    
    @ColumnInfo(name = "kpp")
    val kpp: String?,
    
    @ColumnInfo(name = "ogrn")
    val ogrn: String?,
    
    @ColumnInfo(name = "legal_address")
    val legalAddress: String?,
    
    @ColumnInfo(name = "actual_address")
    val actualAddress: String?,
    
    @ColumnInfo(name = "phone")
    val phone: String?,
    
    @ColumnInfo(name = "email")
    val email: String?,
    
    @ColumnInfo(name = "website")
    val website: String?,
    
    @ColumnInfo(name = "manager")
    val manager: String?,
    
    @ColumnInfo(name = "status")
    val status: String?,
    
    @ColumnInfo(name = "category")
    val category: String?,
    
    @ColumnInfo(name = "notes")
    val notes: String?,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Date,
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Date,
    
    @ColumnInfo(name = "sync_status")
    val syncStatus: SyncStatus,
    
    @ColumnInfo(name = "last_sync_time")
    val lastSyncTime: Date?,
    
    @ColumnInfo(name = "sync_error")
    val syncError: String?
) {
    companion object {
        fun createLocal(
            name: String,
            fullName: String? = null,
            inn: String? = null,
            kpp: String? = null,
            ogrn: String? = null,
            legalAddress: String? = null,
            actualAddress: String? = null,
            phone: String? = null,
            email: String? = null,
            website: String? = null,
            manager: String? = null,
            status: String? = "Активен",
            category: String? = null,
            notes: String? = null
        ): ContragentEntity {
            val now = Date()
            return ContragentEntity(
                id = java.util.UUID.randomUUID().toString(),
                serverId = null,
                name = name,
                fullName = fullName,
                inn = inn,
                kpp = kpp,
                ogrn = ogrn,
                legalAddress = legalAddress,
                actualAddress = actualAddress,
                phone = phone,
                email = email,
                website = website,
                manager = manager,
                status = status,
                category = category,
                notes = notes,
                createdAt = now,
                updatedAt = now,
                syncStatus = SyncStatus.PENDING,
                lastSyncTime = null,
                syncError = null
            )
        }
    }
}