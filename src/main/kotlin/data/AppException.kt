package org.delcom.data

import kotlinx.serialization.Serializable

// Exception umum (misal: 404 Data tidak ditemukan)
@Serializable
open class AppException(
    val code: Int,
    override val message: String
) : RuntimeException(message)

// Exception khusus validasi (400 dengan detail field error)
// Catatan: Map tidak selalu bisa diserialisasi langsung secara otomatis tanpa plugin,
// namun struktur ini digunakan oleh ValidatorHelper Anda
class ValidationException(
    val errors: Map<String, String>
) : AppException(400, "Data yang dikirimkan tidak valid!")