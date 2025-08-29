package com.example.dungeon.controller;

import com.example.dungeon.entity.Player;
import com.example.dungeon.repository.PlayerRepository;
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

@WebMvcTest(PlayerController.class)
class PlayerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PlayerRepository playerRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCreatePlayer() throws Exception {
        Player player = new Player("John Doe", "john@example.com");
        Player savedPlayer = new Player("John Doe", "john@example.com");
        savedPlayer.setId(1L);

        when(playerRepository.save(any(Player.class))).thenReturn(savedPlayer);

        mockMvc.perform(post("/api/players")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(player)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("John Doe")))
                .andExpect(jsonPath("$.email", is("john@example.com")))
                .andExpect(jsonPath("$.id", is(1)));
    }

    @Test
    void testGetAllPlayers() throws Exception {
        Player player1 = new Player("Alice", "alice@example.com");
        player1.setId(1L);
        Player player2 = new Player("Bob", "bob@example.com");
        player2.setId(2L);

        when(playerRepository.findAll()).thenReturn(Arrays.asList(player1, player2));

        mockMvc.perform(get("/api/players"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Alice")))
                .andExpect(jsonPath("$[1].name", is("Bob")));
    }

    @Test
    void testGetPlayerById() throws Exception {
        Player player = new Player("Test Player", "test@example.com");
        player.setId(1L);

        when(playerRepository.findById(1L)).thenReturn(Optional.of(player));

        mockMvc.perform(get("/api/players/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Test Player")))
                .andExpect(jsonPath("$.email", is("test@example.com")))
                .andExpect(jsonPath("$.id", is(1)));
    }

    @Test
    void testGetPlayerByIdNotFound() throws Exception {
        when(playerRepository.findById(anyLong())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/players/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdatePlayer() throws Exception {
        Player existingPlayer = new Player("Original Name", "original@example.com");
        existingPlayer.setId(1L);
        
        Player updatedPlayer = new Player("Updated Name", "updated@example.com");
        updatedPlayer.setId(1L);

        when(playerRepository.findById(1L)).thenReturn(Optional.of(existingPlayer));
        when(playerRepository.save(any(Player.class))).thenReturn(updatedPlayer);

        mockMvc.perform(put("/api/players/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedPlayer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated Name")))
                .andExpect(jsonPath("$.email", is("updated@example.com")));
    }

    @Test
    void testDeletePlayer() throws Exception {
        when(playerRepository.existsById(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/players/1"))
                .andExpect(status().isNoContent());

        verify(playerRepository).deleteById(1L);
    }

    @Test
    void testDeletePlayerNotFound() throws Exception {
        when(playerRepository.existsById(anyLong())).thenReturn(false);

        mockMvc.perform(delete("/api/players/99999"))
                .andExpect(status().isNotFound());
    }
}
