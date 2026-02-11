package org.delcom.data

import org.delcom.controllers.CashFlowController
import org.delcom.repositories.CashFlowRepository
import org.delcom.repositories.ICashFlowRepository
import org.delcom.services.CashFlowService
import org.delcom.services.ICashFlowService
import org.koin.dsl.module

val cashFlowModule = module {
    // 1. Daftarkan Repository
    single<ICashFlowRepository> { CashFlowRepository() }

    // 2. Daftarkan Service
    // PENTING: Controller Anda meminta 'CashFlowService', jadi pastikan ini terdaftar
    single { CashFlowService(get()) }

    // Jika Controller Anda nantinya diubah menggunakan interface, gunakan:
    // single<ICashFlowService> { CashFlowService(get()) }

    // 3. Daftarkan Controller
    single { CashFlowController(get()) }
}