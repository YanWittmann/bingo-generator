package de.yanwittmann.bingo.generator;

import java.util.StringJoiner;

public class BingoBoard {

    private final BingoTile[][] board;

    public BingoBoard(int width, int height) {
        board = new BingoTile[width][height];
    }

    public BingoTile get(int x, int y) {
        return board[x][y];
    }

    public void set(int x, int y, BingoTile value) {
        board[x][y] = value;
    }

    public int getWidth() {
        return board.length;
    }

    public int getHeight() {
        return board[0].length;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int y = 0; y < getHeight(); y++) {
            StringJoiner row = new StringJoiner(",  ");
            for (int x = 0; x < getWidth(); x++) {
                row.add(String.valueOf(board[x][y]));
            }
            sb.append(row).append("\n");
        }
        return sb.toString();
    }
}
