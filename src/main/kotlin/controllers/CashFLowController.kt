package org.delcom.controllers

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import org.delcom.data.*
import org.delcom.helpers.ValidatorHelper
import org.delcom.services.ICashFlowService

class CashFlowController(private val cashFlowService: ICashFlowService) {

    // Helper untuk menangani pengiriman error response
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

    suspend fun getAllCashFlows(call: ApplicationCall) {
        try {
            val p = call.request.queryParameters

            // Memanggil service dengan parameter individual sesuai permintaan tanpa CashFlowQuery
            val result = cashFlowService.getAllCashFlows(
                type = p["type"],
                source = p["source"],
                labels = p["labels"],
                gteAmount = p["gteAmount"]?.toDoubleOrNull(),
                lteAmount = p["lteAmount"]?.toDoubleOrNull(),
                search = p["search"],
                startDate = p["startDate"],
                endDate = p["endDate"]
            )

            call.respond(DataResponse(
                "success",
                "Berhasil mengambil daftar catatan keuangan",
                mapOf("cashFlows" to result, "total" to result.size)
            ))
        } catch (e: Exception) { respondError(call, e) }
    }

    suspend fun getCashFlowById(call: ApplicationCall) {
        try {
            val id = call.parameters["id"]
                ?: throw AppException(400, "ID catatan keuangan tidak boleh kosong!")

            val item = cashFlowService.getCashFlowById(id)
                ?: throw AppException(404, "Data catatan keuangan tidak tersedia!")

            call.respond(DataResponse(
                "success",
                "Berhasil mengambil data catatan keuangan",
                mapOf("cashFlow" to item)
            ))
        } catch (e: Exception) { respondError(call, e) }
    }

    suspend fun createCashFlow(call: ApplicationCall) {
        try {
            val req = try { call.receive<CashFlowRequest>() } catch(e: Exception) { throw AppException(400, "Invalid JSON") }

            // Validasi input
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

            val id = cashFlowService.createCashFlow(req.type!!, req.source!!, req.label!!, req.amount!!, req.description!!)

            call.respond(DataResponse(
                "success",
                "Berhasil menambahkan data catatan keuangan",
                mapOf("cashFlowId" to id)
            ))
        } catch (e: Exception) { respondError(call, e) }
    }

    suspend fun updateCashFlow(call: ApplicationCall) {
        try {
            val id = call.parameters["id"]
                ?: throw AppException(400, "ID catatan keuangan tidak boleh kosong!")

            val req = call.receive<CashFlowRequest>()

            // Validasi input
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

            val isUpdated = cashFlowService.updateCashFlow(id, req.type!!, req.source!!, req.label!!, req.amount!!, req.description!!)
            if (!isUpdated) {
                throw AppException(404, "Data catatan keuangan tidak tersedia!")
            }

            call.respond(DataResponse("success", "Berhasil mengubah data catatan keuangan", null))
        } catch (e: Exception) { respondError(call, e) }
    }

    suspend fun deleteCashFlow(call: ApplicationCall) {
        try {
            val id = call.parameters["id"]
                ?: throw AppException(400, "ID catatan keuangan tidak boleh kosong!")

            if (!cashFlowService.removeCashFlow(id)) {
                throw AppException(404, "Data catatan keuangan tidak tersedia!")
            }

            call.respond(DataResponse("success", "Berhasil menghapus data catatan keuangan", null))
        } catch (e: Exception) { respondError(call, e) }
    }
}