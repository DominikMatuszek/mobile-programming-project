import datetime 

def get_user_id(username, conn):
    with conn.cursor() as cursor:
        cursor.execute("SELECT id FROM users WHERE username = %s;", (username,))
        
        result = cursor.fetchone()
        
        if result is None:
            return None
        
        return result[0]

def add_match_to_database(host, guest, targets, conn):
    timestamp = datetime.datetime.now(datetime.timezone.utc).isoformat()
    host_id = get_user_id(host, conn)
    guest_id = get_user_id(guest, conn)
    
    with conn.cursor() as cursor:
        cursor.execute("INSERT INTO matches (start_timestamp, end_timestamp) VALUES (%s, NULL);", (timestamp,))
        cursor.execute("SELECT max(id) FROM matches;") # I'm sorry. Haha.
        
        result = cursor.fetchone()
        
        print("AAAAAAAAAAAAA", result)
        match_id = result[0]
        
        for target in targets:
            cursor.execute("INSERT INTO targets (longtitude, latitude, match_id) VALUES (%s, %s, %s);", (target.lon, target.lat, match_id))
            cursor.execute("SELECT max(id) FROM targets;")
            
            target_id = cursor.fetchone()[0]
            target.set_id(target_id)
            
        cursor.execute("INSERT INTO results (player_id, match_id, won) VALUES (%s, %s, NULL);", (host_id, match_id))
        cursor.execute("INSERT INTO results (player_id, match_id, won) VALUES (%s, %s, NULL);", (guest_id, match_id))
        
        conn.commit()
        
        return match_id
    
def add_position_to_database(player, lon, lat, match_id, conn):
    player_id = get_user_id(player, conn)
    
    timestamp = datetime.datetime.now(datetime.timezone.utc).isoformat()
    
    with conn.cursor() as cursor:
        cursor.execute("INSERT INTO recorded_locations (user_id, match_id, timestamp, longtitude, latitude) VALUES (%s, %s, %s, %s, %s);", (player_id, match_id, timestamp, lon, lat))
        conn.commit()
        
def add_score_to_database(match_id, player, target_id, conn):
    player_id = get_user_id(player, conn)
    
    with conn.cursor() as cursor:
        cursor.execute("INSERT INTO claims (player_id, target_id) VALUES (%s, %s);", (player_id, target_id))
        conn.commit()
        
def add_winner_to_database(match_id, winner, conn):
    winner_id = get_user_id(winner, conn)
    timestamp = datetime.datetime.now(datetime.timezone.utc).isoformat()

    with conn.cursor() as cursor:
        cursor.execute("UPDATE results SET won = TRUE WHERE player_id = %s AND match_id = %s;", (winner_id, match_id))
        cursor.execute("UPDATE results SET won = FALSE WHERE player_id != %s AND match_id = %s;", (winner_id, match_id))
        
        cursor.execute("UPDATE matches SET end_timestamp = %s WHERE id = %s;", (timestamp, match_id))
        conn.commit()
        
def get_matches_for_user(username, conn):
    user_id = get_user_id(username, conn)
    
    with conn.cursor() as cursor:
        cursor.execute(
            """
            SELECT matches.id, matches.start_timestamp, matches.end_timestamp, my_results.won, users.username AS enemy
            FROM matches
            CROSS JOIN results AS my_results 
            CROSS JOIN results AS enemy_results
            JOIN users ON enemy_results.player_id = users.id 
            WHERE 
                matches.id = my_results.match_id 
                AND enemy_results.match_id = my_results.match_id
                AND my_results.player_id = %s
                AND enemy_results.player_id != %s
            """,
            (user_id, user_id)
        )
        result = cursor.fetchall()
        
        return result
    
def get_user_locations_in_a_match(username, id, conn):
    user_id = get_user_id(username, conn)
    
    with conn.cursor() as cursor:
        cursor.execute(
            """
            SELECT longtitude, latitude
            FROM recorded_locations
            WHERE user_id = %s AND match_id = %s
            """,
            (user_id, id)
        )
        result = cursor.fetchall()
        
        return result