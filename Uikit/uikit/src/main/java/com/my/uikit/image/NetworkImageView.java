package com.my.uikit.image;



import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.ViewPropertyAnimation;
import com.bumptech.glide.request.target.Target;

import java.io.File;

import com.my.uikit.R;
/**
 * @author kun.zhang
 * @version 9.0.0
 *          17/1/17 15:05
 */
public class NetworkImageView extends ImageView {
    private static final String INSTANCE_PLACEHOLDER = "placeholder";
    private static final String INSTANCE_ERROR = "error";
    private static final String INSTANCE_IS_CIRCLE = "is_circle";
    private static final String INSTANCE_CIRCLE_BORDER_WIDTH = "circle_border_width";
    private static final String INSTANCE_BLUR_RADIUS = "blur_radius";
    private static final String INSTANCE_CORNER_RADIUS = "corner_radius";
    private static final String INSTANCE_CORNER_TYPE = "corner_type";
    private static final String INSTANCE_NETWORK_IMAGEVIEW = "networkimageview";
    private static final String INSTANCE_RATIO_WIDTH = "ratio_width";
    private static final String INSTANCE_RATIO_HEIGHT = "ration_height";
    private static final String INSTANCE_MASK_COLOR = "mask_color";
    private static final String INSTANCE_APPLY_TRANSFORMATIONS_TO_PLACEHOLDER = "apply_transformations_to_placeholder";

    private int mPlaceholderImage, mErrorImage;

    private boolean mCircle;
    private int mCircleBorderWidth;
    private ColorStateList mCircleBorderColor = ColorStateList.valueOf(RoundedDrawable.DEFAULT_BORDER_COLOR);
    private int mBlurRadius;
    private int mCornerRadius;
    private int mMaskColor;
    private RoundedCornersTransformation.CornerType mCornerType;
    protected String mUrl;
    protected Target target;
    protected boolean mIsAttached;
    private int mRatioWidth;
    private int mRatioHeight;
    //circle
    private boolean mutateBackground = false;
    private Drawable mDrawable;
    private Drawable mBackgroundDrawable;
    private int mResource = 0;

    private ScaleType mScaleType = ScaleType.FIT_CENTER;
    private AsyncTask mBlurTask;

    private static final ScaleType[] SCALE_TYPES = {
            ScaleType.MATRIX,
            ScaleType.FIT_XY,
            ScaleType.FIT_START,
            ScaleType.FIT_CENTER,
            ScaleType.FIT_END,
            ScaleType.CENTER,
            ScaleType.CENTER_CROP,
            ScaleType.CENTER_INSIDE
    };
    private boolean mApplyTransformationsToPlaceholder;
    private DefaultPlaceHolder mDefaultPlaceHolder;

    public NetworkImageView(Context context) {
        super(context);
    }

    public NetworkImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public NetworkImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public NetworkImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.NetworkImageView);

        mPlaceholderImage = a.getResourceId(R.styleable.NetworkImageView_uikit_niv_placeholder, R.drawable.default_image);
        //这段代码要在mUrl设置值之前,因为setImageResource方法会把mUrl清空
        if (mPlaceholderImage != R.drawable.default_image) {
            setImageResource(mPlaceholderImage);
        }

        mErrorImage = a.getResourceId(R.styleable.NetworkImageView_uikit_niv_error, 0);
        mUrl = a.getString(R.styleable.NetworkImageView_uikit_niv_url);
        setCircle(a.getBoolean(R.styleable.NetworkImageView_uikit_niv_circle, false));
        mBlurRadius = a.getInt(R.styleable.NetworkImageView_uikit_niv_blur_radius, 0);
        mCornerRadius = a.getDimensionPixelSize(R.styleable.NetworkImageView_uikit_niv_corner_radius, 0);
        int cornerTypeValue = a.getInt(R.styleable.NetworkImageView_uikit_niv_corner_type, 0);
        mCornerType = map2CornerType(cornerTypeValue);
        mRatioWidth = a.getInteger(R.styleable.NetworkImageView_uikit_niv_ratio_width, 0);
        mRatioHeight = a.getInteger(R.styleable.NetworkImageView_uikit_niv_ratio_height, 0);
        mCircleBorderColor = a.getColorStateList(R.styleable.NetworkImageView_uikit_niv_circle_border_color);
        mCircleBorderWidth = a.getDimensionPixelSize(R.styleable.NetworkImageView_uikit_niv_circle_border_width, 0);
