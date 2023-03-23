package com.startupsurveys.model

import kotlin.math.ceil

data class Survey(
    val appName: String,
    val numQuestions: Int = 1, // (ceil(Math.random() * 2) + 4).toInt(),
    val reward: String = "0.1"
)
