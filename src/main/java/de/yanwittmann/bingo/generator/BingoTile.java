package de.yanwittmann.bingo.generator;

import de.yanwittmann.bingo.generator.config.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class BingoTile {

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    private final String text;
    private final double difficulty;
    private final List<Category> categories = new ArrayList<>();

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

    public void addCategory(Category category) {
        if (category == null || categories.contains(category)) {
            LOG.warn("Not adding category [{}] to [{}]", category, text);
            return;
        }
        categories.add(category);
    }

    public List<Category> getCategories() {
        return categories;
    }

    @Override
    public String toString() {
        return text;
    }
}
