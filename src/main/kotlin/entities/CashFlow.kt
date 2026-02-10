package org.delcom.entities

import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class CashFlow(
    val id: String,
    val type: String,
    val source: String,
    val label: String,
    val amount: Double,
    val description: String,
    val createdAt: String, // Diubah ke String agar serialisasi JSON lebih aman tanpa konfigurasi tambahan
    val updatedAt: String
)