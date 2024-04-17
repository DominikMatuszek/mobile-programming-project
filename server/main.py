import psycopg2
import bcrypt

from fastapi import FastAPI, Response, status
from pydantic import BaseModel
from auth import check_credentials
from player_matcher import PlayerMatcher

from sql import add_match_to_database, add_position_to_database, add_score_to_database


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

lobbies = PlayerMatcher()

@app.post("/createlobby", status_code=200)
async def create_lobby(user_info: AuthData, response: Response):
    if not check_credentials(user_info.username, user_info.password, conn):
        response.status_code = status.HTTP_401_UNAUTHORIZED
        return 

    print(lobbies)

    username = user_info.username 
    
    if not lobbies.can_participate_in_match(username):
        response.status_code = status.HTTP_409_CONFLICT
        return
    else: 
        lobbies.create_match(username)

    
@app.get("/getlobbies", status_code=200)
async def get_lobbies():
    return lobbies.get_matches()

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

    if not lobbies.can_join_match(host=owner, player=data.username):
        response.status_code = status.HTTP_409_CONFLICT
        return

    lobbies.join_match(owner, data.username)    

@app.post("/leavelobby", status_code=200)
async def leave_lobby(user_info: AuthData, response: Response):
    if not check_credentials(user_info.username, user_info.password, conn):
        response.status_code = status.HTTP_401_UNAUTHORIZED
        return 

    username = user_info.username

    if not lobbies.can_leave_match(username):
        response.status_code = status.HTTP_409_CONFLICT
        return
    else:
        lobbies.leave_match(username)

class CoordinatesInfo(BaseModel):
    username: str 
    password: str
    lon: float
    lat: float
    
@app.post("/reportposition", status_code=200)
async def report_position(data: CoordinatesInfo, response: Response):
    if not check_credentials(data.username, data.password, conn):
        response.status_code = status.HTTP_401_UNAUTHORIZED
        return 
    
    username = data.username
    lon = data.lon
    lat = data.lat

    if not lobbies.is_match_started(username):
        response.status_code = status.HTTP_409_CONFLICT
        return
    
    state_before_report = lobbies.get_state_for_match(username)

    lobbies.report_position_of(username, lon, lat)
    
    state_after_report = lobbies.get_state_for_match(username)
    
    if state_before_report != state_after_report:
        # New goal was scored, oh well 
        match_id = lobbies.get_match_id(username)
        
        for before_dict, after_dict in zip(state_before_report, state_after_report):
            if before_dict["scorer"] is None and after_dict["scorer"] is not None:
                add_score_to_database(match_id, after_dict["scorer"], after_dict["id"], conn)
    
    add_position_to_database(username, lon, lat, lobbies.get_match_id(username), conn)
    
@app.post("/startmatch", status_code=200)
async def start_match(data: AuthData, response: Response):
    if not check_credentials(data.username, data.password, conn):
        response.status_code = status.HTTP_401_UNAUTHORIZED
        return 

    username = data.username

    if not lobbies.can_game_be_started(host=username):
        response.status_code = status.HTTP_409_CONFLICT
        return
    else:       
        targets = lobbies.get_goals(username)
        players = lobbies.get_player_list(username)
        
        host = players[0]
        guest = players[1]
        
        game_id = add_match_to_database(host, guest, targets, conn)
        
        lobbies.start_match(username, game_id)

        
        

@app.post("/getmatchstate", status_code=200)
async def get_match_state(user_info: AuthData, response: Response):
    if not check_credentials(user_info.username, user_info.password, conn):
        response.status_code = status.HTTP_401_UNAUTHORIZED
        return 

    username = user_info.username
    
    if not lobbies.is_match_started(username):
        response.status_code = status.HTTP_409_CONFLICT
        return

    return lobbies.get_state_for_match(username)