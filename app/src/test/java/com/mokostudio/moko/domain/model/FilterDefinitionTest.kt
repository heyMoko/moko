package com.mokostudio.moko.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class FilterDefinitionTest {
    @Test
    fun editorFilters_placesNightFlashImmediatelyAfterFlash() {
        assertEquals(
            listOf("original", "flash", "night_flash"),
            FilterDefinition.EditorFilters.take(3).map(FilterDefinition::id)
        )
    }
}
