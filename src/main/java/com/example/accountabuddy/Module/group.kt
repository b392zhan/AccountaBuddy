package com.example.accountabuddy.Module

data class Group(
    val name: String,
    val members: MutableMap<String, Member> = mutableMapOf()
)

data class Member(
    val name: String,
    val activities: MutableList<LogEntry> = mutableListOf(),
    var streak: Int = 0
)

data class LogEntry(
    val activity: String,
    val duration: Int,
    val timestamp: Long = System.currentTimeMillis()
)