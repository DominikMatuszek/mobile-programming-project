package com.example.app;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;

public class DrawableConverter {
    private DrawableConverter() {
    }

    // From https://stackoverflow.com/questions/50077917/android-graphics-drawable-adaptiveicondrawable-cannot-be-cast-to-android-graphic
    // by Shashank Holla; CC BY-SA 4.0
    @NonNull
    public static Bitmap getBitmapFromDrawable(@NonNull Drawable drawable) {
        final Bitmap bmp = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bmp);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bmp;
    }
}
