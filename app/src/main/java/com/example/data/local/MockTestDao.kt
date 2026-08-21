package com.example.data.local

import androidx.room.*
import com.example.data.model.MockTest
import kotlinx.coroutines.flow.Flow

@Dao
interface MockTestDao {
    @Query("SELECT * FROM mock_tests ORDER BY timestamp DESC, id DESC")
    fun getAllMockTests(): Flow<List<MockTest>>

    @Query("SELECT * FROM mock_tests WHERE id = :id")
    suspend fun getMockTestById(id: Long): MockTest?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMockTest(mockTest: MockTest): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMockTests(mockTests: List<MockTest>)

    @Update
    suspend fun updateMockTest(mockTest: MockTest)

    @Delete
    suspend fun deleteMockTest(mockTest: MockTest)

    @Query("DELETE FROM mock_tests")
    suspend fun deleteAllMockTests()
}
