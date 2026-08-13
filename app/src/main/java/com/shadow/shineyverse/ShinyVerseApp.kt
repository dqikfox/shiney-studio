package com.shadow.shineyverse

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class ShinyVerseApp : Application(), ImageLoaderFactory {

    @Inject lateinit var imageLoader: ImageLoader

    /** Makes every AsyncImage/rememberAsyncImagePainter call app-wide use the
     * authenticated, base-URL-aware loader instead of Coil's cookie-less default. */
    override fun newImageLoader(): ImageLoader = imageLoader
}
