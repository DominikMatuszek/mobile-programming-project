package com.example.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.app.databinding.FragmentGameHistoryChoiceBinding;
import com.example.app.server_wrapper.Client;
import com.example.app.server_wrapper.GameHistoryHeader;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GameHistoryChoiceFragment extends Fragment {

    private FragmentGameHistoryChoiceBinding binding;
    private MainActivity activity;

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

        activity = (MainActivity) getActivity();
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

                // FIXME: This is bad. Add an illustration to symbolize win/loss

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.GERMAN);
                String endTimestamp = sdf.format(new Date(game.endTimestamp.getTime()));

                String displayText = game.enemy + " | " + (game.win ? "Won" : "Lost") + " | " + endTimestamp;
                button.setText(displayText);


                button.setOnClickListener((v) -> {
                    // To make sure that Android does not do something funny when it comes to fragments
                    activity.saveString("presentedGameID", String.valueOf(game.gameID));

                    activity.runOnUiThread(() -> {
                        NavHostFragment.findNavController(GameHistoryChoiceFragment.this).navigate(R.id.action_gameHistoryChoiceFragment_to_singularHistoryFragment);
                    });
                });


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