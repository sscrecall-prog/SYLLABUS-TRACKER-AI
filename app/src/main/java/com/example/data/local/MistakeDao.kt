package com.example.data.local

import androidx.room.*
import com.example.data.model.MistakeCategory
import com.example.data.model.MistakeEntry
import com.example.data.model.MistakeResolutionStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface MistakeDao {
    @Query("SELECT * FROM mistake_entries ORDER BY importanceStar DESC, createdTimestamp DESC")
    fun getAllMistakes(): Flow<List<MistakeEntry>>

    @Query("SELECT * FROM mistake_entries WHERE subjectId = :subjectId ORDER BY createdTimestamp DESC")
    fun getMistakesBySubject(subjectId: Long): Flow<List<MistakeEntry>>

    @Query("SELECT * FROM mistake_entries WHERE resolutionStatus = :status ORDER BY createdTimestamp DESC")
    fun getMistakesByStatus(status: MistakeResolutionStatus): Flow<List<MistakeEntry>>

    @Query("SELECT * FROM mistake_entries WHERE category = :category ORDER BY createdTimestamp DESC")
    fun getMistakesByCategory(category: MistakeCategory): Flow<List<MistakeEntry>>

    @Query("SELECT * FROM mistake_entries WHERE resolutionStatus != 'MASTERED' AND nextReviewTimestamp <= :currentTimestamp ORDER BY nextReviewTimestamp ASC")
    fun getReviewDueMistakes(currentTimestamp: Long): Flow<List<MistakeEntry>>

    @Query("SELECT * FROM mistake_entries WHERE id = :id")
    suspend fun getMistakeById(id: Long): MistakeEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMistake(mistake: MistakeEntry): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMistakes(mistakes: List<MistakeEntry>)

    @Update
    suspend fun updateMistake(mistake: MistakeEntry)

    @Delete
    suspend fun deleteMistake(mistake: MistakeEntry)

    @Query("DELETE FROM mistake_entries WHERE id = :id")
    suspend fun deleteMistakeById(id: Long)

    @Query("SELECT COUNT(*) FROM mistake_entries")
    fun getMistakesCount(): Flow<Int>
}
