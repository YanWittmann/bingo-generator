package de.yanwittmann.upload.plugins;

import de.yanwittmann.upload.BingoDatabaseInterface;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;

@Mojo(name = "delete-board", requiresProject = false)
public class DeleteMojo extends AbstractMojo {

    @Parameter(required = false, defaultValue = "true")
    private boolean active;

    @Parameter(required = true)
    private int boardId;

    @Parameter(required = true)
    private URL apiUrl;

    public void execute() throws MojoExecutionException {
        if (!active) {
            getLog().info("Deletion is disabled by configuration.");
            return;
        }

        getLog().info("Deleting board from " + apiUrl);
        getLog().info("Board id: " + boardId);

        BingoDatabaseInterface database = new BingoDatabaseInterface(apiUrl);
        try {
            JSONObject response = database.delete(boardId);
            if (response.optString("code", "error").equals("success")) {
                getLog().info(response.optString("message", "Successfully deleted board."));
            } else {
                throw new MojoExecutionException("Deletion failed: " + response.optString("message", "unknown error"));
            }
        } catch (IOException e) {
            throw new MojoExecutionException("Could not delete board from " + apiUrl, e);
        }
    }
}