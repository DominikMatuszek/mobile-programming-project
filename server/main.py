import psycopg2
import bcrypt

from fastapi import FastAPI, Response, status
from pydantic import BaseModel
from auth import check_credentials


conn = psycopg2.connect(database="dominik",
                        user="dominik",
                        port="5432",
			password="dominik",
			host="localhost")

app = FastAPI()


@app.get("/")
async def root():
    return {"message": "Hello World"}

class AuthData(BaseModel):
    username: str
    password: str

@app.post("/register", status_code=201)
async def register(data: AuthData, response: Response):
    username = data.username 

    # Checking if login is already in use
    with conn.cursor() as cursor: 
        cursor.execute("SELECT * FROM users WHERE username=%s;", (username,))
        record = cursor.fetchone() 

        if record: 
            response.status_code = status.HTTP_409_CONFLICT
            return 

    password = data.password 
    password = password.encode('utf-8') 
    salt = bcrypt.gensalt() 

    hash = bcrypt.hashpw(password, salt)

    print(type(hash))
    print(hash)

    hash = hash.decode('utf8')

    with conn.cursor() as cursor:
        cursor.execute("INSERT INTO users (username, password_hash) VALUES (%s, %s);", (username, hash))
        conn.commit()

    
@app.post("/login", status_code=200)
async def login(data: AuthData, response: Response):
    print("login")
    if check_credentials(data.username, data.password, conn):
        return 
    else: 
        response.status_code = status.HTTP_401_UNAUTHORIZED
        return 

lobbies = {}

@app.post("/createlobby", status_code=200)
async def create_lobby(user_info: AuthData, response: Response):
    if not check_credentials(user_info.username, user_info.password, conn):
        response.status_code = status.HTTP_401_UNAUTHORIZED
        return 

    print(lobbies)

    username = user_info.username 

    if username in lobbies: 
        response.status_code = status.HTTP_409_CONFLICT
        return 
    

    lobbies[username] = None 
    
@app.get("/getlobbies", status_code=200)
async def get_lobbies():
    return list(lobbies.items())

class LobbyJoinRequest(BaseModel):
    username: str 
    password: str
    lobby_owner_username: str

@app.post("/joinlobby", status_code=200)
async def join_lobby(data: LobbyJoinRequest, response: Response):
    if not check_credentials(data.username, data.password, conn):
        response.status_code = status.HTTP_401_UNAUTHORIZED
        return 

    owner = data.lobby_owner_username

    if owner not in lobbies or lobbies[owner] is not None:
        response.status_code = status.HTTP_409_CONFLICT
        return 

    lobbies[owner] = data.username
    

@app.post("/leavelobby", status_code=200)
async def leave_lobby(user_info: AuthData, response: Response):
    if not check_credentials(user_info.username, user_info.password, conn):
        response.status_code = status.HTTP_401_UNAUTHORIZED
        return 

    username = user_info.username

    try: 
        friend = lobbies[username]
        del lobbies[username]

        if friend:
            lobbies[friend] = None 
        
        return 
    except KeyError:
        for (k, v) in lobbies.items():
            if v == username: 
                lobbies[k] = None 
                return 
   
    response.status_code = status.HTTP_409_CONFLICT
    return 
