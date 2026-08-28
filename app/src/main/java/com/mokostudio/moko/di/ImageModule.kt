package com.mokostudio.moko.di

import com.mokostudio.moko.core.image.ImageProcessor
import com.mokostudio.moko.data.image.MlKitPersonSegmenter
import com.mokostudio.moko.data.image.MokoImageProcessor
import com.mokostudio.moko.data.image.PersonSegmenter
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ImageModule {
    @Binds
    abstract fun bindImageProcessor(
        processor: MokoImageProcessor
    ): ImageProcessor

    @Binds
    abstract fun bindPersonSegmenter(
        segmenter: MlKitPersonSegmenter
    ): PersonSegmenter
}
