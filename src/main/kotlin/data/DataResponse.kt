package org.delcom.data

import kotlinx.serialization.Serializable

@Serializable // Baris ini wajib ada agar tidak muncul error "Serializer not found"
data class DataResponse<T>(
    val status: String,
    val message: String,
    val data: T? = null
)