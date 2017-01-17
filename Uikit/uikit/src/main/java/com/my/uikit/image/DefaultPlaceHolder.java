package com.my.uikit.image;

import android.graphics.*;
import com.my.uikit.R;

/**
 * Created by jiangtao on 16/3/4.
 *
 * @author goddard.jt
 * @version 1.0.0
 */
public class DefaultPlaceHolder {

    private static final int DEFAULT_BACKGROUND_COLOR = Color.argb(255, 248, 248, 248);

    private int mPlaceholderImage;
    private static Bitmap sPlaceHolderBitmap;
    private static int sPlaceHolderWidth, sPlaceHolderHeight;
    private static Rect sPlaceHolderSrcRect;
    private Rect mPlaceHolderDstRect;
    private NetworkImageView mNetworkImageView;
    private Paint mPaint;

    /**
     * @param view view
     */
    DefaultPlaceHolder(NetworkImageView view) {
        mNetworkImageView = view;
        mPlaceholderImage = view.getPlaceholderImage();
        if (sPlaceHolderBitmap == null || sPlaceHolderBitmap.isRecycled()) {
            sPlaceHolderBitmap = BitmapFactory.decodeResource(view.getResources(), R.drawable.default_image);
            if (sPlaceHolderBitmap != null) {
                sPlaceHolderWidth = sPlaceHolderBitmap.getWidth();
                sPlaceHolderHeight = sPlaceHolderBitmap.getHeight();
                sPlaceHolderSrcRect = new Rect(0, 0, sPlaceHolderWidth, sPlaceHolderHeight);
            }
        }
        mPlaceHolderDstRect = new Rect();
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(DEFAULT_BACKGROUND_COLOR);
    }

    /**
     * 绘制默认图片
     *
     * @param canvas canvas
     */
    void draw(Canvas canvas) {
        if (mPlaceholderImage == R.drawable.default_image
                && sPlaceHolderBitmap != null
                && !sPlaceHolderBitmap.isRecycled()
                && mNetworkImageView.getDrawable() == null
                && mNetworkImageView.getBackground() == null) {
            int viewWidth = mNetworkImageView.getWidth();
            int viewHeight = mNetworkImageView.getHeight();

            // 宽高最小值得2/3
            int minWidth = Math.min(viewWidth, viewHeight) * 2 / 3;
            if (minWidth <= 0) {
                return;
            }

            int left = viewWidth / 2 - minWidth / 2;
            int top = viewHeight / 2 - minWidth / 2;
            int right = viewWidth / 2 + minWidth / 2;
            int bottom = viewHeight / 2 + minWidth / 2;

            mPlaceHolderDstRect.set(left, top, right, bottom);

            canvas.save();
            int cornerRadius = mNetworkImageView.getCornerRadius();
            if (cornerRadius > 0) {
                canvas.drawRoundRect(new RectF(0, 0, viewWidth, viewHeight), cornerRadius, cornerRadius, mPaint);
            } else if (mNetworkImageView.isCircle()) {
                drawCircle(canvas, viewWidth, viewHeight);
            } else {
                canvas.drawColor(DEFAULT_BACKGROUND_COLOR);
            }
            canvas.drawBitmap(sPlaceHolderBitmap, sPlaceHolderSrcRect, mPlaceHolderDstRect, null);
            canvas.restore();
        } else {
            canvas.drawColor(Color.TRANSPARENT);
        }
    }

    private void drawCircle(Canvas canvas, int viewWidth, int viewHeight) {
        int minCircle = Math.min(viewHeight, viewWidth);
        int left = viewWidth / 2 - minCircle / 2;
        int top = viewHeight / 2 - minCircle / 2;
        int right = viewWidth / 2 + minCircle / 2;
        int bottom = viewHeight / 2 + minCircle / 2;
        canvas.drawRoundRect(new RectF(left, top, right, bottom), minCircle / 2, minCircle / 2, mPaint);
    }
}
