import http from "k6/http";
import { sleep, check } from "k6";

export let options = {
  vus: 200, // number of concurrent users
  duration: "60s", // test duration
};

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
  let boardRes = http.post(
    "http://app:8080/api/boards",
    JSON.stringify({
      name: `Board_${__VU}_${Date.now()}`,
      board: [
        [-2, -3, 3],
        [-5, -10, 1],
        [10, 30, -5],
      ],
    }),
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
