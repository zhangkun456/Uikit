package com.my.uikit.image;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.util.Patterns.WEB_URL;

/**
 * Created by jiangtao on 08/12/2016.
 *
 * @author jiang.tao
 * @version 1.0.0
 *          <p>
 *          <p>
 *          http://pic.xiami.net/images/trade/ams_banner/108/584905e6e1a00_5447628_d92b63e96f164ac66bf52ef32d714006_face_w800h778_x149y67w317h88.jpg
 *          这个后面的 face_w800h778_x149y67w317h88.jpg 标识
 *          face有人脸 w800h778标识图片原有大小800x778，
 *          x149y67w317h88 标识人脸位置在左上(149, 67）
 *          往右 317个像素 往下88个像素的区域
 *          <p>
 *          http://pic.xiami.net/images/trade/ams_banner/108/584905e6e1a00_5447628_d92b63e96f164ac66bf52ef32d714006_face_w800h778_x149y67w317h88.jpg@149-67-317-88a
 *          这样子就只把识别出的人脸裁出来了
 */
public class FaceUrlParser {

    private static final Pattern FACE_URL = Pattern.compile("_face_w(\\d+)h(\\d+)_x(\\d+)y(\\d+)w(\\d+)h(\\d+)");
    private static final Pattern NUMBER = Pattern.compile("\\d+");
    private static final int MAX_CROP = 4096;

    public static String convertToFaceUrl(String imageUrl, int viewWidth, int viewHeight) {

        if (imageUrl == null || imageUrl.length() == 0 || imageUrl.contains("@")) {
            return imageUrl;
        }

        // 校验是不是web url
        if (!WEB_URL.matcher(imageUrl).matches()) {
            return imageUrl;
        }

        // 校验url中是否有对应的face url格式
        Matcher faceInformationMatcher = FACE_URL.matcher(imageUrl);
        if (!faceInformationMatcher.find()) {
            return imageUrl;
        }

        // 获取图片头像信息
        String faceInformation = faceInformationMatcher.group();

        // 获取图片头像详细信息
        Matcher faceDetailInformationMatcher = NUMBER.matcher(faceInformation);

        int imageWidth = parseNumber(faceDetailInformationMatcher);
        int imageHeight = parseNumber(faceDetailInformationMatcher);
        int faceX = parseNumber(faceDetailInformationMatcher);
        int faceY = parseNumber(faceDetailInformationMatcher);
        int faceWidth = parseNumber(faceDetailInformationMatcher);
        int faceHeight = parseNumber(faceDetailInformationMatcher);

        // 校验图片头像详细信息
        if (imageWidth <= 0
                || imageHeight <= 0
                || faceX < 0
                || faceY < 0
                || faceWidth <= 0
                || faceHeight <= 0) {
            return imageUrl;
        }

        // scale view width height
        float scale;
        // float dx = 0, dy = 0;
        if (imageWidth * viewHeight > viewWidth * imageHeight) {
            scale = (float) viewHeight / (float) imageHeight;
            // dx = (viewWidth - imageWidth * scale) * 0.5f;
        } else {
            scale = (float) viewWidth / (float) imageWidth;
            // dy = (viewHeight - imageHeight * scale) * 0.5f;
        }

        //
        int cropWidth = Math.min((int) (viewWidth / scale), imageWidth);
        int cropHeight = Math.min((int) (viewHeight / scale), imageHeight);

        if (cropWidth > MAX_CROP) {
            cropHeight = (int) (((float) MAX_CROP / cropWidth) * cropHeight);
            cropWidth = MAX_CROP;
        }

        if (cropHeight > MAX_CROP) {
            cropWidth = (int) (((float) MAX_CROP / cropHeight) * cropWidth);
            cropHeight = MAX_CROP;
        }

        int gap;
        gap = (cropWidth - faceWidth) / 2;
        int left = faceX - gap;
        int right = faceX + faceWidth + gap;
        int x = right <= imageWidth ? Math.max(left, 0) : imageWidth - cropWidth;

        gap = (cropHeight - faceHeight) / 2;
        int top = faceY - gap;
        int bottom = faceY + faceHeight + gap;
        int y = bottom <= imageHeight ? Math.max(top, 0) : imageHeight - cropHeight;

        // check crop parameter
        if (x < 0
                || y < 0
                || cropWidth <= 0
                || cropHeight <= 0
                || viewWidth <= 0
                || viewHeight <= 0) {
            return imageUrl;
        }

        // 创建新的image url builder
        return new StringBuilder(imageUrl).append('@')
                .append(viewWidth)
                .append('w')
                .append('_')
                .append(viewHeight)
                .append('h')
                .append('_')
                .append(x)
                .append('-')
                .append(y)
                .append('-')
                .append(cropWidth)
                .append('-')
                .append(cropHeight)
                .append('a').toString();
    }

    static int parseNumber(Matcher matcher) {
        if (matcher.find()) {
            return Integer.valueOf(matcher.group());
        }
        return -1;
    }
}
