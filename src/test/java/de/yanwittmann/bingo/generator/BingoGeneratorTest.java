package de.yanwittmann.bingo.generator;

import de.yanwittmann.bingo.generator.config.BingoConfiguration;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

class BingoGeneratorTest {

    @Test
    void generateTest() throws FileNotFoundException {
        BingoConfiguration configuration = new BingoConfiguration(new File("src/test/resources/bingo/generate/outer_wilds.yaml"));
        BingoGenerator generator = new BingoGenerator(configuration);
        generator.setWidth(5);
        generator.setHeight(10);
        generator.setDifficultyLevel("Hard");
        BingoBoard bingoBoard = generator.generateBingoBoard();
        System.out.println(bingoBoard);
        System.out.println(bingoBoard.toJson());
    }

    @Test
    public void loadTest() throws IOException {
        BingoBoard bingoBoard = new BingoBoard(new JSONObject(String.join("", FileUtils.readLines(new File("src/test/resources/bingo/load/outer_wilds.json"), StandardCharsets.UTF_8))));
        System.out.println(bingoBoard);
        System.out.println(bingoBoard.toJson());
    }
}
