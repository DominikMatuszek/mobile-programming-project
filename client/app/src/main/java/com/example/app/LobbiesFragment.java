package com.example.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.app.databinding.FragmentLobbiesBinding;
import com.example.app.server_wrapper.Client;

import org.json.JSONArray;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class LobbiesFragment extends Fragment {

    private FragmentLobbiesBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentLobbiesBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    private List<String> getLobbies() {
        try {
            URL url = new URL("http://52.169.201.105:8000/getlobbies");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");

            InputStream response = connection.getInputStream();
            Scanner scanner = new Scanner(response);

            String stringList = scanner.nextLine();
            JSONArray arr = new JSONArray(stringList);

            List<String> lobbies = new ArrayList<>();

            for (int i = 0; i < arr.length(); i++) {
                JSONArray lobby = arr.getJSONArray(i);

                String secondUser = lobby.getString(1);

                if (secondUser.equals("null")) {
                    lobbies.add(lobby.getString(0));
                }
            }

            return lobbies;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MainActivity activity = (MainActivity) getActivity();
        Client client = new Client(activity.getString("username"), activity.getString("password"));

        binding.createlobby.setOnClickListener((v) -> {
            new Thread(() -> {
                int status = client.createLobby();

                if (status == 200) {
                    activity.runOnUiThread(() -> {
                        NavHostFragment.findNavController(LobbiesFragment.this).navigate(R.id.action_lobbiesFragment_to_singularLobbyFragment);
                    });
                } else {
                    System.out.println("Failed to create lobby, this should not happen; status: " + status);
                }
            }).start();
        });

        new Thread(() -> {
            List<String> lobbies = getLobbies();

            for (String lobby : lobbies) {
                Button button = new Button(activity);
                button.setText(lobby);

                button.setOnClickListener((v) -> {
                    new Thread(() -> {
                        int status = client.joinLobby(lobby);

                        if (status == 200) {
                            activity.runOnUiThread(() -> {
                                NavHostFragment.findNavController(LobbiesFragment.this).navigate(R.id.action_lobbiesFragment_to_singularLobbyFragment);
                            });
                        }

                    }).start();
                });

                activity.runOnUiThread(() -> binding.lobbiesLayout.addView(button));
            }
        }).start();

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}