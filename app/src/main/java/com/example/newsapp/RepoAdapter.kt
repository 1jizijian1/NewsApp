package com.example.newsapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class RepoAdapter(
    private val repoList: List<Repo>,
    private val onItemClick: (Repo, View) -> Unit        // ★ 多带一个 View（共享元素要用）
) : RecyclerView.Adapter<RepoAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val repoRank: TextView = view.findViewById(R.id.repoRank)
        val repoName: TextView = view.findViewById(R.id.repoName)
        val repoUrl: TextView = view.findViewById(R.id.repoUrl)
        val repoStars: TextView = view.findViewById(R.id.repoStars)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.repo_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val repo = repoList[position]

        // ★★★ 共享元素的名字。
        //     必须在 onBindViewHolder 里设 —— 因为 ViewHolder 会【复用】，
        //     这是一个"设在 View 上"的东西，属于我们那条铁律的管辖范围。
        //     名字里带上仓库名，所以每张卡片的名字都不同。
        holder.itemView.transitionName = "repo_${repo.name}"

        // ★ 排名：position 从 0 开始，所以要 +1
        holder.repoRank.text = "${position + 1}"

        // ★ 每一条都要"上漆"。
        //   写成 if (position < 3) { setTextColor(...) } 是不行的 ——
        //   ViewHolder 是循环使用的车，第 4 名往后不上漆，
        //   车身上就还留着上一位乘客（第 1/2/3 名）的金银铜。
        val colorRes = when (position) {
            0 -> R.color.rank_gold
            1 -> R.color.rank_silver
            2 -> R.color.rank_bronze
            else -> R.color.rank_normal
        }
        holder.repoRank.setTextColor(
            ContextCompat.getColor(holder.itemView.context, colorRes)
        )

        holder.repoName.text = repo.name
        holder.repoUrl.text = repo.url.removePrefix("https://")
        holder.repoStars.text = "★${repo.stars}"

        holder.itemView.setOnClickListener { onItemClick(repo, holder.itemView) }
    }

    override fun getItemCount() = repoList.size
}
