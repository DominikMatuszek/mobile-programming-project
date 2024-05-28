package com.example.app;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.app.databinding.FragmentLoginBinding;
import com.example.app.server_wrapper.Client;

import org.json.JSONArray;

public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;

    private boolean loggedIn(String username, String password) {
        Client client = new Client(username, password);
        return client.login() == 200;
    }

    private boolean inLobbyOrGame(String username, String password) {
        Client client = new Client(username, password);

        String response = client.getLobbies();

        try {
            JSONArray arr = new JSONArray(response);

            for (int i = 0; i < arr.length(); i++) {
                JSONArray lobby = arr.getJSONArray(i);

                String firstUser = lobby.getString(0);
                String secondUser = lobby.getString(1);

                if (firstUser.equals(username) || secondUser.equals(username)) {
                    return true;
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MainActivity activity = (MainActivity) getActivity();

        String savedUsername = activity.getString("username");
        String savedPassword = activity.getString("password");


        if (savedUsername != null && savedPassword != null) {
            binding.editTextText.setText(savedUsername);
            binding.editTextTextPassword.setText(savedPassword);
        }

        binding.signupText.setOnClickListener(
                v -> NavHostFragment.findNavController(LoginFragment.this).navigate(R.id.action_LoginFragment_to_signupFragment)
        );


        binding.button.setOnClickListener(v -> {
            String username = binding.editTextText.getText().toString();
            String password = binding.editTextTextPassword.getText().toString();

            new Thread(() -> {
                if (loggedIn(username, password)) {
                    if (inLobbyOrGame(username, password)) {
                        activity.runOnUiThread(
                                () -> NavHostFragment.findNavController(LoginFragment.this).navigate(R.id.action_LoginFragment_to_singularLobbyFragment));
                    } else {
                        activity.runOnUiThread(
                                () -> NavHostFragment.findNavController(LoginFragment.this).navigate(R.id.action_LoginFragment_to_homeFragment)
                        );
                    }

                    activity.saveString("username", username);
                    activity.saveString("password", password);
                } else {
                    activity.runOnUiThread(
                            () -> new AlertDialog.Builder(activity)
                                    .setTitle("Login Failed")
                                    .setMessage("Invalid username or password")
                                    .setPositiveButton("Ok", null)
                                    .show()
                    );
                }
            }
            ).start();


        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}