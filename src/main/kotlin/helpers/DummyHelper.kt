package org.delcom.helpers

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.delcom.entities.CashFlow
import java.io.File

@Serializable
data class CashFlowsContainer(
    val cashFlows: List<CashFlow>
)

fun loadInitialData(): List<CashFlow> {
    return try {
        // Cek file di root project (priority 1)
        val jsonFile = File("data-awal.json")

        // Cek resource stream (priority 2 - untuk production jar)
        val resourceStream = object {}.javaClass.classLoader.getResourceAsStream("data-awal.json")

        val jsonText = when {
            jsonFile.exists() -> jsonFile.readText()
            resourceStream != null -> resourceStream.bufferedReader().use { it.readText() }
            else -> {
                println("Warning: File data-awal.json tidak ditemukan.")
                return emptyList()
            }
        }

        Json.decodeFromString<CashFlowsContainer>(jsonText).cashFlows
    } catch (e: Exception) {
        println("Error loading JSON data: ${e.message}")
        emptyList()
    }
}