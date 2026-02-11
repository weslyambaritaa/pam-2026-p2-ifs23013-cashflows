package org.delcom.services

import org.delcom.entities.CashFlow
import org.delcom.data.CashFlowQuery

interface ICashFlowService {
    suspend fun getAllCashFlows(query: CashFlowQuery): List<CashFlow>
    suspend fun getCashFlowById(id: String): CashFlow?
    suspend fun createCashFlow(type: String, source: String, label: String, amount: Double, description: String): String
    suspend fun updateCashFlow(id: String, type: String, source: String, label: String, amount: Double, description: String): Boolean
    suspend fun removeCashFlow(id: String): Boolean

    // Fungsi tambahan untuk dropdown/filter
    suspend fun getAvailableTypes(): List<String>
    suspend fun getAvailableSources(): List<String>
    suspend fun getAvailableLabels(): List<String>

    // Fungsi untuk setup data massal
    suspend fun createRawCashFlow(
        id: String, type: String, source: String, label: String,
        amount: Double, createdAt: String, updatedAt: String, description: String
    )
}