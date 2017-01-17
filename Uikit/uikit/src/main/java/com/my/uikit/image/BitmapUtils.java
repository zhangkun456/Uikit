package com.my.uikit.image;

import android.graphics.Bitmap;

/**
 * @author kun.zhang
 * @version 9.0.0
 *          17/1/17 19:06
 */

public class BitmapUtils {
    public static Bitmap fastBlurInJava(Bitmap sentBitmap, int radius) {
        if(radius >= 1 && sentBitmap != null) {
            int width = sentBitmap.getWidth();
            int height = sentBitmap.getHeight();
            return width > 0 && height > 0?Bitmap.createBitmap(fastBlurToColorsInJava(sentBitmap, radius), 0, width, width, height, sentBitmap.getConfig()):null;
        } else {
            return null;
        }
    }

    private static int[] fastBlurToColorsInJava(Bitmap sentBitmap, int radius) {
        int w = sentBitmap.getWidth();
        int h = sentBitmap.getHeight();
        int[] pix = new int[w * h];
        sentBitmap.getPixels(pix, 0, w, 0, 0, w, h);
        int wm = w - 1;
        int hm = h - 1;
        int wh = w * h;
        int div = radius + radius + 1;
        int[] r = new int[wh];
        int[] g = new int[wh];
        int[] b = new int[wh];
        int[] vmin = new int[Math.max(w, h)];
        int divsum = div + 1 >> 1;
        divsum *= divsum;
        boolean dvFactor = true;
        int[] dv = new int[256 * divsum];
        int start = 0;

        int i;
        for(i = 0; i < 256; ++i) {
            for(int stack = 0; stack < divsum; ++stack) {
                dv[start + stack] = i;
            }

            start += divsum;
        }

        int yw = 0;
        int yi = 0;
        int[][] var39 = new int[div][3];
        int r1 = radius + 1;

        int rsum;
        int gsum;
        int bsum;
        int x;
        int y;
        int p;
        int stackpointer;
        int stackstart;
        int[] sir;
        int rbs;
        int routsum;
        int goutsum;
        int boutsum;
        int rinsum;
        int ginsum;
        int binsum;
        for(y = 0; y < h; ++y) {
            rinsum = 0;
            ginsum = 0;
            binsum = 0;
            routsum = 0;
            goutsum = 0;
            boutsum = 0;
            rsum = 0;
            gsum = 0;
            bsum = 0;

            for(i = -radius; i <= radius; ++i) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))];
                sir = var39[i + radius];
                sir[0] = (p & 16711680) >> 16;
                sir[1] = (p & '\uff00') >> 8;
                sir[2] = p & 255;
                rbs = r1 - Math.abs(i);
                rsum += sir[0] * rbs;
                gsum += sir[1] * rbs;
                bsum += sir[2] * rbs;
                if(i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
            }

            stackpointer = radius;

            for(x = 0; x < w; ++x) {
                r[yi] = dv[rsum];
                g[yi] = dv[gsum];
                b[yi] = dv[bsum];
                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;
                stackstart = stackpointer - radius + div;
                sir = var39[stackstart % div];
                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];
                if(y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm);
                }

                p = pix[yw + vmin[x]];
                sir[0] = (p & 16711680) >> 16;
                sir[1] = (p & '\uff00') >> 8;
                sir[2] = p & 255;
                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];
                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;
                stackpointer = (stackpointer + 1) % div;
                sir = var39[stackpointer % div];
                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];
                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];
                ++yi;
            }

            yw += w;
        }

        for(x = 0; x < w; ++x) {
            rinsum = 0;
            ginsum = 0;
            binsum = 0;
            routsum = 0;
            goutsum = 0;
            boutsum = 0;
            rsum = 0;
            gsum = 0;
            bsum = 0;
            int yp = -radius * w;

            for(i = -radius; i <= radius; ++i) {
                yi = Math.max(0, yp) + x;
                sir = var39[i + radius];
                sir[0] = r[yi];
                sir[1] = g[yi];
                sir[2] = b[yi];
                rbs = r1 - Math.abs(i);
                rsum += r[yi] * rbs;
                gsum += g[yi] * rbs;
                bsum += b[yi] * rbs;
                if(i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }

                if(i < hm) {
                    yp += w;
                }
            }

            yi = x;
            stackpointer = radius;

            for(y = 0; y < h; ++y) {
                pix[yi] = -16777216 & pix[yi] | dv[rsum] << 16 | dv[gsum] << 8 | dv[bsum];
                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;
                stackstart = stackpointer - radius + div;
                sir = var39[stackstart % div];
                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];
                if(x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w;
                }

                p = x + vmin[y];
                sir[0] = r[p];
                sir[1] = g[p];
                sir[2] = b[p];
                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];
                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;
                stackpointer = (stackpointer + 1) % div;
                sir = var39[stackpointer];
                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];
                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];
                yi += w;
            }
        }

        return pix;
    }

}
