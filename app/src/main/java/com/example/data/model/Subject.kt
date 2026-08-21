package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subjects")
data class Subject(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val code: String = "",
    val iconName: String = "School",
    val colorHex: String = "#2D4F1E",
    val orderIndex: Int = 0,
    val description: String = ""
)