//        mApplyTransformationsToPlaceholder = a.getBoolean(R.styleable.NetworkImageView_uikit_niv_apply_transformations_to_placeholder, false);
        mMaskColor = a.getColor(R.styleable.NetworkImageView_uikit_niv_mask_color, Color.TRANSPARENT);

        int index = a.getInt(R.styleable.NetworkImageView_android_scaleType, -1);
        if (index >= 0) {
            setScaleType(SCALE_TYPES[index]);
        } else {
            // default scaletype to FIT_CENTER
            setScaleType(ScaleType.FIT_CENTER);
        }

//        updateDrawableAttrs4Circle();
//        updateBackgroundDrawableAttrs4Circle(true);

        mDefaultPlaceHolder = new DefaultPlaceHolder(this);

        a.recycle();
    }

    /**
     * Return the current scale type in use by this ImageView.
     *
     * @return ScaleType
     * @attr ref android.R.styleable#ImageView_scaleType
     * @see ScaleType
     */
    @Override
    public ScaleType getScaleType() {
        return mScaleType;
    }

    private RoundedCornersTransformation.CornerType map2CornerType(int cornerTypeValue) {
        for (RoundedCornersTransformation.CornerType cornerType : RoundedCornersTransformation.CornerType.values()) {
            if (cornerType.ordinal() == cornerTypeValue) {
                return cornerType;
            }
        }
        return RoundedCornersTransformation.CornerType.ALL;
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        Parcelable parcelable = super.onSaveInstanceState();
        bundle.putParcelable(INSTANCE_NETWORK_IMAGEVIEW, parcelable);
        bundle.putInt(INSTANCE_PLACEHOLDER, mPlaceholderImage);
        bundle.putInt(INSTANCE_ERROR, mErrorImage);
        bundle.putBoolean(INSTANCE_IS_CIRCLE, mCircle);
//        bundle.putBoolean(INSTANCE_APPLY_TRANSFORMATIONS_TO_PLACEHOLDER, mApplyTransformationsToPlaceholder);
        bundle.putInt(INSTANCE_CIRCLE_BORDER_WIDTH, mCircleBorderWidth);
        bundle.putInt(INSTANCE_BLUR_RADIUS, mBlurRadius);
        bundle.putInt(INSTANCE_CORNER_RADIUS, mCornerRadius);
        bundle.putInt(INSTANCE_CORNER_TYPE, mCornerType.ordinal());
        bundle.putInt(INSTANCE_RATIO_WIDTH, mRatioWidth);
        bundle.putInt(INSTANCE_RATIO_HEIGHT, mRatioHeight);
        bundle.putInt(INSTANCE_MASK_COLOR, mMaskColor);
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            state = bundle.getParcelable(INSTANCE_NETWORK_IMAGEVIEW);
            mPlaceholderImage = bundle.getInt(INSTANCE_PLACEHOLDER);
            mErrorImage = bundle.getInt(INSTANCE_ERROR);
            mCircle = bundle.getBoolean(INSTANCE_IS_CIRCLE);
//            mApplyTransformationsToPlaceholder = bundle.getBoolean(INSTANCE_APPLY_TRANSFORMATIONS_TO_PLACEHOLDER);
            mCircleBorderWidth = bundle.getInt(INSTANCE_CIRCLE_BORDER_WIDTH);
            mBlurRadius = bundle.getInt(INSTANCE_BLUR_RADIUS);
            mCornerRadius = bundle.getInt(INSTANCE_CORNER_RADIUS);
            int cornerTypeValue = bundle.getInt(INSTANCE_CORNER_TYPE, 0);
            mCornerType = map2CornerType(cornerTypeValue);
            mRatioWidth = bundle.getInt(INSTANCE_RATIO_WIDTH, 1);
            mRatioHeight = bundle.getInt(INSTANCE_RATIO_HEIGHT, 1);
            mMaskColor = bundle.getInt(INSTANCE_MASK_COLOR, Color.TRANSPARENT);
        }
        super.onRestoreInstanceState(state);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mRatioHeight <= 0 || mRatioWidth <= 0) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        if (needRatio()) {
            int mode = MeasureSpec.getMode(widthMeasureSpec);

            int childWidthSize = MeasureSpec.getSize(widthMeasureSpec);
            if (mode != MeasureSpec.UNSPECIFIED) {
                heightMeasureSpec = MeasureSpec.makeMeasureSpec(childWidthSize * mRatioHeight / mRatioWidth, MeasureSpec.EXACTLY);
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public boolean needRatio() {
        return true;
    }

    public int getErrorImage() {
        return mErrorImage;
    }

    public void setErrorImage(int errorImage) {
        mErrorImage = errorImage;
    }

    public int getPlaceholderImage() {
        return mPlaceholderImage;
    }

    /**
     * @param placeholderImage 注意,setPlaceholderImage要在setUrl之前调用
     */
    public void setPlaceholderImage(int placeholderImage) {
        mPlaceholderImage = placeholderImage;
    }

    public boolean isCircle() {
        return mCircle;
    }

    public void setCircle(boolean circle) {
        mCircle = circle;
        //圆形图片，必须指定NetworkImageView的宽高
        update4CircleRadius();
    }

    public int getBlurRadius() {
        return mBlurRadius;
    }

    public void setBlurRadius(int blurRadius) {
        mBlurRadius = blurRadius;
    }

    public int getCornerRadius() {
        return mCornerRadius;
    }

    public void setCornerRadius(int cornerRadius) {
        mCornerRadius = cornerRadius;
        update4CircleRadius();
    }

    private void update4CircleRadius() {
        if (isRoundOrCorner()) {
            updateDrawableAttrs4Circle();
            updateBackgroundDrawableAttrs4Circle(false);
            invalidate();
        }
    }

    public RoundedCornersTransformation.CornerType getCornerType() {
        return mCornerType;
    }

    public void setCornerType(RoundedCornersTransformation.CornerType cornerType) {
        mCornerType = cornerType;
    }

    public void setUrl(String url) {
        //显示默认图,处理list中,convertview重新设置placeholder,并且url为空的场景
        if (TextUtils.isEmpty(url) && mPlaceholderImage > 0) {
            setImageResource(mPlaceholderImage);
        }
        //url不变,就不再重新加载,避免出现闪的现象
        if (!TextUtils.isEmpty(url)
                && url.equals(mUrl)
                && getDrawable() != null) {
            return;
        }
        //调用Glide加载图片之前,先显示默认图
        if (mPlaceholderImage > 0) {
            setImageResource(mPlaceholderImage);
        }
        mUrl = url;
        loadImage(true);
    }

    /**
     * 设置图片路径,即使url不变也重新加载
     *
     * @param url 图片路径
     */
    public void forceSetUrl(String url) {
        //调用Glide加载图片之前,先显示默认图
        if (mPlaceholderImage > 0) {
            setImageResource(mPlaceholderImage);
        }
        mUrl = url;
        loadImage(false);
    }

    public int getCircleBorderWidth() {
        return mCircleBorderWidth;
    }

    public void setCircleBorderColor(ColorStateList circleBorderColor) {
        mCircleBorderColor = circleBorderColor;
        invalidate();
    }

    public void setCircleBorderWidth(int circleBorderWidth) {
        mCircleBorderWidth = circleBorderWidth;
    }

    public int getMaskColor() {
        return mMaskColor;
    }

    public void setMaskColor(int maskColor) {
        mMaskColor = maskColor;
    }

    public int getRatioWidth() {
        return mRatioWidth;
    }

    public void setRatioWidth(int ratioWidth) {
        mRatioWidth = ratioWidth;
    }

    public int getRatioHeight() {
        return mRatioHeight;
    }

    public void setRatioHeight(int ratioHeight) {
        mRatioHeight = ratioHeight;
    }

    private void loadImage(boolean isUseCache) {
        if (isInEditMode()) {
            return;
        }
        cancelRequest();

        if (mIsAttached) {
            doLoad(isUseCache);
        }

        //取消异步加载,会导致全屏measure且刷新,造成卡顿,原因未知
//        post(new Runnable() {
//            @Override
//            public void run() {
//                if (mIsAttached) {
//                    doLoad();
//                }
//            }
//        });
    }

    protected void reloadImage() {
        loadImage(true);
    }

    protected void doLoad(boolean isUseCache) {
        if (!TextUtils.isEmpty(mUrl)) {

            Context context = getContext();
            if (isDestroyed(context)) {
                return;
            }
            if (mUrl.startsWith("/")) {
                target = loadImageFromFile(new File(mUrl), isUseCache);
            } else if (mUrl.startsWith(ContentResolver.SCHEME_FILE + "://")) {
                String path = Uri.parse(mUrl).getPath();
                target = loadImageFromFile(new File(path), isUseCache);
            } else {
                target = loadImageFromUrl();
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    static boolean isDestroyed(Context context) {
        if (context instanceof Activity) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1
                    && ((Activity) context).isDestroyed()) {
                return true;
            }
        }
        return false;
    }

    protected Target loadImageFromFile(File file, boolean isUseCache) {
        BitmapRequestBuilder<File, Bitmap> builder = Glide.with(getContext())
                .load(file)
                .asBitmap()
                .error(mErrorImage);

        if (!isUseCache) {
            builder.skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE);
        }

        return (Target) builder.into(this);
    }

    protected Target loadImageFromUrl() {
        CropModel cropModel = new CropModel();
        cropModel.url = mUrl;
        cropModel.crop = true;
        cropModel.scaleType = getScaleType();

        Target target = Glide.with(getContext())
                .load(cropModel)
                .asBitmap()
                .animate(FADE_IN_ANIMATOR)
                .error(mErrorImage)
                .into(this);

        return target;
    }

    // add view
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mIsAttached = true;
        reloadImage();
        // GlideRecycledHelper.getInstance().detachView(this);
    }

    // remove view
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mIsAttached = false;
        cancelRequest();
        GlideRecycledHelper.getInstance().detachView(this);
    }

    // list view reuse view
    @Override
    public void onFinishTemporaryDetach() {
        super.onFinishTemporaryDetach();
        GlideRecycledHelper.getInstance().detachView(this);
    }

    // list view item no display
    @Override
    public void onStartTemporaryDetach() {
        super.onStartTemporaryDetach();
        GlideRecycledHelper.getInstance().attachView(this);
    }

    /**
     * cancelRequest
     */
    protected void cancelRequest() {
        if (target != null) {
            Glide.clear(target);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mDefaultPlaceHolder != null) {
            mDefaultPlaceHolder.draw(canvas);
        }
        super.onDraw(canvas);
        if (mMaskColor != Color.TRANSPARENT) {
            canvas.drawColor(mMaskColor);
        }
    }

    private boolean isRoundOrCorner() {
        return mCircle || mCornerRadius > 0;
    }

    void superSetImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        if (drawable == null) {
            mDrawable = drawable;
            super.setImageDrawable(drawable);
            return;
        }

        if (isRoundOrCorner()) {
            mResource = 0;
            if (mBlurRadius > 0) {
                blur(drawable);
            } else {
                mDrawable = RoundedDrawable.fromDrawable(drawable);
                updateDrawableAttrs4Circle();
                super.setImageDrawable(mDrawable);
            }
        } else {
            if (mBlurRadius > 0 && (drawable instanceof RoundedDrawable || drawable instanceof BitmapDrawable)) {
                blur(drawable);
            } else {
                super.setImageDrawable(drawable);
            }
        }
    }

    private void blur(Drawable drawable) {
        Bitmap bitmap = null;
        if (drawable instanceof RoundedDrawable) {
            bitmap = ((RoundedDrawable) drawable).toBitmap();
            if (bitmap == null) {
                return;
            }
            bitmap = zoomoutBlurImage(bitmap);
            doBlur(bitmap);
        } else if (drawable instanceof BitmapDrawable) {
            bitmap = ((BitmapDrawable) drawable).getBitmap();
            if (bitmap == null) {
                return;
            }
            bitmap = zoomoutBlurImage(bitmap);
            doBlur4BitmapDrawable(bitmap);
        } else {
            blur(RoundedDrawable.fromDrawable(drawable));
        }
    }

    private Bitmap zoomoutBlurImage(Bitmap bitmap) {
        float scaleFactor = 4;
        Bitmap overlay = Bitmap.createBitmap((int) (bitmap.getWidth() / scaleFactor),
                (int) (bitmap.getHeight() / scaleFactor), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(overlay);
        canvas.scale(1 / scaleFactor, 1 / scaleFactor);
        Paint paint = new Paint();
        paint.setFlags(Paint.FILTER_BITMAP_FLAG);
        canvas.drawBitmap(bitmap, 0, 0, paint);

        return overlay;
    }

    private void doBlur(final Bitmap bitmap) {
        if (mBlurTask != null) {
            mBlurTask.cancel(true);
        }
        mBlurTask = new BlurTask(bitmap, this, mBlurRadius);
        mBlurTask.execute();
    }

    private void doBlur4BitmapDrawable(final Bitmap bitmap) {
        if (mBlurTask != null) {
            mBlurTask.cancel(true);
        }
        mBlurTask = new BitmapDrawableBlurTask(bitmap, this, mBlurRadius);
        mBlurTask.execute();
    }

    @Override
    public void setImageResource(int resId) {
        mUrl = null;
        if (mResource != resId) {
            mResource = resId;
            mDrawable = resolveResource();
            setImageDrawable(mDrawable);
        } else {
            super.setImageResource(resId);
        }
    }

    private Drawable resolveResource() {
        Resources rsrc = getResources();
        if (rsrc == null) {
            return null;
        }
        Drawable d = null;
        if (mResource != 0) {
            try {
                d = rsrc.getDrawable(mResource);
            } catch (Throwable e) {
                mResource = 0;
            }
        }
        return RoundedDrawable.fromDrawable(d);
    }

    @Override
    public void setImageURI(Uri uri) {
        super.setImageURI(uri);
        setImageDrawable(getDrawable());
    }

    @Override
    public void setBackground(Drawable background) {
        setBackgroundDrawable(background);
    }

    @Override
    @Deprecated
    public void setBackgroundDrawable(Drawable background) {
        if (isRoundOrCorner()) {
            mBackgroundDrawable = background;
            updateBackgroundDrawableAttrs4Circle(true);
        }
        super.setBackgroundDrawable(mBackgroundDrawable);
    }

    @Override
    public void setScaleType(ScaleType scaleType) {
        assert scaleType != null;
        if (isRoundOrCorner()) {
            if (mScaleType != scaleType) {
                mScaleType = scaleType;
                switch (scaleType) {
                    case CENTER:
                    case CENTER_CROP:
                    case CENTER_INSIDE:
                    case FIT_CENTER:
                    case FIT_START:
                    case FIT_END:
                    case FIT_XY:
                        super.setScaleType(ScaleType.FIT_XY);
                        break;
                    default:
                        super.setScaleType(scaleType);
                        break;
                }
                updateDrawableAttrs4Circle();
                updateBackgroundDrawableAttrs4Circle(false);
                invalidate();
            }
        } else {
            mScaleType = scaleType;

            super.setScaleType(scaleType);
        }
    }

    private void updateDrawableAttrs4Circle() {
        updateAttrs4Circle(mDrawable);
    }

    private void updateBackgroundDrawableAttrs4Circle(boolean convert) {
        if (mutateBackground) {
            if (convert) {
                mBackgroundDrawable = RoundedDrawable.fromDrawable(mBackgroundDrawable);
            }
            updateAttrs4Circle(mBackgroundDrawable);
        }
    }

    private void updateAttrs4Circle(Drawable drawable) {
        if (drawable == null) {
            return;
        }
        if (drawable instanceof RoundedDrawable) {
            ((RoundedDrawable) drawable)
                    .setCornerRadius(mCornerRadius == 0 ? getHeight() / 2 : mCornerRadius)
                    .setBorderWidth(mCircleBorderWidth)
                    //保证scaleType在cornerRadius之后设置，因为会根据是否要做圆角，进行一些特殊处理
                    .setScaleType(getScaleType())
                    .setBorderColor(mCircleBorderColor)
                    .setOval(mCircle);
        } else if (drawable instanceof LayerDrawable) {
            // loop through layers to and set drawable attrs
            LayerDrawable ld = ((LayerDrawable) drawable);
            for (int i = 0, layers = ld.getNumberOfLayers(); i < layers; i++) {
                updateAttrs4Circle(ld.getDrawable(i));
            }
        }
    }

    public static class CropModel {
        public String url;
        public boolean crop;
        public ScaleType scaleType;
    }

    private final ViewPropertyAnimation.Animator FADE_IN_ANIMATOR = new ViewPropertyAnimation.Animator() {
        @Override
        public void animate(View view) {
            // if it's a custom view class, cast it here
            // then find subviews and do the animations
            // here, we just use the entire view for the fade animation
            view.setAlpha(0f);

            ObjectAnimator fadeAnim = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
            fadeAnim.setDuration(250);
            fadeAnim.start();
        }
    };
}