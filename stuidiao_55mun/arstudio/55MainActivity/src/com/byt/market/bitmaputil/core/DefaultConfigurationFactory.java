package com.byt.market.bitmaputil.core;

import java.io.File;

import android.content.Context;
import android.graphics.Bitmap;

import com.byt.market.bitmaputil.cache.disc.DiscCacheAware;
import com.byt.market.bitmaputil.cache.disc.impl.FileCountLimitedDiscCache;
import com.byt.market.bitmaputil.cache.disc.impl.TotalSizeLimitedDiscCache;
import com.byt.market.bitmaputil.cache.disc.impl.UnlimitedDiscCache;
import com.byt.market.bitmaputil.cache.disc.naming.FileNameGenerator;
import com.byt.market.bitmaputil.cache.disc.naming.HashCodeFileNameGenerator;
import com.byt.market.bitmaputil.cache.memory.MemoryCacheAware;
import com.byt.market.bitmaputil.cache.memory.impl.FuzzyKeyMemoryCache;
import com.byt.market.bitmaputil.cache.memory.impl.UsingFreqLimitedMemoryCache;
import com.byt.market.bitmaputil.core.assist.MemoryCacheUtil;
import com.byt.market.bitmaputil.core.display.BitmapDisplayer;
import com.byt.market.bitmaputil.core.display.SimpleBitmapDisplayer;
import com.byt.market.bitmaputil.core.download.ImageDownloader;
import com.byt.market.bitmaputil.core.download.URLConnectionImageDownloader;
import com.byt.market.bitmaputil.utils.StorageUtils;

/**
 * Factory for providing of default options for {@linkplain ImageLoaderConfiguration configuration}
 * 
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 */
public class DefaultConfigurationFactory {

	/** Create {@linkplain HashCodeFileNameGenerator default implementation} of FileNameGenerator */
	public static FileNameGenerator createFileNameGenerator() {
		return new HashCodeFileNameGenerator();
	}

	/** Create default implementation of {@link DisckCacheAware} depends on incoming parameters */
	public static DiscCacheAware createDiscCache(Context context, FileNameGenerator discCacheFileNameGenerator, int discCacheSize, int discCacheFileCount) {
		if (discCacheSize > 0) {
			File individualCacheDir = StorageUtils.getIndividualCacheDirectory(context);
			return new TotalSizeLimitedDiscCache(individualCacheDir, discCacheFileNameGenerator, discCacheSize);
		} else if (discCacheFileCount > 0) {
			File individualCacheDir = StorageUtils.getIndividualCacheDirectory(context);
			return new FileCountLimitedDiscCache(individualCacheDir, discCacheFileNameGenerator, discCacheFileCount);
		} else {
			File cacheDir = StorageUtils.getCacheDirectory(context);
			return new UnlimitedDiscCache(cacheDir, discCacheFileNameGenerator);
		}
	}

	/** Create default implementation of {@link MemoryCacheAware} depends on incoming parameters */
	public static MemoryCacheAware<String, Bitmap> createMemoryCache(int memoryCacheSize, boolean denyCacheImageMultipleSizesInMemory) {
		MemoryCacheAware<String, Bitmap> memoryCache = new UsingFreqLimitedMemoryCache(memoryCacheSize);
		if (denyCacheImageMultipleSizesInMemory) {
			memoryCache = new FuzzyKeyMemoryCache<String, Bitmap>(memoryCache, MemoryCacheUtil.createFuzzyKeyComparator());
		}
		return memoryCache;
	}

	/** Create default implementation of {@link ImageDownloader} */
	public static ImageDownloader createImageDownloader() {
		return new URLConnectionImageDownloader();
	}

	/** Create default implementation of {@link BitmapDisplayer} */
	public static BitmapDisplayer createBitmapDisplayer() {
		return new SimpleBitmapDisplayer();
	}
}
