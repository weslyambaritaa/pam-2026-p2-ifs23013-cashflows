package org.delcom.data

import org.delcom.controllers.CashFlowController
import org.delcom.repositories.ICashFlowRepository
import org.delcom.repositories.CashFlowRepository
import org.delcom.services.ICashFlowService
import org.delcom.services.CashFlowService
import org.koin.dsl.module

val cashFlowModule = module {
    // Repository
    // Menyediakan implementasi untuk pengelolaan data CashFlow
    single<ICashFlowRepository> {
        CashFlowRepository()
    }

    // Service
    // Menyediakan logika bisnis dan menghubungkannya dengan repository
    single<ICashFlowService> {
        CashFlowService(get())
    }

    // Controller
    // Menyediakan endpoint API yang akan digunakan di Routing
    single {
        CashFlowController(get())
    }
}