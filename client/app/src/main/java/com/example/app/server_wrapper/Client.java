package com.example.app.server_wrapper;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    public int reportPosition(double lon, double lat) {
        Map<String, String> body = new HashMap<>();
        body.put("username", login);
        body.put("password", password);

        body.put("lon", Double.toString(lon));
        body.put("lat", Double.toString(lat));

        try {
            return postToServer("/reportposition", body).getResponseCode();
        } catch (IOException e) {
            e.printStackTrace();
            return 400;
        }
    }

    public List<TargetState> getMatchState() {
        Map<String, String> body = new HashMap<>();
        body.put("username", login);
        body.put("password", password);

        try {
            HttpURLConnection connection = postToServer("/getmatchstate", body);

            InputStream response = connection.getInputStream();
            Scanner scanner = new Scanner(response);

            String responseString = scanner.nextLine();

            List<TargetState> targetStates = new ArrayList<>();

            JSONArray objectMaps = new JSONArray(responseString);

            for (int i = 0; i < objectMaps.length(); i++) {
                JSONObject map = objectMaps.getJSONObject(i);

                System.out.println(map);

                double lon = map.getDouble("lon");
                double lat = map.getDouble("lat");
                String scorer = map.getString("scorer");

                TargetState targetState = new TargetState(lon, lat, scorer);

                targetStates.add(targetState);
            }

            return targetStates;

        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public boolean amIInActiveMatch() {
        Map<String, String> body = new HashMap<>();
        body.put("username", login);
        body.put("password", password);

        try {
            HttpURLConnection connection = postToServer("/getmatchstate", body);
            return connection.getResponseCode() == 200;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getWinner() {
        Map<String, String> body = new HashMap<>();
        body.put("username", login);
        body.put("password", password);

        try {
            HttpURLConnection connection = postToServer("/getwinner", body);

            if (connection.getResponseCode() != 200) {
                return null;
            }

            InputStream response = connection.getInputStream();
            Scanner scanner = new Scanner(response);

            String responseString = scanner.nextLine();

            // Removing the quotes
            return responseString.substring(1, responseString.length() - 1);

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
