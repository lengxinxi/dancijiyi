package com.example.dancijiyi.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 单词实体
 */
@Entity(tableName = "words")
data class WordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val word: String,
    val phonetic: String,
    val meaning: String,
    val example: String,
    val exampleTrans: String,
    /** 记忆盒等级 0~5，等级越高越熟练 */
    val box: Int = 0,
    /** 是否已掌握 */
    val mastered: Boolean = false,
    /** 上次学习时间（epoch millis，0 表示从未学过） */
    val lastStudyAt: Long = 0,
    val correctCount: Int = 0,
    val wrongCount: Int = 0
)
