package com.my.uikit.image;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

/**
 * Created by brucelee on 3/4/16.
 */
public class BitmapDrawableBlurTask extends BlurTask {
    public BitmapDrawableBlurTask(Bitmap bitmap, NetworkImageView networkImageView, int blurRadius) {
        super(bitmap, networkImageView, blurRadius);
    }

    @Override
    protected Drawable getDrawable(Bitmap bitmap) {
        return new BitmapDrawable(bitmap);
    }
}
