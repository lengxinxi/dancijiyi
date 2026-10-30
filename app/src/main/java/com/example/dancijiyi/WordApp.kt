package com.example.dancijiyi

import android.app.Application
import android.util.Log
import com.example.dancijiyi.data.WordRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * 全局 Application：启动时初始化词库。
 */
class WordApp : Application() {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        appScope.launch {
            val imported = WordRepository.get(this@WordApp).seedIfNeeded()
            if (imported > 0) {
                Log.i(TAG, "内置词库导入完成，共 $imported 个单词")
            }
        }
    }

    companion object {
        private const val TAG = "WordApp"
    }
}
