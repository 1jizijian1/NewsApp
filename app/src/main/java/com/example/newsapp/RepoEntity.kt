package com.example.newsapp

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 【角色一：Entity —— 表长什么样】
 *
 * 一个 @Entity 类 = 数据库里的一张表。
 *     类名     →  表名（这里用 tableName 指定成 "repo"）
 *     每个字段  →  表里的一列
 *     每个对象  →  表里的一行
 */
@Entity(tableName = "repo")
data class RepoEntity(
    /** 主键：这一行的身份证，不能重复。full_name 天然唯一，直接拿来当主键 */
    @PrimaryKey val fullName: String,

    val stars: Int,
    val url: String,
    val description: String,

    /** 属于哪个榜（topic:android / language:python …）。有了它才能"只读某一类" */
    val category: String,

    // ★ 卡片信息
    val language: String,     // 可能是 ""（GitHub 给的是 null）
    val license: String,      // 可能是 ""（很多仓库没有许可证）
    val forks: Int,
    val issues: Int,
    val createdAt: String,    // "2015-03-06T22:54:58Z"
    val pushedAt: String,
    val topics: String        // 逗号分隔的一串；""表示没有标签
)
