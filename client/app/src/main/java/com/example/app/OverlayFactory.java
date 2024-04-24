package com.example.app;

import android.location.Location;

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
}
