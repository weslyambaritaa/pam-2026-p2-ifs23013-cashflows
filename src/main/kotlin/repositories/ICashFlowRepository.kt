package org.delcom.repositories

import org.delcom.entities.CashFlow

interface ICashFlowRepository {
    suspend fun getAll(): List<CashFlow>
    suspend fun getById(id: String): CashFlow?
    suspend fun add(cashFlow: CashFlow): Boolean
    suspend fun update(cashFlow: CashFlow): Boolean
    suspend fun delete(id: String): Boolean
}