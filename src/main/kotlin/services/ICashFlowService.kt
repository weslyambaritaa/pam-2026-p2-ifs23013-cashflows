package org.delcom.services

import org.delcom.entities.CashFlow

interface ICashFlowService {
    // Parameter filter dikirim langsung tanpa CashFlowQuery
    suspend fun getAllCashFlows(
        type: String? = null,
        source: String? = null,
        labels: String? = null,
        gteAmount: Double? = null,
        lteAmount: Double? = null,
        search: String? = null,
        startDate: String? = null,
        endDate: String? = null
    ): List<CashFlow>

    suspend fun getCashFlowById(id: String): CashFlow?
    suspend fun createCashFlow(type: String, source: String, label: String, amount: Double, description: String): String
    suspend fun createRawCashFlow(id: String, type: String, source: String, label: String, amount: Double, description: String, createdAt: String, updatedAt: String)
    suspend fun updateCashFlow(id: String, type: String, source: String, label: String, amount: Double, description: String): Boolean
    suspend fun removeCashFlow(id: String): Boolean

    suspend fun getAvailableTypes(): List<String>
    suspend fun getAvailableSources(): List<String>
    suspend fun getAvailableLabels(): List<String>
}