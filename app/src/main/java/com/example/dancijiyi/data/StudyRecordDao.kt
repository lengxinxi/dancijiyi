package com.example.dancijiyi.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

data class DailyStat(
    val date: String,
    val total: Int,
    val correct: Int
)

@Dao
interface StudyRecordDao {

    @Insert
    fun insert(record: StudyRecordEntity)

    /** 某一天的学习次数 */
    @Query("SELECT COUNT(*) FROM study_records WHERE date = :date")
    fun countByDate(date: String): Int

    /** 某一天的"认识"次数 */
    @Query("SELECT COUNT(*) FROM study_records WHERE date = :date AND result = 1")
    fun correctByDate(date: String): Int

    /** 最近 :days 天的按日统计（日期升序） */
    @Query("SELECT date, COUNT(*) AS total, SUM(CASE WHEN result = 1 THEN 1 ELSE 0 END) AS correct FROM study_records GROUP BY date ORDER BY date ASC")
    fun dailyStats(): List<DailyStat>

    @Query("SELECT COUNT(*) FROM study_records")
    fun totalCount(): Int

    @Query("SELECT COUNT(*) FROM study_records WHERE result = 1")
    fun totalCorrectCount(): Int

    /** 今天已学习的不同单词数 */
    @Query("SELECT COUNT(DISTINCT wordId) FROM study_records WHERE date = :date")
    fun distinctWordsByDate(date: String): Int
}
