package com.example.newsapp

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * 【角色三：Database —— 总入口】
 *
 * 这里说明：这个库里有哪些表、现在是第几版。
 * 真正的数据库对象从这里拿。
 */
@Database(
    entities = [RepoEntity::class, TranslationEntity::class],
    version = 4,                 // ★ 3 → 4：repo 表加了 7 个卡片字段，旧数据作废
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun repoDao(): RepoDao
    abstract fun translationDao(): TranslationDao

    companion object {

        @Volatile
        private var instance: AppDatabase? = null

        /**
         * 1. 数据库【全局只能有一份】—— 建出两份，两边写的缓存互相看不见。
         * 2. 那为什么不直接写 object？—— 建库要 Context，
         *    而 object 没有构造参数，塞不进去。所以只能自己手动做单例。
         * 3. 用 applicationContext —— 数据库活得比 Activity 久，
         *    抓住 Activity 不放就是内存泄漏。
         */
        fun get(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "news.db"
                )
                    // 开发阶段图省事：版本对不上就把旧库删了重建。
                    // 正式项目不能这么干 —— 用户的数据不能随便丢，得写 Migration。
                    .fallbackToDestructiveMigration()
                    .build().also { instance = it }
            }
        }
    }
}
