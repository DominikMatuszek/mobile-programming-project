import psycopg2

from fastapi import FastAPI, Response, status
from pydantic import BaseModel

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
    password = data.password 

    # Checking if login is already in use
    with conn.cursor() as cursor: 
        cursor.execute("SELECT * FROM users WHERE username=%s;", (username,))
        record = cursor.fetchone() 

        print(record)

        if record: 
            response.status_code = status.HTTP_409_CONFLICT
            return 


    with conn.cursor() as cursor:
        cursor.execute("INSERT INTO users (username, password_hash) VALUES (%s, %s);", (username, password))
        conn.commit()

    


