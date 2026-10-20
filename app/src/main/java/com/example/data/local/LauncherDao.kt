package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AppItem
import com.example.data.model.CategoryTab
import com.example.data.model.WidgetItem
import kotlinx.coroutines.flow.Flow

@Dao
interface LauncherDao {
    @Query("SELECT * FROM apps ORDER BY orderIndex ASC, launchCount DESC")
    fun getAllApps(): Flow<List<AppItem>>

    @Query("SELECT * FROM apps WHERE isHidden = 0 ORDER BY orderIndex ASC, launchCount DESC")
    fun getVisibleApps(): Flow<List<AppItem>>

    @Query("SELECT * FROM apps WHERE isHidden = 1 ORDER BY label ASC")
    fun getHiddenApps(): Flow<List<AppItem>>

    @Query("SELECT * FROM apps WHERE isPinnedToDock = 1 AND isHidden = 0 ORDER BY orderIndex ASC")
    fun getDockApps(): Flow<List<AppItem>>

    @Query("SELECT * FROM apps WHERE isFavorite = 1 AND isHidden = 0 ORDER BY orderIndex ASC")
    fun getFavoriteApps(): Flow<List<AppItem>>

    @Query("SELECT * FROM apps WHERE categoryId = :categoryId AND isHidden = 0 ORDER BY orderIndex ASC")
    fun getAppsForCategory(categoryId: String): Flow<List<AppItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApps(apps: List<AppItem>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApp(app: AppItem)

    @Update
    suspend fun updateApp(app: AppItem)

    @Query("UPDATE apps SET isHidden = :hidden WHERE packageName = :packageName")
    suspend fun setAppHidden(packageName: String, hidden: Boolean)

    @Query("UPDATE apps SET isPinnedToDock = :pinned WHERE packageName = :packageName")
    suspend fun setAppPinnedToDock(packageName: String, pinned: Boolean)

    @Query("UPDATE apps SET isFavorite = :favorite WHERE packageName = :packageName")
    suspend fun setAppFavorite(packageName: String, favorite: Boolean)

    @Query("UPDATE apps SET categoryId = :categoryId WHERE packageName = :packageName")
    suspend fun setAppCategory(packageName: String, categoryId: String)

    @Query("UPDATE apps SET categoryId = :newCategoryId WHERE categoryId = :oldCategoryId")
    suspend fun reassignAppsCategory(oldCategoryId: String, newCategoryId: String)

    @Query("UPDATE apps SET launchCount = launchCount + 1, lastLaunched = :time WHERE packageName = :packageName")
    suspend fun incrementLaunch(packageName: String, time: Long)

    // Categories
    @Query("SELECT * FROM categories ORDER BY orderIndex ASC")
    fun getAllCategories(): Flow<List<CategoryTab>>

    @Query("SELECT * FROM categories WHERE id = :id LIMIT 1")
    suspend fun getCategoryById(id: String): CategoryTab?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryTab>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryTab)

    @Update
    suspend fun updateCategory(category: CategoryTab)

    @Query("UPDATE categories SET title = :newTitle, iconName = :newIcon, colorHex = :newColor WHERE id = :id")
    suspend fun updateCategoryDetails(id: String, newTitle: String, newIcon: String, newColor: String)

    @Query("UPDATE categories SET orderIndex = :newOrder WHERE id = :id")
    suspend fun updateCategoryOrder(id: String, newOrder: Int)

    @Query("DELETE FROM categories WHERE id = :id AND isCustom = 1")
    suspend fun deleteCategory(id: String)

    // Widgets
    @Query("SELECT * FROM widgets ORDER BY orderIndex ASC")
    fun getAllWidgets(): Flow<List<WidgetItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWidgets(widgets: List<WidgetItem>)

    @Query("UPDATE widgets SET isEnabled = :enabled WHERE id = :id")
    suspend fun setWidgetEnabled(id: String, enabled: Boolean)
}
