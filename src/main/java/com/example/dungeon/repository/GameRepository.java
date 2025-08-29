package com.example.dungeon.repository;

import com.example.dungeon.entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {
    List<Game> findByPlayerId(Long playerId);
    List<Game> findByBoardId(Long boardId);
}
