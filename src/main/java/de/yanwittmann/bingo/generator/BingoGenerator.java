package de.yanwittmann.bingo.generator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class BingoGenerator {

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    private BingoConfiguration configuration;
    private double difficulty = 1;
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
        for (int i = 0; i < (width * height) * 2; i++) {
            fillBoard(tiles);
            removeByDifficulty(tiles);
        }
        fillBoard(tiles);

        BingoBoard board = new BingoBoard(width, height);
        tiles.sort((o1, o2) -> (int) (Math.random() * 2) - 1);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                board.set(i, j, tiles.get(i * height + j));
            }
        }

        LOG.info("Board difficulty is [{}]", calculateDifficulty(tiles));

        return board;
    }

    private void removeByDifficulty(List<BingoTile> tiles) {
        tiles.sort((o1, o2) -> Double.compare(o2.getDifficulty(), o1.getDifficulty()));
        double currentDifficulty = calculateDifficulty(tiles);
        if (currentDifficulty > difficulty) {
            tiles.remove(0);
        } else {
            tiles.remove(tiles.size() - 1);
        }
    }

    private void fillBoard(List<BingoTile> tiles) {
        int tileCount = width * height;
        while (tiles.size() < tileCount) tiles.add(configuration.generateTile());
    }

    private double calculateDifficulty(List<BingoTile> tiles) {
        return tiles.stream().mapToDouble(BingoTile::getDifficulty).average().orElse(0.0);
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
