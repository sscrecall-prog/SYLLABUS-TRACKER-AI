package com.example.data.local

import androidx.room.*
import com.example.data.model.ItemType
import com.example.data.model.SyllabusItem
import com.example.data.model.ChapterStatus
import com.example.data.model.SubjectHierarchy
import kotlinx.coroutines.flow.Flow

@Dao
interface SyllabusDao {
    @Transaction
    @Query("SELECT * FROM subjects WHERE id = :subjectId")
    fun getSubjectHierarchy(subjectId: Long): Flow<SubjectHierarchy?>

    @Transaction
    @Query("SELECT * FROM subjects ORDER BY orderIndex ASC")
    fun getAllSubjectHierarchies(): Flow<List<SubjectHierarchy>>

    @Query("SELECT * FROM syllabus_items ORDER BY orderIndex ASC, id ASC")
    fun getAllItems(): Flow<List<SyllabusItem>>

    @Query("SELECT * FROM syllabus_items WHERE subjectId = :subjectId ORDER BY orderIndex ASC, id ASC")
    fun getItemsForSubject(subjectId: Long): Flow<List<SyllabusItem>>

    @Query("SELECT * FROM syllabus_items WHERE id = :id")
    suspend fun getItemById(id: Long): SyllabusItem?

    @Query("SELECT * FROM syllabus_items WHERE parentId = :parentId ORDER BY orderIndex ASC, id ASC")
    fun getItemsByParent(parentId: Long): Flow<List<SyllabusItem>>

    @Query("SELECT * FROM syllabus_items WHERE status = :status ORDER BY id DESC")
    fun getItemsByStatus(status: ChapterStatus): Flow<List<SyllabusItem>>

    @Query("SELECT * FROM syllabus_items WHERE itemType = :itemType ORDER BY id DESC")
    fun getItemsByType(itemType: ItemType): Flow<List<SyllabusItem>>

    @Query("SELECT * FROM syllabus_items WHERE isImportant = 1")
    fun getImportantItems(): Flow<List<SyllabusItem>>

    @Query("SELECT * FROM syllabus_items WHERE isBookmarked = 1")
    fun getBookmarkedItems(): Flow<List<SyllabusItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: SyllabusItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<SyllabusItem>): List<Long>

    @Update
    suspend fun updateItem(item: SyllabusItem)

    @Update
    suspend fun updateItems(items: List<SyllabusItem>)

    @Delete
    suspend fun deleteItem(item: SyllabusItem)

    @Query("DELETE FROM syllabus_items WHERE id = :id OR parentId = :id")
    suspend fun deleteItemAndChildren(id: Long)

    @Query("DELETE FROM syllabus_items WHERE subjectId = :subjectId")
    suspend fun deleteItemsForSubject(subjectId: Long)

    @Query("DELETE FROM syllabus_items")
    suspend fun deleteAllItems()

    @Query("SELECT * FROM syllabus_items WHERE title LIKE '%' || :query || '%' OR notes LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%'")
    fun searchItems(query: String): Flow<List<SyllabusItem>>
}
