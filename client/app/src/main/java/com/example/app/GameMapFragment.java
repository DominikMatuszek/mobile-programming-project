package com.example.app;

import android.app.AlertDialog;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

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
    private Timer gameUpdateTimer;
    private Timer serverUpdateTimer;
    private List<TargetState> targets;

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

    private void notifyIfSomeoneHasWon() {
        Client client = new Client(
                mainActivity.getString("username"),
                mainActivity.getString("password")
        );

        String winner = client.getWinner();

        if (winner == null) {
            return;
        }

        System.out.println("Winner is: " + winner);

        if (winner.equals(mainActivity.getString("username"))) {
            mainActivity.runOnUiThread(
                    () -> NavHostFragment.findNavController(GameMapFragment.this).navigate(R.id.action_gameMapFragment_to_winnerFragment)
            );
        } else {
            mainActivity.runOnUiThread(
                    () -> NavHostFragment.findNavController(GameMapFragment.this).navigate(R.id.action_gameMapFragment_to_loserFragment)
            );
        }
    }

    private void notifyIfSomebodyScored(List<TargetState> earlier, List<TargetState> later) {
        // We assume that server always sends goals in the same order.
        // That *should* be the case.
        if (earlier == null || later == null) {
            return;
        }

        if (earlier.size() != later.size()) {
            return;
        }

        for (int i = 0; i < earlier.size(); i++) {
            String earlierScorer = earlier.get(i).getScorer();
            String laterScorer = later.get(i).getScorer();

            if (earlierScorer.equals(laterScorer)) {
                continue;
            }

            boolean itWasMe = laterScorer.equals(mainActivity.getString("username"));

            String message = itWasMe ? "Congrats, you have succesfully captured a point!" :
                    "Oh no, your enemy has captured a point!";

            String button = itWasMe ? "Awesome!" : "Quite the predicament";

            mainActivity.runOnUiThread(
                    () -> new AlertDialog.Builder(mainActivity)
                            .setTitle("Point captured" + (itWasMe ? " by you!" : " by enemy!"))
                            .setMessage(message)
                            .setPositiveButton(button, null)
                            .show()
            );
        }
    }

    private List<Overlay> getGoalsOverlays() {
        List<Overlay> goalOverlays = new ArrayList<>();

        Client client = new Client(
                mainActivity.getString("username"),
                mainActivity.getString("password")
        );

        System.out.println("Setting goals");

        List<TargetState> goals = client.getMatchState();

        List<TargetState> previousTargets = targets;
        targets = goals;

        notifyIfSomebodyScored(previousTargets, targets);

        for (TargetState goal : goals) {
            System.out.println("Goal: " + goal.getLat() + ", " + goal.getLon() + " by " + goal.getScorer());

            Location loc = new Location("");
            loc.setLatitude(goal.getLat());
            loc.setLongitude(goal.getLon());

            OverlayFactory.GoalType type;

            // sorry
            if (goal.getScorer().equals(mainActivity.getString("username"))) {
                type = OverlayFactory.GoalType.FRIENDLY;
            } else if (goal.getScorer().equals("null")) {
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
        gameUpdateTimer = new Timer();

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

                notifyIfSomeoneHasWon();
            }
        };

        gameUpdateTimer.schedule(task, 0, 5000);
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
        controller.setZoom(14.0);

        serverUpdateTimer = new Timer();

        TimerTask updateServer = new TimerTask() {
            @Override
            public void run() {
                Client client = new Client(
                        mainActivity.getString("username"),
                        mainActivity.getString("password")
                );

                Location lastKnownLocation = mainActivity.lastKnownLocation;

                if (lastKnownLocation == null) {
                    return;
                }

                client.reportPosition(
                        lastKnownLocation.getLongitude(),
                        lastKnownLocation.getLatitude()
                );
            }
        };

        serverUpdateTimer.schedule(updateServer, 0, 2000);

        centerMapOnCurrentLocationOncePossible();
        startRefreshingMarkers();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        locationUpdateTimer.cancel();
        gameUpdateTimer.cancel();
        serverUpdateTimer.cancel();

        binding = null;
    }

}