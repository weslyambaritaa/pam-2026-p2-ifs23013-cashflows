package org.delcom.helpers

import org.delcom.data.ValidationException

class ValidatorHelper(
    private val body: Any?
) {
    private val errors = mutableMapOf<String, String>()

    // Helper untuk akses properti map secara aman
    private val data: Map<String, Any?> = if (body is Map<*, *>) {
        @Suppress("UNCHECKED_CAST")
        body as Map<String, Any?>
    } else {
        emptyMap()
    }

    fun required(field: String, message: String) {
        val value = data[field]
        if (value == null || (value is String && value.isBlank())) {
            errors[field] = message
        }
    }

    fun min(field: String, minValue: Double, message: String) {
        val value = data[field]
        if (value is Number && value.toDouble() <= minValue) {
            errors[field] = message
        }
        // Note: Jika null, validator 'required' yang akan menangani
    }

    fun validate() {
        if (errors.isNotEmpty()) {
            throw ValidationException(errors)
        }
    }
}