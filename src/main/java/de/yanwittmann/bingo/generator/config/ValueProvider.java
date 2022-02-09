package de.yanwittmann.bingo.generator.config;

import java.util.HashMap;
import java.util.Map;

public class ValueProvider {
    private final Map<String, NumberProvider> difficultyProviders = new HashMap<>();

    public ValueProvider(Map<String, Object> optionMap) {
        for (Map.Entry<String, Object> difficultyEntry : optionMap.entrySet()) {
            difficultyProviders.put(difficultyEntry.getKey(), new NumberProvider((Map<String, Object>) difficultyEntry.getValue()));
        }
    }

    public NumberProviderResult getValue(String difficulty) {
        return difficultyProviders.get(difficulty).getValue();
    }

    public static boolean validate(Map<String, Object> optionMap) {
        for (Map.Entry<String, Object> difficultyEntry : optionMap.entrySet()) {
            if (difficultyEntry.getValue() instanceof Map) {
                BingoConfiguration.validateContained((Map<String, Object>) difficultyEntry.getValue(), BingoConfiguration.KEY_VALUE_PROVIDERS_MIN, true, Integer.class);
                BingoConfiguration.validateContained((Map<String, Object>) difficultyEntry.getValue(), BingoConfiguration.KEY_VALUE_PROVIDERS_MAX, true, Integer.class);
                BingoConfiguration.validateContained((Map<String, Object>) difficultyEntry.getValue(), BingoConfiguration.KEY_VALUE_PROVIDERS_SCORE, true, Double.class, Integer.class);
            } else {
                throw new IllegalArgumentException("Difficulty value provider must be a map");
            }
        }
        return true;
    }

    private static class NumberProvider {
        private final int min, max;
        private final double score;

        public NumberProvider(Map<String, Object> optionMap) {
            this.min = (int) optionMap.get(BingoConfiguration.KEY_VALUE_PROVIDERS_MIN);
            this.max = (int) optionMap.get(BingoConfiguration.KEY_VALUE_PROVIDERS_MAX);
            this.score = Double.parseDouble(String.valueOf(optionMap.getOrDefault(BingoConfiguration.KEY_VALUE_PROVIDERS_SCORE, 1)));
        }

        public NumberProviderResult getValue() {
            double random = Math.round(Math.random() * (max - min) + min);
            return new NumberProviderResult((int) (random), score);
        }

        public int getMin() {
            return min;
        }

        public int getMax() {
            return max;
        }

        public double getScore() {
            return score;
        }
    }

    static class NumberProviderResult {
        private final int value;
        private final double score;

        public NumberProviderResult(int value, double score) {
            this.value = value;
            this.score = score;
        }

        public int getValue() {
            return value;
        }

        public double getScore() {
            return score;
        }
    }
}
