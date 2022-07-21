package com.example.kbbqreview.data.roomplaces

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface StoredPlaceDao {
    //ONCONFLICT IGNORE DOESN'T WORK, BUT REPLACE DOES... Why?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: StoredPlace)

    @Update
    suspend fun update(item: StoredPlace)

    @Delete
    suspend fun delete(item: StoredPlace)
    //I use this
    @Query("DELETE FROM stored_place_table")
    suspend fun deleteAll()
    //I use this
    @Query("SELECT * FROM stored_place_table WHERE itemId = :id LIMIT 1")
    fun searchStoredItem(id: Long): List<StoredPlace>

    //This one works
    @Query("DELETE FROM stored_place_table WHERE itemId = :id")
    suspend fun delete(id: Long)
    //I use this???
    @Query("SELECT * FROM stored_place_table WHERE itemId = :key")
    suspend fun get(key: String): StoredPlace?
    //I use this
    @Query("SELECT * FROM stored_place_table ORDER BY itemId DESC")
    fun getAllItems(): LiveData<List<StoredPlace>>



}