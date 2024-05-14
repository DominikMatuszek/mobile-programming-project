package com.example.app.server_wrapper;

import java.sql.Timestamp;

public class GameHistoryHeader {
    public final int gameID;
    public final String enemy;
    public final Timestamp endTimestamp;
    public final Timestamp startTimestamp;
    public final boolean win;

    public GameHistoryHeader(int gameID, String enemy, Timestamp endTimestamp, Timestamp startTimestamp, boolean win) {
        this.gameID = gameID;
        this.enemy = enemy;
        this.endTimestamp = endTimestamp;
        this.startTimestamp = startTimestamp;
        this.win = win;
    }
}
