package de.yanwittmann.bingo.generator;

import de.yanwittmann.bingo.interfaces.Weightable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextSnippet implements Weightable {

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    private String text;
    private String tooltip;
    private double difficulty;
    private double weight;
    private final List<Category> categories;
    private final Set<Category> derivedCategories;
    private final List<String> tags;

    public TextSnippet(Map<String, Object> optionMap, List<Category> categories) {
        this.text = (String) optionMap.get(BingoConfiguration.KEY_TEXT_SNIPPETS_TEXT);
        this.tooltip = (String) optionMap.getOrDefault(BingoConfiguration.KEY_TEXT_SNIPPETS_TOOLTIP, null);
        this.difficulty = Double.parseDouble(String.valueOf(optionMap.getOrDefault(BingoConfiguration.KEY_TEXT_SNIPPETS_DIFFICULTY, 0)));
        this.weight = Double.parseDouble(String.valueOf(optionMap.getOrDefault(BingoConfiguration.KEY_TEXT_SNIPPETS_WEIGHT, 1)));
        this.categories = new ArrayList<>();
        Category.createCategories((List<String>) optionMap.getOrDefault(BingoConfiguration.KEY_TEXT_SNIPPETS_CATEGORIES, Collections.emptyList()), categories, this.categories, text);
        this.derivedCategories = new HashSet<>();
        this.tags = (List<String>) optionMap.getOrDefault(BingoConfiguration.KEY_TEXT_SNIPPETS_TAGS, Collections.emptyList());
    }

    public void deriveSnippetCategories(String text, Map<String, List<TextSnippet>> textSnippets) {
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

    public String getText() {
        return text;
    }

    public double getDifficulty() {
        return difficulty;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setDifficulty(double difficulty) {
        this.difficulty = difficulty;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public double getWeight() {
        return weight;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public void addCategory(Category category) {
        categories.add(category);
    }

    public void removeCategory(Category category) {
        categories.remove(category);
    }

    public String getTooltip() {
        return tooltip;
    }

    public List<String> getTags() {
        return tags;
    }

    public boolean matchesCondition(String condition, String textSoFar) {
        if (condition == null) return true;

        switch (condition) {
            case "avoid_duplicates":
                return !textSoFar.contains(text);
        }

        Matcher matcher = Pattern.compile("^([a-zA-Z0-9!]+)(?:\\((.*)\\))?$").matcher(condition);
        if (!matcher.matches()) {
            LOG.warn("Invalid condition [{}] on [{}]", condition, text);
            return true;
        }
        String function = matcher.group(1);
        String args = matcher.group(2);
        switch (function) {
            case "tag":
                return tags.contains(args);
            case "category":
                return categories.stream().anyMatch(category -> category.getName().equals(args));
            case "derivedCategory":
                return derivedCategories.stream().anyMatch(category -> category.getName().equals(args));
            case "!tag":
                return !tags.contains(args);
            case "!category":
                return categories.stream().noneMatch(category -> category.getName().equals(args));
            case "!derivedCategory":
                return derivedCategories.stream().noneMatch(category -> category.getName().equals(args));
        }
        LOG.warn("Unknown condition function [{}] on [{}]", function, text);
        return true;
    }

    @Override
    public String toString() {
        return text;
    }

    public static boolean validate(Map<String, Object> optionMap) {
        BingoConfiguration.validateContained(optionMap, BingoConfiguration.KEY_TEXT_SNIPPETS_TEXT, true, String.class);
        BingoConfiguration.validateContained(optionMap, BingoConfiguration.KEY_TEXT_SNIPPETS_TOOLTIP, false, String.class);
        BingoConfiguration.validateContained(optionMap, BingoConfiguration.KEY_TEXT_SNIPPETS_CATEGORIES, false, List.class);
        BingoConfiguration.validateContained(optionMap, BingoConfiguration.KEY_TEXT_SNIPPETS_TAGS, false, List.class);
        BingoConfiguration.validateContained(optionMap, BingoConfiguration.KEY_TEXT_SNIPPETS_DIFFICULTY, false, Double.class, Integer.class);
        BingoConfiguration.validateContained(optionMap, BingoConfiguration.KEY_TEXT_SNIPPETS_WEIGHT, false, Double.class, Integer.class);
        return true;
    }

    final static Pattern SNIPPET_PATTERN = Pattern.compile("\\[(.*?)]");
}
