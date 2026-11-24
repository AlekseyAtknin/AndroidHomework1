package com.example.sr.Repository

import kotlinx.coroutines.delay

class WorkRepository {
    suspend fun doWork(): String {
        delay(5_000)
        return "Работа завершена"
    }
}