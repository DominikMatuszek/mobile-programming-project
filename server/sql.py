import datetime 

def get_user_id(username, conn):
    with conn.cursor() as cursor:
        cursor.execute("SELECT id FROM players WHERE username = %s;", (username,))
        
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
            cursor.execute("INSERT INTO targets (longtitude, latitude, match_id) VALUES (%s, %s, %s);", (target[0], target[1], match_id))
            
        cursor.execute("INSERT INTO results (player_id, match_id, won) VALUES (%s, %s, NULL);", (host_id, match_id))
        cursor.execute("INSERT INTO results (player_id, match_id, won) VALUES (%s, %s, NULL);", (guest_id, match_id))
        
        conn.commit()
        
        return match_id