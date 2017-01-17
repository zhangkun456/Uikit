package com.my.test.uikit.demo;

import android.content.Context;
import android.text.TextUtils;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.load.engine.bitmap_recycle.LruBitmapPool;
import com.bumptech.glide.load.engine.cache.DiskLruCacheFactory;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.load.model.GenericLoaderFactory;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.stream.BaseGlideUrlLoader;
import com.bumptech.glide.module.GlideModule;
import com.my.uikit.image.FaceUrlParser;
import com.my.uikit.image.NetworkImageView;

import java.io.InputStream;


/**
 * Created by brucelee on 2/19/16.
 */
public class TTGlideModule implements GlideModule {

    private static final int ROUND_PACE = 5; //px
    private static final int MAX_IMAGE_SIDE_WIDTH = 4096; // px
    private static final String HOST_SUPPORT_CROP = "http://3p.pic.ttdtweb.com";
    private static final String HOST_XIAMI_SUPPORT_CROP = "http://pic.xiami.net";

    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        long memorySize = Runtime.getRuntime().maxMemory();
        builder.setMemoryCache(new LruResourceCache((int) (memorySize / 16)));
        builder.setBitmapPool(new LruBitmapPool((int) (memorySize / 16)));
        String imageCachePath = "/sdcard/mytest/image_cache";
        //100MB图片缓存
        builder.setDiskCache(new DiskLruCacheFactory(imageCachePath, 100 * 1024 * 1024));
    }

    @Override
    public void registerComponents(Context context, Glide glide) {
        glide.register(NetworkImageView.CropModel.class, InputStream.class,
                new Factory());
    }

    public static class MyUrlLoader extends BaseGlideUrlLoader<NetworkImageView.CropModel> {
        public MyUrlLoader(Context context) {
            super(context);
        }

        @Override
        protected String getUrl(NetworkImageView.CropModel cropModel, int width, int height) {
            final String cropUrl = cropModel.url;

            //多次添加@会导致图片打不开
            if (cropUrl != null && cropUrl.contains("@")) {
                return cropUrl;
            }

            //face url just for center crop.
            if (cropModel.scaleType == ImageView.ScaleType.CENTER_CROP) {
                String faceUrl = FaceUrlParser.convertToFaceUrl(cropUrl, width, height);
                if (cropUrl != faceUrl) {
                    return faceUrl;
                }
            }

            //Construct the url for the correct size here.
            return buildCropUrl(cropUrl, width, height, cropModel.scaleType);
        }
    }

    static class Factory implements ModelLoaderFactory<NetworkImageView.CropModel, InputStream> {

        @Override
        public ModelLoader<NetworkImageView.CropModel, InputStream> build(Context context, GenericLoaderFactory factories) {
            return new MyUrlLoader(context);
        }

        @Override
        public void teardown() {

        }
    }

    /**
     * 生产裁剪的图片Url
     *
     * @param url       源url
     * @param width     目标图片宽度
     * @param height    目标图片高度
     * @param scaleType 视图拉伸类型
     * @return 拉伸裁剪后的图片大小
     */
    private static String buildCropUrl(String url, int width, int height, ImageView.ScaleType scaleType) {
        if (!TextUtils.isEmpty(url) && (url.startsWith(HOST_SUPPORT_CROP) || url.startsWith(HOST_XIAMI_SUPPORT_CROP))
                && width > 0 && height > 0 && width <= MAX_IMAGE_SIDE_WIDTH && height <= MAX_IMAGE_SIDE_WIDTH) {
            final String host = getHost(url);
            final String params = url.substring(host.length());
            width = roundUp(width);
            height = roundUp(height);

            String suffix = "1x" + "." + getFileExtension(url).toLowerCase();
            String cropParam = "";
            switch (scaleType) {
                // {1e} 是按短边拉伸{0e}是按长边拉伸  (缩放优先边)
                // {1i} 是固定高宽, 与c配合，是固定高宽的非缩放裁剪
                // {1c} 是裁剪
                // 具体参见  <阿里云 OSS 图片处理服务.PDF>
                case FIT_XY://按短边拉伸
                case CENTER_CROP:
                case CENTER_INSIDE:
                    cropParam += width + "w_" + height + "h_" + "1c_" + "1e_";
                    break;
                case FIT_CENTER://按长边拉伸
                    cropParam += width + "w_" + height + "h_" + "1c_" + "0e_";
                    break;
                case CENTER:
                    cropParam += width + "w_" + height + "h_" + "1c_" + "1i_";
                    break;
                default:
                    break;
            }
            if (!TextUtils.isEmpty(cropParam)) {
                url = host + "@" + cropParam + suffix;
            }
            url += params;
        }
        return url;
    }

    public static String getHost(String url) {
        return url != null && url.indexOf(63) > 0 ? url.substring(0, url.indexOf(63)) : url;
    }

    public static String getFileExtension(String path) {
        if (path != null) {
            int query = path.lastIndexOf(63);
            if (query > 0) {
                path = path.substring(0, query);
            }

            int filenamePos = path.lastIndexOf(47);
            String filename = filenamePos >= 0 ? path.substring(filenamePos + 1) : path;
            if (filename.length() > 0) {
                int dotPos = filename.lastIndexOf(46);
                if (0 <= dotPos) {
                    return filename.substring(dotPos + 1);
                }
            }
        }

        return "";
    }

    private static int roundUp(int src) {
        return (src + (ROUND_PACE >> 1)) / ROUND_PACE * ROUND_PACE;
    }
}
