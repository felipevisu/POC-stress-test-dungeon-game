package com.example.dungeon.controller;

import com.example.dungeon.entity.Board;
import com.example.dungeon.repository.BoardRepository;
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

@WebMvcTest(BoardController.class)
class BoardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BoardRepository boardRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCreateBoard() throws Exception {
        int[][] boardData = {{-2, -3, 3}, {-5, -10, 1}, {10, 30, -5}};
        BoardController.BoardRequest request = new BoardController.BoardRequest();
        request.setName("Test Dungeon");
        request.setBoard(boardData);

        Board savedBoard = new Board("Test Dungeon", boardData);
        savedBoard.setId(1L);

        when(boardRepository.save(any(Board.class))).thenReturn(savedBoard);

        mockMvc.perform(post("/api/boards")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Test Dungeon")))
                .andExpect(jsonPath("$.id", is(1)));
    }

    @Test
    void testGetAllBoards() throws Exception {
        Board board1 = new Board("Easy Dungeon", new int[][]{{1, 2}, {3, 4}});
        board1.setId(1L);
        Board board2 = new Board("Hard Dungeon", new int[][]{{-1, -2}, {-3, -4}});
        board2.setId(2L);

        when(boardRepository.findAll()).thenReturn(Arrays.asList(board1, board2));

        mockMvc.perform(get("/api/boards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Easy Dungeon")))
                .andExpect(jsonPath("$[1].name", is("Hard Dungeon")));
    }

    @Test
    void testGetBoardById() throws Exception {
        Board board = new Board("Test Board", new int[][]{{1, 2}, {3, 4}});
        board.setId(1L);

        when(boardRepository.findById(1L)).thenReturn(Optional.of(board));

        mockMvc.perform(get("/api/boards/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Test Board")))
                .andExpect(jsonPath("$.id", is(1)));
    }

    @Test
    void testGetBoardByIdNotFound() throws Exception {
        when(boardRepository.findById(anyLong())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/boards/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateBoard() throws Exception {
        Board existingBoard = new Board("Original Board", new int[][]{{1, 2}});
        existingBoard.setId(1L);

        int[][] newBoardData = {{-1, -2}, {-3, -4}};
        BoardController.BoardRequest request = new BoardController.BoardRequest();
        request.setName("Updated Board");
        request.setBoard(newBoardData);

        Board updatedBoard = new Board("Updated Board", newBoardData);
        updatedBoard.setId(1L);

        when(boardRepository.findById(1L)).thenReturn(Optional.of(existingBoard));
        when(boardRepository.save(any(Board.class))).thenReturn(updatedBoard);

        mockMvc.perform(put("/api/boards/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated Board")));
    }

    @Test
    void testDeleteBoard() throws Exception {
        when(boardRepository.existsById(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/boards/1"))
                .andExpect(status().isNoContent());

        verify(boardRepository).deleteById(1L);
    }

    @Test
    void testDeleteBoardNotFound() throws Exception {
        when(boardRepository.existsById(anyLong())).thenReturn(false);

        mockMvc.perform(delete("/api/boards/99999"))
                .andExpect(status().isNotFound());
    }
}
