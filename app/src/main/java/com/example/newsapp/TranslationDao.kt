package com.example.newsapp

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface TranslationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(t: TranslationEntity)

    @Query("SELECT * FROM translation WHERE fullName = :fullName")
    suspend fun get(fullName: String): TranslationEntity?
}