package de.yanwittmann.bingo.generator;

import de.yanwittmann.bingo.generator.config.BingoConfiguration;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;

class BingoGeneratorTest {

    @Test
    void generateTest() throws FileNotFoundException {
        BingoConfiguration configuration = new BingoConfiguration(new File("src/test/resources/bingo/generate/test_outer_wilds.yaml"));
        BingoGenerator generator = new BingoGenerator(configuration);
        generator.setWidth(5);
        generator.setHeight(5);
        generator.setDifficultyLevel("Hard");
        System.out.println(generator.generateBingoBoard());
    }
}