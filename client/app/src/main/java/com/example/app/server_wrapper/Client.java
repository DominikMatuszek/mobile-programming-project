package com.example.app.server_wrapper;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Client {
    private final String login;
    private final String password;
    private final String serverURL;

    public Client(String login, String password, String serverURL) {
        this.login = login;
        this.password = password;
        this.serverURL = serverURL;
    }

    public Client(String login, String password) {
        this.login = login;
        this.password = password;
        this.serverURL = "http://52.169.201.105:8000";
    }

    private static void addBody(@NonNull HttpURLConnection connection, @NonNull Map<String, String> body) throws IOException {
        connection.setDoOutput(true);

        StringBuilder stringBody = new StringBuilder();
        stringBody.append("{");
        for (Map.Entry<String, String> entry : body.entrySet()) {
            stringBody.append("\"").append(entry.getKey()).append("\": \"").append(entry.getValue()).append("\", ");
        }
        stringBody.delete(stringBody.length() - 2, stringBody.length());
        stringBody.append("}");

        connection.getOutputStream().write(stringBody.toString().getBytes());
    }

    private HttpURLConnection postToServer(String method, Map<String, String> body) throws IOException {

        URL url = new URL(serverURL + method);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestMethod("POST");

        addBody(connection, body);


        return connection;
    }

    private HttpURLConnection getFromServer(String method) throws IOException {
        URL url = new URL(serverURL + method);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        return connection;
    }

    public int register() {
        Map<String, String> body = new HashMap<>();
        body.put("username", login);
        body.put("password", password);

        try {
            HttpURLConnection connection = postToServer("/register", body);
            return connection.getResponseCode();
        } catch (IOException e) {
            e.printStackTrace();
            return 400;
        }
    }

    public int createLobby() {
        Map<String, String> body = new HashMap<>();
        body.put("username", login);
        body.put("password", password);

        try {
            HttpURLConnection connection = postToServer("/createlobby", body);
            return connection.getResponseCode();
        } catch (IOException e) {
            e.printStackTrace();
            return 400;
        }

    }

    public int joinLobby(String lobby_owner) {
        Map<String, String> body = new HashMap<>();
        body.put("username", login);
        body.put("password", password);
        body.put("lobby_owner_username", lobby_owner);

        try {
            HttpURLConnection connection = postToServer("/joinlobby", body);
            return connection.getResponseCode();
        } catch (IOException e) {
            e.printStackTrace();
            return 400;
        }
    }

    public void leaveLobby() {
        Map<String, String> body = new HashMap<>();
        body.put("username", login);
        body.put("password", password);

        new Thread(() -> {
            try {
                HttpURLConnection connection = postToServer("/leavelobby", body);
                int code = connection.getResponseCode();

                if (code != 200) {
                    System.out.println("Failed to leave lobby, we're gonna have a bad time. Response code: " + code);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public String getLobbies() {
        try {
            HttpURLConnection connection = getFromServer("/getlobbies");
            InputStream response = connection.getInputStream();
            Scanner scanner = new Scanner(response);

            return scanner.nextLine();
        } catch (IOException e) {
            e.printStackTrace();
            return "[]";
        }

    }

    public int startMatch() {
        Map<String, String> body = new HashMap<>();
        body.put("username", login);
        body.put("password", password);

        try {
            HttpURLConnection connection = postToServer("/startmatch", body);
            return connection.getResponseCode();
        } catch (IOException e) {
            e.printStackTrace();
            return 400;
        }
    }

    public int login() {
        Map<String, String> body = new HashMap<>();
        body.put("username", login);
        body.put("password", password);

        try {
            HttpURLConnection connection = postToServer("/login", body);
            return connection.getResponseCode();
        } catch (IOException e) {
            e.printStackTrace();
            return 400;
        }
    }
}
