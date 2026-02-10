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
            post("/setup") { controller.setupData(call) }
            get { controller.getAllCashFlows(call) }
            post { controller.createCashFlow(call) }
            get("/{id}") { controller.getCashFlowById(call) }
            put("/{id}") { controller.updateCashFlow(call) }
            delete("/{id}") { controller.deleteCashFlow(call) }

            get("/types") { controller.getCashFlowTypes(call) }
            get("/sources") { controller.getCashFlowSources(call) }
            get("/labels") { controller.getCashFlowLabels(call) }
        }
    }
}