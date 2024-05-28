package com.example.app;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.app.databinding.FragmentSignupBinding;
import com.example.app.server_wrapper.Client;

public class SignupFragment extends Fragment {

    private FragmentSignupBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentSignupBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MainActivity activity = (MainActivity) getActivity();

        binding.button.setOnClickListener((v) -> {
                    String username = binding.editTextText.getText().toString();
                    String password = binding.editTextTextPassword.getText().toString();
                    String confirmPassword = binding.editTextTextPassword2.getText().toString();


                    if (!password.equals(confirmPassword)) {
                        new AlertDialog.Builder(activity)
                                .setTitle("Error")
                                .setMessage("Passwords do not match!")
                                .setPositiveButton("OK", null)
                                .show();
                    }

                    new Thread(
                            () -> {
                                int response_code = new Client(username, password).register();

                                String title = "Registration successful";
                                String message = "You can now start using the app!";
                                String button_text = "OK";

                                if (response_code != 201) {
                                    title = "Error";
                                    message = "An error has occurred. Please try again.";
                                }

                                if (response_code == 409) {
                                    title = "Error";
                                    message = "Username already exists!";
                                }

                                String finalTitle = title;
                                String finalMessage = message;

                                activity.runOnUiThread(() ->
                                        {
                                            new AlertDialog.Builder(activity)
                                                    .setTitle(finalTitle)
                                                    .setMessage(finalMessage)
                                                    .setPositiveButton(button_text, null)
                                                    .show();

                                            if (response_code == 201) {
                                                activity.saveString("username", username);
                                                activity.saveString("password", password);
                                                activity.setClient(new Client(username, password));
                                                NavHostFragment.findNavController(SignupFragment.this).navigate(R.id.action_signupFragment_to_LoginFragment);
                                            }
                                        }
                                );

                            }
                    ).start();

                }
        );

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}