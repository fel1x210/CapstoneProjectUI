package ca.gbc.comp3074.uiprototype.util

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory
import com.bumptech.glide.load.engine.cache.LruResourceCache
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestOptions

/**
 * Custom Glide configuration for optimal image loading performance
 */
@GlideModule
class GlideConfiguration : AppGlideModule() {
    
    override fun applyOptions(context: Context, builder: GlideBuilder) {
        // Set memory cache size (25% of available memory)
        val memoryCacheSizeBytes = 1024 * 1024 * 20 // 20MB
        builder.setMemoryCache(LruResourceCache(memoryCacheSizeBytes.toLong()))
        
        // Set disk cache size (100MB)
        val diskCacheSizeBytes = 1024 * 1024 * 100 // 100MB
        builder.setDiskCache(InternalCacheDiskCacheFactory(context, diskCacheSizeBytes.toLong()))
        
        // Set default request options
        builder.setDefaultRequestOptions(
            RequestOptions()
                .format(DecodeFormat.PREFER_RGB_565) // Use less memory
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC) // Smart caching
        )
    }
    
    override fun isManifestParsingEnabled(): Boolean {
        // Disable manifest parsing for faster startup
        return false
    }
}
