package de.yanwittmann.bingo.generator;

import de.yanwittmann.bingo.generator.config.BingoBoardMetadata;
import de.yanwittmann.bingo.generator.config.Category;
import de.yanwittmann.bingo.interfaces.Jsonable;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public class BingoBoard implements Jsonable {

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    private final BingoTile[][] board;
    private BingoBoardMetadata boardMetadata;

    public BingoBoard(int width, int height) {
        board = new BingoTile[width][height];
    }

    public BingoBoard(JSONObject json) {
        this(json.getInt("width"), json.getInt("height"));
        if (json.has("metadata")) {
            boardMetadata = new BingoBoardMetadata(json.getJSONObject("metadata").toMap());
        }
        JSONArray jsonBoard = json.getJSONArray("board");
        for (int x = 0; x < getWidth(); x++) {
            JSONArray row = jsonBoard.getJSONArray(x);
            for (int y = 0; y < getHeight(); y++) {
                BingoTile tile = new BingoTile(row.getJSONObject(y));
                set(x, y, tile);
            }
        }
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

    public void setBoardMetadata(BingoBoardMetadata boardMetadata) {
        if (this.boardMetadata != null) {
            throw new IllegalStateException("Board metadata already set");
        }
        this.boardMetadata = boardMetadata;
    }

    public BingoBoardMetadata getBoardMetadata() {
        return boardMetadata;
    }

    public void populate(List<BingoTile> tiles, Map<String, Category> categories) {
        LOG.info("Populating board with [{}] tiles", tiles.size());
        int bestScore = Integer.MIN_VALUE;
        BingoTile[][] bestBoard = null;
        for (int i = 0; i < 1000; i++) {
            BingoTile[][] populatedBoard = populate(tiles);
            int score = scoreBoard(populatedBoard, categories);
            if (score > bestScore) {
                bestScore = score;
                bestBoard = populatedBoard;
                LOG.info("New best score [{}]", bestScore);
            }
        }
        // board is final, copy cells individually
        if (bestBoard != null) {
            for (int x = 0; x < getWidth(); x++) {
                if (getHeight() >= 0) System.arraycopy(bestBoard[x], 0, board[x], 0, getHeight());
            }
        }

        LOG.info("Board score is [{}]", bestScore);
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
        int diagonalRowLength = Math.min(getWidth(), getHeight());
        if (getWidth() >= getHeight()) {
            for (int x = 0; x < getWidth() - diagonalRowLength + 1; x++) {
                for (int y = 0; y < diagonalRowLength; y++) {
                    row.add(board[x + y][y]);
                }
                score += scoreRow(row, categories);
                row.clear();

                int i = 0;
                for (int y = getHeight() - 1; y >= 0; y--) {
                    row.add(board[x + i][y]);
                    i++;
                }
                score += scoreRow(row, categories);
                row.clear();
            }
        } else {
            for (int y = 0; y < getHeight() - diagonalRowLength + 1; y++) {
                for (int x = 0; x < diagonalRowLength; x++) {
                    row.add(board[x][y + x]);
                }
                score += scoreRow(row, categories);
                row.clear();

                int i = 0;
                for (int x = getWidth() - 1; x >= 0; x--) {
                    row.add(board[x][y + i]);
                    i++;
                }
                score += scoreRow(row, categories);
                row.clear();
            }
        }

        // give points for having a larger difficulty on outer layers compared to inner layers (circles)
        // example, a board like this, the numbers being the difficulties:
        //  1 2 3 4
        //  5 6 7 8
        //  9 10 11 12
        //  13 14 15 16
        // would have two layers, one the outer ring and one the inner ring.
        // the outer ring would have a score of (1+2+3+4+5+8+9+19+13+14+15+16 = 109), the inner ring would have a score of (6+7+10+11 = 34).
        int layers = (int) Math.ceil(Math.max(getWidth(), getHeight()) / 2.0);
        double[] totalDifficultyPerLayer = new double[layers];
        int[] elementsPerLayer = new int[layers];
        double centerX = (getWidth() - 1) / 2.0;
        double centerY = (getHeight() - 1) / 2.0;
        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                double distance = Math.max(Math.abs(x - centerX), Math.abs(y - centerY));
                int layer = (int) Math.floor(distance);
                totalDifficultyPerLayer[layer] += board[x][y].getDifficulty();
                elementsPerLayer[layer]++;
            }
        }
        for (int i = 0; i < layers; i++) {
            totalDifficultyPerLayer[i] /= elementsPerLayer[i];
        }
        for (int i = 0; i < layers - 1; i++) {
            if (totalDifficultyPerLayer[i] <= totalDifficultyPerLayer[i + 1]) {
                score += 5;
            } else {
                score -= 2;
            }
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

    @Override
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("width", getWidth());
        json.put("height", getHeight());
        if (boardMetadata != null) json.put("metadata", boardMetadata.toJson());
        JSONArray rows = new JSONArray();
        for (int x = 0; x < getWidth(); x++) {
            JSONArray row = new JSONArray();
            for (int y = 0; y < getHeight(); y++) {
                row.put(board[x][y].toJson());
            }
            rows.put(row);
        }
        json.put("board", rows);
        return json;
    }
}
