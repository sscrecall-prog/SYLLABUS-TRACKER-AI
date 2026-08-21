package com.example.data.local

import androidx.room.*
import com.example.data.model.AchievementBadge
import kotlinx.coroutines.flow.Flow

@Dao
interface AchievementBadgeDao {
    @Query("SELECT * FROM achievement_badges ORDER BY isUnlocked DESC, rewardXp ASC")
    fun getAllBadges(): Flow<List<AchievementBadge>>

    @Query("SELECT * FROM achievement_badges WHERE isUnlocked = 1 ORDER BY unlockedAt DESC")
    fun getUnlockedBadges(): Flow<List<AchievementBadge>>

    @Query("SELECT * FROM achievement_badges WHERE id = :id LIMIT 1")
    suspend fun getBadgeById(id: String): AchievementBadge?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBadges(badges: List<AchievementBadge>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBadge(badge: AchievementBadge)

    @Update
    suspend fun updateBadge(badge: AchievementBadge)

    @Query("SELECT COUNT(*) FROM achievement_badges WHERE isUnlocked = 1")
    fun getUnlockedCount(): Flow<Int>
}
