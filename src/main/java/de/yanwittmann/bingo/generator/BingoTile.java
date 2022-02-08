package de.yanwittmann.bingo.generator;

public class BingoTile {

    private final String text;
    private final double difficulty;

    public BingoTile(String text, double difficulty) {
        this.text = text;
        this.difficulty = difficulty;
    }

    public String getText() {
        return text;
    }

    public double getDifficulty() {
        return difficulty;
    }

    @Override
    public String toString() {
        return text;
    }
}
