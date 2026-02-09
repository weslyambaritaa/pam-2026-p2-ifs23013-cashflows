package org.delcom.controllers

import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import org.delcom.data.*
import org.delcom.helpers.ValidatorHelper
import org.delcom.helpers.loadInitialData
import org.delcom.services.ICashFlowService

class CashFlowController(private val cashFlowService: ICashFlowService) {

    // POST /cash-flows/setup
    suspend fun setupData(call: ApplicationCall) {
        // 1. Ambil semua data lama untuk dihapus (pembersihan state)
        val query = CashFlowQuery()
        val existingCashFlows = cashFlowService.getAllCashFlows(query)
        for (item in existingCashFlows) {
            cashFlowService.removeCashFlow(item.id)
        }

        // 2. Muat data dari JSON menggunakan DummyHelper
        val initCashFlows = loadInitialData()

        // 3. Simpan ke service menggunakan createRawCashFlow
        for (item in initCashFlows) {
            cashFlowService.createRawCashFlow(
                item.id,
                item.type,
                item.source,
                item.label,
                item.amount,
                item.description,
                item.createdAt,
                item.updatedAt
            )
        }

        val response = DataResponse(
            "success",
            "Berhasil memuat data awal",
            null
        )
        call.respond(response)
    }

    // GET /cash-flows (dengan filter query)
    suspend fun getAllCashFlows(call: ApplicationCall) {
        val params = call.request.queryParameters

        // Mapping query parameters ke CashFlowQuery object
        val query = CashFlowQuery(
            type = params["type"],
            source = params["source"],
            labels = params["labels"],
            gteAmount = params["gteAmount"]?.toDoubleOrNull(),
            lteAmount = params["lteAmount"]?.toDoubleOrNull(),
            search = params["search"],
            startDate = params["startDate"],
            endDate = params["endDate"]
        )

        val cashFlows = cashFlowService.getAllCashFlows(query)

        val response = DataResponse(
            "success",
            "Berhasil mengambil daftar cash flow",
            mapOf("cashFlows" to cashFlows)
        )
        call.respond(response)
    }

    // GET /cash-flows/{id}
    suspend fun getCashFlowById(call: ApplicationCall) {
        val id = call.parameters["id"]
            ?: throw AppException(400, "ID cash flow tidak boleh kosong!")

        val cashFlow = cashFlowService.getCashFlowById(id)
            ?: throw AppException(404, "Data cash flow tidak tersedia!")

        val response = DataResponse(
            "success",
            "Berhasil mengambil data cash flow",
            mapOf("cashFlow" to cashFlow)
        )
        call.respond(response)
    }

    // POST /cash-flows
    suspend fun createCashFlow(call: ApplicationCall) {
        val request = call.receive<CashFlowRequest>()

        // Validasi input
        val validatorHelper = ValidatorHelper(mapOf(
            "type" to request.type,
            "source" to request.source,
            "amount" to request.amount
        ))
        validatorHelper.required("type", "Tipe tidak boleh kosong")
        validatorHelper.required("source", "Sumber tidak boleh kosong")
        validatorHelper.required("amount", "Jumlah tidak boleh kosong")
        validatorHelper.validate()

        val cashFlowId = cashFlowService.createCashFlow(
            request.type!!,
            request.source!!,
            request.label ?: "",
            request.amount!!,
            request.description ?: ""
        )

        val response = DataResponse(
            "success",
            "Berhasil menambahkan data cash flow",
            mapOf("cashFlowId" to cashFlowId)
        )
        call.respond(response)
    }

    // PUT /cash-flows/{id}
    suspend fun updateCashFlow(call: ApplicationCall) {
        val id = call.parameters["id"]
            ?: throw AppException(400, "ID cash flow tidak boleh kosong!")

        val request = call.receive<CashFlowRequest>()

        val isUpdated = cashFlowService.updateCashFlow(
            id,
            request.type!!,
            request.source!!,
            request.label ?: "",
            request.amount!!,
            request.description ?: ""
        )

        if (!isUpdated) {
            throw AppException(404, "Data cash flow tidak tersedia!")
        }

        val response = DataResponse(
            "success",
            "Berhasil mengubah data cash flow",
            null
        )
        call.respond(response)
    }

    // DELETE /cash-flows/{id}
    suspend fun deleteCashFlow(call: ApplicationCall) {
        val id = call.parameters["id"]
            ?: throw AppException(400, "ID cash flow tidak boleh kosong!")

        val isDeleted = cashFlowService.removeCashFlow(id)
        if (!isDeleted) {
            throw AppException(404, "Data cash flow tidak tersedia!")
        }

        val response = DataResponse(
            "success",
            "Berhasil menghapus data cash flow",
            null
        )
        call.respond(response)
    }

    // Extension Endpoints
    suspend fun getCashFlowTypes(call: ApplicationCall) {
        val types = cashFlowService.getAvailableTypes()
        call.respond(DataResponse("success", "Daftar tipe", mapOf("types" to types)))
    }

    suspend fun getCashFlowSources(call: ApplicationCall) {
        val sources = cashFlowService.getAvailableSources()
        call.respond(DataResponse("success", "Daftar sumber", mapOf("sources" to sources)))
    }

    suspend fun getCashFlowLabels(call: ApplicationCall) {
        val labels = cashFlowService.getAvailableLabels()
        call.respond(DataResponse("success", "Daftar label", mapOf("labels" to labels)))
    }
}