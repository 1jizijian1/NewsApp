package com.example.newsapp

import android.app.ActivityOptions
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    /**
     * ★ 榜单清单。
     *
     * 每一条是"一对"：
     *     左边 = 给用户看的名字
     *     右边 = 真正传给 GitHub 的 query
     *
     * 为什么要分开？因为 loadRepos(query) 收的是后者。
     * 名字只是"壳"，用户看不懂 topic:android 这种东西。
     */
    private val categories = listOf(
        "Android 榜" to "topic:android",
        "Python 榜" to "language:python",
        "Java 榜" to "language:java"
    )

    private lateinit var repoDao: RepoDao
    private lateinit var listView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        repoDao = AppDatabase.get(this).repoDao()
        listView = findViewById(R.id.repoList)
        listView.layoutManager = LinearLayoutManager(this)

        // ★ 下拉框：塞选项 → 默认选第一个 → 选了就换榜
        val dropdown = findViewById<MaterialAutoCompleteTextView>(R.id.categoryDropdown)
        dropdown.setSimpleItems(categories.map { it.first }.toTypedArray())
        dropdown.setText(categories[0].first, false)   // 第二个参数 false = 别拿它去过滤列表
        dropdown.setOnItemClickListener { _, _, position, _ ->
            loadRepos(categories[position].second)
        }

        loadRepos(categories[0].second)
    }

    private fun loadRepos(query: String) {
        lifecycleScope.launch {

            // ★ 第一步：先问数据库。有就立刻显示 —— 这一步不碰网络，所以是"秒出"
            val cached = repoDao.getByCategory(query)
            if (cached.isNotEmpty()) {
                showList(cached.map { it.toRepo() })
                Log.d("RoomTest", "[缓存] $query 命中 ${cached.size} 条，先显示")
            }

            // ★ 第二步：再去拉新的。断了网？catch 住 —— 缓存已经在屏幕上了，不慌
            val fresh = withContext(Dispatchers.IO) {
                try {
                    fetchRepos(query)
                } catch (e: Exception) {
                    Log.e("RoomTest", "[网络] 拉取失败：${e.message}")
                    emptyList()
                }
            }

            // ★ 第三步：真拉到了 → 存库 → 更新画面
            if (fresh.isNotEmpty()) {
                repoDao.insertAll(fresh.map { it.toEntity(query) })
                showList(fresh)
                Log.d("RoomTest", "[网络] $query 拉到 ${fresh.size} 条，已存库")
            }
        }
    }

    /** 只干一件事：网络请求 + 解析。不碰数据库，不碰界面 */
    private suspend fun fetchRepos(query: String): List<Repo> {
        val text = HttpUtil.get("https://api.github.com/search/repositories?q=$query&sort=stars&order=desc&per_page=30")

        val items = JSONObject(text).getJSONArray("items")
        val list = mutableListOf<Repo>()
        for (i in 0 until items.length()) {
            val obj = items.getJSONObject(i)

            // license 整个对象可能是 null（很多仓库根本没有许可证）
            val licenseObj = if (obj.isNull("license")) null else obj.getJSONObject("license")
            // topics 是一个字符串数组，用逗号连起来存
            val topicsArr = obj.getJSONArray("topics")
            val topics = (0 until topicsArr.length()).joinToString(",") { topicsArr.getString(it) }

            list.add(
                Repo(
                    name = obj.getString("full_name"),
                    stars = obj.getInt("stargazers_count"),
                    url = obj.getString("html_url"),
                    description = if (obj.isNull("description")) "（这个仓库没写简介）"
                                  else obj.getString("description"),

                    language = if (obj.isNull("language")) "" else obj.getString("language"),
                    license = licenseObj?.optString("spdx_id").orEmpty(),
                    forks = obj.getInt("forks_count"),
                    issues = obj.getInt("open_issues_count"),
                    createdAt = obj.getString("created_at"),
                    pushedAt = obj.getString("pushed_at"),
                    topics = topics
                )
            )
        }
        return list
    }

    /** 只干一件事：把列表放上屏幕。不碰网络，不碰数据库 */
    private fun showList(list: List<Repo>) {
        // ⚠️ 这里原本会带一个 ActivityOptions，做"从卡片长出来"的共享元素过渡。
        //    暂时下线 —— 原因见 RepoDetailActivity 顶部那段说明。
        listView.adapter = RepoAdapter(list) { repo, _ ->
            // ★ 现在只传一个仓库名 —— 详情页自己按名字去数据库取全部
            RepoDetailActivity.actionStart(this, repo.name)
        }
    }
}
