package com.fuck.learn.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StreamerGroupDao {
    @Insert
    suspend fun insert(group: StreamerGroup): Long

    @Query("SELECT id FROM streamer_group WHERE name = :name LIMIT 1")
    suspend fun getGroupIdByName(name: String): Long?

    @Query("SELECT * FROM streamer_group")
    suspend fun getAllGroupsOnce(): List<StreamerGroup>

    @Query("SELECT * FROM streamer_group ORDER BY displayOrder ASC")
    fun getAllGroups(): Flow<List<StreamerGroup>>

    @Query("SELECT COUNT(*) FROM streamer_group")
    suspend fun getGroupCount(): Int

    @Query("DELETE FROM streamer_group WHERE id = :groupId")
    suspend fun deleteGroup(groupId: Long)

    @Query("UPDATE streamer_group SET name = :newName WHERE id = :groupId")
    suspend fun renameGroup(groupId: Long, newName: String)

    @Query("SELECT COUNT(*) FROM streamer_group WHERE name = :name")
    suspend fun isGroupNameExists(name: String): Int
}
