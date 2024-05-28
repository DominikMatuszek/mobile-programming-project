package com.example.app;

import android.location.Location;

import org.osmdroid.views.overlay.mylocation.IMyLocationConsumer;
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider;

import java.util.function.Supplier;

public class PrimitiveLocationProvider implements IMyLocationProvider {
    private final Supplier<Location> supp;

    PrimitiveLocationProvider(Supplier<Location> supp) {
        this.supp = supp;
    }

    @Override
    public boolean startLocationProvider(IMyLocationConsumer myLocationConsumer) {
        return true;
    }

    @Override
    public void stopLocationProvider() {
        
    }

    @Override
    public Location getLastKnownLocation() {
        return supp.get();
    }

    @Override
    public void destroy() {

    }
}
