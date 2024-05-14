package com.example.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.app.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MainActivity activity = (MainActivity) getActivity();

        String username = activity.getString("username");
        binding.greeter.setText("Hello there, " + username + "!");
        binding.lobbyButton.setOnClickListener((v) -> {
            NavHostFragment.findNavController(HomeFragment.this).navigate(R.id.action_homeFragment_to_lobbiesFragment);
        });

        binding.historyButton.setOnClickListener((v) -> {
            NavHostFragment.findNavController(HomeFragment.this).navigate(R.id.action_homeFragment_to_gameHistoryChoiceFragment);
        });

        binding.testButton.setOnClickListener((v) -> {
            NavHostFragment.findNavController(HomeFragment.this).navigate(R.id.action_homeFragment_to_winnerFragment);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}