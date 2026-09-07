package com.mokostudio.moko.data.image

import android.graphics.Bitmap
import android.net.Uri
import com.mokostudio.moko.core.image.ImageProcessor
import com.mokostudio.moko.domain.model.FilterDefinition
import com.mokostudio.moko.domain.model.PersonMask
import com.mokostudio.moko.domain.model.ProcessedImage
import javax.inject.Inject

class MokoImageProcessor @Inject constructor() : ImageProcessor {
    override fun process(
        sourceUri: Uri,
        source: Bitmap,
        filter: FilterDefinition,
        strength: Float,
        personMask: PersonMask?
    ): ProcessedImage {
        val bitmap = when (filter.id) {
            FilterDefinition.Flash.id -> applyFlashLook(source, strength, personMask)
            FilterDefinition.NightFlash.id -> applyNightFlashLook(source, strength, personMask)
            else -> filter.parameters?.let { parameters ->
                applyCameraLook(source, parameters, strength)
            } ?: source
        }

        return ProcessedImage(
            sourceUri = sourceUri,
            bitmap = bitmap,
            filter = filter
        )
    }

    private fun applyFlashLook(
        source: Bitmap,
        strength: Float,
        personMask: PersonMask?
    ): Bitmap {
        if (strength <= 0f) return source

        val width = source.width
        val height = source.height
        val original = IntArray(width * height)
        source.getPixels(original, 0, width, 0, 0, width, height)

        val flashed = FlashImageComposer.compose(
            original = original,
            width = width,
            height = height,
            strength = strength,
            personMask = personMask
        )

        return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
            setPixels(flashed, 0, width, 0, 0, width, height)
        }
    }

    private fun applyNightFlashLook(
        source: Bitmap,
        strength: Float,
        personMask: PersonMask?
    ): Bitmap {
        val flashBase = applyFlashLook(source, strength, personMask)
        return applyCameraLook(
            source = flashBase,
            parameters = requireNotNull(FilterDefinition.NightFlash.parameters),
            strength = strength
        )
    }

    private fun applyCameraLook(
        source: Bitmap,
        parameters: com.mokostudio.moko.domain.model.FilterParameters,
        strength: Float
    ): Bitmap {
        if (strength <= 0f) return source

        val width = source.width
        val height = source.height
        val original = IntArray(width * height)
        source.getPixels(original, 0, width, 0, 0, width, height)
        val processed = CameraLookComposer.compose(
            original = original,
            width = width,
            height = height,
            parameters = parameters,
            strength = strength
        )

        return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
            setPixels(processed, 0, width, 0, 0, width, height)
        }
    }
}
