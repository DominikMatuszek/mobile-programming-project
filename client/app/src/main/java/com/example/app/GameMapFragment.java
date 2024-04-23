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
import org.osmdroid.views.MapView;

import java.util.List;

public class GameMapFragment extends Fragment {

    private FragmentGameMapBinding binding;
    private MapView mapView;
    private MainActivity mainActivity;

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

    private void setGoals() {
        new Thread(
                () -> {
                    Client client = new Client(
                            mainActivity.getString("username"),
                            mainActivity.getString("password")
                    );

                    System.out.println("Setting goals");

                    List<TargetState> goals = client.getMatchState();

                    for (TargetState goal : goals) {
                        System.out.println("Goal: " + goal.getLat() + ", " + goal.getLon() + " by " + goal.getScorer());
                    }
                }
        ).start();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mainActivity = (MainActivity) getActivity();

        IMapController controller = mapView.getController();
        controller.setZoom(19.0);

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

        setGoals();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}