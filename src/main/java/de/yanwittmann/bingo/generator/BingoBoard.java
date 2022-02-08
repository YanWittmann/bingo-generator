package de.yanwittmann.bingo.generator;

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
            for (int x = 0; x < getWidth(); x++) {
                sb.append(board[x][y]);
                sb.append(" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
