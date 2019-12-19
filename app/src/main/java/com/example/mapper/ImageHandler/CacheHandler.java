package com.example.mapper.ImageHandler;

import android.graphics.Bitmap;
import androidx.collection.LruCache;

/**
 * @author Neville Kitala
 * @version 1.0
 * @since 1.0
 */
public class CacheHandler {
    /**
     * To improve the speed and responsiveness of the app it is important to use the app.
     * If you do not cache the images used in the app the lag of the application will increase and reduce the user experience.
     * This is where the decoded bitmaps are stored from the app.
     */
    private LruCache<String, Bitmap> cacheStore;

    /**
     *
     */
    private static CacheHandler cache;


    /**
     * For the cache to work, it needs to be accessible from all the application activities. This means that once the cache has been instantiated
     * the same cache needs to be maintained throughout the application.
     * This method retrieves the current cache that has been instantiated, or creates a new one if non is found.
     * @return cache
     */

    public static CacheHandler getInstance()
    {
        if(cache == null)
        {
            cache = new CacheHandler();
        }

        return cache;
    }

    /**
     * This initialises the cache as a portion of the memory which this app is assigned. This is  an eigth
     * of the max memory of the app
     */

    public void initializeCache()
    {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() /1024);

        final int cacheSize = maxMemory / 8;

        System.out.println("cache size = "+cacheSize);

        cacheStore = new LruCache<String, Bitmap>(cacheSize)
        {
            protected int sizeOf(String key, Bitmap value)
            {
                // The cache size will be measured in kilobytes rather than number of items.

                int bitmapByteCount = value.getRowBytes() * value.getHeight();

                return bitmapByteCount / 1024;
            }
        };
    }

    /**
     * A method to add a new bitmap to the application cache. This is stored as a pair of a bitmap, and a key.
     * The key is used to reference the stored bitmap in the cache.
     * @param key
     * @param value
     */

    public void addToCache(String key, Bitmap value)
    {
        if(cacheStore != null && cacheStore.get(key) == null)
        {
            cacheStore.put(key, value);
        }
    }

    /**
     * A method used to get entries from the cache
     * This method allows the stored entries to be retrieved using the key value, which is a unique string stores
     * within the entry.
     * @param key
     * @return
     */

    public Bitmap getFromCache(String key)
    {
        if(key != null)
        {
            return cacheStore.get(key);
        }
        else
        {
            return null;
        }
    }
}