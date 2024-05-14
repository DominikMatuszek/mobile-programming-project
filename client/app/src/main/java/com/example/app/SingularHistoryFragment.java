package com.example.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.app.databinding.FragmentSingularHistoryBinding;

import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.views.MapView;

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

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mainActivity = (MainActivity) getActivity();


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        binding = null;
    }


}