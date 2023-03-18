package com.startupsurveys.model

import kotlin.math.ceil

data class Survey(
    val appName: String,
    val numQuestions: Int = (ceil(Math.random() * 2) + 4).toInt(),
    val reward: Float = 0.1f
)
