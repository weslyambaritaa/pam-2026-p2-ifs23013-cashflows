package org.delcom.services

import org.delcom.entities.CashFlow
import org.delcom.data.CashFlowQuery

interface ICashFlowService {
    // Parameter filter diganti menjadi objek query tunggal
    suspend fun getAllCashFlows(query: CashFlowQuery = CashFlowQuery()): List<CashFlow>

    suspend fun getCashFlowById(id: String): CashFlow?
    suspend fun createCashFlow(type: String, source: String, label: String, amount: Double, description: String): String
    suspend fun createRawCashFlow(id: String, type: String, source: String, label: String, amount: Double, description: String, createdAt: String, updatedAt: String)
    suspend fun updateCashFlow(id: String, type: String, source: String, label: String, amount: Double, description: String): Boolean
    suspend fun removeCashFlow(id: String): Boolean
    suspend fun getAvailableTypes(): List<String>
    suspend fun getAvailableSources(): List<String>
    suspend fun getAvailableLabels(): List<String>
}