package com.example.newsapp

/**
 * 【网络来的数据】从 GitHub 接口解析出来的一个仓库。
 */
data class Repo(
    val name: String,
    val stars: Int,
    val url: String,
    val description: String,

    // ★ 卡片信息：全部是 GitHub 直接给的【事实】，不需要翻译
    val language: String,     // "Dart"；GitHub 会给 null → 统一存成 ""
    val license: String,      // "BSD-3-Clause"；可能为 null → ""
    val forks: Int,
    val issues: Int,
    val createdAt: String,    // "2015-03-06T22:54:58Z"
    val pushedAt: String,
    val topics: String        // 逗号分隔："android,flutter,dart"；没有就是 ""
)

/* ---------------- 中间那道翻译 ---------------- */

fun Repo.toEntity(category: String) = RepoEntity(
    fullName = name,
    stars = stars,
    url = url,
    description = description,
    category = category,
    language = language,
    license = license,
    forks = forks,
    issues = issues,
    createdAt = createdAt,
    pushedAt = pushedAt,
    topics = topics
)

fun RepoEntity.toRepo() = Repo(
    name = fullName,
    stars = stars,
    url = url,
    description = description,
    language = language,
    license = license,
    forks = forks,
    issues = issues,
    createdAt = createdAt,
    pushedAt = pushedAt,
    topics = topics
)
