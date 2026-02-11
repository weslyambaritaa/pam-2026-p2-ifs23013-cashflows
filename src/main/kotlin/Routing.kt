// src/main/kotlin/Routing.kt
package org.delcom

import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import org.delcom.controllers.CashFlowController
import org.delcom.data.DataResponse
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    val controller by inject<CashFlowController>()

    routing {
        get("/") {
            val response = DataResponse<String?>(
                status = "success",
                message = "Service Cash Flows by ifs23013 - Wesly Ambarita",
                data = null
            )
            call.respond(response)
        }

        route("/cash-flows") {
            post("/setup") { controller.setupData(call) } // Sesuai
            get { controller.getAll(call) }               // Ubah dari getAllCashFlows ke getAll
            post { controller.create(call) }             // Ubah dari createCashFlow ke create

            get("/types") { controller.getTypes(call) }     // Sesuai
            get("/sources") { controller.getSources(call) } // Sesuai
            get("/labels") { controller.getLabels(call) }   // Sesuai

            get("/{id}") { controller.getById(call) }       // Ubah dari getCashFlowById ke getById
            put("/{id}") { controller.update(call) }        // Ubah dari updateCashFlow ke update
            delete("/{id}") { controller.delete(call) }     // Ubah dari deleteCashFlow ke delete
        }
    }
}