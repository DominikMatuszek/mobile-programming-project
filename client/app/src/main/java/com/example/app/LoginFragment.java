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

import java.net.HttpURLConnection;
import java.net.URL;

public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;

    private boolean loggedIn(String username, String password) {

        try {
            URL url = new URL("http://52.169.201.105:8000/login");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");

            String jsonInputString = "{\"username\": \"" + username + "\", \"password\": \"" + password + "\"}";

            connection.setDoOutput(true);
            connection.getOutputStream().write(jsonInputString.getBytes());

            int responseCode = connection.getResponseCode();
            return responseCode == 200;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MainActivity activity = (MainActivity) getActivity();

        String saved_username = activity.getString("username");
        String saved_password = activity.getString("password");


        if (saved_username != null && saved_password != null) {
            binding.editTextText.setText(saved_username);
            binding.editTextTextPassword.setText(saved_password);
        }

        binding.signupText.setOnClickListener(v -> {
                    NavHostFragment.findNavController(LoginFragment.this).navigate(R.id.action_LoginFragment_to_signupFragment);
                }
        );


        binding.button.setOnClickListener(v -> {
            String username = binding.editTextText.getText().toString();
            String password = binding.editTextTextPassword.getText().toString();

            new Thread(() -> {
                if (loggedIn(username, password)) {
                    activity.runOnUiThread(
                            () -> NavHostFragment.findNavController(LoginFragment.this).navigate(R.id.action_LoginFragment_to_homeFragment)
                    );

                    activity.saveString("username", username);
                    activity.saveString("password", password);
                } else {
                    activity.runOnUiThread(() -> {
                                new AlertDialog.Builder(activity)
                                        .setTitle("Login Failed")
                                        .setMessage("Invalid username or password")
                                        .setPositiveButton("Ok", null)
                                        .show();
                            }
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