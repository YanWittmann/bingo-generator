package de.yanwittmann.bingo.generator;

import de.yanwittmann.bingo.interfaces.Weightable;

import java.util.*;
import java.util.regex.Matcher;

public class TileGenerator implements Weightable {

    private String text;
    private String tooltip;
    private double difficulty;
    private double weight;
    private final List<Category> categories;
    private final List<String> difficulties;
    private final Set<Category> derivedCategories;

    public TileGenerator(Map<String, Object> optionMap, Map<String, Category> categories, Map<String, List<TextSnippet>> textSnippets) {
        this.text = (String) optionMap.get(BingoConfiguration.KEY_TILE_GENERATOR_TEXT);
        this.tooltip = (String) optionMap.get(BingoConfiguration.KEY_TILE_GENERATOR_TOOLTIP);
        this.difficulty = Double.parseDouble(String.valueOf(optionMap.getOrDefault(BingoConfiguration.KEY_TILE_GENERATOR_DIFFICULTY, 1)));
        this.difficulties = (List<String>) optionMap.getOrDefault(BingoConfiguration.KEY_TILE_GENERATOR_DIFFICULTIES, Collections.emptyList());
        this.weight = Double.parseDouble(String.valueOf(optionMap.getOrDefault(BingoConfiguration.KEY_TILE_GENERATOR_WEIGHT, 1)));
        this.categories = new ArrayList<>();
        Category.createCategories((List<String>) optionMap.getOrDefault(BingoConfiguration.KEY_TILE_GENERATOR_CATEGORIES, Collections.emptyList()), categories, this.categories, text);
        this.derivedCategories = new HashSet<>();
        deriveSnippetCategories(text, textSnippets);
    }

    private void deriveSnippetCategories(String text, Map<String, List<TextSnippet>> textSnippets) {
        boolean found;
        do {
            found = false;
            Matcher snippetsMatcher = TextSnippet.SNIPPET_PATTERN.matcher(text);
            while (snippetsMatcher.find()) {
                String snippetType = snippetsMatcher.group(1);
                List<TextSnippet> snippets = textSnippets.get(snippetType);
                if (snippets != null && snippets.size() > 0) {
                    for (TextSnippet snippet : snippets) {
                        for (Category category : snippet.getCategories()) {
                            if (derivedCategories.add(category)) {
                                deriveSnippetCategories(snippet.getText(), textSnippets);
                            }
                        }
                    }
                    found = true;
                }
                text = text.replace(snippetType, "");
            }
        } while (found);
    }

    public boolean containsAnyCategory(Collection<Category> categories) {
        return this.categories.stream().anyMatch(categories::contains);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public double getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(double difficulty) {
        this.difficulty = difficulty;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public void addCategory(Category category) {
        this.categories.add(category);
    }

    public void addCategory(String category) {
        this.categories.add(new Category(category));
    }

    public Set<Category> getDerivedCategories() {
        return derivedCategories;
    }

    public List<String> getDifficulties() {
        return difficulties;
    }

    public String getTooltip() {
        return tooltip;
    }

    @Override
    public String toString() {
        return text;
    }

    public static boolean validate(Map<String, Object> optionMap) {
        BingoConfiguration.validateContained(optionMap, BingoConfiguration.KEY_TILE_GENERATOR_TEXT, true, String.class);
        BingoConfiguration.validateContained(optionMap, BingoConfiguration.KEY_TILE_GENERATOR_TOOLTIP, false, String.class);
        BingoConfiguration.validateContained(optionMap, BingoConfiguration.KEY_TILE_GENERATOR_DIFFICULTY, false, Double.class, Integer.class);
        BingoConfiguration.validateContained(optionMap, BingoConfiguration.KEY_TILE_GENERATOR_DIFFICULTIES, false, List.class);
        BingoConfiguration.validateContained(optionMap, BingoConfiguration.KEY_TILE_GENERATOR_WEIGHT, false, Double.class, Integer.class);
        BingoConfiguration.validateContained(optionMap, BingoConfiguration.KEY_TILE_GENERATOR_CATEGORIES, false, List.class);
        return true;
    }
}
