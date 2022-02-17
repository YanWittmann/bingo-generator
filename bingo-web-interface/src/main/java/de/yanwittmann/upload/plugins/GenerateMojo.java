package de.yanwittmann.upload.plugins;

import de.yanwittmann.bingo.generator.BingoConfiguration;
import de.yanwittmann.bingo.generator.BingoGenerator;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Random;

@Mojo(name = "generate-board", requiresProject = false)
public class GenerateMojo extends AbstractMojo {

    @Parameter(defaultValue = "true")
    private boolean active;

    @Parameter(defaultValue = "5")
    private int width;

    @Parameter(defaultValue = "5")
    private int height;

    @Parameter(defaultValue = "-1")
    private int seed;

    @Parameter(defaultValue = "-1")
    private int generationAttempts;

    @Parameter(defaultValue = "")
    private Object difficulty;

    @Parameter(required = true)
    private File configurationFile;

    @Parameter(required = true)
    private File outputFile;

    public void execute() throws MojoExecutionException {
        if (!active) {
            getLog().info("Board generation is disabled by configuration.");
            return;
        }

        getLog().info("Generating board with:");
        getLog().info("width " + width + ", height " + height);
        getLog().info("Difficulty: " + difficulty);
        getLog().info("Configuration: " + configurationFile);

        BingoConfiguration bingoConfiguration;
        try {
            bingoConfiguration = new BingoConfiguration(configurationFile);
        } catch (FileNotFoundException e) {
            throw new MojoExecutionException("Configuration file not found: " + configurationFile, e);
        }

        BingoGenerator bingoGenerator = new BingoGenerator(bingoConfiguration);
        if (generationAttempts > 0) {
            bingoGenerator.setMaxGenerationAttempts(generationAttempts);
        }
        int difficultyType = -1;
        if (difficulty instanceof Double && ((Double) difficulty) > 0) {
            bingoGenerator.setDifficulty((Double) difficulty);
            difficultyType = 1;
        } else if (difficulty instanceof Integer && ((Integer) difficulty) > 0) {
            bingoGenerator.setDifficulty((Integer) difficulty);
            difficultyType = 2;
        } else if (difficulty instanceof String && !difficulty.equals("")) {
            if (((String) difficulty).matches("[0-9]+")) {
                bingoGenerator.setDifficulty(Integer.parseInt((String) difficulty));
                difficultyType = 3;
            } else if (((String) difficulty).matches("[0-9]+.?[0-9]*")) {
                bingoGenerator.setDifficulty(Double.parseDouble((String) difficulty));
                difficultyType = 4;
            } else {
                bingoGenerator.setDifficultyLevel((String) difficulty);
                difficultyType = 5;
            }
        } else {
            bingoGenerator.setDifficulty(-1);
            difficultyType = 6;
        }
        getLog().info("Effective difficulty [" + bingoGenerator.getDifficulty() + "] of type [" + difficultyType + "]");
        bingoGenerator.setWidth(width);
        bingoGenerator.setHeight(height);
        Random random;
        if (seed != -1) {
            random = new Random(seed);
        } else {
            random = new Random();
        }
        JSONObject bingoJson = bingoGenerator.generateBingoBoard(random).toJson();

        try {
            FileUtils.write(outputFile, bingoJson.toString(), StandardCharsets.UTF_8);
            getLog().info("Board written to " + outputFile.getAbsolutePath());
        } catch (IOException e) {
            throw new MojoExecutionException("Could not write board to file: " + outputFile, e);
        }
    }
}