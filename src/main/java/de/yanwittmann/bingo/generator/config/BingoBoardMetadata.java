package de.yanwittmann.bingo.generator.config;

import de.yanwittmann.bingo.interfaces.Jsonable;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BingoBoardMetadata implements Jsonable {

    private final String title;
    private final String game;
    private final String description;
    private final String version;
    private final List<String> authors;

    public BingoBoardMetadata(Map<String, Object> optionMap) {
        title = String.valueOf(optionMap.getOrDefault(BingoConfiguration.KEY_GENERAL_TITLE, null));
        game = String.valueOf(optionMap.getOrDefault(BingoConfiguration.KEY_GENERAL_GAME, null));
        description = String.valueOf(optionMap.getOrDefault(BingoConfiguration.KEY_GENERAL_DESCRIPTION, null));
        version = String.valueOf(optionMap.getOrDefault(BingoConfiguration.KEY_GENERAL_VERSION, null));
        authors = (List<String>) optionMap.getOrDefault(BingoConfiguration.KEY_GENERAL_AUTHORS, new ArrayList<>());
    }

    public String getTitle() {
        return title;
    }

    public String getGame() {
        return game;
    }

    public String getDescription() {
        return description;
    }

    public String getVersion() {
        return version;
    }

    public List<String> getAuthors() {
        return authors;
    }

    public static boolean validate(Map<String, Object> optionMap) {
        BingoConfiguration.validateContained(optionMap, BingoConfiguration.KEY_GENERAL_TITLE, false, String.class);
        BingoConfiguration.validateContained(optionMap, BingoConfiguration.KEY_GENERAL_GAME, false, String.class);
        BingoConfiguration.validateContained(optionMap, BingoConfiguration.KEY_GENERAL_DESCRIPTION, false, String.class);
        BingoConfiguration.validateContained(optionMap, BingoConfiguration.KEY_GENERAL_VERSION, false, String.class, Integer.class, Double.class);
        BingoConfiguration.validateContained(optionMap, BingoConfiguration.KEY_GENERAL_AUTHORS, false, List.class);
        return true;
    }

    @Override
    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        if (title != null) jsonObject.put(BingoConfiguration.KEY_GENERAL_TITLE, title);
        if (game != null) jsonObject.put(BingoConfiguration.KEY_GENERAL_GAME, game);
        if (description != null) jsonObject.put(BingoConfiguration.KEY_GENERAL_DESCRIPTION, description);
        if (version != null) jsonObject.put(BingoConfiguration.KEY_GENERAL_VERSION, version);
        if (authors != null) jsonObject.put(BingoConfiguration.KEY_GENERAL_AUTHORS, authors);
        return jsonObject;
    }
}
