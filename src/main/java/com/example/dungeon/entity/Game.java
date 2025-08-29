package com.example.dungeon.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "games")
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;
    
    @ManyToOne
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;
    
    @Column(nullable = false)
    private Integer result; // The minimum health required
    
    @Column(nullable = false)
    private LocalDateTime playedAt;

    // Default constructor
    public Game() {
        this.playedAt = LocalDateTime.now();
    }

    // Constructor with parameters
    public Game(Player player, Board board, Integer result) {
        this.player = player;
        this.board = board;
        this.result = result;
        this.playedAt = LocalDateTime.now();
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Board getBoard() {
        return board;
    }

    public void setBoard(Board board) {
        this.board = board;
    }

    public Integer getResult() {
        return result;
    }

    public void setResult(Integer result) {
        this.result = result;
    }

    public LocalDateTime getPlayedAt() {
        return playedAt;
    }

    public void setPlayedAt(LocalDateTime playedAt) {
        this.playedAt = playedAt;
    }
}
