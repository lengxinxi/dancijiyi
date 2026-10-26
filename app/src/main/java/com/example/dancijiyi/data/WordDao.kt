package com.example.dancijiyi.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface WordDao {

    @Insert
    fun insertAll(words: List<WordEntity>)

    @Query("SELECT COUNT(*) FROM words")
    fun count(): Int

    /** 随机取一批未掌握的单词用于学习 */
    @Query("SELECT * FROM words WHERE mastered = 0 ORDER BY RANDOM() LIMIT :limit")
    fun randomUnmastered(limit: Int): List<WordEntity>

    /** 随机取一批已学过但未掌握的单词用于复习（学过 = box > 0 或 lastStudyAt > 0） */
    @Query("SELECT * FROM words WHERE mastered = 0 AND lastStudyAt > 0 ORDER BY RANDOM() LIMIT :limit")
    fun randomForReview(limit: Int): List<WordEntity>

    @Query("SELECT * FROM words WHERE id = :id")
    fun getById(id: Long): WordEntity?

    @Query("SELECT * FROM words ORDER BY word ASC")
    fun getAll(): List<WordEntity>

    @Query("UPDATE words SET box = :box, mastered = :mastered, lastStudyAt = :lastStudyAt, correctCount = :correctCount, wrongCount = :wrongCount WHERE id = :id")
    fun updateAfterStudy(id: Long, box: Int, mastered: Boolean, lastStudyAt: Long, correctCount: Int, wrongCount: Int)

    @Query("SELECT COUNT(*) FROM words WHERE mastered = 1")
    fun masteredCount(): Int

    @Query("SELECT COUNT(*) FROM words WHERE mastered = 0 AND lastStudyAt > 0")
    fun reviewingCount(): Int
}
