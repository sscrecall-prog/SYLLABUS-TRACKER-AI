package com.example.data.local

import androidx.room.*
import com.example.data.model.StudyPlan
import kotlinx.coroutines.flow.Flow

@Dao
interface StudyPlanDao {
    @Query("SELECT * FROM study_plans WHERE dateStr = :dateStr ORDER BY timeStr ASC, id ASC")
    fun getPlansForDate(dateStr: String): Flow<List<StudyPlan>>

    @Query("SELECT * FROM study_plans ORDER BY dateStr DESC, timeStr ASC")
    fun getAllPlans(): Flow<List<StudyPlan>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlan(plan: StudyPlan): Long

    @Update
    suspend fun updatePlan(plan: StudyPlan)

    @Delete
    suspend fun deletePlan(plan: StudyPlan)

    @Query("DELETE FROM study_plans WHERE id = :id")
    suspend fun deletePlanById(id: Long)

    @Query("DELETE FROM study_plans")
    suspend fun deleteAllPlans()
}
