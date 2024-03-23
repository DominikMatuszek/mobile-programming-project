import psycopg2
import bcrypt

from fastapi import FastAPI, Response, status
from pydantic import BaseModel
from auth import check_credentials

conn = psycopg2.connect(database="dominik",
                        user="dominik",
                        port="5432")

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
    print("after", hash)

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
