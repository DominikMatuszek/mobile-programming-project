package com.example.app;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Space;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.app.databinding.FragmentLobbiesBinding;
import com.example.app.server_wrapper.Client;
import com.example.app.views.IconWithText;
import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

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
            MainActivity activity = (MainActivity) getActivity();
            String password = activity.getString("password");
            String username = activity.getString("username");

            Client client = new Client(username, password);
            String stringList = client.getWaitingLobbies();

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
        activity.toolbar.setVisibility(View.VISIBLE);
        Client client = new Client(activity.getString("username"), activity.getString("password"));

        binding.createlobby.setOnClickListener((v) -> {
            new Thread(() -> {
                int status = client.createLobby();

                if (status == 200) {
                    activity.runOnUiThread(() -> {
                        NavHostFragment.findNavController(LobbiesFragment.this).navigate(R.id.action_lobbiesFragment_to_singularLobbyFragment);
                    });
                } else {
                    Log.e("Critical", "Failed to create lobby, this should not happen; status: " + status);
                }
            }).start();
        });

        new Thread(() -> {
            // I know this looks funny, but that is to make things prettier by not asking server about state of lobbies immediately after leaving
            // If we are leaving
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            List<String> lobbies = getLobbies();

            for (String lobby : lobbies) {

                LobbyChoice row = new LobbyChoice(getContext(), lobby);
                Space space = new Space(getContext());

                space.setMinimumHeight(40);

                activity.runOnUiThread(() -> {
                    binding.lobbiesLayout.addView(row);
                    binding.lobbiesLayout.addView(space);
                });
            }
        }).start();

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private class LobbyChoice extends LinearLayout {

        public LobbyChoice(Context context, String username) {
            super(context);

            this.setOrientation(LinearLayout.HORIZONTAL);
            this.setGravity(Gravity.CENTER_HORIZONTAL);

            IconWithText iconWithText = new IconWithText(context, getResources().getDrawable(R.drawable.person_icon), username, 20);
            Space space = new Space(context);
            space.setMinimumWidth(40);
            Button button = new MaterialButton(context);
            button.setText("Join lobby");

            button.setOnClickListener(
                    (v) -> new Thread(() -> {
                        MainActivity activity = (MainActivity) getActivity();
                        Client client = new Client(activity.getString("username"), activity.getString("password"));

                        int status = client.joinLobby(username);

                        if (status == 200) {
                            activity.runOnUiThread(() -> {
                                NavHostFragment.findNavController(LobbiesFragment.this).navigate(R.id.action_lobbiesFragment_to_singularLobbyFragment);
                            });
                        }

                    }).start()
            );

            this.addView(iconWithText);
            this.addView(space);
            this.addView(button);
        }
    }

}