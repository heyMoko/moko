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
                // Airy everyday color, open shadows and clean whites.
                exposure = 0.055f,
                contrast = 0.98f,
                saturation = 0.94f,
                temperature = 0.008f,
                tint = 0.006f,
                highlights = -0.055f,
                shadows = 0.045f
            )
        )

        val Film = FilterDefinition(
            id = "film",
            displayName = "Film",
            parameters = FilterParameters(
                // Soft matte print: muted color, warm paper and gentle blacks.
                exposure = 0.035f,
                contrast = 0.86f,
                saturation = 0.76f,
                temperature = 0.026f,
                tint = 0.008f,
                highlights = -0.015f,
                shadows = 0.025f,
                fade = 0.10f,
                grain = 0.018f,
                vignette = 0.035f
            )
        )

        val Disposable = FilterDefinition(
            id = "disposable",
            displayName = "Disposable",
            parameters = FilterParameters(
                // Sun-warmed snapshot: amber color, punch and visible texture.
                exposure = 0.06f,
                contrast = 1.12f,
                saturation = 1.10f,
                temperature = 0.045f,
                tint = -0.008f,
                highlights = -0.075f,
                shadows = 0.04f,
                fade = 0.045f,
                grain = 0.035f,
                vignette = 0.10f
            )
        )

        val Digital = FilterDefinition(
            id = "digital",
            displayName = "Digital",
            parameters = FilterParameters(
                // Crisp cool color and deeper blacks, without a film-grain overlay.
                exposure = 0.025f,
                contrast = 1.16f,
                saturation = 1.16f,
                temperature = -0.022f,
                tint = -0.006f,
                highlights = -0.065f,
                shadows = 0.035f,
                vignette = 0.025f
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
