package com.example.paralleltimer.domain.model

import java.util.UUID

object DefaultGroups {
    val ALL = TimerGroup(
        id = "all",
        name = "all_timers",  // will use string resource
        icon = GroupIcon.DEFAULT
    )

    val COOKING = TimerGroup(
        id = "cooking",
        name = "cooking",
        icon = GroupIcon.COOKING
    )

    val EXERCISE = TimerGroup(
        id = "exercise",
        name = "exercise",
        icon = GroupIcon.EXERCISE
    )

    val STUDY = TimerGroup(
        id = "study",
        name = "study",
        icon = GroupIcon.STUDY
    )

    val WORK = TimerGroup(
        id = "work",
        name = "work",
        icon = GroupIcon.WORK
    )

    val BREAK = TimerGroup(
        id = "break",
        name = "break",
        icon = GroupIcon.BREAK
    )

    val defaults = listOf(COOKING, EXERCISE, STUDY, WORK, BREAK)
}
