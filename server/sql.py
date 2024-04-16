import time 

def add_match_to_database(host, guest, targets, conn):
    with conn.cursor() as cursor:
        cursor.execute("INSERT INTO matches (start_timestamp, end_timestamp) VALUES (%s, NULL);", (round(time.time()),))
        
        match_id = cursor.lastrowid
        
        for target in targets:
            cursor.execute("INSERT INTO targets (match_id, x, y) VALUES (%s, %s, %s);", (match_id, target[0], target[1]))
            
        cursor.execute("INSERT INTO results (player_id, match_id, won) VALUES (%s, %s, NULL);", (host, match_id))
        cursor.execute("INSERT INTO results (player_id, match_id, won) VALUES (%s, %s, NULL);", (guest, match_id))
        
        conn.commit()
        
        return match_id