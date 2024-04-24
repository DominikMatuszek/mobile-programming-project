package com.example.app;

import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.app.databinding.FragmentGameMapBinding;
import com.example.app.server_wrapper.Client;
import com.example.app.server_wrapper.TargetState;

import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class GameMapFragment extends Fragment {

    private FragmentGameMapBinding binding;
    private MapView mapView;
    private MainActivity mainActivity;
    private Timer locationUpdateTimer;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentGameMapBinding.inflate(inflater, container, false);

        MapTileProviderBasic tileProvider = new MapTileProviderBasic(inflater.getContext());
        mapView = new MapView(inflater.getContext(), tileProvider, null);

        mainActivity = (MainActivity) getActivity();

        return mapView;

    }

    private List<Overlay> getGoalsOverlays() {
        List<Overlay> goalOverlays = new ArrayList<>();

        Client client = new Client(
                mainActivity.getString("username"),
                mainActivity.getString("password")
        );

        System.out.println("Setting goals");

        List<TargetState> goals = client.getMatchState();

        for (TargetState goal : goals) {
            System.out.println("Goal: " + goal.getLat() + ", " + goal.getLon() + " by " + goal.getScorer());

            Location loc = new Location("");
            loc.setLatitude(goal.getLat());
            loc.setLongitude(goal.getLon());

            OverlayFactory.GoalType type = null;

            // sorry
            if (goal.getScorer().equals(mainActivity.getString("username"))) {
                type = OverlayFactory.GoalType.FRIENDLY;
            } else if (goal.getScorer() == null) {
                type = OverlayFactory.GoalType.NEUTRAL;
            } else {
                type = OverlayFactory.GoalType.ENEMY;
            }

            CustomMarkerOverlay overlay = OverlayFactory.createGoalOverlay(
                    getResources(),
                    type,
                    loc,
                    mapView
            );

            goalOverlays.add(overlay);
        }

        return goalOverlays;
    }

    private MyLocationNewOverlay getCurrentLocationOverlay() {
        return OverlayFactory.createLocationOverlay(
                () -> mainActivity.lastKnownLocation,
                mapView);
    }

    private void startRefreshingMarkers() {
        Timer timer = new Timer();

        TimerTask task = new TimerTask() {
            @Override
            public void run() {

                List<Overlay> newOverlays = getGoalsOverlays();
                newOverlays.add(getCurrentLocationOverlay());

                mainActivity.runOnUiThread(
                        () -> {
                            mapView.getOverlays().clear();
                            mapView.invalidate();
                            mapView.getOverlays().addAll(newOverlays);
                        }

                );

            }
        };

        timer.schedule(task, 0, 5000);
    }

    private void centerMapOnCurrentLocationOncePossible() {
        locationUpdateTimer = new Timer();

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                Location lastKnownLocation = mainActivity.lastKnownLocation;

                if (lastKnownLocation != null) {
                    IMapController controller = mapView.getController();
                    GeoPoint center = new GeoPoint(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());

                    mainActivity.runOnUiThread(
                            () -> {
                                controller.setCenter(center);
                            }
                    );
                    locationUpdateTimer.cancel();
                }

            }
        };

        locationUpdateTimer.schedule(task, 0, 1000);

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mainActivity = (MainActivity) getActivity();

        IMapController controller = mapView.getController();
        controller.setZoom(4.0);

        new Thread(
                () -> {
                    Client client = new Client(
                            mainActivity.getString("username"),
                            mainActivity.getString("password")
                    );

                    while (true) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        Location lastKnownLocation = mainActivity.lastKnownLocation;

                        if (lastKnownLocation != null) {
                            client.reportPosition(
                                    lastKnownLocation.getLongitude(),
                                    lastKnownLocation.getLatitude()
                            );
                        }
                    }
                }
        ).start();

        centerMapOnCurrentLocationOncePossible();
        startRefreshingMarkers();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}