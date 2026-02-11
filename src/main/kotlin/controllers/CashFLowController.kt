package org.delcom.controllers

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import kotlinx.serialization.json.* // Import wajib untuk JsonObject, jsonPrimitive, dll
import org.delcom.entities.CashFlow
import org.delcom.helpers.loadInitialData
import org.delcom.services.CashFlowQuery
import org.delcom.services.CashFlowService
import java.time.OffsetDateTime
import java.util.*

class CashFlowController(private val cashFlowService: CashFlowService) {

    suspend fun setupData(call: ApplicationCall) {
        try {
            val all = cashFlowService.getAllCashFlows(CashFlowQuery())
            all.forEach { cashFlowService.remove(it.id) }

            val initialData = try {
                loadInitialData()
            } catch (e: Throwable) {
                println("Warning: Gagal load data: ${e.message}")
                emptyList()
            }

            initialData.forEach {
                cashFlowService.createRawCashFlow(it.id, it.type, it.source, it.label, it.amount, it.createdAt, it.updatedAt, it.description)
            }

            // Gunakan <String?> agar serializer aman
            call.respond(HttpStatusCode.OK, DataResponse<String?>(
                status = "success",
                message = "Berhasil memuat data awal",
                data = null
            ))
        } catch (e: Throwable) {
            e.printStackTrace()
            call.respond(HttpStatusCode.InternalServerError, DataResponse<String?>(
                status = "error",
                message = "Setup gagal: ${e.message}",
                data = null
            ))
        }
    }

    suspend fun getAll(call: ApplicationCall) {
        val p = call.request.queryParameters
        val query = CashFlowQuery(
            type = p["type"], source = p["source"], labels = p["labels"],
            gteAmount = p["gteAmount"]?.toDoubleOrNull(), lteAmount = p["lteAmount"]?.toDoubleOrNull(),
            search = p["search"], startDate = p["startDate"], endDate = p["endDate"]
        )
        val list = cashFlowService.getAllCashFlows(query)

        // Menggunakan buildJsonObject agar tipe datanya jelas (tidak 'Any')
        val responseData = buildJsonObject {
            put("cashFlows", Json.encodeToJsonElement(list))
            put("total", list.size)
        }

        call.respond(HttpStatusCode.OK, DataResponse<JsonObject>(
            status = "success",
            message = "Berhasil mengambil daftar catatan keuangan",
            data = responseData
        ))
    }

    suspend fun create(call: ApplicationCall) {
        // PERBAIKAN: Gunakan JsonObject, bukan Map<String, Any?>
        val req = call.receiveNullable<JsonObject>() ?: JsonObject(emptyMap())
        val errors = mutableMapOf<String, String>()

        var amount = 0.0

        // Logika Strict: Jika field ada tapi string kosong -> force error 500 (sesuai tes)
        if (req.containsKey("amount")) {
            val primitive = req["amount"]?.jsonPrimitive
            val content = primitive?.content ?: ""
            // Jika content "", toDouble() akan throw NumberFormatException -> ditangkap StatusPages -> 500
            amount = content.toDouble()
        }

        // Validasi Field Required
        val fields = listOf("type", "source", "label", "amount", "description")
        fields.forEach { field ->
            val primitive = req[field]?.jsonPrimitive
            // Cek null atau string kosong/blank
            val isBlank = primitive == null || (primitive.isString && primitive.content.isBlank())

            if (!req.containsKey(field) || isBlank) {
                errors[field] = "Is required"
            }
        }

        // Validasi amount <= 0
        if (req.containsKey("amount") && amount <= 0.0 && !errors.containsKey("amount")) {
            errors["amount"] = "Must be > 0"
        }

        if (errors.isNotEmpty()) {
            return call.respond(HttpStatusCode.BadRequest, DataResponse(
                status = "fail",
                message = "Data yang dikirimkan tidak valid!",
                data = errors
            ))
        }

        val id = UUID.randomUUID().toString()
        val now = OffsetDateTime.now().toString()

        // Ekstrak data dari JsonObject
        val cf = CashFlow(
            id = id,
            type = req["type"]!!.jsonPrimitive.content,
            source = req["source"]!!.jsonPrimitive.content,
            label = req["label"]!!.jsonPrimitive.content,
            amount = amount,
            description = req["description"]!!.jsonPrimitive.content,
            createdAt = now,
            updatedAt = now
        )
        cashFlowService.create(cf)

        call.respond(HttpStatusCode.OK, DataResponse(
            status = "success",
            message = "Berhasil menambahkan data catatan keuangan",
            data = mapOf("cashFlowId" to id)
        ))
    }

