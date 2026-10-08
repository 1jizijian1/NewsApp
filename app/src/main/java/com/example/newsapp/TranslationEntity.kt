package com.example.newsapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "translation")
data class TranslationEntity(
    @PrimaryKey val fullName: String,
    val descriptionZh: String
)