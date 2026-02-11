package org.delcom.services

import org.delcom.entities.CashFlow
import org.delcom.repositories.ICashFlowRepository
import org.delcom.data.CashFlowQuery
import java.util.UUID
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class CashFlowService(private val repository: ICashFlowRepository) : ICashFlowService {
    private val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")

    override suspend fun getAllCashFlows(query: CashFlowQuery): List<CashFlow> {
        var data: List<CashFlow> = repository.getAll()

        query.type?.let { t -> data = data.filter { it.type.equals(t, ignoreCase = true) } }
        query.source?.let { s -> data = data.filter { it.source.equals(s, ignoreCase = true) } }

        query.labels?.takeIf { it.isNotBlank() }?.let { l ->
            val queryLabels = l.split(",").map { it.trim().lowercase() }
            data = data.filter { item ->
                val itemLabels = item.label.split(",").map { it.trim().lowercase() }
                itemLabels.any { it in queryLabels }
            }
        }

        query.gteAmount?.let { min -> data = data.filter { it.amount >= min } }
        query.lteAmount?.let { max -> data = data.filter { it.amount <= max } }
        query.search?.takeIf { it.isNotBlank() }?.let { term ->
            data = data.filter { it.description.contains(term, ignoreCase = true) }
        }

        try {
            query.startDate?.let { d ->
                val start = LocalDate.parse(d, formatter).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                data = data.filter { Instant.parse(it.createdAt).toEpochMilli() >= start }
            }
            query.endDate?.let { d ->
                val end = LocalDate.parse(d, formatter).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                data = data.filter { Instant.parse(it.createdAt).toEpochMilli() <= end }
            }
        } catch (e: Exception) { }

        return data
    }

    override suspend fun getCashFlowById(id: String): CashFlow? = repository.getById(id)

    override suspend fun createCashFlow(type: String, source: String, label: String, amount: Double, description: String): String {
        val now = Instant.now().toString()
        val id = UUID.randomUUID().toString()
        repository.add(CashFlow(id, type, source, label, amount, description, now, now))
        return id
    }

    override suspend fun updateCashFlow(id: String, type: String, source: String, label: String, amount: Double, description: String): Boolean {
        // Perbaikan: Panggil fungsi dari repository langsung
        val existing = repository.getById(id) ?: return false
        val updated = existing.copy(
            type = type,
            source = source,
            label = label,
            amount = amount,
            description = description,
            updatedAt = Instant.now().toString()
        )
        return repository.update(updated)
    }

    override suspend fun removeCashFlow(id: String): Boolean = repository.delete(id)

    override suspend fun getAvailableTypes(): List<String> = repository.getAll().map { it.type }.distinct()

    override suspend fun getAvailableSources(): List<String> = repository.getAll().map { it.source }.distinct()

    override suspend fun getAvailableLabels(): List<String> = repository.getAll()
        .flatMap { it.label.split(",") }
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .distinct()

    override suspend fun createRawCashFlow(id: String, type: String, source: String, label: String, amount: Double, createdAt: String, updatedAt: String, description: String) {
        repository.add(CashFlow(id, type, source, label, amount, description, createdAt, updatedAt))
    }
}