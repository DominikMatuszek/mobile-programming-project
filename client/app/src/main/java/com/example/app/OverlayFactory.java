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

    public static CustomMarkerOverlay createGoalOverlay(Resources res, GoalType type, Location location, MapView mapView) {
        Supplier<Location> supp = () -> location;

        Drawable drawable = null;

        switch (type) {
            case ENEMY:
                drawable = ResourcesCompat.getDrawable(res, R.drawable.enemy_marker, null);
                break;
            case FRIENDLY:
                drawable = ResourcesCompat.getDrawable(res, R.drawable.friendly_marker, null);
                break;
            case NEUTRAL:
                drawable = ResourcesCompat.getDrawable(res, R.drawable.neutral_marker, null);
                break;
        }

        // Drawable drawable = ResourcesCompat.getDrawable(res, R.drawable.ic_launcher_foreground, null);
        CustomMarkerOverlay overlay = new CustomMarkerOverlay(drawable, new PrimitiveLocationProvider(supp), mapView);
        overlay.enableMyLocation();

        return overlay;
    }

    public enum GoalType {
        ENEMY,
        FRIENDLY,
        NEUTRAL
    }
}
