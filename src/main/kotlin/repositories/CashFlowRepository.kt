package org.delcom.repositories

import org.delcom.entities.CashFlow
import java.time.Instant

class CashFlowRepository : ICashFlowRepository {
    // List mutable sebagai database sementara
    private val db = mutableListOf<CashFlow>()

    override suspend fun getAll(): List<CashFlow> {
        return db.toList() // Return copy agar aman
    }

    override suspend fun getById(id: String): CashFlow? {
        return db.find { it.id == id }
    }

    override suspend fun add(cashFlow: CashFlow): Boolean {
        return db.add(cashFlow)
    }

    override suspend fun update(cashFlow: CashFlow): Boolean {
        val index = db.indexOfFirst { it.id == cashFlow.id }
        if (index != -1) {
            // Memperbarui data dengan entitas yang sudah diproses oleh service
            db[index] = cashFlow
            return true
        }
        return false
    }

    override suspend fun delete(id: String): Boolean {
        return db.removeIf { it.id == id }
    }
}