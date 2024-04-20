package com.example.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.app.databinding.FragmentSingulerLobbyBinding;
import com.example.app.server_wrapper.Client;

import org.json.JSONArray;

public class SingularLobbyFragment extends Fragment {
    private boolean update = true;

    private FragmentSingulerLobbyBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentSingulerLobbyBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    private OpponentInfo getOpponent() {
        try {
            MainActivity activity = (MainActivity) getActivity();
            String password = activity.getString("password");
            String username = activity.getString("username");

            Client client = new Client(username, password);
            String stringList = client.getLobbies();

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
            System.out.println("WORST ERROR EVER");
            e.printStackTrace();
        }

        return null;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MainActivity activity = (MainActivity) getActivity();

        new Thread(() -> {
            while (update) {
                OpponentInfo opponent = getOpponent();

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
                        binding.startGame.setEnabled(false);
                    } else {
                        binding.startGame.setClickable(true);
                        binding.startGame.setEnabled(true);
                    }
                });

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();


    }

    @Override
    public void onDestroyView() {
        update = false;

        String username = ((MainActivity) getActivity()).getString("username");
        String password = ((MainActivity) getActivity()).getString("password");

        Client client = new Client(username, password);
        client.leaveLobby();

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