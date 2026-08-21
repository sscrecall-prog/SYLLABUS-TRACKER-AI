package com.example.data.repository

import com.example.data.local.*
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import kotlinx.coroutines.flow.map

class MockTestRepository(
    private val mockTestDao: MockTestDao
) {
    val allMockTests: Flow<List<MockTest>> = mockTestDao.getAllMockTests()

    suspend fun insertMockTest(mockTest: MockTest): Long = withContext(Dispatchers.IO) {
        mockTestDao.insertMockTest(mockTest)
    }


    suspend fun updateMockTest(mockTest: MockTest) = withContext(Dispatchers.IO) {
        mockTestDao.updateMockTest(mockTest)
    }


    suspend fun deleteMockTest(mockTest: MockTest) = withContext(Dispatchers.IO) {
        mockTestDao.deleteMockTest(mockTest)
    }


    suspend fun getMockTestById(id: Long): MockTest? = withContext(Dispatchers.IO) {
        mockTestDao.getMockTestById(id)
    }

    // Mistake Notebook / Error Diary CRUD

}
