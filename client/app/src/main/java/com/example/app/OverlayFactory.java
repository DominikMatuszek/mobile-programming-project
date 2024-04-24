package com.example.app;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.location.Location;

import androidx.core.content.res.ResourcesCompat;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.function.Supplier;

public class OverlayFactory {
    private OverlayFactory() {
    }

    public static MyLocationNewOverlay createLocationOverlay(Supplier<Location> supp, MapView mapView) {
        MyLocationNewOverlay overlay = new MyLocationNewOverlay(new PrimitiveLocationProvider(supp), mapView);
        overlay.enableMyLocation();

        return overlay;
    }

    public static CustomMarkerOverlay createGoalOverlay(Resources res, Location location, MapView mapView) {
        Supplier<Location> supp = () -> location;
        Drawable drawable = ResourcesCompat.getDrawable(res, R.mipmap.ic_launcher, null);
        CustomMarkerOverlay overlay = new CustomMarkerOverlay(drawable, new PrimitiveLocationProvider(supp), mapView);
        overlay.enableMyLocation();

        return overlay;
    }
}
