package com.example.dungeon.controller;

import com.example.dungeon.entity.Game;
import com.example.dungeon.entity.Player;
import com.example.dungeon.entity.Board;
import com.example.dungeon.repository.GameRepository;
import com.example.dungeon.repository.PlayerRepository;
import com.example.dungeon.repository.BoardRepository;
import com.example.dungeon.service.DungeonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/games")
public class GameController {

    @Autowired
    private GameRepository gameRepository;
    
    @Autowired
    private PlayerRepository playerRepository;
    
    @Autowired
    private BoardRepository boardRepository;
    
    @Autowired
    private DungeonService dungeonService;

    // GET all games
    @GetMapping
    public List<Game> getAllGames() {
        return gameRepository.findAll();
    }

    // GET game by ID
    @GetMapping("/{id}")
    public ResponseEntity<Game> getGameById(@PathVariable Long id) {
        Optional<Game> game = gameRepository.findById(id);
        if (game.isPresent()) {
            return ResponseEntity.ok(game.get());
        }
        return ResponseEntity.notFound().build();
    }

    // GET games by player ID
    @GetMapping("/player/{playerId}")
    public List<Game> getGamesByPlayerId(@PathVariable Long playerId) {
        return gameRepository.findByPlayerId(playerId);
    }

    // GET games by board ID
    @GetMapping("/board/{boardId}")
    public List<Game> getGamesByBoardId(@PathVariable Long boardId) {
        return gameRepository.findByBoardId(boardId);
    }

    // POST play a new game
    @PostMapping("/play")
    public ResponseEntity<GameResult> playGame(@RequestBody PlayGameRequest request) {
        // Find player and board
        Optional<Player> playerOpt = playerRepository.findById(request.getPlayerId());
        Optional<Board> boardOpt = boardRepository.findById(request.getBoardId());
        
        if (!playerOpt.isPresent()) {
            return ResponseEntity.badRequest().body(new GameResult("Player not found"));
        }
        
        if (!boardOpt.isPresent()) {
            return ResponseEntity.badRequest().body(new GameResult("Board not found"));
        }
        
        Player player = playerOpt.get();
        Board board = boardOpt.get();
        
        // Calculate minimum health using the dungeon service
        int minHealth = dungeonService.calculateMinimumHP(board.getBoardArray());
        
        // Save the game result
        Game game = new Game(player, board, minHealth);
        Game savedGame = gameRepository.save(game);
        
        return ResponseEntity.ok(new GameResult(savedGame.getId(), player.getName(), board.getName(), minHealth));
    }

    // Inner classes for request and response
    public static class PlayGameRequest {
        private Long playerId;
        private Long boardId;

        public Long getPlayerId() { return playerId; }
        public void setPlayerId(Long playerId) { this.playerId = playerId; }
        public Long getBoardId() { return boardId; }
        public void setBoardId(Long boardId) { this.boardId = boardId; }
    }

    public static class GameResult {
        private Long gameId;
        private String playerName;
        private String boardName;
        private Integer minimumHealth;
        private String error;

        // Success constructor
        public GameResult(Long gameId, String playerName, String boardName, Integer minimumHealth) {
            this.gameId = gameId;
            this.playerName = playerName;
            this.boardName = boardName;
            this.minimumHealth = minimumHealth;
        }

        // Error constructor
        public GameResult(String error) {
            this.error = error;
        }

        // Getters and setters
        public Long getGameId() { return gameId; }
        public void setGameId(Long gameId) { this.gameId = gameId; }
        public String getPlayerName() { return playerName; }
        public void setPlayerName(String playerName) { this.playerName = playerName; }
        public String getBoardName() { return boardName; }
        public void setBoardName(String boardName) { this.boardName = boardName; }
        public Integer getMinimumHealth() { return minimumHealth; }
        public void setMinimumHealth(Integer minimumHealth) { this.minimumHealth = minimumHealth; }
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
    }
}
