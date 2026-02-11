package org.delcom.data

import kotlinx.serialization.Serializable

@Serializable
data class CashFlowQuery(
    val type: String? = null,
    val source: String? = null,
    val labels: String? = null,
    val gteAmount: Double? = null,
    val lteAmount: Double? = null,
    val search: String? = null,
    val startDate: String? = null,
    val endDate: String? = null
)