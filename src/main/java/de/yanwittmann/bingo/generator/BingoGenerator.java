package de.yanwittmann.bingo.generator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class BingoGenerator {

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    private BingoConfiguration configuration;
    private double difficulty = 2;
    private int width = 5;
    private int height = 5;

    public BingoGenerator(BingoConfiguration configuration) {
        this.configuration = configuration;
    }

    public BingoBoard generateBingoBoard() {
        if (configuration == null) {
            throw new IllegalStateException("Bingo configuration is not set.");
        }

        List<BingoTile> tiles = new ArrayList<>();
        for (int i = 0; i < (width * height) * 5; i++) {
            createAndRemoveTiles(tiles, width * height);
        }

        BingoBoard board = new BingoBoard(width, height);
        board.populate(tiles, configuration.getCategories());

        LOG.info("Board difficulty is [{}]", calculateDifficulty(tiles));
        LOG.info("Categories [{}]", configuration.countCategories(tiles));

        return board;
    }

    private void createAndRemoveTiles(List<BingoTile> tiles, int maxTileCount) {
        for (int i = 0; i < maxTileCount; i++) {
            fillBoard(tiles);
            ArrayList<BingoTile> backup = new ArrayList<>(tiles);
            removeByDifficulty(tiles, (width + height) / 2);
            fillBoard(tiles);
            if (distanceToDestinationDifficulty(calculateDifficulty(tiles)) > distanceToDestinationDifficulty(calculateDifficulty(backup))) {
                tiles = new ArrayList<>(backup);
                removeRandom(tiles, 2);
                fillBoard(tiles);
                if (distanceToDestinationDifficulty(calculateDifficulty(tiles)) > distanceToDestinationDifficulty(calculateDifficulty(backup))) {
                    tiles = backup;
                }
            }
        }
    }

    private void removeByDifficulty(List<BingoTile> tiles, int amount) {
        tiles.sort((o1, o2) -> Double.compare(o2.getDifficulty(), o1.getDifficulty()));
        for (int i = 0; i < amount && tiles.size() > 0; i++) {
            double currentDifficulty = calculateDifficulty(tiles);
            if (currentDifficulty > difficulty) {
                tiles.remove(0);
            } else {
                tiles.remove(tiles.size() - 1);
            }
        }
    }

    private void removeRandom(List<BingoTile> tiles, int amount) {
        for (int i = 0; i < amount && tiles.size() > 0; i++) {
            tiles.remove((int) Math.abs(Math.random() * tiles.size()));
        }
    }

    private void fillBoard(List<BingoTile> tiles) {
        int tileCount = width * height;
        while (tiles.size() < tileCount) tiles.add(configuration.generateTile(tiles, width * height, difficulty));
    }

    private double calculateDifficulty(List<BingoTile> tiles) {
        return tiles.stream().mapToDouble(BingoTile::getDifficulty).average().orElse(0.0);
    }

    private double distanceToDestinationDifficulty(double difficulty) {
        return Math.abs(difficulty - this.difficulty);
    }

    public BingoConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(BingoConfiguration configuration) {
        this.configuration = configuration;
    }

    public double getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(double difficulty) {
        this.difficulty = difficulty;
    }

    public void setDifficultyLevel(int difficulty) {
        this.difficulty = configuration.getDifficultyForLevel(difficulty);
    }

    public void setDifficultyLevel(String difficulty) {
        this.difficulty = configuration.getDifficultyForLevel(difficulty);
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}