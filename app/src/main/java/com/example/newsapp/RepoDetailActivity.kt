package com.example.newsapp

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.transition.platform.MaterialContainerTransform
import io.noties.markwon.Markwon
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.linkify.LinkifyPlugin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URLEncoder

class RepoDetailActivity : AppCompatActivity() {

    private lateinit var descView: TextView
    private lateinit var metaView: TextView
    private lateinit var readmeView: TextView
    private lateinit var markwon: Markwon

    override fun onCreate(savedInstanceState: Bundle?) {

        // =====================================================================
        // ⚠️ 【暂时下线】共享元素过渡（容器变换）—— 2026-10-07
        //
        // 试了两轮，崩了两次，都修好了，但还没成。判断是：
        //   1a（居中面板 + 背景变暗）已经给了 80% 的效果，
        //   这一层"从卡片长出来"的糖，不值得卡在这儿 —— 先放一放，记账上。
        //
        // 想接着做？把这整块取消注释，同时把 MainActivity 里 showList()
        // 的跳转改回【带 ActivityOptions】的那个版本。
        //
        // 还没排除的嫌疑：
        //   · transitionName 里带了斜杠（"repo_flutter/flutter"）
        //   · Material 的这份 transition 和 windowIsTranslucent 一起用，
        //     可能还有别的讲究
        //
        // val transform = MaterialContainerTransform().apply {
        //     drawingViewId = R.id.panelBackdrop
        //     scrimColor = Color.TRANSPARENT
        //     duration = 400
        // }
        // window.sharedElementEnterTransition = transform
        // window.sharedElementReturnTransition = transform
        // =====================================================================

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_repo_detail)

        // ★ 点面板【外面】那一圈暗背景 → 关闭
        findViewById<View>(R.id.panelBackdrop).setOnClickListener { finish() }

        // ★ 点面板【本身】不该关闭。
        //   View 默认不"吃"点击事件 —— 不处理就会【穿透】给下面的背景，
        //   结果就是"点面板也会关掉"。这一行让它把点击吃掉。
        findViewById<View>(R.id.panel).isClickable = true

        // ★ "在浏览器中打开"也挂上按压回弹
        findViewById<Button>(R.id.btnOpenUrl).addPressSpring()

        // ★ 现在只传一个仓库名进来 —— 其余全部从数据库查
        val fullName = intent.getStringExtra(EXTRA_NAME).orEmpty()

        // ★★★ 面板的「共享元素名字」必须和列表里那张卡片【一模一样】。
        //     这个只能放在 setContentView 之后 —— 那时才拿得到这个 View。
        findViewById<View>(R.id.panel).transitionName = "repo_$fullName"

        descView = findViewById(R.id.detailDesc)
        metaView = findViewById(R.id.detailMeta)
        readmeView = findViewById(R.id.detailReadme)
        findViewById<TextView>(R.id.detailName).text = fullName

        markwon = Markwon.builder(this)
            .usePlugin(HtmlPlugin.create())
            .usePlugin(TablePlugin.create(this))
            .usePlugin(LinkifyPlugin.create())
            .build()

        val db = AppDatabase.get(this)
        val repoDao = db.repoDao()
        val transDao = db.translationDao()

        // ---------- 协程 A：查库 → 卡片 → 翻译简介 ----------
        lifecycleScope.launch {
            val repo = repoDao.getByFullName(fullName)
            if (repo == null) {
                descView.text = "（本地没有这条数据，回列表刷新一下）"
                return@launch
            }

            // ① 卡片：全是事实，秒出，不用翻译
            metaView.text = buildMeta(repo)

            // ② 地址和按钮（url 也存在库里）
            findViewById<TextView>(R.id.detailUrl).text = repo.url
            findViewById<Button>(R.id.btnOpenUrl).setOnClickListener { openInBrowser(repo.url) }

            // ③ 简介：先把英文放上去，不让人干等
            descView.text = repo.description

            // ④ 译文有就直接用，一个字都不翻
            val cached = transDao.get(fullName)
            if (cached != null) {
                descView.text = cached.descriptionZh
                Log.d("TransTest", "[缓存] 用已存的译文：${cached.descriptionZh}")
                return@launch
            }

            // ⑤ 简介里一个英文字母都没有 → 本来就是中文，不用翻
            if (repo.description.none { it in 'a'..'z' || it in 'A'..'Z' }) {
                Log.d("TransTest", "[跳过] 简介本来就没有英文，不用翻")
                return@launch
            }

            val zh = withContext(Dispatchers.IO) {
                try {
                    translate(repo.description)
                } catch (e: Exception) {
                    Log.e("TransTest", "翻译失败：${e.message}")
                    null
                }
            } ?: return@launch

            transDao.save(TranslationEntity(fullName, zh))
            descView.text = zh
            Log.d("TransTest", "[翻译] 翻好了，已存库：$zh")
        }

