package com.example.app.views;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

public class IconWithText extends LinearLayout {
    public IconWithText(Context context, @NotNull Drawable icon, @NotNull String text, int separatorHeight) {
        super(context);

        this.setOrientation(LinearLayout.VERTICAL);

        ImageView imageView = new ImageView(context);
        imageView.setImageDrawable(icon);
        imageView.setForegroundGravity(Gravity.CENTER_HORIZONTAL);

        Space space = new Space(context);
        space.setMinimumHeight(separatorHeight);
        space.setForegroundGravity(Gravity.CENTER_HORIZONTAL);

        TextView textView = new TextView(context);
        textView.setText(text);
        textView.setGravity(Gravity.CENTER_HORIZONTAL);

        this.addView(imageView);
        this.addView(space);
        this.addView(textView);
    }
}
