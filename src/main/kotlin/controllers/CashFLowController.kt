package org.delcom.controllers

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import org.delcom.data.*
import org.delcom.helpers.ValidatorHelper
import org.delcom.helpers.loadInitialData
import org.delcom.services.ICashFlowService

class CashFlowController(private val cashFlowService: ICashFlowService) {

    private suspend fun respondError(call: ApplicationCall, e: Exception) {
        when (e) {
            is ValidationException -> call.respond(HttpStatusCode.BadRequest, DataResponse("fail", e.message!!, e.errors))
            is AppException -> call.respond(HttpStatusCode.fromValue(e.code), DataResponse<Any>("fail", e.message!!))
            else -> call.respond(HttpStatusCode.InternalServerError, DataResponse<Any>("error", "Internal Error: ${e.message}"))
        }
    }

    suspend fun setupData(call: ApplicationCall) {
        try {
            cashFlowService.getAllCashFlows().forEach { cashFlowService.removeCashFlow(it.id) }
            loadInitialData().forEach {
                cashFlowService.createRawCashFlow(it.id, it.type, it.source, it.label, it.amount, it.description, it.createdAt, it.updatedAt)
            }
            call.respond(DataResponse<Any>("success", "Berhasil memuat data awal"))
        } catch (e: Exception) { respondError(call, e) }
    }

    suspend fun getAllCashFlows(call: ApplicationCall) {
        try {
            val p = call.request.queryParameters
            val result = cashFlowService.getAllCashFlows(p["type"], p["source"], p["labels"], p["gteAmount"]?.toDoubleOrNull(), p["lteAmount"]?.toDoubleOrNull(), p["search"], p["startDate"], p["endDate"])
            call.respond(DataResponse("success", "Berhasil mengambil daftar catatan keuangan", mapOf("cashFlows" to result, "total" to result.size)))
        } catch (e: Exception) { respondError(call, e) }
    }

    suspend fun createCashFlow(call: ApplicationCall) {
        try {
            val req = call.receive<CashFlowRequest>()
            val id = cashFlowService.createCashFlow(req.type!!, req.source!!, req.label!!, req.amount!!, req.description!!)
            call.respond(DataResponse("success", "Berhasil menambahkan data catatan keuangan", mapOf("cashFlowId" to id)))
        } catch (e: Exception) { respondError(call, e) }
    }

    suspend fun getCashFlowById(call: ApplicationCall) {
        try {
            val id = call.parameters["id"] ?: throw AppException(400, "ID kosong")
            val item = cashFlowService.getCashFlowById(id) ?: throw AppException(404, "Data catatan keuangan tidak tersedia!")
            call.respond(DataResponse("success", "Berhasil mengambil data catatan keuangan", mapOf("cashFlow" to item)))
        } catch (e: Exception) { respondError(call, e) }
    }

    suspend fun updateCashFlow(call: ApplicationCall) {
        try {
            val id = call.parameters["id"] ?: throw AppException(400, "ID kosong")
            val req = call.receive<CashFlowRequest>()
            cashFlowService.updateCashFlow(id, req.type!!, req.source!!, req.label!!, req.amount!!, req.description!!)
            call.respond(DataResponse<Any>("success", "Berhasil mengubah data catatan keuangan"))
        } catch (e: Exception) { respondError(call, e) }
    }

    suspend fun deleteCashFlow(call: ApplicationCall) {
        try {
            val id = call.parameters["id"] ?: throw AppException(400, "ID kosong")
            if (!cashFlowService.removeCashFlow(id)) throw AppException(404, "Data catatan keuangan tidak tersedia!")
            call.respond(DataResponse<Any>("success", "Berhasil menghapus data catatan keuangan"))
        } catch (e: Exception) { respondError(call, e) }
    }

    suspend fun getCashFlowTypes(call: ApplicationCall) = call.respond(DataResponse("success", "Berhasil", mapOf("types" to cashFlowService.getAvailableTypes())))
    suspend fun getCashFlowSources(call: ApplicationCall) = call.respond(DataResponse("success", "Berhasil", mapOf("sources" to cashFlowService.getAvailableSources())))
    suspend fun getCashFlowLabels(call: ApplicationCall) = call.respond(DataResponse("success", "Berhasil", mapOf("labels" to cashFlowService.getAvailableLabels())))
}