package com.example.app;

import android.Manifest;
import android.annotation.SuppressLint;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.app.databinding.ActivityMainBinding;
import com.google.android.material.appbar.MaterialToolbar;

import org.osmdroid.config.Configuration;
import org.osmdroid.config.IConfigurationProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    public Location lastKnownLocation = null;
    public MaterialToolbar toolbar;
    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private LocationManager locationManager;
    private Runnable fabRunnable = null;
    private Timer locationUpdateTimer;

    private void startUpdatingLocation() {
        locationUpdateTimer = new Timer();

        LocationListener locationListener = (Location l) -> lastKnownLocation = l;

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(
                        () -> {
                            try {
                                locationManager.requestLocationUpdates(
                                        "fused",
                                        1L,
                                        0.1f,
                                        locationListener);
                            } catch (SecurityException e) {
                                // We will wait
                            }
                        }
                );
            }
        };


        locationUpdateTimer.schedule(task, 0, 3000);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        toolbar = binding.toolbar;

        requestPermissions();

        Configuration.getInstance().setUserAgentValue("MPP/0.1");

        IConfigurationProvider mapConfig = Configuration.getInstance();
        File basePath = new File(getCacheDir().getAbsolutePath(), "osmdroid");
        File tileCache = new File(basePath, "tile");
        mapConfig.setOsmdroidBasePath(basePath);
        mapConfig.setOsmdroidTileCache(tileCache);

        startUpdatingLocation();

        binding.floatingActionButton.setOnClickListener(v -> {
            if (fabRunnable != null) {
                fabRunnable.run();
            }
        });

    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    private void requestPermissions() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                },
                1
        );
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }


    public void saveString(String key, String value) {
        try {
            FileOutputStream stream = openFileOutput(key, MODE_PRIVATE);
            OutputStreamWriter writer = new OutputStreamWriter(stream);

            writer.write(value);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Nullable
    public String getString(String key) {

        try {
            FileInputStream stream = openFileInput(key);
            Scanner scanner = new Scanner(stream);

            return scanner.nextLine();

        } catch (FileNotFoundException e) {
            return null;
        }
    }

    public void setFabRunnable(Runnable fabRunnable) {
        this.fabRunnable = fabRunnable;
        binding.floatingActionButton.setVisibility(View.VISIBLE);
    }

    public void removeFabRunnable() {
        this.fabRunnable = null;
        binding.floatingActionButton.setVisibility(View.GONE);
    }


}