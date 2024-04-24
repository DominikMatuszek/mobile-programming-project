package com.example.app;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

public class CustomMarkerOverlay extends MyLocationNewOverlay {

    public CustomMarkerOverlay(Bitmap bmp, IMyLocationProvider provider, MapView mapView) {
        super(provider, mapView);

        mPersonBitmap = bmp;
    }

    public CustomMarkerOverlay(Drawable drawable, IMyLocationProvider provider, MapView mapView) {
        super(provider, mapView);
        mPersonBitmap = DrawableConverter.getBitmapFromDrawable(drawable);
    }

}
