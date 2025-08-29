package com.example.dungeon.controller;

import com.example.dungeon.entity.Board;
import com.example.dungeon.repository.BoardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/boards")
public class BoardController {

    @Autowired
    private BoardRepository boardRepository;

    // GET all boards
    @GetMapping
    public List<Board> getAllBoards() {
        return boardRepository.findAll();
    }

    // GET board by ID
    @GetMapping("/{id}")
    public ResponseEntity<Board> getBoardById(@PathVariable Long id) {
        Optional<Board> board = boardRepository.findById(id);
        if (board.isPresent()) {
            return ResponseEntity.ok(board.get());
        }
        return ResponseEntity.notFound().build();
    }

    // POST create new board
    @PostMapping
    public ResponseEntity<Board> createBoard(@RequestBody BoardRequest request) {
        Board board = new Board(request.getName(), request.getBoard());
        Board savedBoard = boardRepository.save(board);
        return ResponseEntity.ok(savedBoard);
    }

    // PUT update board
    @PutMapping("/{id}")
    public ResponseEntity<Board> updateBoard(@PathVariable Long id, @RequestBody BoardRequest request) {
        Optional<Board> optionalBoard = boardRepository.findById(id);
        if (optionalBoard.isPresent()) {
            Board board = optionalBoard.get();
            board.setName(request.getName());
            board.setBoardArray(request.getBoard());
            return ResponseEntity.ok(boardRepository.save(board));
        }
        return ResponseEntity.notFound().build();
    }

    // DELETE board
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBoard(@PathVariable Long id) {
        if (boardRepository.existsById(id)) {
            boardRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // Inner class for request body
    public static class BoardRequest {
        private String name;
        private int[][] board;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int[][] getBoard() { return board; }
        public void setBoard(int[][] board) { this.board = board; }
    }
}
