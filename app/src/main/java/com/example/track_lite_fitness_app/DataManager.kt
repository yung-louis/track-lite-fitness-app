package com.example.track_lite_fitness_app

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update

@Database(entities = [DataType::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dataItemDao(): DataItemDao
}
@Dao
interface DataItemDao {
    @Insert
    suspend fun insertDataType(dataType: DataType)

    @Update
    suspend fun updateDataType(dataType: DataType)
    @Query("DELETE FROM DataType")
    suspend fun deleteAllData()
    @Query("SELECT * FROM DataType WHERE dateId = :dateId")
    suspend fun getDataItemsByDateId(dateId: String): DataType?
}

@Entity
data class DataType(
    @PrimaryKey val dateId: String,
    val json: String,
)
