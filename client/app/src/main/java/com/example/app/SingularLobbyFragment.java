package com.example.app;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.app.databinding.FragmentSingulerLobbyBinding;
import com.example.app.server_wrapper.Client;

import org.json.JSONArray;

public class SingularLobbyFragment extends Fragment {
    private boolean update = true;

    private FragmentSingulerLobbyBinding binding;
    private ProgressDialog progressDialog;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentSingulerLobbyBinding.inflate(inflater, container, false);
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle("Loading");
        progressDialog.show();
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
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MainActivity activity = (MainActivity) getActivity();
        activity.toolbar.setVisibility(View.GONE);

        new Thread(() -> {
            while (update) {
                Client client = new Client(
                        activity.getString("username"),
                        activity.getString("password")
                );

                new Thread(() -> {
                    if (client.amIInActiveMatch()) {
                        update = false;
                        activity.runOnUiThread(() -> {
                            System.out.println("Navigating to game map");

                            try {
                                NavHostFragment.findNavController(SingularLobbyFragment.this).navigate(R.id.action_singularLobbyFragment_to_gameMapFragment);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });

                    } else {
                        System.out.println("Not in active match");
                    }
                }
                ).start();


                OpponentInfo opponent = getOpponent();

                String opponentText = opponent.username == null ? "Waiting for opponent..." : "Playing against " + opponent.username;

                activity.runOnUiThread(() -> {
                    try {
                        binding.opponent.setText(opponentText);

                        if (opponent.host) {
                            binding.waiting.setVisibility(View.VISIBLE);
                            binding.startGame.setVisibility(View.INVISIBLE);
                        } else {
                            binding.waiting.setVisibility(View.INVISIBLE);
                            binding.startGame.setVisibility(View.VISIBLE);
                        }

                        if (opponent.username == null) {
                            binding.startGame.setClickable(false);
                            binding.startGame.setEnabled(false);
                        } else {
                            binding.startGame.setClickable(true);
                            binding.startGame.setEnabled(true);
                        }
                    } catch (NullPointerException npe) {
                        // It happens
                    }
                });
                progressDialog.dismiss();

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();


        binding.startGame.setOnClickListener((v) -> {
            Client client = new Client(activity.getString("username"), activity.getString("password"));

            // Navigation to the game map is done earlier in the loop
            new Thread(client::startMatch).start();

        });

        binding.leaveLobby.setOnClickListener((v) -> {
            update = false;
            Client client = new Client(activity.getString("username"), activity.getString("password"));
            client.leaveLobby();
            NavHostFragment.findNavController(SingularLobbyFragment.this).navigate(R.id.action_singularLobbyFragment_to_lobbiesFragment);
        });
    }

    @Override
    public void onDestroyView() {
        System.out.println("Destroying view");

        update = false;

        String username = ((MainActivity) getActivity()).getString("username");
        String password = ((MainActivity) getActivity()).getString("password");

        new Thread(() -> {
            Client client = new Client(username, password);
            client.leaveLobby();
        }).start();

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