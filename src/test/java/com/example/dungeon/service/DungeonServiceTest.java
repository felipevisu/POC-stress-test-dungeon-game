package com.example.dungeon.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

class DungeonServiceTest {

    private DungeonService service;

    @BeforeEach
    void setUp() {
        service = new DungeonService();
    }

    @Test
    void testExampleCase() {
        int[][] board = {
                {-2, -3, 3},
                {-5, -10, 1},
                {10, 30, -5}
        };
        assertEquals(7, service.calculateMinimumHP(board));
    }

    @Test
    void testSingleCellPositive() {
        int[][] board = {{5}};
        assertEquals(1, service.calculateMinimumHP(board));
    }

    @Test
    void testSingleCellNegative() {
        int[][] board = {{-5}};
        assertEquals(6, service.calculateMinimumHP(board));
    }

    @Test
    void testAllZeros() {
        int[][] board = {
                {0, 0},
                {0, 0}
        };
        assertEquals(1, service.calculateMinimumHP(board));
    }

    @Test
    void testLargeDungeon() {
        int[][] board = {
                {-3, 5, -1, 4},
                {-2, -10, 3, -1},
                {4, -5, -2, 8},
                {-1, 3, -4, 2}
        };
        int result = service.calculateMinimumHP(board);
        assertTrue(result > 0, "Minimum health should be positive");
    }

    @Test
    void testAllPositiveValues() {
        int[][] board = {
                {1, 2, 3},
                {4, 5, 6},
                {7, 8, 9}
        };
        assertEquals(1, service.calculateMinimumHP(board));
    }

    @Test
    void testAllNegativeValues() {
        int[][] board = {
                {-1, -2, -3},
                {-4, -5, -6},
                {-7, -8, -9}
        };
        int result = service.calculateMinimumHP(board);
        assertTrue(result > 1, "Should need more than 1 health for all negative values");
    }

    @Test
    void testSingleRow() {
        int[][] board = {{-3, 5, -1, 4}};
        int result = service.calculateMinimumHP(board);
        assertTrue(result > 0, "Minimum health should be positive");
    }

    @Test
    void testSingleColumn() {
        int[][] board = {{-3}, {5}, {-1}, {4}};
        int result = service.calculateMinimumHP(board);
        assertTrue(result > 0, "Minimum health should be positive");
    }
}
