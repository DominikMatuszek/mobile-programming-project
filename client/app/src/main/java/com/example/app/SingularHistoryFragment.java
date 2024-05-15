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

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polyline;

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

        return mapView;
    }

    private void configureMap() {
        IMapController mapController = mapView.getController();
        mapController.setZoom(14.0);
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
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        binding = null;
    }


}