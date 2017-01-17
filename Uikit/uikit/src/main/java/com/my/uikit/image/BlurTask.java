package com.my.uikit.image;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;

import java.lang.ref.WeakReference;

/**
 * Created by brucelee on 3/1/16.
 */
class BlurTask extends AsyncTask<Object, Void, Bitmap> {
    private Bitmap mBitmap;
    private WeakReference<NetworkImageView> mNetworkImageViewWeakReference;
    private int mBlurRadius;

    public BlurTask(Bitmap bitmap, NetworkImageView networkImageView, int blurRadius) {
        mBitmap = bitmap;
        mNetworkImageViewWeakReference = new WeakReference<NetworkImageView>(networkImageView);
        mBlurRadius = blurRadius;
    }

    @Override
    protected Bitmap doInBackground(Object... params) {
        return BitmapUtils.fastBlurInJava(mBitmap, mBlurRadius);
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);

        NetworkImageView networkImageView = mNetworkImageViewWeakReference.get();
        if (networkImageView != null) {
            networkImageView.superSetImageDrawable(getDrawable(bitmap));
        }
    }

    protected Drawable getDrawable(Bitmap bitmap) {
        return new RoundedDrawable(bitmap);
    }
}
