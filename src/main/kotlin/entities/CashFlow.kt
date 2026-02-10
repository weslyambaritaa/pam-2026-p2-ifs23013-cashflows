package org.delcom.entities

import kotlinx.serialization.Serializable
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

@Serializable
data class CashFlow(
    val id: String,
    val type: String,
    val source: String,
    val label: String,
    val amount: Double,
    val description: String,
    val createdAt: Instant,
    val updatedAt: Instant
)