package org.delcom.entities

import kotlinx.serialization.Serializable

@Serializable
data class CashFlow(
    val id: String,
    val type: String,
    val source: String,
    val label: String,
    val amount: Double,
    val description: String,
    val createdAt: String,
    val updatedAt: String
)