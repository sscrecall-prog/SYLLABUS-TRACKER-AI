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

class PlannerRepository(
    private val studyPlanDao: StudyPlanDao
) {
    val allPlans: Flow<List<StudyPlan>> = studyPlanDao.getAllPlans()


    fun getPlansForDate(dateStr: String): Flow<List<StudyPlan>> =
        studyPlanDao.getPlansForDate(dateStr)


    suspend fun insertPlan(plan: StudyPlan) = withContext(Dispatchers.IO) {
        studyPlanDao.insertPlan(plan)
    }


    suspend fun updatePlan(plan: StudyPlan) = withContext(Dispatchers.IO) {
        studyPlanDao.updatePlan(plan)
    }


    suspend fun deletePlan(plan: StudyPlan) = withContext(Dispatchers.IO) {
        studyPlanDao.deletePlan(plan)
    }

    // Goals CRUD

}
