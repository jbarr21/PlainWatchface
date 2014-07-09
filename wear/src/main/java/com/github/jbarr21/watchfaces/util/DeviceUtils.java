package com.github.jbarr21.watchfaces.util;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Pair;

public class DeviceUtils {

    public static Pair<Float, Float> getScreenDimensPx(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels;
        float dpHeight = displayMetrics.heightPixels;
        return new Pair(dpWidth, dpHeight);
    }
}
