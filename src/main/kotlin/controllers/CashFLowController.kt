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

    // Helper Error Response
    private suspend fun respondError(call: ApplicationCall, e: Exception) {
        when (e) {
            is ValidationException -> {
                call.respond(HttpStatusCode.BadRequest, DataResponse(
                    status = "fail",
                    message = e.message ?: "Data yang dikirimkan tidak valid!",
                    data = e.errors
                ))
            }
            is AppException -> {
                val status = HttpStatusCode.fromValue(e.code)
                call.respond(status, DataResponse(
                    status = "fail",
                    message = e.message,
                    data = null
                ))
            }
            else -> {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, DataResponse(
                    status = "error",
                    message = "Terjadi kesalahan server: ${e.message}",
                    data = null
                ))
            }
        }
    }

    suspend fun setupData(call: ApplicationCall) {
        try {
            val all = cashFlowService.getAllCashFlows(CashFlowQuery())
            all.forEach { cashFlowService.removeCashFlow(it.id) }

            val initData = loadInitialData()
            initData.forEach {
                cashFlowService.createRawCashFlow(it.id, it.type, it.source, it.label, it.amount, it.description, it.createdAt, it.updatedAt)
            }

            call.respond(DataResponse("success", "Berhasil memuat data awal", null))
        } catch (e: Exception) { respondError(call, e) }
    }

    suspend fun getAllCashFlows(call: ApplicationCall) {
        try {
            val p = call.request.queryParameters
            val query = CashFlowQuery(
                type = p["type"], source = p["source"], labels = p["labels"],
                gteAmount = p["gteAmount"]?.toDoubleOrNull(), lteAmount = p["lteAmount"]?.toDoubleOrNull(),
                search = p["search"], startDate = p["startDate"], endDate = p["endDate"]
            )

            val result = cashFlowService.getAllCashFlows(query)

            // Format response harus ada 'total'
            call.respond(DataResponse(
                "success",
                "Berhasil mengambil daftar catatan keuangan",
                mapOf("cashFlows" to result, "total" to result.size)
            ))
        } catch (e: Exception) { respondError(call, e) }
    }

    suspend fun getCashFlowById(call: ApplicationCall) {
        try {
            val id = call.parameters["id"] ?: ""
            val item = cashFlowService.getCashFlowById(id)
                ?: throw AppException(404, "Data catatan keuangan tidak tersedia!")

            call.respond(DataResponse("success", "Berhasil mengambil data catatan keuangan", mapOf("cashFlow" to item)))
        } catch (e: Exception) { respondError(call, e) }
    }

    suspend fun createCashFlow(call: ApplicationCall) {
        try {
            val req = try { call.receive<CashFlowRequest>() } catch(e: Exception) { throw AppException(400, "Invalid JSON") }

            // Validasi Manual
            val valData = mapOf(
                "type" to req.type, "source" to req.source, "label" to req.label,
                "amount" to req.amount, "description" to req.description
            )
            val v = ValidatorHelper(valData)
            v.required("type", "Tipe tidak boleh kosong")
            v.required("source", "Sumber tidak boleh kosong")
            v.required("label", "Label tidak boleh kosong")
            v.required("amount", "Jumlah tidak boleh kosong")
            v.min("amount", 0.0, "Jumlah harus lebih besar dari 0")
            v.required("description", "Deskripsi tidak boleh kosong")
            v.validate() // Throws ValidationException jika ada error

            val id = cashFlowService.createCashFlow(req.type!!, req.source!!, req.label!!, req.amount!!, req.description!!)
            call.respond(DataResponse("success", "Berhasil menambahkan data catatan keuangan", mapOf("cashFlowId" to id)))
        } catch (e: Exception) { respondError(call, e) }
    }

    suspend fun updateCashFlow(call: ApplicationCall) {
        try {
            val id = call.parameters["id"] ?: ""
            // Cek exists dulu (Requirement Test 404)
            if (cashFlowService.getCashFlowById(id) == null) throw AppException(404, "Data catatan keuangan tidak tersedia!")

            val req = call.receive<CashFlowRequest>()
            val valData = mapOf(
                "type" to req.type, "source" to req.source, "label" to req.label,
                "amount" to req.amount, "description" to req.description
            )
            val v = ValidatorHelper(valData)
            v.required("type", "Tipe tidak boleh kosong")
            v.required("source", "Sumber tidak boleh kosong")
            v.required("label", "Label tidak boleh kosong")
            v.required("amount", "Jumlah tidak boleh kosong")
            v.min("amount", 0.0, "Jumlah harus lebih besar dari 0")
            v.required("description", "Deskripsi tidak boleh kosong")
            v.validate()

            cashFlowService.updateCashFlow(id, req.type!!, req.source!!, req.label!!, req.amount!!, req.description!!)
            call.respond(DataResponse("success", "Berhasil mengubah data catatan keuangan", null))
        } catch (e: Exception) { respondError(call, e) }
    }

    suspend fun deleteCashFlow(call: ApplicationCall) {
        try {
            val id = call.parameters["id"] ?: ""
            if (!cashFlowService.removeCashFlow(id)) {
                throw AppException(404, "Data catatan keuangan tidak tersedia!")
            }
            call.respond(DataResponse("success", "Berhasil menghapus data catatan keuangan", null))
        } catch (e: Exception) { respondError(call, e) }
    }

    suspend fun getCashFlowTypes(call: ApplicationCall) {
        call.respond(DataResponse("success", "Berhasil mengambil daftar tipe catatan keuangan", mapOf("types" to cashFlowService.getAvailableTypes())))
    }
    suspend fun getCashFlowSources(call: ApplicationCall) {
        call.respond(DataResponse("success", "Berhasil mengambil daftar source catatan keuangan", mapOf("sources" to cashFlowService.getAvailableSources())))
    }
    suspend fun getCashFlowLabels(call: ApplicationCall) {
        call.respond(DataResponse("success", "Berhasil mengambil daftar label catatan keuangan", mapOf("labels" to cashFlowService.getAvailableLabels())))
    }
}