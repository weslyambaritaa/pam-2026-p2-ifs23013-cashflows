package org.delcom.data

// Exception umum (misal: 404 Data tidak ditemukan)
open class AppException(
    val code: Int,
    override val message: String
) : RuntimeException(message)

// Exception khusus validasi (400 dengan detail field error)
class ValidationException(
    val errors: Map<String, String>
) : AppException(400, "Data yang dikirimkan tidak valid!")