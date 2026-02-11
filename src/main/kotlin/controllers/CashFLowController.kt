package org.delcom.controllers

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import kotlinx.serialization.json.* import org.delcom.entities.CashFlow
import org.delcom.helpers.loadInitialData
import org.delcom.services.CashFlowService
import java.time.OffsetDateTime
import java.util.*
import org.delcom.data.DataResponse
import org.delcom.data.CashFlowQuery

class CashFlowController(private val cashFlowService: CashFlowService) {

    suspend fun setupData(call: ApplicationCall) {
        try {
            val all = cashFlowService.getAllCashFlows(CashFlowQuery())
            // Perbaikan: remove -> removeCashFlow
            all.forEach { cashFlowService.removeCashFlow(it.id) }

            val initialData = try {
                loadInitialData()
            } catch (e: Throwable) {
                println("Warning: Gagal load data: ${e.message}")
                emptyList()
            }

            initialData.forEach {
                cashFlowService.createRawCashFlow(it.id, it.type, it.source, it.label, it.amount, it.createdAt, it.updatedAt, it.description)
            }

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
        val req = call.receive<JsonObject>()
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

        // Perbaikan: create -> createCashFlow
        val id = cashFlowService.createCashFlow(
            type = req["type"]!!.jsonPrimitive.content,
            source = req["source"]!!.jsonPrimitive.content,
            label = req["label"]!!.jsonPrimitive.content,
            amount = amount,
            description = req["description"]!!.jsonPrimitive.content
        )

        call.respond(HttpStatusCode.OK, DataResponse(
            status = "success",
            message = "Berhasil menambahkan data catatan keuangan",
            data = mapOf("cashFlowId" to id)
        ))
    }

    suspend fun getById(call: ApplicationCall) {
        val id = call.parameters["id"] ?: ""
        // Perbaikan: findById -> getCashFlowById
        val cf = cashFlowService.getCashFlowById(id) ?: return call.respond(HttpStatusCode.NotFound,
            DataResponse<String?>("fail", "Data catatan keuangan tidak tersedia!", null))

        call.respond(HttpStatusCode.OK, DataResponse(
            status = "success",
            message = "Berhasil mengambil data catatan keuangan",
            data = mapOf("cashFlow" to cf)
        ))
    }

    suspend fun update(call: ApplicationCall) {
        val id = call.parameters["id"] ?: ""
        // Perbaikan: findById -> getCashFlowById
        val existing = cashFlowService.getCashFlowById(id) ?: return call.respond(HttpStatusCode.NotFound,
            DataResponse<String?>("fail", "Data catatan keuangan tidak tersedia!", null))

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

        // Perbaikan: update -> updateCashFlow
        cashFlowService.updateCashFlow(
            id = id,
            type = req["type"]!!.jsonPrimitive.content,
            source = req["source"]!!.jsonPrimitive.content,
            label = req["label"]!!.jsonPrimitive.content,
            amount = amount,
            description = req["description"]!!.jsonPrimitive.content
        )

        call.respond(HttpStatusCode.OK, DataResponse<String?>("success", "Berhasil mengubah data catatan keuangan", null))
    }

    suspend fun delete(call: ApplicationCall) {
        val id = call.parameters["id"] ?: ""
        // Perbaikan: remove -> removeCashFlow
        if (!cashFlowService.removeCashFlow(id)) return call.respond(HttpStatusCode.NotFound,
            DataResponse<String?>("fail", "Data catatan keuangan tidak tersedia!", null))

        call.respond(HttpStatusCode.OK, DataResponse<String?>("success", "Berhasil menghapus data catatan keuangan", null))
    }

    // Perbaikan: getDistinctTypes -> getAvailableTypes
    suspend fun getTypes(call: ApplicationCall) = call.respond(HttpStatusCode.OK,
        DataResponse("success", "Berhasil mengambil daftar tipe catatan keuangan", mapOf("types" to cashFlowService.getAvailableTypes())))

    // Perbaikan: getDistinctSources -> getAvailableSources
    suspend fun getSources(call: ApplicationCall) = call.respond(HttpStatusCode.OK,
        DataResponse("success", "Berhasil mengambil daftar source catatan keuangan", mapOf("sources" to cashFlowService.getAvailableSources())))

    // Perbaikan: getDistinctLabels -> getAvailableLabels
    suspend fun getLabels(call: ApplicationCall) = call.respond(HttpStatusCode.OK,
        DataResponse("success", "Berhasil mengambil daftar label catatan keuangan", mapOf("labels" to cashFlowService.getAvailableLabels())))
}