package com.example.dungeon.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Entity
@Table(name = "boards")
public class Board {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String boardData; // Store the 2D array as JSON string

    private static final ObjectMapper objectMapper = new ObjectMapper();

    // Default constructor
    public Board() {}

    // Constructor with parameters
    public Board(String name, int[][] board) {
        this.name = name;
        setBoardArray(board);
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBoardData() {
        return boardData;
    }

    public void setBoardData(String boardData) {
        this.boardData = boardData;
    }

    // Helper methods to convert between int[][] and JSON string
    public int[][] getBoardArray() {
        if (boardData == null) return null;
        try {
            return objectMapper.readValue(boardData, int[][].class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error parsing board data", e);
        }
    }

    public void setBoardArray(int[][] board) {
        try {
            this.boardData = objectMapper.writeValueAsString(board);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing board data", e);
        }
    }
}
