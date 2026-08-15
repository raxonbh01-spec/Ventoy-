package com.aistudio.ventoyboot.mbxrtq.data.db

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PayloadDao {
    @Query("SELECT * FROM payloads ORDER BY isFavorite DESC, fileName ASC")
    fun getAllPayloads(): Flow<List<PayloadEntity>>

    @Query("SELECT * FROM payloads WHERE id = :id")
    suspend fun getPayloadById(id: Long): PayloadEntity?

    @Query("SELECT * FROM payloads WHERE osFamily = :osFamily ORDER BY fileName ASC")
    fun getPayloadsByFamily(osFamily: String): Flow<List<PayloadEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayload(payload: PayloadEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(payloads: List<PayloadEntity>)

    @Update
    suspend fun updatePayload(payload: PayloadEntity)

    @Delete
    suspend fun deletePayload(payload: PayloadEntity)

    @Query("DELETE FROM payloads WHERE id = :id")
    suspend fun deletePayloadById(id: Long)

    @Query("DELETE FROM payloads")
    suspend fun clearAll()
}

@Dao
interface UsbDriveDao {
    @Query("SELECT * FROM usb_drives ORDER BY lastConnected DESC")
    fun getAllDrives(): Flow<List<UsbDriveEntity>>

    @Query("SELECT * FROM usb_drives WHERE id = :id LIMIT 1")
    suspend fun getDriveById(id: String): UsbDriveEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDrive(drive: UsbDriveEntity)

    @Update
    suspend fun updateDrive(drive: UsbDriveEntity)

    @Delete
    suspend fun deleteDrive(drive: UsbDriveEntity)
}

@Dao
interface DistroCatalogDao {
    @Query("SELECT * FROM distro_catalog ORDER BY category ASC, name ASC")
    fun getAllDistros(): Flow<List<DistroCatalogEntity>>

    @Query("SELECT * FROM distro_catalog WHERE category = :category ORDER BY name ASC")
    fun getDistrosByCategory(category: String): Flow<List<DistroCatalogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(distros: List<DistroCatalogEntity>)

    @Query("SELECT COUNT(*) FROM distro_catalog")
    suspend fun getCount(): Int
}

@Database(
    entities = [PayloadEntity::class, UsbDriveEntity::class, DistroCatalogEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun payloadDao(): PayloadDao
    abstract fun usbDriveDao(): UsbDriveDao
    abstract fun distroCatalogDao(): DistroCatalogDao
}
