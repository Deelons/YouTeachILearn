/**
 * 文件名：ImageManager
 * 描 述：图片管理工具类
 * 作 者：DJZ
 * 时 间：2015-11-24
 */
package com.netease.nim.demo.main;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.LruCache;

/**
 * Created by Admin on 2015/11/15.
 */
public class ImageManager {
    /*
    图片缓存类 LruCache
     */
    private static LruCache<String,Bitmap> myMemoryCache;

    /*
    ImageManager对象
     */
    private static ImageManager myImageManager;

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    private ImageManager() {
        /*
        *获取程序试图使用的最大内存
        *以KB为单位
        */
        int maxMemory = (int) Runtime.getRuntime().maxMemory()/1024;
        /*
        * 利用程序试图使用的最大内存的1/8来作为缓存
        **/
        int CacheSize = maxMemory / 8;
        /*
         * LruCache的构造函数存入缓存大小
         */
        myMemoryCache = new LruCache<String, Bitmap>(CacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount()/1024;
            }
        };

    }

    /*
     *单例模式创建类对象( getInstance() )
     */
    public static ImageManager getInstance() {
        if (myImageManager == null) {
            myImageManager = new ImageManager();
        }
        return myImageManager;
    }
    /*
     *储存一张图片到LruCache
     * key为LuCache的键值
     * Bitmap为LuCache的值，这里为图片Bitmap对象
     */

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    public void addBitmapToMemoryCache(String key,Bitmap bitmap){
        if(getBitmapFromMemoryCache(key) == null) {
            myMemoryCache.put(key,bitmap);
        }
    }
    /*
    * 从LruCache中获取一张图片，不存在则返回null
    *key为图片对象的键值
    * 存在该键值的图片则返回对应键值的Bitmap对象
    */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    public Bitmap getBitmapFromMemoryCache(String key) {
        return myMemoryCache.get(key);
    }

    /*
     *计算将利用Bitmap进行放缩的比率
     */

    public static int calculateInSampleSize(BitmapFactory.Options options,int reqWidth) {
        //计算源图片的长，宽
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if(width>reqWidth) {
            //计算实际宽高和要求宽高的比率

            final int widthRation = Math.round((float) height / (float) reqWidth);
            //选择最小的比率作为最终所需要图片的宽高比率
            inSampleSize =widthRation;

        }

        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromResource(String pathName, int reqWidth) {
        //第一次将inJustDecodeBounds属性设置为true，禁止分配内存，获取图片
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pathName, options);
        //只用已经定义过的方法来计算inSampleSize值
        options.inSampleSize = calculateInSampleSize(options,reqWidth);
        //用获取的inSampleSize再次解析图片，这次分配内存
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(pathName, options);
    }

}
