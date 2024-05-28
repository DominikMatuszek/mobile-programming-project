package com.example.app;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.app.databinding.FragmentLoserBinding;

public class LoserFragment extends Fragment {

    private FragmentLoserBinding binding;
    private MediaPlayer mediaPlayer;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentLoserBinding.inflate(inflater, container, false);
        mediaPlayer = MediaPlayer.create(getContext(), R.raw.loss);
        mediaPlayer.setVolume(1f, 1f);
        mediaPlayer.start();

        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MainActivity activity = (MainActivity) getActivity();

        binding.button2.setOnClickListener(
                v -> activity.runOnUiThread(
                        () -> NavHostFragment.findNavController(LoserFragment.this).navigate(R.id.action_loserFragment_to_homeFragment)
                )
        );

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mediaPlayer.stop();
        mediaPlayer.release();
        binding = null;
    }

}