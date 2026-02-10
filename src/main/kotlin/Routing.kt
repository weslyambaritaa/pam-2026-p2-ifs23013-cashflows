package org.delcom

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.routing.*
import io.ktor.server.response.*
import org.delcom.controllers.CashFlowController
import org.delcom.data.DataResponse
import org.delcom.data.AppException
import org.delcom.data.ValidationException
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    // Inject CashFlowController menggunakan Koin
    val controller by inject<CashFlowController>()

    // Konfigurasi StatusPages untuk menangani error secara global
    install(StatusPages) {
        // Menangkap error aplikasi umum (seperti 404)
        exception<AppException> { call, cause ->
            call.respond(
                status = HttpStatusCode.fromValue(cause.code),
                message = DataResponse<Any>(
                    status = "fail",
                    message = cause.message,
                    data = null
                )
            )
        }

        // Menangkap error validasi data (400)
        exception<ValidationException> { call, cause ->
            call.respond(
                status = HttpStatusCode.BadRequest,
                message = DataResponse(
                    status = "fail",
                    message = cause.message ?: "Data yang dikirimkan tidak valid!",
                    data = cause.errors
                )
            )
        }

        // Menangkap error server yang tidak terduga (500)
        exception<Throwable> { call, cause ->
            cause.printStackTrace()
            call.respond(
                status = HttpStatusCode.InternalServerError,
                message = DataResponse<Any>(
                    status = "error",
                    message = cause.message ?: "Terjadi kesalahan server internal",
                    data = null
                )
            )
        }
    }

    routing {
        // Endpoint Root identitas proyek
        get("/") {
            call.respondText("PAM 2026 - Cashflow API")
        }

        // Grup rute untuk manajemen Cash Flow
        route("/cash-flows") {
            // Mendapatkan semua data dengan filter individual
            get {
                controller.getAllCashFlows(call)
            }

            // Membuat catatan keuangan baru
            post {
                controller.createCashFlow(call)
            }

            // Operasi berdasarkan ID
            get("/{id}") {
                controller.getCashFlowById(call)
            }
            put("/{id}") {
                controller.updateCashFlow(call)
            }
            delete("/{id}") {
                controller.deleteCashFlow(call)
            }

            // Endpoint tambahan untuk mendapatkan kategori unik
            get("/types") {
                controller.getCashFlowTypes(call)
            }
            get("/sources") {
                controller.getCashFlowSources(call)
            }
            get("/labels") {
                controller.getCashFlowLabels(call)
            }
        }
    }
}