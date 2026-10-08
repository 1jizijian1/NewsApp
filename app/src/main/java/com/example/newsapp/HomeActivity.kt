package com.example.newsapp

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * 【首页 / 入口页】
 *
 * 它自己什么都不显示 —— 只负责"把人送到该去的地方"。
 * 背景渐变和插画是"白天/夜晚两套"，由 drawable-night/ 自动挑，这里一行代码都没有。
 * 但【欢迎语】得自己判断时间 —— 文字不归资源系统管。
 */
class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        // ★★★ 这两样是一对，缺一不可：
        //   Manifest 里的 android:theme="...Starting" 让系统"先用启动画面主题"
        //   这一行负责"把它换回正常主题"（按 postSplashScreenTheme 指定的换）
        // 少写这一行 → 整个页面会一直停在 Theme.SplashScreen 里
        //            → Material3 控件找不到属性 → 一开就崩
        val splash = installSplashScreen()

        // ⚠️⚠️ 【临时代码】—— 只为看清那三根柱子的动画，看完请整块删掉！
        //
        // 我们的首页 onCreate 太快了（查个时间 + 挂 4 个监听器，几十毫秒），
        // 系统一看"你准备好了"就立刻把启动画面撤走 —— 动画根本来不及看。
        // 这几行让它多停 1.5 秒。
        //
        // ★ 注意位置：setKeepOnScreenCondition 必须在 super.onCreate 【之前】，
        //   而下面对那个 lifecycleScope.launch 必须在【之后】（生命周期要先就绪）。
        //   —— 今天第三次"位置有讲究"。
        var keep = true
        splash.setKeepOnScreenCondition { keep }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        lifecycleScope.launch {
            delay(1500)
            keep = false
        }

        showGreeting()

        // ★ 四个入口都挂上"按压回弹"
        listOf(R.id.btnRank, R.id.btnNews, R.id.btnSearch, R.id.btnFavorite).forEach { id ->
            findViewById<MaterialButton>(id).addPressSpring()
        }

        findViewById<MaterialButton>(R.id.btnRank).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        findViewById<MaterialButton>(R.id.btnNews).setOnClickListener {
            Toast.makeText(this, "科技新闻还没做 —— 下一步", Toast.LENGTH_SHORT).show()
        }

        findViewById<MaterialButton>(R.id.btnSearch).setOnClickListener {
            Toast.makeText(this, "搜索还没做", Toast.LENGTH_SHORT).show()
        }

        findViewById<MaterialButton>(R.id.btnFavorite).setOnClickListener {
            Toast.makeText(this, "收藏还没做", Toast.LENGTH_SHORT).show()
        }
    }

    /** 欢迎语跟着钟点变。文字是"活的"，资源系统管不了它，只能自己算。 */
    private fun showGreeting() {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

        val (greeting, sub) = when (hour) {
            in 5..10 -> "早上好" to "新的一天，想找点什么？"
            in 11..13 -> "中午好" to "要不要看看今天有什么新项目？"
            in 14..17 -> "下午好" to "想找点什么？"
            in 18..22 -> "晚上好" to "今天想看点啥？"
            else -> "夜深了" to "还在写代码吗？"
        }

        findViewById<TextView>(R.id.tvGreeting).text = greeting
        findViewById<TextView>(R.id.tvSub).text = sub
    }
}
