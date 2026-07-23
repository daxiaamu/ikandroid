package com.ikan.app.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "library")
data class LibraryEntity(
    @PrimaryKey val videoId: String,
    val title: String,
    val poster: String,
    val favorite: Boolean = false,
    val playedAt: Long? = null,
    val positionMs: Long = 0,
    val durationMs: Long = 0,
    val lineId: String? = null,
    val episodeName: String? = null,
    val streamUrl: String? = null,
)

@Dao
interface LibraryDao {
    @Query("SELECT * FROM library WHERE favorite = 1 ORDER BY title COLLATE NOCASE")
    fun favorites(): Flow<List<LibraryEntity>>

    @Query("SELECT * FROM library WHERE playedAt IS NOT NULL ORDER BY playedAt DESC")
    fun history(): Flow<List<LibraryEntity>>

    @Query("SELECT * FROM library WHERE videoId = :id")
    suspend fun find(id: String): LibraryEntity?

    @Upsert
    suspend fun upsert(entity: LibraryEntity)

    @Query("DELETE FROM library WHERE favorite = 0 AND playedAt IS NULL")
    suspend fun pruneEmpty()

    @Query("UPDATE library SET playedAt = NULL, positionMs = 0, durationMs = 0, lineId = NULL, episodeName = NULL, streamUrl = NULL")
    suspend fun clearHistory()
}

@Database(entities = [LibraryEntity::class], version = 1, exportSchema = true)
abstract class IKanDatabase : RoomDatabase() {
    abstract fun libraryDao(): LibraryDao

    companion object {
        fun create(context: Context): IKanDatabase = Room.databaseBuilder(
            context,
            IKanDatabase::class.java,
            "ikan.db",
        ).fallbackToDestructiveMigration(false).build()
    }
}