        // ---------- 协程 B：拉 README 并渲染 ----------
        lifecycleScope.launch {
            val md = withContext(Dispatchers.IO) {
                try {
                    fetchReadme(fullName)
                } catch (e: Exception) {
                    Log.e("ReadmeTest", "拉 README 失败：${e.message}")
                    null
                }
            } ?: return@launch

            markwon.setMarkdown(readmeView, md)
            Log.d("ReadmeTest", "README 已渲染：${md.length} 字")
        }
    }

    /** 把仓库的结构化信息拼成一张小卡片 —— 全是事实，不用翻译 */
    private fun buildMeta(e: RepoEntity): String {
        val lang = e.language.ifEmpty { "未知语言" }
        val lic = e.license.ifEmpty { "无许可证" }

        val lines = listOf(
            "$lang · $lic",
            "★${count(e.stars)} · ${count(e.forks)} fork · ${count(e.issues)} 待处理 issue",
            "创建 ${e.createdAt.take(7)} · 最近更新 ${e.pushedAt.take(10)}",
            if (e.topics.isEmpty()) ""
            else "标签：" + e.topics.split(",").take(5).joinToString(" · ")
        )
        return lines.filter { it.isNotEmpty() }.joinToString("\n")
    }

    /** 179358 → "17.9 万"；33213 → "3.3 万"；1234 → "1234" */
    private fun count(n: Int): String =
        if (n >= 10000) "${n / 10000}.${(n % 10000) / 1000} 万" else "$n"

    /** 用系统浏览器打开这个仓库主页（隐式 Intent：问系统谁能处理） */
    private fun openInBrowser(url: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "手机上找不到能打开链接的应用", Toast.LENGTH_SHORT).show()
        }
    }

    /** 调 MyMemory 翻译 */
    private suspend fun translate(text: String): String {
        val encoded = URLEncoder.encode(text, "UTF-8")
        // ★ 必须是 ?q= —— 中间多一个空格，参数名就变成 "q " 了
        val resp = HttpUtil.get("https://api.mymemory.translated.net/get?q=$encoded&langpair=en%7Czh-CN")

        val json = JSONObject(resp)

        // ★★★ 光"能解析出 JSON"不算成功：这个接口出错时【也返回 HTTP 200】，
        //     还把错误信息直接塞进 translatedText 字段里。要读它【自己的】成功标志。
        val status = json.optString("responseStatus")
        if (status != "200") {
            throw IllegalStateException("翻译接口没成功，responseStatus=$status")
        }

        return json.getJSONObject("responseData").getString("translatedText")
    }

    /** 拉这个仓库的 README，Base64 解码成 Markdown 文本 */
    private suspend fun fetchReadme(fullName: String): String {
        val resp = HttpUtil.get("https://api.github.com/repos/$fullName/readme")
        val b64 = JSONObject(resp).getString("content")
        return String(Base64.decode(b64, Base64.DEFAULT), Charsets.UTF_8)
    }

    companion object {
        private const val EXTRA_NAME = "repo_name"

        fun actionStart(context: Context, fullName: String, options: Bundle? = null) {
            val intent = Intent(context, RepoDetailActivity::class.java).apply {
                putExtra(EXTRA_NAME, fullName)
            }
            context.startActivity(intent, options)      // ★ 带 options 的那个重载
        }
    }
}
