package com.example.app.server_wrapper;

import android.location.Location;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Client {
    private static final String USERNAME_KEY = "username";
    private static final String PASSWORD_KEY = "password";
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
        this.serverURL = "http://soturi.online:8000";
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

    @NonNull
    private HttpURLConnection getFromServer(String method) throws IOException {
        URL url = new URL(serverURL + method);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        return connection;
    }

    public int register() {
        Map<String, String> body = new HashMap<>();
        body.put(USERNAME_KEY, login);
        body.put(PASSWORD_KEY, password);

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
        body.put(USERNAME_KEY, login);
        body.put(PASSWORD_KEY, password);

        try {
            HttpURLConnection connection = postToServer("/createlobby", body);
            return connection.getResponseCode();
        } catch (IOException e) {
            e.printStackTrace();
            return 400;
        }

    }

    public int joinLobby(String lobbyOwner) {
        Map<String, String> body = new HashMap<>();
        body.put(USERNAME_KEY, login);
        body.put(PASSWORD_KEY, password);
        body.put("lobby_owner_username", lobbyOwner);

        try {
            HttpURLConnection connection = postToServer("/joinlobby", body);
            return connection.getResponseCode();
        } catch (IOException e) {
            e.printStackTrace();
            return 400;
        }
    }

    public int leaveLobby() {
        Map<String, String> body = new HashMap<>();
        body.put(USERNAME_KEY, login);
        body.put(PASSWORD_KEY, password);

        try {
            HttpURLConnection conn = postToServer("/leavelobby", body);
            return conn.getResponseCode();
        } catch (IOException e) {
            throw new RuntimeException("Failed to leave lobby");
        }

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

    public String getWaitingLobbies() {
        try {
            HttpURLConnection connection = getFromServer("/getwaitinglobbies");
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
        body.put(USERNAME_KEY, login);
        body.put(PASSWORD_KEY, password);

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
        body.put(USERNAME_KEY, login);
        body.put(PASSWORD_KEY, password);

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
        body.put(USERNAME_KEY, login);
        body.put(PASSWORD_KEY, password);

        body.put("lon", Double.toString(lon));
        body.put("lat", Double.toString(lat));

        try {
            return postToServer("/reportposition", body).getResponseCode();
        } catch (IOException e) {
            e.printStackTrace();
            return 400;
        }
    }

    @NonNull
    private List<TargetState> parseTargetStringWithMap(String JSONString) throws JSONException {
        List<TargetState> targetStates = new ArrayList<>();

        JSONArray objectMaps = new JSONArray(JSONString);

        for (int i = 0; i < objectMaps.length(); i++) {
            JSONObject map = objectMaps.getJSONObject(i);

            double lon = map.getDouble("lon");
            double lat = map.getDouble("lat");
            String scorer = map.getString("scorer");

            TargetState targetState = new TargetState(lon, lat, scorer);

            targetStates.add(targetState);
        }

        return targetStates;
    }

    @NonNull
    private List<TargetState> parseTargetStringWithArray(String JSONString) throws JSONException {
        List<TargetState> targetStates = new ArrayList<>();

        JSONArray objectMaps = new JSONArray(JSONString);

        for (int i = 0; i < objectMaps.length(); i++) {
            JSONArray arr = objectMaps.getJSONArray(i);

            double lon = arr.getDouble(1);
            double lat = arr.getDouble(2);
            String scorer = arr.getString(3);

            TargetState targetState = new TargetState(lon, lat, scorer);

            targetStates.add(targetState);
        }

        return targetStates;
    }

    public List<TargetState> getMatchState() throws MessedUpMatchStateException {
        Map<String, String> body = new HashMap<>();
        body.put(USERNAME_KEY, login);
        body.put(PASSWORD_KEY, password);

        try {
            HttpURLConnection connection = postToServer("/getmatchstate", body);

            if (connection.getResponseCode() != 200) {
                throw new MessedUpMatchStateException("Server returned " + connection.getResponseCode());
            }

            InputStream response = connection.getInputStream();
            Scanner scanner = new Scanner(response);
            String responseString = scanner.nextLine();

            return parseTargetStringWithMap(responseString);

        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<TargetState> getClaims(int id) {
        Map<String, String> body = new HashMap<>();
        body.put(USERNAME_KEY, login);
        body.put(PASSWORD_KEY, password);
        body.put("game_id", Integer.toString(id));

        try {
            HttpURLConnection connection = postToServer("/getclaims", body);

            InputStream response = connection.getInputStream();
            Scanner scanner = new Scanner(response);
            String responseString = scanner.nextLine();

            return parseTargetStringWithArray(responseString);

        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public boolean amIInActiveMatch() {
        Map<String, String> body = new HashMap<>();
        body.put(USERNAME_KEY, login);
        body.put(PASSWORD_KEY, password);

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
        body.put(USERNAME_KEY, login);
        body.put(PASSWORD_KEY, password);

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

    public List<GameHistoryHeader> getGameHistory() {
        Map<String, String> body = new HashMap<>();
        body.put(USERNAME_KEY, login);
        body.put(PASSWORD_KEY, password);

        try {
            HttpURLConnection connection = postToServer("/getmatchhistory", body);

            if (connection.getResponseCode() != 200) {
                return new ArrayList<>();
            }

            InputStream response = connection.getInputStream();
            Scanner scanner = new Scanner(response);

            String responseString = scanner.nextLine();

            List<GameHistoryHeader> gameHistoryHeaders = new ArrayList<>();

            JSONArray objectMaps = new JSONArray(responseString);

            for (int i = 0; i < objectMaps.length(); i++) {
                JSONArray arr = objectMaps.getJSONArray(i);

                String gameIDString = arr.getString(0);
                String startTimestampString = arr.getString(1);
                String endTimestampString = arr.getString(2);
                String wonString = arr.getString(3);
                String enemy = arr.getString(4);

                // Just in case server decides to send us some nonsensical data
                if (startTimestampString.equals("null") || endTimestampString.equals("null")) {
                    continue;
                }

                // ISO 8601 -> Java
                startTimestampString = startTimestampString.replace("T", " ");
                endTimestampString = endTimestampString.replace("T", " ");

                int gameID = Integer.parseInt(gameIDString);
                Timestamp startTimestamp = Timestamp.valueOf(startTimestampString);
                Timestamp endTimestamp = Timestamp.valueOf(endTimestampString);
                boolean won = Boolean.parseBoolean(wonString);

                GameHistoryHeader header = new GameHistoryHeader(gameID, enemy, endTimestamp, startTimestamp, won);

                gameHistoryHeaders.add(header);
            }

            return gameHistoryHeaders;

        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }

    }

    public List<Location> getLocations(String gameID) {
        Map<String, String> body = new HashMap<>();
        body.put(USERNAME_KEY, login);
        body.put(PASSWORD_KEY, password);
        body.put("game_id", gameID);

        try {
            HttpURLConnection connection = postToServer("/getlocations", body);

            if (connection.getResponseCode() != 200) {
                return new ArrayList<>();
            }

            InputStream response = connection.getInputStream();
            Scanner scanner = new Scanner(response);

            String responseString = scanner.nextLine();

            List<Location> locations = new ArrayList<>();

            JSONArray objectMaps = new JSONArray(responseString);

            for (int i = 0; i < objectMaps.length(); i++) {
                JSONArray array = objectMaps.getJSONArray(i);

                double lon = array.getDouble(0);
                double lat = array.getDouble(1);

                Location location = new Location("");
                location.setLongitude(lon);
                location.setLatitude(lat);

                locations.add(location);
            }

            return locations;
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }

    }

    public static class MessedUpMatchStateException extends Exception {
        public MessedUpMatchStateException(String message) {
            super(message);
        }
    }


}
