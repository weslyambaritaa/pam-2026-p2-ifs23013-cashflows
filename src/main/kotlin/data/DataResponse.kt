package org.delcom.data

import kotlinx.serialization.Serializable

@Serializable
data class DataResponse<T>(
    val status: String,   // "success", "fail", "error"
    val message: String,
    val data: T? = null
)

@Serializable
data class CashFlowRequest(
    val type: String? = null,
    val source: String? = null,
    val label: String? = null,
    val amount: Double? = null,
    val description: String? = null
)

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