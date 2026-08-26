package com.mokostudio.moko.domain.model

data class FilterDefinition(
    val id: String,
    val displayName: String
) {
    companion object {
        val Original = FilterDefinition(
            id = "original",
            displayName = "Original"
        )
    }
}
