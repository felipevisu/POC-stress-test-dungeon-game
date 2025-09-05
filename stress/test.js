import http from "k6/http";
import { sleep, check } from "k6";

export let options = {
  thresholds: {
    http_req_failed: ["rate<0.01"], // http errors should be less than 1%
    http_req_duration: ["p(95)<200"], // 95% of requests should be below 200ms
  },
  stages: [
    { duration: "10s", target: 50 }, // quickly ramp to 50 users
    { duration: "30s", target: 50 }, // sustain 50
    { duration: "20s", target: 200 }, // ramp up to 200 users
    { duration: "1m", target: 200 }, // sustain 200
    { duration: "20s", target: 500 }, // ramp up to 500 users
    { duration: "2m", target: 500 }, // sustain 500 (heavy load)
    { duration: "30s", target: 0 }, // ramp down
  ],
};

function randomBoard(minSize = 5, maxSize = 20, minVal = -10, maxVal = 10) {
  const rows = Math.floor(Math.random() * (maxSize - minSize + 1)) + minSize;
  const cols = Math.floor(Math.random() * (maxSize - minSize + 1)) + minSize;

  const board = Array.from({ length: rows }, () =>
    Array.from(
      { length: cols },
      () => Math.floor(Math.random() * (maxVal - minVal + 1)) + minVal
    )
  );

  return board;
}

export default function () {
  sleep(10);

  // Create a player
  let playerRes = http.post(
    "http://app:8080/api/players",
    JSON.stringify({
      name: `Player_${__VU}_${Date.now()}`,
      email: `player_${__VU}_${Date.now()}@test.com`,
    }),
    { headers: { "Content-Type": "application/json" } }
  );

  check(playerRes, {
    "player created": (r) => r.status === 200 || r.status === 201,
  });

  let playerId = playerRes.json("id");

  // Create a board
  const boardData = {
    name: `Board_${__VU}_${Date.now()}`,
    board: randomBoard(),
  };
  let boardRes = http.post(
    "http://app:8080/api/boards",
    JSON.stringify(boardData),
    { headers: { "Content-Type": "application/json" } }
  );

  check(boardRes, {
    "board created": (r) => r.status === 200 || r.status === 201,
  });

  let boardId = boardRes.json("id");

  // Play the game
  let gameRes = http.post(
    "http://app:8080/api/games/play",
    JSON.stringify({
      playerId: playerId,
      boardId: boardId,
    }),
    { headers: { "Content-Type": "application/json" } }
  );

  check(gameRes, { "game played": (r) => r.status === 200 });

  // Retrieve data
  let playerGet = http.get(`http://app:8080/api/players/${playerId}`);
  check(playerGet, { "player fetched": (r) => r.status === 200 });

  let boardGet = http.get(`http://app:8080/api/boards/${boardId}`);
  check(boardGet, { "board fetched": (r) => r.status === 200 });

  let gameGet = http.get(`http://app:8080/api/games/${gameId}`);
  check(gameGet, { "game fetched": (r) => r.status === 200 });

  // Fetch all games (simulate dashboard load)
  let gamesRes = http.get("http://app:8080/api/games");
  check(gamesRes, { "games fetched": (r) => r.status === 200 });

  sleep(1);
}
