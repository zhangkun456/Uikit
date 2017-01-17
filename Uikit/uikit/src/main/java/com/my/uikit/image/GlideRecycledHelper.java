package com.my.uikit.image;

import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.bumptech.glide.Glide;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Created by jiangtao on 16/5/15.
 *
 * @author jiang.tao
 * @version 1.0.0
 */
public class GlideRecycledHelper {

    private LinkedList<WeakReference<View>> mRecycledViews = new LinkedList<>();

    private static GlideRecycledHelper sInstance = new GlideRecycledHelper();

    /**
     * GlideRecycledHelper
     *
     * @return GlideRecycledHelper instance
     */
    public static GlideRecycledHelper getInstance() {
        return sInstance;
    }

    private GlideRecycledHelper() {
    }

    /**
     * attachView
     *
     * @param view view
     */
    public void attachView(View view) {
        if (view == null) {
            return;
        }

        if (checkThread()) {
            return;
        }

        if (!findView(view)) {
            mRecycledViews.add(new WeakReference<View>(view));
        }
    }

    /**
     * detachView
     *
     * @param view view
     */
    public void detachView(View view) {
        if (view == null) {
            return;
        }

        if (checkThread()) {
            return;
        }

        removeView(view);
    }

    private boolean findView(View view) {
        Iterator<WeakReference<View>> iterator = mRecycledViews.iterator();
        while (iterator.hasNext()) {
            WeakReference<View> weakView = iterator.next();
            View v = weakView.get();
            if (v == null) {
                iterator.remove();
            } else if (v == view) {
                return true;
            }
        }
        return false;
    }

    private void removeView(View view) {
        Iterator<WeakReference<View>> iterator = mRecycledViews.iterator();
        while (iterator.hasNext()) {
            WeakReference<View> weakView = iterator.next();
            View v = weakView.get();
            if (v == null) {
                iterator.remove();
            } else if (v == view) {
                iterator.remove();
            }
        }
    }

    /**
     * clear recycled view memory
     */
    public void clearMemory() {
        if (checkThread()) {
            return;
        }
        try {
            Iterator<WeakReference<View>> iterator = mRecycledViews.iterator();
            while (iterator.hasNext()) {
                WeakReference<View> weakView = iterator.next();
                View v = weakView.get();
                if (v == null) {
                    iterator.remove();
                } else {
                    clear(v);
                }
            }
        } catch (StackOverflowError error) {
        }
    }

    /**
     * clear view reference to glide request
     *
     * @param view view
     */
    public static void clear(View view) {
        if (checkThread()) {
            return;
        }
        try {
            clearInternal(view);
        } catch (StackOverflowError e) {
        }
    }

    private static void clearInternal(View view) {
        if (view == null) {
            return;
        }

        if (view instanceof NetworkImageView) {
            try {
                Glide.clear(view);
            } catch (Exception e) {
                e.printStackTrace();
            }
            ((ImageView) view).setImageDrawable(null);
            return;
        }

        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            int childCount = viewGroup.getChildCount();
            for (int i = 0; i < childCount; i++) {
                clearInternal(viewGroup.getChildAt(i));
            }
        }
    }

    private static boolean checkThread() {
        if (Looper.getMainLooper().getThread() != Thread.currentThread()) {
            return true;
        }
        return false;
    }
}
