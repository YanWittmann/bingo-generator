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
    private final Map<String, Category> categories = new HashMap<>();

    public BingoConfiguration(File file) throws FileNotFoundException {
        parse(new Yaml().load(new FileInputStream(file)));
    }

    public List<TileGenerator> getTileGenerators() {
        return tileGenerators;
    }

    public Map<String, List<TextSnippet>> getTextSnippets() {
        return textSnippets;
    }

    public Map<String, Category> getCategories() {
        return categories;
    }

    private void parse(Object rootObject) {
        if (rootObject instanceof Map) {
            Map<String, Object> rootMap = (Map<String, Object>) rootObject;
            if (rootMap.containsKey((KEY_CATEGORY))) {
                Object optionsObject = rootMap.get(KEY_CATEGORY);
                if (optionsObject instanceof Map) {
                    Map<String, Object> optionsMap = (Map<String, Object>) optionsObject;
                    for (Map.Entry<String, Object> entry : optionsMap.entrySet()) {
                        if (entry.getValue() instanceof Map) {
                            Map<String, Object> optionMap = (Map<String, Object>) entry.getValue();
                            if (Category.validate(optionMap)) {
                                categories.put(entry.getKey(), new Category(optionMap));
                            }
                        }
                    }
                }
            }
        }

        if (rootObject instanceof Map) {
            Map<String, Object> rootMap = (Map<String, Object>) rootObject;
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
                                        textSnippets.computeIfAbsent(entry.getKey(), k -> new ArrayList<>()).add(new TextSnippet(optionMap, categories));
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
                                tileGenerators.add(new TileGenerator(optionMap, categories, textSnippets));
                            } else {
                                LOG.error("Invalid tile generator: {}", optionMap);
                            }
                        }
                    }
                }
            }
        }

        for (Map.Entry<String, List<TextSnippet>> snippet : textSnippets.entrySet()) {
            for (TextSnippet textSnippet : snippet.getValue()) {
                textSnippet.deriveSnippetCategories(snippet.getKey(), textSnippets);
            }
        }

        LOG.info("Loaded [{}] tile generators", tileGenerators.size());
        LOG.info("Loaded [{}] text snippet types with a total of [{}] snippets", textSnippets.size(), textSnippets.values().stream().mapToInt(List::size).sum());
        LOG.info("Loaded [{}] categories", categories.size());
    }

    public Map<Category, Integer> countCategories(List<BingoTile> tiles) {
        Map<Category, Integer> counts = new HashMap<>();
        for (BingoTile tile : tiles) {
            for (Category category : tile.getCategories()) {
                counts.compute(category, (k, v) -> v == null ? 1 : v + 1);
            }
        }
        for (Category category : categories.values()) {
            counts.computeIfAbsent(category, k -> 0);
        }
        return counts;
    }

    public BingoTile generateTile(List<BingoTile> existingTiles, int destAmount) {
        LOG.info("Generating tile with destination of [{}] and current [{}]", destAmount, existingTiles.size());
        // find what categories are allowed and not allowed in the existing tiles
        Map<Category, Integer> categoryCount = countCategories(existingTiles);
        Set<Category> createdMustBeCategories = new HashSet<>();
        Set<Category> createdMayNotBeCategories = new HashSet<>();
        for (Map.Entry<Category, Integer> category : categoryCount.entrySet()) {
            double min = Math.max(category.getKey().getMinAbsolute(), category.getKey().getMinRelative() * 0.01 * destAmount);
            double max = Math.ceil(Math.min(category.getKey().getMaxAbsolute(), category.getKey().getMaxRelative() * 0.01 * destAmount));
            int currentCount = category.getValue();
            if (currentCount < min) {
                createdMustBeCategories.add(category.getKey());
            } else if (currentCount >= max) {
                createdMayNotBeCategories.add(category.getKey());
            }
        }

        List<TileGenerator> filteredTileGenerators = this.tileGenerators;
        if (!createdMustBeCategories.isEmpty()) {
            for (int i = filteredTileGenerators.size() - 1; i >= 0; i--) {
                TileGenerator tileGenerator = filteredTileGenerators.get(i);
                List<Category> categories = tileGenerator.getCategories();
                Set<Category> derivedCategories = tileGenerator.getDerivedCategories();
                if (categories.stream().noneMatch(createdMustBeCategories::contains) && derivedCategories.stream().noneMatch(createdMustBeCategories::contains)) {
                    filteredTileGenerators.remove(i);
                }
            }
        }
        if (!createdMayNotBeCategories.isEmpty()) {
            for (int i = filteredTileGenerators.size() - 1; i >= 0; i--) {
                TileGenerator tileGenerator = filteredTileGenerators.get(i);
                List<Category> categories = tileGenerator.getCategories();
                if (categories.stream().anyMatch(createdMayNotBeCategories::contains)) {
                    filteredTileGenerators.remove(i);
                }
            }
        }

        LOG.info("there are [{}] possible generators for existing categories [{}]", filteredTileGenerators.size(), categoryCount);
        TileGenerator selectedGenerator = getRandom(filteredTileGenerators);
        String text = selectedGenerator.getText();
        AtomicReference<Double> currentDifficulty = new AtomicReference<>(selectedGenerator.getDifficulty());
        Set<Category> tileCategories = new HashSet<>(selectedGenerator.getCategories());

        boolean found;
        do {
            found = false;
            Matcher snippetsMatcher = TextSnippet.SNIPPET_PATTERN.matcher(text);
            while (snippetsMatcher.find()) {
                String snippetType = snippetsMatcher.group(1);
                List<TextSnippet> snippets = new ArrayList<>(textSnippets.getOrDefault(snippetType, new ArrayList<>()));
                if (snippets.size() > 0) {
                    if (!createdMustBeCategories.isEmpty()) {
                        for (int i = snippets.size() - 1; i >= 0; i--) {
                            TextSnippet snippet = snippets.get(i);
                            if (snippet.getCategories().stream().noneMatch(createdMustBeCategories::contains)) {
                                snippets.remove(i);
                            }
                        }
                    }
                    if (!createdMayNotBeCategories.isEmpty()) {
                        for (int i = snippets.size() - 1; i >= 0; i--) {
                            TextSnippet snippet = snippets.get(i);
                            if (snippet.getCategories().stream().anyMatch(createdMayNotBeCategories::contains)) {
                                snippets.remove(i);
                            }
                        }
                    }
                    if (snippets.size() == 0) {
                        snippets = textSnippets.getOrDefault(snippetType, new ArrayList<>());
                    }
                    TextSnippet selectedSnippet = getRandom(snippets);
                    currentDifficulty.set(currentDifficulty.get() + selectedSnippet.getDifficulty());
                    tileCategories.addAll(selectedSnippet.getCategories());
                    text = text.replaceFirst(Pattern.quote(snippetsMatcher.group()), selectedSnippet.getText());
                    found = true;
                } else {
                    LOG.error("No snippets found for type [{}]", snippetType);
                }
            }
        } while (found);

        BingoTile bingoTile = new BingoTile(text, currentDifficulty.get());
        tileCategories.forEach(bingoTile::addCategory);
        return bingoTile;
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

        private final Logger LOG = LoggerFactory.getLogger(getClass());

        private String text;
        private double difficulty;
        private double weight;
        private final List<Category> categories;
        private final Set<Category> derivedCategories;

        public TileGenerator(Map<String, Object> optionMap, Map<String, Category> categories, Map<String, List<TextSnippet>> textSnippets) {
            this.text = (String) optionMap.get(KEY_TILE_GENERATOR_TEXT);
            this.difficulty = Double.parseDouble(String.valueOf(optionMap.getOrDefault(KEY_TILE_GENERATOR_DIFFICULTY, 1)));
            this.weight = Double.parseDouble(String.valueOf(optionMap.getOrDefault(KEY_TILE_GENERATOR_WEIGHT, 1)));
            this.categories = new ArrayList<>();
            createCategories((List<String>) optionMap.getOrDefault(KEY_TILE_GENERATOR_CATEGORIES, Collections.emptyList()), categories);
            this.derivedCategories = new HashSet<>();
            deriveSnippetCategories(text, textSnippets);
            LOG.info("Derived [{}] categories for snippets on [{}]: {}", derivedCategories.size(), text, derivedCategories);
        }

        private void createCategories(List<String> categories, Map<String, Category> knownCategories) {
            for (String category : categories) {
                if (!knownCategories.containsKey(category)) {
                    LOG.warn("Category [{}] on tile generator {} does not exist yet, creating new", category, text);
                    knownCategories.put(category, new Category(category));
                } else {
                    this.categories.add(knownCategories.get(category));
                }
            }
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

        @Override
        public String toString() {
            return text;
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

        private final Logger LOG = LoggerFactory.getLogger(getClass());

        private String text;
        private double difficulty;
        private double weight;
        private final List<Category> categories;
        private final Set<Category> derivedCategories;

        public TextSnippet(Map<String, Object> optionMap, Map<String, Category> categories) {
            this.text = (String) optionMap.get(KEY_TEXT_SNIPPETS_TEXT);
            this.difficulty = Double.parseDouble(String.valueOf(optionMap.getOrDefault(KEY_TEXT_SNIPPETS_DIFFICULTY, 1)));
            this.weight = Double.parseDouble(String.valueOf(optionMap.getOrDefault(KEY_TEXT_SNIPPETS_WEIGHT, 1)));
            this.categories = new ArrayList<>();
            createCategories((List<String>) optionMap.getOrDefault(KEY_TEXT_SNIPPETS_CATEGORIES, Collections.emptyList()), categories);
            this.derivedCategories = new HashSet<>();
        }

        private void createCategories(List<String> categories, Map<String, Category> knownCategories) {
            for (String category : categories) {
                if (!knownCategories.containsKey(category)) {
                    LOG.warn("Category [{}] on text snippet {} does not exist yet, creating new", category, text);
                    Category cat = new Category(category);
                    knownCategories.put(category, cat);
                    this.categories.add(cat);
                } else {
                    this.categories.add(knownCategories.get(category));
                }
            }
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

        @Override
        public String toString() {
            return text;
        }

        public static boolean validate(Map<String, Object> optionMap) {
            validateContained(optionMap, KEY_TEXT_SNIPPETS_TEXT, true, String.class);
            validateContained(optionMap, KEY_TEXT_SNIPPETS_CATEGORIES, false, List.class);
            validateContained(optionMap, KEY_TEXT_SNIPPETS_DIFFICULTY, false, Double.class, Integer.class);
            validateContained(optionMap, KEY_TEXT_SNIPPETS_WEIGHT, false, Double.class, Integer.class);
            return true;
        }

        private final static Pattern SNIPPET_PATTERN = Pattern.compile("\\[(.*?)]");
    }

    public static class Category {
        public static final Category UNKNOWN = new Category("Unknown");
        private String name;
        private double maxAbsolute = Double.MAX_VALUE, maxRelative = Double.MAX_VALUE;
        private double minAbsolute = 0, minRelative = 0;

        public Category(Map<String, Object> optionMap) {
            this.name = (String) optionMap.get(KEY_CATEGORY_NAME);
            Map<String, Object> maxMap = (Map<String, Object>) optionMap.getOrDefault(KEY_CATEGORY_MAX, null);
            Map<String, Object> minMap = (Map<String, Object>) optionMap.getOrDefault(KEY_CATEGORY_MIN, null);
            if (maxMap != null) {
                if (maxMap.containsKey(KEY_CATEGORY_ABSOLUTE)) {
                    this.maxAbsolute = Double.parseDouble(String.valueOf(maxMap.get(KEY_CATEGORY_ABSOLUTE)));
                }
                if (maxMap.containsKey(KEY_CATEGORY_RELATIVE)) {
                    this.maxRelative = Double.parseDouble(String.valueOf(maxMap.get(KEY_CATEGORY_RELATIVE)));
                }
            }
            if (minMap != null) {
                if (minMap.containsKey(KEY_CATEGORY_ABSOLUTE)) {
                    this.minAbsolute = Double.parseDouble(String.valueOf(minMap.get(KEY_CATEGORY_ABSOLUTE)));
                }
                if (minMap.containsKey(KEY_CATEGORY_RELATIVE)) {
                    this.minRelative = Double.parseDouble(String.valueOf(minMap.get(KEY_CATEGORY_RELATIVE)));
                }
            }
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

        public static boolean validate(Map<String, Object> optionMap) {
            validateContained(optionMap, KEY_CATEGORY_NAME, true, String.class);
            validateContained(optionMap, KEY_CATEGORY_MAX, false, Map.class);
            validateContained(optionMap, KEY_CATEGORY_MIN, false, Map.class);
            Map<String, Object> maxMap = (Map<String, Object>) optionMap.getOrDefault(KEY_CATEGORY_MAX, null);
            Map<String, Object> minMap = (Map<String, Object>) optionMap.getOrDefault(KEY_CATEGORY_MIN, null);
            if (maxMap != null) {
                validateContained(maxMap, KEY_CATEGORY_ABSOLUTE, false, Double.class, Integer.class);
                validateContained(maxMap, KEY_CATEGORY_RELATIVE, false, Double.class, Integer.class);
            }
            if (minMap != null) {
                validateContained(minMap, KEY_CATEGORY_ABSOLUTE, false, Double.class, Integer.class);
                validateContained(minMap, KEY_CATEGORY_RELATIVE, false, Double.class, Integer.class);
            }
            return true;
        }

        @Override
        public String toString() {
            return name;
        }
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
    private final static String KEY_TEXT_SNIPPETS_CATEGORIES = "categories";
    private final static String KEY_TEXT_SNIPPETS_TEXT = "text";
    private final static String KEY_TEXT_SNIPPETS_DIFFICULTY = "difficulty";
    private final static String KEY_TEXT_SNIPPETS_WEIGHT = "weight";
    private final static String KEY_CATEGORY = "categories";
    private final static String KEY_CATEGORY_NAME = "name";
    private final static String KEY_CATEGORY_MAX = "max";
    private final static String KEY_CATEGORY_MIN = "min";
    private final static String KEY_CATEGORY_ABSOLUTE = "absolute";
    private final static String KEY_CATEGORY_RELATIVE = "relative";
}
