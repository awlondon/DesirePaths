package com.pdceng.www.desirepaths;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.util.Log;
import android.util.LruCache;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

/**
 * Created by alondon on 8/4/2017.
 */

public class Universals {
    static boolean isAnon = false;
    static String SOCIAL_MEDIA_ID;
    static String NAME;
    static boolean SYNCHRONIZING = false;
    static Bitmap bitmapBeingProcessed;
    static boolean chooseLocation = false;
    static MapActivity mapActivity;
    private static LruCache<String, Bitmap> bitmapMemoryCache;
    private static Universals instance;
    Context mContext;

    public Universals(Context context) {

        mContext = context;

        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        int cacheSize = maxMemory / 8;

        if (bitmapMemoryCache == null) {
            System.out.println("Creating new LruCache!");
            bitmapMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
                @Override
                protected int sizeOf(String key, Bitmap bitmap) {
                    return bitmap.getByteCount() / 1024;
                }
            };
        }

    }

    public static synchronized Universals getInstance(Context context) {
        if (instance == null) {
            instance = new Universals(context.getApplicationContext());
        }

        return instance;
    }

    static Bitmap sampleBitmap(Bitmap bitmap) {

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        Log.d("starting width", String.valueOf(width));
        Log.d("starting height", String.valueOf(height));

        float scaleFactor = (float) 700 / height;
        int newHeight = (int) (height * scaleFactor);
        int newWidth = (int) (width * scaleFactor);

        Log.d("new width", String.valueOf(newWidth));
        Log.d("new height", String.valueOf(newHeight));

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        System.out.println("ImageView height: " + reqHeight);
        System.out.println("ImageView width: " + reqWidth);
        final int height = options.outHeight;
        System.out.println("Image height: " + height);
        final int width = options.outWidth;
        System.out.println("Image width: " + width);
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        System.out.println("inSampleSize: " + inSampleSize);
        return inSampleSize;
    }

    static Bitmap getBitmapFromURL(String image, int reqWidth, int reqHeight) {
        //Rect is an object class that contains the lengths of the 4 sides of a rectangle.
        Rect rect = new Rect(0, 0, 0, 0);
        //A BitmapFactory is a class that creates bitmaps.
        //The BitmapFactory.Options subclass allows you to change settings associated with a bitmap
        //and access properties of the bitmap.
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        //URLs must be used (instead of Strings) to send information to the Internet
        URL imageURL;
        //A try/catch routine must be applied to deal with errors when sending data outside of the app
        try {
            imageURL = new URL(image);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
        try {
            //The following line downloads the bitmap from the URL and applies the bitmap info to the options variable.
            BitmapFactory.decodeStream(imageURL.openConnection().getInputStream(), rect, options);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        //'inSampleSize' is used to decrease the quality of large images that are going to be shown in the UI in a
        //smaller ImageView. We decrease the quality in order to save on memory (the high quality is not needed if
        //the ImageView is smaller.
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        try {
            return BitmapFactory.decodeStream(imageURL.openConnection().getInputStream(), rect, options);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    static String addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemoryCache(key) == null) {
            bitmapMemoryCache.put(key, bitmap);
        }
        return key;
    }

    static Bitmap getBitmapFromMemoryCache(String key) {
        return bitmapMemoryCache.get(key);
    }

    static boolean isBitmapInMemoryCache(String key) {
        if (key == null) {
            return false;
        } else {
            return bitmapMemoryCache.get(key) != null;
        }
    }

    static void sendBitmapForProcessing(Bitmap bitmap) {
        bitmapBeingProcessed = bitmap;
    }

    static void nullifyBitmapBeingProcessed() {
        bitmapBeingProcessed = null;
    }

    static void setChooseLocation(boolean bool) {
        chooseLocation = bool;
    }

    static void defaultChooseLocation() {
        chooseLocation = false;
    }
}
