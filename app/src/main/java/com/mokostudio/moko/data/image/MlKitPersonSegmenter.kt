package com.mokostudio.moko.data.image

import android.graphics.Bitmap
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.Segmentation
import com.google.mlkit.vision.segmentation.SegmentationMask
import com.google.mlkit.vision.segmentation.selfie.SelfieSegmenterOptions
import com.mokostudio.moko.domain.model.PersonMask
import java.nio.ByteOrder
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class MlKitPersonSegmenter @Inject constructor() : PersonSegmenter {
    private val options = SelfieSegmenterOptions.Builder()
        .setDetectorMode(SelfieSegmenterOptions.SINGLE_IMAGE_MODE)
        .build()

    private val segmenter = Segmentation.getClient(options)

    override suspend fun createMask(bitmap: Bitmap): PersonMask {
        val inputImage = InputImage.fromBitmap(bitmap, 0)
        val rawMask = segmenter.process(inputImage).await()
        val rawPersonMask = rawMask.toPersonMask()
        val scaled = PersonMaskProcessor.scaleAndFeather(
            source = rawPersonMask,
            targetWidth = bitmap.width,
            targetHeight = bitmap.height,
            // Flash uses the mask as a low-frequency illumination field. A broad pass
            // removes segmentation blocks without spreading light into the background.
            featherRadius = 14
        )

        Log.d(
            TAG,
            "ML Kit mask raw=${rawPersonMask.width}x${rawPersonMask.height}, " +
                "target=${bitmap.width}x${bitmap.height}, hasPerson=${scaled.hasPerson()}"
        )

        return scaled
    }

    private fun SegmentationMask.toPersonMask(): PersonMask {
        val maskWidth = width
        val maskHeight = height
        val buffer = buffer.order(ByteOrder.nativeOrder())
        buffer.rewind()

        val confidence = FloatArray(maskWidth * maskHeight)
        for (index in confidence.indices) {
            confidence[index] = buffer.float.coerceIn(0f, 1f)
        }

        return PersonMask(
            width = maskWidth,
            height = maskHeight,
            confidence = confidence
        )
    }

    private suspend fun <T> Task<T>.await(): T {
        return suspendCancellableCoroutine { continuation ->
            addOnSuccessListener { result ->
                if (continuation.isActive) {
                    continuation.resume(result)
                }
            }
            addOnFailureListener { error ->
                if (continuation.isActive) {
                    continuation.resumeWithException(error)
                }
            }
            addOnCanceledListener {
                continuation.cancel()
            }
        }
    }

    private companion object {
        const val TAG = "MlKitPersonSegmenter"
    }
}
