package de.yanwittmann.bingo.generator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BingoConfiguration {

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    private final List<TileGenerator> tileGenerators = new ArrayList<>();
    private final Map<String, List<TextSnippet>> textSnippets = new HashMap<>();

    public BingoConfiguration(File file) throws FileNotFoundException {
        parse(new Yaml().load(new FileInputStream(file)));
    }

    private void parse(Object rootObject) {
        if (rootObject instanceof Map) {
            Map<String, Object> rootMap = (Map<String, Object>) rootObject;

            if (rootMap.containsKey(KEY_TILE_GENERATOR)) {
                Object optionsObject = rootMap.get(KEY_TILE_GENERATOR);
                if (optionsObject instanceof List) {
                    List<Object> optionsList = (List<Object>) optionsObject;
                    for (Object optionObject : optionsList) {
                        if (optionObject instanceof Map) {
                            Map<String, Object> optionMap = (Map<String, Object>) optionObject;
                            if (TileGenerator.validate(optionMap)) {
                                tileGenerators.add(new TileGenerator(optionMap));
                            } else {
                                LOG.error("Invalid tile generator: {}", optionMap);
                            }
                        }
                    }
                }
            }


            if (rootMap.containsKey(KEY_TEXT_SNIPPETS)) {
                Object optionsObject = rootMap.get(KEY_TEXT_SNIPPETS);
                if (optionsObject instanceof Map) {
                    Map<String, Object> optionsMap = (Map<String, Object>) optionsObject;
                    for (Map.Entry<String, Object> entry : optionsMap.entrySet()) {
                        if (entry.getValue() instanceof List) {
                            List<Object> optionsList = (List<Object>) entry.getValue();
                            for (Object optionObject : optionsList) {
                                if (optionObject instanceof Map) {
                                    Map<String, Object> optionMap = (Map<String, Object>) optionObject;
                                    if (TextSnippet.validate(optionMap)) {
                                        textSnippets.computeIfAbsent(entry.getKey(), k -> new ArrayList<>()).add(new TextSnippet(optionMap));
                                    } else {
                                        LOG.error("Invalid text snippet: {}", optionMap);
                                    }
                                }
                            }
                        }
                    }
                }

            }

        }

        LOG.info("Loaded [{}] tile generators", tileGenerators.size());
        LOG.info("Loaded [{}] text snippet types with a total of [{}] snippets", textSnippets.size(), textSnippets.values().stream().mapToInt(List::size).sum());
    }

    public BingoTile generateTile() {
        TileGenerator selectedGenerator = getRandom(tileGenerators);
        String text = selectedGenerator.getText();
        AtomicReference<Double> currentDifficulty = new AtomicReference<>(selectedGenerator.getDifficulty());

        boolean found;
        do {
            found = false;
            Matcher snippetsMatcher = TextSnippet.SNIPPET_PATTERN.matcher(text);
            while (snippetsMatcher.find()) {
                String snippetType = snippetsMatcher.group(1);
                List<TextSnippet> snippets = textSnippets.get(snippetType);
                if (snippets != null) {
                    TextSnippet selectedSnippet = getRandom(snippets);
                    currentDifficulty.set(currentDifficulty.get() + selectedSnippet.getDifficulty());
                    text = text.replaceFirst(Pattern.quote(snippetsMatcher.group()), selectedSnippet.getText());
                    found = true;
                } else {
                    LOG.error("No snippets found for type [{}]", snippetType);
                }
            }
        } while (found);

        return new BingoTile(text, currentDifficulty.get());
    }

    public <T extends Weightable> T getRandom(Collection<T> collection) {
        double totalWeight = collection.stream().mapToDouble(Weightable::getWeight).sum();
        double random = new Random().nextDouble() * totalWeight;
        double currentWeight = 0;
        for (T weightable : collection) {
            currentWeight += weightable.getWeight();
            if (currentWeight >= random) {
                return weightable;
            }
        }
        return collection.stream().findAny().orElse(null);
    }

    public static class TileGenerator implements Weightable {
        private String text;
        private double difficulty;
        private double weight;
        private List<String> categories;

        public TileGenerator(Map<String, Object> optionMap) {
            this.text = (String) optionMap.get(KEY_TILE_GENERATOR_TEXT);
            this.difficulty = Double.parseDouble(String.valueOf(optionMap.getOrDefault(KEY_TILE_GENERATOR_DIFFICULTY, 1)));
            this.weight = Double.parseDouble(String.valueOf(optionMap.getOrDefault(KEY_TILE_GENERATOR_WEIGHT, 1)));
            this.categories = (List<String>) optionMap.getOrDefault(KEY_TILE_GENERATOR_CATEGORIES, Collections.emptyList());
        }

        public TileGenerator(String text, double difficulty, List<String> categories) {
            this.text = text;
            this.categories = categories;
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

        public List<String> getCategories() {
            return categories;
        }

        public void setCategories(List<String> categories) {
            this.categories = categories;
        }

        public static boolean validate(Map<String, Object> optionMap) {
            validateContained(optionMap, KEY_TILE_GENERATOR_TEXT, true, String.class);
            validateContained(optionMap, KEY_TILE_GENERATOR_DIFFICULTY, false, Double.class, Integer.class);
            validateContained(optionMap, KEY_TILE_GENERATOR_WEIGHT, false, Double.class, Integer.class);
            validateContained(optionMap, KEY_TILE_GENERATOR_CATEGORIES, false, List.class);
            return true;
        }
    }

    public static class TextSnippet implements Weightable {
        private String text;
        private double difficulty;
        private double weight;

        public TextSnippet(Map<String, Object> optionMap) {
            this.text = (String) optionMap.get(KEY_TEXT_SNIPPETS_TEXT);
            this.difficulty = Double.parseDouble(String.valueOf(optionMap.getOrDefault(KEY_TEXT_SNIPPETS_DIFFICULTY, 1)));
            this.weight = Double.parseDouble(String.valueOf(optionMap.getOrDefault(KEY_TEXT_SNIPPETS_WEIGHT, 1)));
        }

        public TextSnippet(String text, double difficulty) {
            this.text = text;
            this.difficulty = difficulty;
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

        public static boolean validate(Map<String, Object> optionMap) {
            validateContained(optionMap, KEY_TEXT_SNIPPETS_TEXT, true, String.class);
            validateContained(optionMap, KEY_TEXT_SNIPPETS_DIFFICULTY, false, Double.class, Integer.class);
            validateContained(optionMap, KEY_TEXT_SNIPPETS_WEIGHT, false, Double.class, Integer.class);
            return true;
        }

        private final static Pattern SNIPPET_PATTERN = Pattern.compile("\\[(.*?)]");
    }

    private static void validateContained(Map<String, Object> optionMap, String key, boolean forcePresent, Class<?>... clazz) {
        if (!optionMap.containsKey(key)) {
            if (forcePresent) {
                throw new IllegalArgumentException("Missing required option: [" + key + "] in " + optionMap);
            }
            return;
        }
        if (clazz.length > 0) {
            for (Class<?> clazz1 : clazz) {
                if (clazz1.isInstance(optionMap.get(key))) {
                    return;
                }
            }
            throw new IllegalArgumentException("Option [" + key + "] is not of type " + clazz[0].getName() + " in " + optionMap);
        }
    }

    private final static String KEY_TILE_GENERATOR = "tile generators";
    private final static String KEY_TILE_GENERATOR_TEXT = "text";
    private final static String KEY_TILE_GENERATOR_DIFFICULTY = "difficulty";
    private final static String KEY_TILE_GENERATOR_WEIGHT = "weight";
    private final static String KEY_TILE_GENERATOR_CATEGORIES = "categories";
    private final static String KEY_TEXT_SNIPPETS = "snippets";
    private final static String KEY_TEXT_SNIPPETS_TEXT = "text";
    private final static String KEY_TEXT_SNIPPETS_DIFFICULTY = "difficulty";
    private final static String KEY_TEXT_SNIPPETS_WEIGHT = "weight";
}
