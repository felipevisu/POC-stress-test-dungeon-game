package com.example.dungeon.controller;

import com.example.dungeon.entity.Board;
import com.example.dungeon.entity.Game;
import com.example.dungeon.entity.Player;
import com.example.dungeon.repository.BoardRepository;
import com.example.dungeon.repository.GameRepository;
import com.example.dungeon.repository.PlayerRepository;
import com.example.dungeon.service.DungeonService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@WebMvcTest(GameController.class)
class GameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GameRepository gameRepository;

    @MockBean
    private PlayerRepository playerRepository;

    @MockBean
    private BoardRepository boardRepository;

    @MockBean
    private DungeonService dungeonService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testPlayGame() throws Exception {
        Player player = new Player("Alice", "alice@example.com");
        player.setId(1L);

        int[][] boardData = {{-2, -3, 3}, {-5, -10, 1}, {10, 30, -5}};
        Board board = new Board("Test Dungeon", boardData);
        board.setId(1L);

        Game savedGame = new Game(player, board, 7);
        savedGame.setId(1L);

        when(playerRepository.findById(1L)).thenReturn(Optional.of(player));
        when(boardRepository.findById(1L)).thenReturn(Optional.of(board));
        when(dungeonService.calculateMinimumHP(boardData)).thenReturn(7);
        when(gameRepository.save(any(Game.class))).thenReturn(savedGame);

        GameController.PlayGameRequest request = new GameController.PlayGameRequest();
        request.setPlayerId(1L);
        request.setBoardId(1L);

        mockMvc.perform(post("/api/games/play")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId", is(1)))
                .andExpect(jsonPath("$.playerName", is("Alice")))
                .andExpect(jsonPath("$.boardName", is("Test Dungeon")))
                .andExpect(jsonPath("$.minimumHealth", is(0)));
    }

    @Test
    void testPlayGamePlayerNotFound() throws Exception {
        when(playerRepository.findById(anyLong())).thenReturn(Optional.empty());
        when(boardRepository.findById(anyLong())).thenReturn(Optional.of(new Board()));

        GameController.PlayGameRequest request = new GameController.PlayGameRequest();
        request.setPlayerId(99999L);
        request.setBoardId(1L);

        mockMvc.perform(post("/api/games/play")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Player not found")));
    }

    @Test
    void testPlayGameBoardNotFound() throws Exception {
        when(playerRepository.findById(anyLong())).thenReturn(Optional.of(new Player()));
        when(boardRepository.findById(anyLong())).thenReturn(Optional.empty());

        GameController.PlayGameRequest request = new GameController.PlayGameRequest();
        request.setPlayerId(1L);
        request.setBoardId(99999L);

        mockMvc.perform(post("/api/games/play")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Board not found")));
    }

    @Test
    void testGetAllGames() throws Exception {
        Player player = new Player("Alice", "alice@example.com");
        Board board = new Board("Test Board", new int[][]{{1, 2}});
        Game game1 = new Game(player, board, 5);
        game1.setId(1L);
        Game game2 = new Game(player, board, 3);
        game2.setId(2L);

        when(gameRepository.findAll()).thenReturn(Arrays.asList(game1, game2));

        mockMvc.perform(get("/api/games"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void testGetGameById() throws Exception {
        Player player = new Player("Alice", "alice@example.com");
        Board board = new Board("Test Board", new int[][]{{1, 2}});
        Game game = new Game(player, board, 5);
        game.setId(1L);

        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));

        mockMvc.perform(get("/api/games/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.result", is(5)));
    }

    @Test
    void testGetGameByIdNotFound() throws Exception {
        when(gameRepository.findById(anyLong())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/games/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetGamesByPlayerId() throws Exception {
        Player player = new Player("Alice", "alice@example.com");
        Board board = new Board("Test Board", new int[][]{{1, 2}});
        Game game = new Game(player, board, 5);

        when(gameRepository.findByPlayerId(1L)).thenReturn(Arrays.asList(game));

        mockMvc.perform(get("/api/games/player/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void testGetGamesByBoardId() throws Exception {
        Player player = new Player("Alice", "alice@example.com");
        Board board = new Board("Test Board", new int[][]{{1, 2}});
        Game game = new Game(player, board, 5);

        when(gameRepository.findByBoardId(1L)).thenReturn(Arrays.asList(game));

        mockMvc.perform(get("/api/games/board/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }
}