    suspend fun getById(call: ApplicationCall) {
        val id = call.parameters["id"] ?: ""
        val cf = cashFlowService.findById(id) ?: return call.respond(HttpStatusCode.NotFound,
            DataResponse<String?>("fail", "Data catatan keuangan tidak tersedia!", null))

        call.respond(HttpStatusCode.OK, DataResponse(
            status = "success",
            message = "Berhasil mengambil data catatan keuangan",
            data = mapOf("cashFlow" to cf)
        ))
    }

    suspend fun update(call: ApplicationCall) {
        val id = call.parameters["id"] ?: ""
        val existing = cashFlowService.findById(id) ?: return call.respond(HttpStatusCode.NotFound,
            DataResponse<String?>("fail", "Data catatan keuangan tidak tersedia!", null))

        // PERBAIKAN: Gunakan JsonObject untuk Update juga
        val req = call.receiveNullable<JsonObject>() ?: JsonObject(emptyMap())
        val errors = mutableMapOf<String, String>()

        var amount = 0.0
        if (req.containsKey("amount")) {
            val primitive = req["amount"]?.jsonPrimitive
            val content = primitive?.content ?: ""
            amount = content.toDouble()
        }

        val fields = listOf("type", "source", "label", "amount", "description")
        fields.forEach { field ->
            val primitive = req[field]?.jsonPrimitive
            val isBlank = primitive == null || (primitive.isString && primitive.content.isBlank())

            if (!req.containsKey(field) || isBlank) {
                errors[field] = "Is required"
            }
        }

        if (req.containsKey("amount") && amount <= 0.0 && !errors.containsKey("amount")) {
            errors["amount"] = "Must be > 0"
        }

        if (errors.isNotEmpty()) {
            return call.respond(HttpStatusCode.BadRequest, DataResponse(
                status = "fail",
                message = "Data yang dikirimkan tidak valid!",
                data = errors
            ))
        }

        val updated = existing.copy(
            type = req["type"]!!.jsonPrimitive.content,
            source = req["source"]!!.jsonPrimitive.content,
            label = req["label"]!!.jsonPrimitive.content,
            amount = amount,
            description = req["description"]!!.jsonPrimitive.content,
            updatedAt = OffsetDateTime.now().toString()
        )
        cashFlowService.update(id, updated)

        call.respond(HttpStatusCode.OK, DataResponse<String?>("success", "Berhasil mengubah data catatan keuangan", null))
    }

    suspend fun delete(call: ApplicationCall) {
        val id = call.parameters["id"] ?: ""
        if (!cashFlowService.remove(id)) return call.respond(HttpStatusCode.NotFound,
            DataResponse<String?>("fail", "Data catatan keuangan tidak tersedia!", null))

        call.respond(HttpStatusCode.OK, DataResponse<String?>("success", "Berhasil menghapus data catatan keuangan", null))
    }

    suspend fun getTypes(call: ApplicationCall) = call.respond(HttpStatusCode.OK,
        DataResponse("success", "Berhasil mengambil daftar tipe catatan keuangan", mapOf("types" to cashFlowService.getDistinctTypes())))

    suspend fun getSources(call: ApplicationCall) = call.respond(HttpStatusCode.OK,
        DataResponse("success", "Berhasil mengambil daftar source catatan keuangan", mapOf("sources" to cashFlowService.getDistinctSources())))

    suspend fun getLabels(call: ApplicationCall) = call.respond(HttpStatusCode.OK,
        DataResponse("success", "Berhasil mengambil daftar label catatan keuangan", mapOf("labels" to cashFlowService.getDistinctLabels())))
}