package de.yanwittmann.bingo.generator;

import de.yanwittmann.bingo.generator.config.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public class BingoBoard {

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    private final BingoTile[][] board;

    public BingoBoard(int width, int height) {
        board = new BingoTile[width][height];
    }

    public BingoTile get(int x, int y) {
        validateCoordinates(x, y);
        return board[x][y];
    }

    public void set(int x, int y, BingoTile value) {
        validateCoordinates(x, y);
        board[x][y] = value;
    }

    private void validateCoordinates(int x, int y) {
        if (x < 0 || x >= getWidth()) {
            throw new IllegalArgumentException("X coordinate out of bounds: " + x);
        }
        if (y < 0 || y >= getHeight()) {
            throw new IllegalArgumentException("Y coordinate out of bounds: " + y);
        }
    }

    public int getWidth() {
        return board.length;
    }

    public int getHeight() {
        return board[0].length;
    }

    public void populate(List<BingoTile> tiles, Map<String, Category> categories) {
        System.out.println(tiles);
        int bestScore = Integer.MIN_VALUE;
        BingoTile[][] bestBoard = null;
        for (int i = 0; i < 100; i++) {
            BingoTile[][] populatedBoard = populate(tiles);
            int score = scoreBoard(populatedBoard, categories);
            if (score > bestScore) {
                bestScore = score;
                bestBoard = populatedBoard;
                LOG.info("New best score: [{}]", score);
            }
        }
        // board is final, copy cells individually
        if (bestBoard != null) {
            for (int x = 0; x < getWidth(); x++) {
                if (getHeight() >= 0) System.arraycopy(bestBoard[x], 0, board[x], 0, getHeight());
            }
        }
    }

    /**
     * Calculates the score of the board.<br>
     * Score is determined by checking if rows contain synergies and antisynergies.<br>
     * The higher the score, the better the board.
     *
     * @param board      The board to score.
     * @param categories The categories to check the antisynergies and synergies of.
     * @return The score of the board. The higher, the better.
     */
    private int scoreBoard(BingoTile[][] board, Map<String, Category> categories) {
        // find vertical rows
        int score = 0;
        List<BingoTile> row = new ArrayList<>(getWidth());
        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                row.add(board[x][y]);
            }
            score += scoreRow(row, categories);
            row.clear();
        }

        // find horizontal rows
        for (int y = 0; y < getHeight(); y++) {
            for (int x = 0; x < getWidth(); x++) {
                row.add(board[x][y]);
            }
            score += scoreRow(row, categories);
            row.clear();
        }

        // find diagonal rows
        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                if (x < getHeight()) {
                    row.add(board[x][x]);
                }
            }
            score += scoreRow(row, categories);
            row.clear();

            for (int y = 0; y < getHeight(); y++) {
                if (x < getWidth() && y < getHeight()) {
                    row.add(board[getWidth() - x - 1][x]);
                }
            }
            score += scoreRow(row, categories);
            row.clear();
        }

        return score;
    }

    private int scoreRow(List<BingoTile> row, Map<String, Category> categories) {
        int score = 0;
        for (Map.Entry<String, Category> category : categories.entrySet()) {
            if (rowContainsCategory(row, category.getValue())) {
                // check for antisynergies
                for (Category antisynergy : category.getValue().getAntisynergy()) {
                    if (rowContainsCategory(row, antisynergy)) {
                        score -= 10;
                    }
                }
                // check for synergies
                for (Category synergy : category.getValue().getSynergies()) {
                    if (rowContainsCategory(row, synergy)) {
                        score += 4;
                    }
                }
            }
        }
        return score;
    }

    private boolean rowContainsCategory(List<BingoTile> row, Category category) {
        return row.stream().anyMatch(tile -> tile.getCategories().contains(category));
    }

    private BingoTile[][] populate(List<BingoTile> tiles) {
        tiles.sort((o1, o2) -> (int) (Math.random() * 2) - 1);
        BingoTile[][] board = new BingoTile[getWidth()][getHeight()];
        for (int i = 0; i < getWidth(); i++) {
            for (int j = 0; j < getHeight(); j++) {
                board[i][j] = tiles.get(i * getHeight() + j);
            }
        }
        return board;
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
