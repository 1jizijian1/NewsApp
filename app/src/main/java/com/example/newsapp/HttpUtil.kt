package com.example.newsapp

import java.net.HttpURLConnection
import java.net.URL

object HttpUtil{
    suspend fun get(address:String):String{
        val connection = URL(address).openConnection() as HttpURLConnection
        return try {
            connection.connectTimeout = 5000
            connection.readTimeout = 8000
            connection.inputStream.bufferedReader().readText()
        } finally {
            connection.disconnect()          // ★ 不管成功失败，都要断开
        }
    }
}