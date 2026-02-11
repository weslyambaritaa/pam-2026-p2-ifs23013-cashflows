package org.delcom.controllers

import kotlinx.serialization.Serializable

/**
 * DataResponse adalah wrapper standar untuk semua respon API.
 * Menggunakan Generic <T> agar properti 'data' bisa berisi objek apa pun.
 */
@Serializable
data class DataResponse<T>(
    val status: String,
    val message: String,
    val data: T? = null
)