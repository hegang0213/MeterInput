package com.bdwater.meterinput.base;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;

/**
 * Created by hegang on 16/6/29.
 */
public class DrawableUtils {
    public static Drawable getIcon(Context context, int drawableId) {

        float px = 24 * context.getResources().getDisplayMetrics().density;
        int size = (int)px;
        Drawable dr = ContextCompat.getDrawable(context, drawableId);
        Bitmap bitmap = ((BitmapDrawable) dr).getBitmap();
        Drawable d = new BitmapDrawable(context.getResources(),
                Bitmap.createScaledBitmap(bitmap, size, size, true));
        return d;
    }
}
