package de.yanwittmann.bingo.generator.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Category {

    private static final Logger LOG = LoggerFactory.getLogger(Category.class);

    private String name;
    private double maxAbsolute = Double.MAX_VALUE, maxRelative = Double.MAX_VALUE;
    private double minAbsolute = 0, minRelative = 0;
    private final List<Category> synergies = new ArrayList<>();
    private final List<Category> antisynergy = new ArrayList<>();

    public Category(Map<String, Object> optionMap, Map<String, Category> categories) {
        this.name = (String) optionMap.get(BingoConfiguration.KEY_CATEGORY_NAME);
        Map<String, Object> maxMap = (Map<String, Object>) optionMap.getOrDefault(BingoConfiguration.KEY_CATEGORY_MAX, null);
        Map<String, Object> minMap = (Map<String, Object>) optionMap.getOrDefault(BingoConfiguration.KEY_CATEGORY_MIN, null);
        if (maxMap != null) {
            if (maxMap.containsKey(BingoConfiguration.KEY_CATEGORY_ABSOLUTE)) {
                this.maxAbsolute = Double.parseDouble(String.valueOf(maxMap.get(BingoConfiguration.KEY_CATEGORY_ABSOLUTE)));
            }
            if (maxMap.containsKey(BingoConfiguration.KEY_CATEGORY_RELATIVE)) {
                this.maxRelative = Double.parseDouble(String.valueOf(maxMap.get(BingoConfiguration.KEY_CATEGORY_RELATIVE)));
            }
        }
        if (minMap != null) {
            if (minMap.containsKey(BingoConfiguration.KEY_CATEGORY_ABSOLUTE)) {
                this.minAbsolute = Double.parseDouble(String.valueOf(minMap.get(BingoConfiguration.KEY_CATEGORY_ABSOLUTE)));
            }
            if (minMap.containsKey(BingoConfiguration.KEY_CATEGORY_RELATIVE)) {
                this.minRelative = Double.parseDouble(String.valueOf(minMap.get(BingoConfiguration.KEY_CATEGORY_RELATIVE)));
            }
        }
        Category.createCategories((List<String>) optionMap.getOrDefault(BingoConfiguration.KEY_CATEGORY_SYNERGY, Collections.emptyList()), categories, this.synergies, name);
        Category.createCategories((List<String>) optionMap.getOrDefault(BingoConfiguration.KEY_CATEGORY_ANTISYNERGY, Collections.emptyList()), categories, this.antisynergy, name);
    }

    public Category(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getMaxAbsolute() {
        return maxAbsolute;
    }

    public void setMaxAbsolute(double maxAbsolute) {
        this.maxAbsolute = maxAbsolute;
    }

    public double getMaxRelative() {
        return maxRelative;
    }

    public void setMaxRelative(double maxRelative) {
        this.maxRelative = maxRelative;
    }

    public double getMinAbsolute() {
        return minAbsolute;
    }

    public void setMinAbsolute(double minAbsolute) {
        this.minAbsolute = minAbsolute;
    }

    public double getMinRelative() {
        return minRelative;
    }

    public void setMinRelative(double minRelative) {
        this.minRelative = minRelative;
    }

    public List<Category> getSynergies() {
        return synergies;
    }

    public List<Category> getAntisynergy() {
        return antisynergy;
    }

    public static boolean validate(Map<String, Object> optionMap) {
        BingoConfiguration.validateContained(optionMap, BingoConfiguration.KEY_CATEGORY_NAME, true, String.class);
        BingoConfiguration.validateContained(optionMap, BingoConfiguration.KEY_CATEGORY_MAX, false, Map.class);
        BingoConfiguration.validateContained(optionMap, BingoConfiguration.KEY_CATEGORY_MIN, false, Map.class);
        Map<String, Object> maxMap = (Map<String, Object>) optionMap.getOrDefault(BingoConfiguration.KEY_CATEGORY_MAX, null);
        Map<String, Object> minMap = (Map<String, Object>) optionMap.getOrDefault(BingoConfiguration.KEY_CATEGORY_MIN, null);
        if (maxMap != null) {
            BingoConfiguration.validateContained(maxMap, BingoConfiguration.KEY_CATEGORY_ABSOLUTE, false, Double.class, Integer.class);
            BingoConfiguration.validateContained(maxMap, BingoConfiguration.KEY_CATEGORY_RELATIVE, false, Double.class, Integer.class);
        }
        if (minMap != null) {
            BingoConfiguration.validateContained(minMap, BingoConfiguration.KEY_CATEGORY_ABSOLUTE, false, Double.class, Integer.class);
            BingoConfiguration.validateContained(minMap, BingoConfiguration.KEY_CATEGORY_RELATIVE, false, Double.class, Integer.class);
        }
        BingoConfiguration.validateContained(optionMap, BingoConfiguration.KEY_CATEGORY_ANTISYNERGY, false, List.class);
        BingoConfiguration.validateContained(optionMap, BingoConfiguration.KEY_CATEGORY_SYNERGY, false, List.class);
        return true;
    }

    public static void createCategories(List<String> categories, Map<String, Category> knownCategories, List<Category> addTo, String forWhat) {
        for (String category : categories) {
            if (!knownCategories.containsKey(category)) {
                LOG.warn("Category [{}] on text snippet {} does not exist yet, creating new", category, forWhat);
                Category cat = new Category(category);
                knownCategories.put(category, cat);
                addTo.add(cat);
            } else {
                addTo.add(knownCategories.get(category));
            }
        }
    }

    @Override
    public String toString() {
        return name;
    }
}
