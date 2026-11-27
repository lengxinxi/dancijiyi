package com.example.dancijiyi.data

import android.content.Context

/**
 * 每日学习目标（个/天），存 SharedPreferences。
 * 目标值用于学习进度条：今日已学（去重）/ 目标。
 */
class DailyGoalStore(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun get(): Int = prefs.getInt(KEY_GOAL, DEFAULT_GOAL)

    /** 设置目标，范围外自动收敛到合法区间 */
    fun set(goal: Int): Int {
        val value = goal.coerceIn(MIN_GOAL, MAX_GOAL)
        prefs.edit().putInt(KEY_GOAL, value).apply()
        return value
    }

    companion object {
        private const val PREF_NAME = "word_memo_settings"
        private const val KEY_GOAL = "daily_goal"

        const val DEFAULT_GOAL = 20
        const val MIN_GOAL = 5
        const val MAX_GOAL = 200

        @Volatile
        private var instance: DailyGoalStore? = null

        fun get(context: Context): DailyGoalStore =
            instance ?: synchronized(this) {
                instance ?: DailyGoalStore(context).also { instance = it }
            }
    }
}
