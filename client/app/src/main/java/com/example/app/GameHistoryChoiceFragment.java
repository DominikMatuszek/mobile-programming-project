package com.example.app;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Space;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.app.databinding.FragmentGameHistoryChoiceBinding;
import com.example.app.server_wrapper.Client;
import com.example.app.server_wrapper.GameHistoryHeader;
import com.example.app.views.IconWithText;
import com.google.android.material.button.MaterialButton;

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

        activity.toolbar.setVisibility(View.VISIBLE);

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
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.GERMAN);
                String endTimestamp = sdf.format(new Date(game.endTimestamp.getTime()));

                HistoryChoiceView historyChoiceView = new HistoryChoiceView(activity, game.enemy, endTimestamp, game.win, game.gameID);
                Space space = new Space(activity);
                space.setMinimumHeight(40);

                activity.runOnUiThread(() -> {
                    binding.historyLayout.addView(historyChoiceView);
                    binding.historyLayout.addView(space);
                });

            }
        }).start();

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private class HistoryChoiceView extends LinearLayout {

        public HistoryChoiceView(Context context, String enemyString, String dateString, boolean won, int gameID) {
            super(context);

            this.setOrientation(LinearLayout.HORIZONTAL);
            this.setGravity(Gravity.CENTER);

            IconWithText enemyIcon = new IconWithText(context, getResources().getDrawable(R.drawable.person_icon), enemyString, 30);
            IconWithText dateIcon = new IconWithText(context, getResources().getDrawable(R.drawable.calendar), dateString, 30);
            IconWithText resultIcon = new IconWithText(context, getResources().getDrawable(won ? R.drawable.checkmark : R.drawable.failmark), won ? "Won" : "Lost", 30);

            Space space1 = new Space(context);
            space1.setMinimumWidth(20);

            Space space2 = new Space(context);
            space2.setMinimumWidth(20);

            Space space3 = new Space(context);
            space3.setMinimumWidth(60);

            MaterialButton button = new MaterialButton(context);
            button.setText("See more");
            button.setOnClickListener((v) -> {
                // To make sure that Android does not do something funny when it comes to fragments
                activity.saveString("presentedGameID", String.valueOf(gameID));

                activity.runOnUiThread(() -> {
                    NavHostFragment.findNavController(GameHistoryChoiceFragment.this).navigate(R.id.action_gameHistoryChoiceFragment_to_singularHistoryFragment);
                });
            });

            button.setGravity(Gravity.CENTER_VERTICAL);


            this.addView(enemyIcon);
            this.addView(space1);
            this.addView(dateIcon);
            this.addView(space2);
            this.addView(resultIcon);
            this.addView(space3);
            this.addView(button);

        }
    }

}