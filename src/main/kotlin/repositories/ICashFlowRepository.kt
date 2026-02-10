package org.delcom.repositories

import org.delcom.entities.CashFlow

interface ICashFlowRepository {
    // Mengambil semua data catatan keuangan
    suspend fun getAll(): List<CashFlow>

    // Mencari satu catatan keuangan berdasarkan ID
    suspend fun getById(id: String): CashFlow?

    // Menambahkan catatan keuangan baru ke dalam penyimpanan
    suspend fun add(cashFlow: CashFlow): Boolean

    // Memperbarui data catatan keuangan yang sudah ada
    suspend fun update(cashFlow: CashFlow): Boolean

    // Menghapus catatan keuangan berdasarkan ID
    suspend fun delete(id: String): Boolean
}