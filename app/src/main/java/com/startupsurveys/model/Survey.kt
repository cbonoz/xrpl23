package com.startupsurveys.model

data class Survey(
    val appName: String,
    val numQuestions: Int = (Math.ceil(Math.random() * 2) + 4).toInt(),
    val reward: Float = 0.1f
)
