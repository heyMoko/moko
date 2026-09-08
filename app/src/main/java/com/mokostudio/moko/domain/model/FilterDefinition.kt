package com.mokostudio.moko.domain.model

data class FilterDefinition(
    val id: String,
    val displayName: String,
    val parameters: FilterParameters? = null,
    val requiresPersonMask: Boolean = false
) {
    companion object {
        val Original = FilterDefinition(
            id = "original",
            displayName = "Original"
        )

        val Flash = FilterDefinition(
            id = "flash",
            displayName = "Flash",
            requiresPersonMask = true
        )

        val IPhone6 = FilterDefinition(
            id = "iphone_6",
            displayName = "iPhone 6",
            parameters = FilterParameters(
                exposure = 0.025f,
                contrast = 1.07f,
                saturation = 1.06f,
                temperature = 0.025f,
                highlights = -0.04f,
                shadows = 0.04f,
                grain = 0.002f,
                vignette = 0.02f
            )
        )

        val Film = FilterDefinition(
            id = "film",
            displayName = "Film",
            parameters = FilterParameters(
                exposure = 0.015f,
                contrast = 0.90f,
                saturation = 0.82f,
                temperature = 0.05f,
                tint = 0.012f,
                highlights = -0.03f,
                shadows = 0.09f,
                fade = 0.12f,
                grain = 0.015f,
                vignette = 0.09f
            )
        )

        val Disposable = FilterDefinition(
            id = "disposable",
            displayName = "Disposable",
            parameters = FilterParameters(
                exposure = 0.035f,
                contrast = 1.18f,
                saturation = 1.18f,
                temperature = 0.035f,
                highlights = -0.06f,
                shadows = -0.02f,
                fade = 0.03f,
                grain = 0.025f,
                vignette = 0.16f
            )
        )

        val Digital = FilterDefinition(
            id = "digital",
            displayName = "Digital",
            parameters = FilterParameters(
                exposure = -0.01f,
                contrast = 1.23f,
                saturation = 1.13f,
                temperature = -0.035f,
                tint = -0.01f,
                highlights = -0.08f,
                shadows = -0.02f,
                fade = 0.01f,
                grain = 0.012f,
                vignette = 0.06f
            )
        )

        val NightFlash = FilterDefinition(
            id = "night_flash",
            displayName = "Night Flash",
            parameters = FilterParameters(
                exposure = -0.04f,
                contrast = 1.15f,
                saturation = 0.82f,
                temperature = -0.035f,
                highlights = -0.08f,
                shadows = -0.02f,
                grain = 0.022f,
                vignette = 0.15f
            ),
            requiresPersonMask = true
        )

        val EditorFilters = listOf(
            Original,
            Flash,
            NightFlash,
            IPhone6,
            Film,
            Disposable,
            Digital
        )
    }
}
