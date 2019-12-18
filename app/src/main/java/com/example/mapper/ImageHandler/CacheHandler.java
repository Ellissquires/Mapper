package com.example.mapper.ImageHandler;

import android.graphics.Bitmap;

import androidx.collection.LruCache;


public class CacheHandler {
    private LruCache<String, Bitmap> cacheStore;

    private static CacheHandler cache;

    public static CacheHandler getInstance()
    {
        if(cache == null)
        {
            cache = new CacheHandler();
        }

        return cache;
    }

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

    public void addToCache(String key, Bitmap value)
    {
        if(cacheStore != null && cacheStore.get(key) == null)
        {
            cacheStore.put(key, value);
        }
    }

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

    public void removeFromCache(String key)
    {
        cacheStore.remove(key);
    }

    public void clearCache()
    {
        if(cacheStore != null)
        {
            cacheStore.evictAll();
        }
    }

    public int getCacheSize(){
        return cacheStore.size();
    }

    public LruCache getCache(){
        return cacheStore;
    }

    public Bitmap loadBitmap(String imageKey) {
        final Bitmap bitmap = getFromCache(imageKey);
        return bitmap;
    }
}