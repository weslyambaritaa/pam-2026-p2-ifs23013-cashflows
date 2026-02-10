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
        type: String?,
        source: String?,
        labels: String?,
        gteAmount: Double?,
        lteAmount: Double?,
        search: String?,
        startDate: String?,
        endDate: String?
    ): List<CashFlow> {
        var data = repository.getAll()

        // 1. Filter Type (Exact match)
        type?.let { t ->
            data = data.filter { it.type.equals(t, ignoreCase = true) }
        }

        // 2. Filter Source (Exact match)
        source?.let { s ->
            data = data.filter { it.source.equals(s, ignoreCase = true) }
        }

        // 3. Filter Labels (Intersection logic)
        labels?.let { l ->
            val queryLabels = l.split(",").map { it.trim().lowercase() }
            data = data.filter { item ->
                val itemLabels = item.label.split(",").map { it.trim().lowercase() }
                itemLabels.any { it in queryLabels }
            }
        }

        // 4. Filter Amount (GTE)
        gteAmount?.let { min ->
            data = data.filter { it.amount >= min }
        }

        // 5. Filter Amount (LTE)
        lteAmount?.let { max ->
            data = data.filter { it.amount <= max }
        }

        // 6. Search Description (Partial match)
        search?.let { term ->
            data = data.filter { it.description.contains(term, ignoreCase = true) }
        }

        // 7. Filter Tanggal (Menggunakan java.time karena format String ISO-8601)
        startDate?.let { dateStr ->
            val startMilli = parseDateToLong(dateStr, isStartOfDay = true)
            data = data.filter { Instant.parse(it.createdAt).toEpochMilli() >= startMilli }
        }

        endDate?.let { dateStr ->
            val endMilli = parseDateToLong(dateStr, isStartOfDay = false)
            data = data.filter { Instant.parse(it.createdAt).toEpochMilli() <= endMilli }
        }

        return data
    }

    private fun parseDateToLong(dateStr: String, isStartOfDay: Boolean): Long {
        return try {
            val localDate = LocalDate.parse(dateStr, formatter)
            if (isStartOfDay) {
                localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            } else {
                localDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            }
        } catch (e: Exception) {
            if (isStartOfDay) 0L else Long.MAX_VALUE
        }
    }

    override suspend fun getCashFlowById(id: String): CashFlow? = repository.getById(id)

    override suspend fun createCashFlow(type: String, source: String, label: String, amount: Double, description: String): String {
        val now = Instant.now().toString()
        val newCashFlow = CashFlow(
            id = UUID.randomUUID().toString(),
            type = type, source = source, label = label, amount = amount, description = description,
            createdAt = now, updatedAt = now
        )
        repository.add(newCashFlow)
        return newCashFlow.id
    }

    override suspend fun createRawCashFlow(id: String, type: String, source: String, label: String, amount: Double, description: String, createdAt: String, updatedAt: String) {
        val cashFlow = CashFlow(id, type, source, label, amount, description, createdAt, updatedAt)
        repository.add(cashFlow)
    }

    override suspend fun updateCashFlow(id: String, type: String, source: String, label: String, amount: Double, description: String): Boolean {
        val existing = repository.getById(id) ?: return false
        val updated = existing.copy(
            type = type, source = source, label = label, amount = amount, description = description,
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
}