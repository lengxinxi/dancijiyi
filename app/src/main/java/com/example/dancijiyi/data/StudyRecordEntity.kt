package com.example.dancijiyi.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 单次学习记录
 */
@Entity(tableName = "study_records")
data class StudyRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** 关联的单词 id */
    val wordId: Long,
    /** 学习结果：0=模糊 1=认识 2=不认识 */
    val result: Int,
    /** 学习时间（epoch millis） */
    val studiedAt: Long,
    /** 学习日期，格式 yyyy-MM-dd，便于按天统计 */
    val date: String
)
