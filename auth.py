import bcrypt 

def check_credentials(login, password, db):  
    password = password.encode('utf8')
    
    with db.cursor() as cursor:
        cursor.execute("SELECT * FROM users WHERE username=%s;", (login,))
        record = cursor.fetchone() 

        if not record: 
            return False 
        
        legit_hash = record[2].encode('utf8')
        
        print(legit_hash)
        print(type(legit_hash))

        return bcrypt.checkpw(password, legit_hash)