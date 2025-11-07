package com.example.accountabuddy.Module

data class Activity(
    val userName: String,
    val activity: String,
    val duration: Int,
    val timestamp: Long = System.currentTimeMillis()
)
