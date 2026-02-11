package org.delcom.helpers

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.delcom.entities.CashFlow
import java.io.File

@Serializable
data class CashFlowsContainer(
    val cashFlows: List<CashFlow>
)

val jsonConfig = Json {
    ignoreUnknownKeys = true
    isLenient = true
}

fun loadInitialData(): List<CashFlow> {
    val fileName = "data-awal.json"

    val strategies = listOf(
        {
            Thread.currentThread().contextClassLoader.getResourceAsStream(fileName)?.bufferedReader()?.use { it.readText() }
        },
        {
            val file = File("src/main/resources/$fileName")
            if (file.exists()) file.readText() else null
        },
        {
            val file = File(fileName)
            if (file.exists()) file.readText() else null
        }
    )

    for (strategy in strategies) {
        try {
            val jsonText = strategy()
            if (!jsonText.isNullOrBlank()) {
                println("Berhasil memuat '$fileName'")
                return jsonConfig.decodeFromString<CashFlowsContainer>(jsonText).cashFlows
            }
        } catch (e: Throwable) {
            println("Gagal strategi load: ${e.message}")
        }
    }

    println("!!! PERINGATAN: File '$fileName' tidak ditemukan di lokasi manapun. Menggunakan data kosong.")
    return emptyList()
}