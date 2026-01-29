package com.example.paralleltimer.data.local

import com.example.paralleltimer.domain.model.TimerItem
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object TimerSerializer {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun serialize(timers: List<TimerItem>): String = json.encodeToString(timers)

    fun deserialize(jsonString: String): List<TimerItem> = try {
        json.decodeFromString(jsonString)
    } catch (e: Exception) {
        emptyList()
    }
}
