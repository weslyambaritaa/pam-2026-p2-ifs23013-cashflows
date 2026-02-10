package org.delcom

import io.ktor.server.application.*
import io.ktor.server.routing.*
import org.delcom.controllers.CashFlowController
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    // Inject Controller dari Koin
    val controller by inject<CashFlowController>()

    routing {
        route("/cash-flows") {
            post("/setup") { controller.setupData(call) }

            get { controller.getAllCashFlows(call) }
            post { controller.createCashFlow(call) }

            get("/{id}") { controller.getCashFlowById(call) }
            put("/{id}") { controller.updateCashFlow(call) }
            delete("/{id}") { controller.deleteCashFlow(call) }

            // Extension
            get("/types") { controller.getCashFlowTypes(call) }
            get("/sources") { controller.getCashFlowSources(call) }
            get("/labels") { controller.getCashFlowLabels(call) }
        }
    }
}