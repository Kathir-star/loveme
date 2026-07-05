package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StyleDao {
    @Query("SELECT * FROM style_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<StyleHistoryEntity>>

    @Query("SELECT * FROM style_history WHERE id = :id")
    fun getHistoryById(id: Int): Flow<StyleHistoryEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(entity: StyleHistoryEntity): Long

    @Query("DELETE FROM style_history WHERE id = :id")
    suspend fun deleteHistoryById(id: Int)

    @Query("DELETE FROM style_history")
    suspend fun clearAllHistory()
}
