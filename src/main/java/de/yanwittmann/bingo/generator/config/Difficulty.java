package de.yanwittmann.bingo.generator.config;

import java.util.Map;

public class Difficulty {
    private final String name;
    private final double score;

    public Difficulty(Map<String, Object> optionMap) {
        this.name = (String) optionMap.get(BingoConfiguration.KEY_DIFFICULTY_NAME);
        this.score = Double.parseDouble(String.valueOf(optionMap.getOrDefault(BingoConfiguration.KEY_DIFFICULTY_SCORE, 1)));
    }

    public String getName() {
        return name;
    }

    public double getScore() {
        return score;
    }

    public static boolean validate(Map<String, Object> optionMap) {
        BingoConfiguration.validateContained(optionMap, BingoConfiguration.KEY_DIFFICULTY_NAME, true, String.class);
        BingoConfiguration.validateContained(optionMap, BingoConfiguration.KEY_DIFFICULTY_SCORE, true, Double.class, Integer.class);
        return true;
    }
}
