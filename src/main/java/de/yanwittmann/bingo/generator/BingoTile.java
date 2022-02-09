package de.yanwittmann.bingo.generator;

import de.yanwittmann.bingo.generator.config.Category;
import de.yanwittmann.bingo.interfaces.Jsonable;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BingoTile implements Jsonable {

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    private final String text;
    private final String tooltip;
    private final double difficulty;
    private final List<Category> categories = new ArrayList<>();

    public BingoTile(String text, String tooltip, double difficulty) {
        this.text = text;
        this.tooltip = tooltip;
        this.difficulty = difficulty;
    }

    public BingoTile(JSONObject jsonObject) {
        text = jsonObject.getString("text");
        tooltip = jsonObject.getString("tooltip");
        difficulty = jsonObject.getDouble("difficulty");
        categories.addAll(jsonObject.getJSONArray("categories").toList().stream().map(s -> new Category(String.valueOf(s))).collect(Collectors.toList()));
    }

    public String getText() {
        return text;
    }

    public double getDifficulty() {
        return difficulty;
    }

    public String getTooltip() {
        return tooltip;
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
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("text", text);
        json.put("tooltip", tooltip);
        json.put("difficulty", difficulty);
        json.put("categories", categories.stream().map(Category::getName).collect(Collectors.toList()));
        return json;
    }

    @Override
    public String toString() {
        return text;
    }
}
