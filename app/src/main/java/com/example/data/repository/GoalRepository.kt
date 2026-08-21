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

class GoalRepository(
    private val goalDao: GoalDao
) {
    val allGoals: Flow<List<Goal>> = goalDao.getAllGoals()

    suspend fun insertGoal(goal: Goal) = withContext(Dispatchers.IO) {
        goalDao.insertGoal(goal)
    }


    suspend fun updateGoal(goal: Goal) = withContext(Dispatchers.IO) {
        goalDao.updateGoal(goal)
    }


    suspend fun deleteGoal(goal: Goal) = withContext(Dispatchers.IO) {
        goalDao.deleteGoal(goal)
    }

    // Mock Tests CRUD

}
