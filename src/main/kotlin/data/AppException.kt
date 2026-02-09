package org.delcom.data

import kotlinx.serialization.Serializable

@Serializable
data class CashFlowRequest(
    val type: String? = null,
    val source: String? = null,
    val label: String? = null,
    val amount: Double? = null,
    val description: String? = null
)