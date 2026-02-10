package org.delcom.services

import kotlinx.datetime.*
import org.delcom.data.CashFlowQuery
import org.delcom.entities.CashFlow
import org.delcom.repositories.ICashFlowRepository
import java.util.UUID

class CashFlowService(private val repository: ICashFlowRepository) : ICashFlowService {

    override suspend fun getAllCashFlows(query: CashFlowQuery): List<CashFlow> {
        var data = repository.getAll()

        // 1. Filter Type (Exact match)
        query.type?.let { type ->
            data = data.filter { it.type.equals(type, ignoreCase = true) }
        }

        // 2. Filter Source (Exact match)
        query.source?.let { source ->
            data = data.filter { it.source.equals(source, ignoreCase = true) }
        }

        // 3. Filter Labels (Intersection / Contains logic)
        query.labels?.let { labels ->
            val queryLabels = labels.split(",").map { it.trim().lowercase() }
            data = data.filter { item ->
                val itemLabels = item.label.split(",").map { it.trim().lowercase() }
                // Item lolos jika memiliki setidaknya satu label yang diminta
                itemLabels.any { it in queryLabels }
            }
        }

        // 4. Filter Amount (GTE)
        query.gteAmount?.let { min ->
            data = data.filter { it.amount >= min }
        }

        // 5. Filter Amount (LTE)
        query.lteAmount?.let { max ->
            data = data.filter { it.amount <= max }
        }

        // 6. Search Description (Partial)
        query.search?.let { term ->
            data = data.filter { it.description.contains(term, ignoreCase = true) }
        }

        // 7. Date Logic (Format dd-MM-yyyy)
        val timeZone = TimeZone.currentSystemDefault()

        query.startDate?.let { dateStr ->
            parseDateToInstant(dateStr, timeZone, startOfDay = true)?.let { startInstant ->
                data = data.filter { it.createdAt >= startInstant }
            }
        }

        query.endDate?.let { dateStr ->
            parseDateToInstant(dateStr, timeZone, startOfDay = false)?.let { endInstant ->
                data = data.filter { it.createdAt <= endInstant }
            }
        }

        return data
    }

    // Helper Parsing Tanggal dd-MM-yyyy
    private fun parseDateToInstant(dateStr: String, zone: TimeZone, startOfDay: Boolean): Instant? {
        return try {
            val parts = dateStr.split("-")
            if (parts.size != 3) return null
            val day = parts[0].toInt()
            val month = parts[1].toInt()
            val year = parts[2].toInt()

            val localDate = LocalDate(year, month, day)
            if (startOfDay) {
                localDate.atStartOfDayIn(zone)
            } else {
                // Akhir hari (awal hari besoknya)
                localDate.plus(1, DateTimeUnit.DAY).atStartOfDayIn(zone)
            }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getCashFlowById(id: String): CashFlow? = repository.getById(id)

    override suspend fun createCashFlow(type: String, source: String, label: String, amount: Double, description: String): String {
        val now = Clock.System.now()
        val newCashFlow = CashFlow(
            id = UUID.randomUUID().toString(),
            type = type, source = source, label = label, amount = amount, description = description,
            createdAt = now, updatedAt = now
        )
        repository.add(newCashFlow)
        return newCashFlow.id
    }

    override suspend fun createRawCashFlow(id: String, type: String, source: String, label: String, amount: Double, description: String, createdAt: Instant, updatedAt: Instant) {
        val cashFlow = CashFlow(id, type, source, label, amount, description, createdAt, updatedAt)
        repository.add(cashFlow)
    }

    override suspend fun updateCashFlow(id: String, type: String, source: String, label: String, amount: Double, description: String): Boolean {
        val existing = repository.getById(id) ?: return false
        val updated = existing.copy(
            type = type, source = source, label = label, amount = amount, description = description,
            updatedAt = Clock.System.now()
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