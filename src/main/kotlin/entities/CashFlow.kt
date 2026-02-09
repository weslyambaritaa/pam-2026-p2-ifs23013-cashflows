package org.delcom.entities

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

@Serializable
data class CashFlow(
    val id: String = UUID.randomUUID().toString(),
    var type: String,        // "Pemasukan" atau "Pengeluaran"
    var source: String,      // "Tabungan", "Tunai", dll
    var label: String,       // "kebutuhan,elektronik", dll
    var amount: Double,
    var description: String,

    @Contextual
    val createdAt: Instant = Clock.System.now(),
    @Contextual
    var updatedAt: Instant = Clock.System.now()
)