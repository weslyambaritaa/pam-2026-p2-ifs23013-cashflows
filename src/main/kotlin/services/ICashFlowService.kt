package org.delcom.services

import org.delcom.data.CashFlowQuery
import org.delcom.entities.CashFlow
import kotlinx.datetime.Instant

interface ICashFlowService {
    suspend fun getAllCashFlows(query: CashFlowQuery): List<CashFlow>
    suspend fun getCashFlowById(id: String): CashFlow?
    suspend fun createCashFlow(type: String, source: String, label: String, amount: Double, description: String): String
    suspend fun createRawCashFlow(id: String, type: String, source: String, label: String, amount: Double, description: String, createdAt: Instant, updatedAt: Instant)
    suspend fun updateCashFlow(id: String, type: String, source: String, label: String, amount: Double, description: String): Boolean
    suspend fun removeCashFlow(id: String): Boolean

    suspend fun getAvailableTypes(): List<String>
    suspend fun getAvailableSources(): List<String>
    suspend fun getAvailableLabels(): List<String>
}