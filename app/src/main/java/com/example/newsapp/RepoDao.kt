package com.example.newsapp

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * 【角色二：Dao —— 能对这张表干什么】
 *
 * 你只写"要什么"，SQL 由 Room 生成。
 * 注意每个方法都带 suspend —— 数据库读写同样属于"要等的"事，
 * 必须扔进协程里，不能在主线程上干。
 */
@Dao
interface RepoDao {

    /** 存一批。主键撞了就覆盖 —— 同一个仓库反复拉，不会存出重复行 */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<RepoEntity>)

    /** 取某一类，按星数从多到少 */
    @Query("SELECT * FROM repo WHERE category = :category ORDER BY stars DESC")
    suspend fun getByCategory(category: String): List<RepoEntity>

    /** 取全部 */
    @Query("SELECT * FROM repo ORDER BY stars DESC")
    suspend fun getAll(): List<RepoEntity>

    /** 按仓库名取一条 —— 详情页用 */
    @Query("SELECT * FROM repo WHERE fullName = :fullName LIMIT 1")
    suspend fun getByFullName(fullName: String): RepoEntity?
}
