CREATE TABLE "users" (
  "id" serial PRIMARY KEY,
  "username" varchar,
  "password_hash" varchar
);

CREATE TABLE "recorded_locations" (
  "user_id" integer,
  "match_id" integer,
  "timestamp" timestamp,
  "longtitude" double precision,
  "latitude" double precision 
);

CREATE TABLE "matches" (
  "id" serial PRIMARY KEY,
  "start_timestamp" timestamp,
  "end_timestamp" timestamp
);

CREATE TABLE "results" (
  "player_id" integer,
  "match_id" integer,
  "won" boolean
);

CREATE TABLE "targets" (
  "id" serial PRIMARY KEY,
  "longtitude" double precision,
  "latitude" double precision,
  "match_id" integer
);

CREATE TABLE "claims" (
  "player_id" integer,
  "target_id" integer
);

ALTER TABLE "recorded_locations" ADD FOREIGN KEY ("user_id") REFERENCES "users" ("id");

ALTER TABLE "recorded_locations" ADD FOREIGN KEY ("match_id") REFERENCES "matches" ("id");

ALTER TABLE "results" ADD FOREIGN KEY ("player_id") REFERENCES "users" ("id");

ALTER TABLE "results" ADD FOREIGN KEY ("match_id") REFERENCES "matches" ("id");

ALTER TABLE "targets" ADD FOREIGN KEY ("match_id") REFERENCES "matches" ("id");

ALTER TABLE "claims" ADD FOREIGN KEY ("player_id") REFERENCES "users" ("id");

ALTER TABLE "claims" ADD FOREIGN KEY ("target_id") REFERENCES "targets" ("id");