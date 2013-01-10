package com.flounder.fishDiary.image;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Color;

public class ImageData {

    private Bitmap srcBitmap;
    private Bitmap dstBitmap;

    private int mWidth;
    private int mHeight;
    protected int[] mColorArray;

    public ImageData(Bitmap bitmap) {
        srcBitmap = bitmap;
        mWidth = bitmap.getWidth();
        mHeight = bitmap.getHeight();
        dstBitmap = Bitmap.createBitmap(mWidth, mHeight, Config.ARGB_8888);
        initColorArray();
    }

    private void initColorArray() {
        mColorArray = new int[mWidth * mHeight];
        srcBitmap.getPixels(mColorArray, 0, mWidth, 0, 0, mWidth, mHeight);
    }

    public int getRComponent(int x, int y) {
        return Color.red(mColorArray[y * srcBitmap.getWidth() + x]);
    }

    public int getGComponent(int x, int y) {
        return Color.green(mColorArray[y * srcBitmap.getWidth() + x]);
    }

    public int getBComponent(int x, int y) {
        return Color.blue(mColorArray[y * srcBitmap.getWidth() + x]);
    }

    public void setPixelColor(int x, int y, int r, int g, int b) {
        int rgbColor = (255 << 24) + (r << 16) + (g << 8) + b;
        mColorArray[((y * srcBitmap.getWidth() + x))] = rgbColor;
    }

    public int getWidth() {
        return srcBitmap.getWidth();
    }

    public int getHeight() {
        return srcBitmap.getHeight();
    }

    public Bitmap getDstBitmap() {
        dstBitmap.setPixels(mColorArray, 0, mWidth, 0, 0, mWidth, mHeight);
        return dstBitmap;
    }
}
