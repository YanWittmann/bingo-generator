package de.yanwittmann.upload.plugins;

import de.yanwittmann.bingo.BingoBoard;
import de.yanwittmann.upload.BingoDatabaseInterface;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URL;

@Mojo(name = "upload-board", requiresProject = false)
public class UploadMojo extends AbstractMojo {

    @Parameter(required = false, defaultValue = "true")
    private boolean active;

    @Parameter(required = true)
    private File boardJson;

    @Parameter(required = true)
    private URL apiUrl;

    public void execute() throws MojoExecutionException {
        if (!active) {
            getLog().info("Uploading is disabled by configuration.");
            return;
        }

        getLog().info("Uploading board to " + apiUrl);
        getLog().info("Board file: " + boardJson.getAbsolutePath());

        BingoBoard board;
        try {
            board = new BingoBoard(boardJson);
        } catch (IOException e) {
            throw new MojoExecutionException("Could not read generated board file. Make sure the board has been generated using the generate-board goal", e);
        }

        BingoDatabaseInterface database = new BingoDatabaseInterface(apiUrl);
        try {
            JSONObject response = database.upload(board);
            if (response.optString("code", "error").equals("success")) {
                getLog().info("Upload successful. Created board with id " + response.optInt("message", -1));
            } else {
                getLog().error("Upload failed: " + response.optString("message", "unknown error"));
            }
        } catch (IOException e) {
            throw new MojoExecutionException("Could not upload board to " + apiUrl, e);
        }
    }
}