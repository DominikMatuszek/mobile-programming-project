package com.example.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.app.databinding.FragmentSingulerLobbyBinding;

import org.json.JSONArray;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class SingularLobbyFragment extends Fragment {

    private FragmentSingulerLobbyBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentSingulerLobbyBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    private OpponentInfo getOpponent(String username) {
        try {
            URL url = new URL("http://52.169.201.105:8000/getlobbies");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");

            InputStream response = connection.getInputStream();
            Scanner scanner = new Scanner(response);

            String stringList = scanner.nextLine();
            JSONArray arr = new JSONArray(stringList);


            for (int i = 0; i < arr.length(); i++) {
                JSONArray lobby = arr.getJSONArray(i);

                String firstUser = lobby.getString(0);
                String secondUser = lobby.getString(1);

                if (firstUser.equals(username)) {
                    return secondUser.equals("null") ? new OpponentInfo(null, false) : new OpponentInfo(secondUser, false);
                } else if (secondUser.equals(username)) {
                    return firstUser.equals("null") ? new OpponentInfo(null, true) : new OpponentInfo(firstUser, true);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MainActivity activity = (MainActivity) getActivity();

        new Thread(() -> {
            String username = activity.getString("username");
            OpponentInfo opponent = getOpponent(username);

            String opponentText = opponent.username == null ? "Waiting for opponent..." : "Playing against " + opponent.username;

            activity.runOnUiThread(() -> {

                binding.opponent.setText(opponentText);

                if (opponent.host) {
                    binding.startGame.setVisibility(View.INVISIBLE);
                } else {
                    binding.waiting.setVisibility(View.INVISIBLE);
                }

                if (opponent.username == null) {
                    binding.startGame.setClickable(false);
                }
            });

        }).start();


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private static class OpponentInfo {
        public String username;
        public boolean host;

        public OpponentInfo(String username, boolean host) {
            this.username = username;
            this.host = host;
        }
    }

}