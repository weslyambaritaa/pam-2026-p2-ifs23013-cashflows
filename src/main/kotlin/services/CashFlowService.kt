package org.delcom.services

import org.delcom.entities.CashFlow
import org.delcom.repositories.ICashFlowRepository
import java.util.UUID
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class CashFlowService(private val repository: ICashFlowRepository) : ICashFlowService {
    private val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")

    override suspend fun getAllCashFlows(
        type: String?, source: String?, labels: String?,
        gteAmount: Double?, lteAmount: Double?, search: String?,
        startDate: String?, endDate: String?
    ): List<CashFlow> {
        var data = repository.getAll()

        type?.let { t -> data = data.filter { it.type.equals(t, ignoreCase = true) } }
        source?.let { s -> data = data.filter { it.source.equals(s, ignoreCase = true) } }
        labels?.let { l ->
            val queryLabels = l.split(",").map { it.trim().lowercase() }
            data = data.filter { item ->
                val itemLabels = item.label.split(",").map { it.trim().lowercase() }
                itemLabels.any { it in queryLabels }
            }
        }
        gteAmount?.let { min -> data = data.filter { it.amount >= min } }
        lteAmount?.let { max -> data = data.filter { it.amount <= max } }
        search?.let { term -> data = data.filter { it.description.contains(term, ignoreCase = true) } }

        startDate?.let { d ->
            val start = LocalDate.parse(d, formatter).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            data = data.filter { Instant.parse(it.createdAt).toEpochMilli() >= start }
        }
        endDate?.let { d ->
            val end = LocalDate.parse(d, formatter).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            data = data.filter { Instant.parse(it.createdAt).toEpochMilli() <= end }
        }
        return data
    }

    override suspend fun getCashFlowById(id: String): CashFlow? = repository.getById(id)

    override suspend fun createCashFlow(type: String, source: String, label: String, amount: Double, description: String): String {
        val now = Instant.now().toString()
        val cashFlow = CashFlow(UUID.randomUUID().toString(), type, source, label, amount, description, now, now)
        repository.add(cashFlow)
        return cashFlow.id
    }

    override suspend fun createRawCashFlow(id: String, type: String, source: String, label: String, amount: Double, description: String, createdAt: String, updatedAt: String) {
        repository.add(CashFlow(id, type, source, label, amount, description, createdAt, updatedAt))
    }

    override suspend fun updateCashFlow(id: String, type: String, source: String, label: String, amount: Double, description: String): Boolean {
        val existing = repository.getById(id) ?: return false
        val updated = existing.copy(type = type, source = source, label = label, amount = amount, description = description, updatedAt = Instant.now().toString())
        return repository.update(updated)
    }

    override suspend fun removeCashFlow(id: String): Boolean = repository.delete(id)

    override suspend fun getAvailableTypes() = repository.getAll().map { it.type }.distinct()
    override suspend fun getAvailableSources() = repository.getAll().map { it.source }.distinct()
    override suspend fun getAvailableLabels() = repository.getAll().flatMap { it.label.split(",") }.map { it.trim() }.filter { it.isNotEmpty() }.distinct()
}