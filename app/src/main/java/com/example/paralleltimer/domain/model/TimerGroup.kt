package com.example.paralleltimer.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class TimerGroup(
    val id: String,
    val name: String,
    val icon: GroupIcon = GroupIcon.DEFAULT,
    val createdAtEpochMs: Long = System.currentTimeMillis()
)

@Serializable
enum class GroupIcon {
    DEFAULT,      // 기본
    COOKING,      // 요리
    EXERCISE,     // 운동
    STUDY,        // 공부
    WORK,         // 업무
    BREAK,        // 휴식
    CUSTOM        // 사용자 정의
}
