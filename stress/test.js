import http from "k6/http";
import { sleep, check } from "k6";

export let options = {
  thresholds: {
    http_req_failed: ["rate<0.01"], // http errors should be less than 1%
    http_req_duration: ["p(95)<200"], // 95% of requests should be below 200ms
  },
  stages: [
    { duration: "60s", target: 10 },
    { duration: "60s", target: 20 },
    { duration: "60s", target: 30 },
    { duration: "60s", target: 40 },
    { duration: "60s", target: 50 },
  ],
};

function randomBoard(minSize = 2, maxSize = 10, minVal = -10, maxVal = 10) {
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

  // Fetch all games (simulate dashboard load)
  let gamesRes = http.get("http://app:8080/api/games");
  check(gamesRes, { "games fetched": (r) => r.status === 200 });

  sleep(1);
}
