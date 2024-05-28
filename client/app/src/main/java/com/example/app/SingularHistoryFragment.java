package com.example.app;

import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.app.databinding.FragmentSingularHistoryBinding;
import com.example.app.server_wrapper.Client;
import com.example.app.server_wrapper.GameHistoryHeader;
import com.example.app.server_wrapper.TargetState;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.Polyline;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SingularHistoryFragment extends Fragment {

    private FragmentSingularHistoryBinding binding;
    private MapView mapView;
    private MainActivity mainActivity;


    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentSingularHistoryBinding.inflate(inflater, container, false);

        MapTileProviderBasic tileProvider = new MapTileProviderBasic(inflater.getContext());
        mapView = new MapView(inflater.getContext(), tileProvider, null);
        binding.mapLayout.addView(mapView);

        return binding.getRoot();
    }

    private void configureMap() {
        IMapController mapController = mapView.getController();
        mapController.setZoom(14.0);
    }

    private void setTextOverlay() {
        String username = mainActivity.getString("username");
        String gameID = mainActivity.getString("presentedGameID");
        String password = mainActivity.getString("password");

        Client client = new Client(username, password);

        GameHistoryHeader header = client.getGameHistory().stream()
                .filter(game -> Integer.valueOf(game.gameID).equals(Integer.valueOf(gameID)))
                .findFirst()
                .orElse(null);

        if (header == null) {
            // We have a serious problem
            mainActivity.runOnUiThread(
                    NavHostFragment.findNavController(this)::popBackStack
            );
            return;
        }

        Timestamp startTimestamp = header.startTimestamp;
        Timestamp endTimestamp = header.endTimestamp;

        long duration = endTimestamp.getTime() - startTimestamp.getTime();
        long seconds = duration / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        long truncatedHours = hours;
        long truncatedMinutes = minutes % 60;
        long truncatedSeconds = seconds % 60;

        String durationString = "Duration: " + truncatedHours + "h " + truncatedMinutes + "m " + truncatedSeconds + "s";

        mainActivity.runOnUiThread(
                () -> {
                    binding.startTimestamp.setText("Started: " + startTimestamp.toString());
                    binding.endTimestamp.setText("Ended: " + endTimestamp.toString());
                    binding.duration.setText(durationString);
                }
        );
    }

    private void setPath() {
        String username = mainActivity.getString("username");
        String gameID = mainActivity.getString("presentedGameID");
        String password = mainActivity.getString("password");

        Client client = new Client(username, password);

        List<Location> locations = client.getLocations(gameID);

        if (locations == null || locations.isEmpty()) {
            return;
        }

        ArrayList<GeoPoint> geoPoints = locations.stream()
                .map(location -> new GeoPoint(location.getLatitude(), location.getLongitude())).collect(Collectors.toCollection(ArrayList::new));

        Road road = new Road(geoPoints);

        Polyline roadOverlay = RoadManager.buildRoadOverlay(road);

        roadOverlay.setWidth(10.0f);
        GeoPoint firstPoint = geoPoints.get(0);

        mainActivity.runOnUiThread(
                () -> {
                    IMapController mapController = mapView.getController();
                    mapController.setCenter(firstPoint);
                    mapView.getOverlays().add(roadOverlay);
                    mapView.invalidate();
                }
        );

    }

    @NonNull
    private List<Overlay> getGoalsOverlays() {
        List<Overlay> goalOverlays = new ArrayList<>();

        Client client = new Client(
                mainActivity.getString("username"),
                mainActivity.getString("password")
        );

        String gameID = mainActivity.getString("presentedGameID");

        List<TargetState> goals;

        try {
            goals = client.getClaims(Integer.parseInt(gameID));
        } catch (Client.MessedUpMatchStateException e) {
            // Should absolutely not happen
            // But if it does
            // Well alright, keep your secrets
            return new ArrayList<>();
        }

        for (TargetState goal : goals) {
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


    private void setGoalOverlays() {
        List<Overlay> goalOverlays = getGoalsOverlays();

        mainActivity.runOnUiThread(
                () -> {
                    mapView.getOverlays().addAll(goalOverlays);
                    mapView.invalidate();
                }
        );
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mainActivity = (MainActivity) getActivity();

        String gameID = mainActivity.getString("presentedGameID");

        if (gameID == null) {
            // Something was messed up, fall back
            mainActivity.runOnUiThread(
                    NavHostFragment.findNavController(this)::popBackStack
            );
        }

        configureMap();
        new Thread(this::setPath).start();
        new Thread(this::setTextOverlay).start();
        new Thread(this::setGoalOverlays).start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        binding = null;
    }


}