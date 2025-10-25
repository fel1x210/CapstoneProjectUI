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
 * Custom Glide configuration optimized for 120 FPS performance
 * Configures memory cache, disk cache, and image quality
 */
@GlideModule
class GlideConfiguration : AppGlideModule() {
    
    override fun applyOptions(context: Context, builder: GlideBuilder) {
        // Increase memory cache size for ultra-smooth scrolling at high refresh rates
        val memoryCacheSizeBytes = 1024 * 1024 * 50 // 50MB (increased from 20MB)
        builder.setMemoryCache(LruResourceCache(memoryCacheSizeBytes.toLong()))
        
        // Increase disk cache size for better offline performance
        val diskCacheSizeBytes = 1024 * 1024 * 250 // 250MB (increased from 100MB)
        builder.setDiskCache(InternalCacheDiskCacheFactory(context, diskCacheSizeBytes.toLong()))
        
        // Optimize for high refresh rate displays
        builder.setDefaultRequestOptions(
            RequestOptions()
                .format(DecodeFormat.PREFER_RGB_565) // 50% less memory than ARGB_8888
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC) // Smart caching
                .disallowHardwareConfig() // Better for frequent bitmap manipulation
        )
        
        // Reduce logging overhead for better performance
        builder.setLogLevel(android.util.Log.ERROR)
    }
    
    override fun isManifestParsingEnabled(): Boolean {
        // Disable manifest parsing for faster app startup
        return false
    }
}
