package com.example.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.app.databinding.FragmentGameHistoryChoiceBinding;
import com.example.app.server_wrapper.Client;
import com.example.app.server_wrapper.GameHistoryHeader;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class GameHistoryChoiceFragment extends Fragment {

    private FragmentGameHistoryChoiceBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentGameHistoryChoiceBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MainActivity activity = (MainActivity) getActivity();
        Client client = new Client(activity.getString("username"), activity.getString("password"));


        new Thread(() -> {
            // I know this looks funny, but that is to make things prettier by not asking server about state of lobbies immediately after leaving
            // If we are leaving
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            List<GameHistoryHeader> games = client.getGameHistory();

            for (GameHistoryHeader game : games) {
                Button button = new Button(activity);

                // FIXME: This is bad.

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String endTimestamp = sdf.format(new Date(game.endTimestamp.getTime()));

                String displayText = game.enemy + " | " + (game.win ? "Won" : "Lost") + " | " + endTimestamp;
                button.setText(displayText);

                /*
                button.setOnClickListener((v) -> {
                    new Thread(() -> {
                        // TODO
                    }).start();
                });
                */

                activity.runOnUiThread(() -> binding.historyLayout.addView(button));

            }
        }).start();

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}