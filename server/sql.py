import datetime 

def add_match_to_database(host, guest, targets, conn):
    timestamp = datetime.datetime.now(datetime.timezone.utc).isoformat()
    with conn.cursor() as cursor:
        cursor.execute("INSERT INTO matches (start_timestamp, end_timestamp) VALUES (%s, NULL);", (timestamp,))
        cursor.execute("SELECT max(id) FROM matches;") # I'm sorry. Haha.
        
        result = cursor.fetchone()
        
        print("AAAAAAAAAAAAA", result)
        match_id = result[0]
        
        for target in targets:
            cursor.execute("INSERT INTO targets (longtitude, latitude, match_id) VALUES (%s, %s, %s);", (target[0], target[1], match_id))
            
        cursor.execute("INSERT INTO results (player_id, match_id, won) VALUES (%s, %s, NULL);", (host, match_id))
        cursor.execute("INSERT INTO results (player_id, match_id, won) VALUES (%s, %s, NULL);", (guest, match_id))
        
        conn.commit()
        
        return match_id