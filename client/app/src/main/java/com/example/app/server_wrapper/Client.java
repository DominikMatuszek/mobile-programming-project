package com.example.app.server_wrapper;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class Client {
    private final String login;
    private final String password;
    private final String serverURL;

    public Client(String login, String password, String serverURL) {
        this.login = login;
        this.password = password;
        this.serverURL = serverURL;
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
}
