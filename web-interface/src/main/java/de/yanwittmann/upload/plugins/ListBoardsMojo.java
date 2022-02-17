package de.yanwittmann.upload.plugins;

import de.yanwittmann.upload.BingoDatabaseInterface;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;

@Mojo(name = "list-boards", requiresProject = false)
public class ListBoardsMojo extends AbstractMojo {

    @Parameter(required = false, defaultValue = "true")
    private boolean active;

    @Parameter(required = true)
    private URL apiUrl;

    public void execute() throws MojoExecutionException {
        if (!active) {
            getLog().info("Listing is disabled by configuration.");
            return;
        }

        getLog().info("Listing boards from " + apiUrl);

        BingoDatabaseInterface database = new BingoDatabaseInterface(apiUrl);
        try {
            JSONArray response = database.getBoards();
            for (int i = 0; i < response.length(); i++) {
                JSONObject board = response.getJSONObject(i);
                getLog().info(board.getString("id") + ": " + board.getString("title") + " (" + board.getString("description") + ")");
            }
        } catch (IOException e) {
            throw new MojoExecutionException("Could not get boards from " + apiUrl, e);
        }
    }
}