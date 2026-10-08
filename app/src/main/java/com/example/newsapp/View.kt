package com.example.newsapp

import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator

/**
 * 【给任何 View 挂上"按压回弹"】
 *
 * 按下 → 快速缩到 93%（干脆，不拖泥带水）
 * 松开 → 慢一点弹回 100%，而且【冲过头再回来】
 *
 * ★ 关键在那个 OvershootInterpolator ——
 *   它不是"从 0.93 平滑回到 1.0"，而是"冲过 1.0（比如 1.06），再回落"。
 *   那一点点过头，就是"重量感"的来源。
 *
 * ★ 第 5 章那条规矩：向哪个类加扩展函数，就建一个【同名】的文件。
 *   这里是给 View 加的，所以文件叫 View.kt。
 */
fun View.addPressSpring() {
    setOnTouchListener { v, event ->
        when (event.action) {

            MotionEvent.ACTION_DOWN -> {
                v.animate()
                    .scaleX(0.93f).scaleY(0.93f)
                    .setDuration(90)
                    .setInterpolator(DecelerateInterpolator())
                    .start()
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                v.animate()
                    .scaleX(1f).scaleY(1f)
                    .setDuration(340)
                    .setInterpolator(OvershootInterpolator(2.2f))   // ★★ 手感旋钮
                    .start()
            }
        }

        // ★★★ 必须返回 false ——
        //   返回值的含义是"这个事件我处理掉了吗？"
        //     写 true  = 我处理了，别再往下传 → 【点击事件就没了】，按钮点了没反应
        //     写 false = 我没处理完，继续传   → 点击照常触发
        false
    }
}
