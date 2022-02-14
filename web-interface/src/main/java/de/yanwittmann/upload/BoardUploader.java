package de.yanwittmann.upload;

import de.yanwittmann.bingo.BingoBoard;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

public class BoardUploader {

    private final String baseUrl;

    public BoardUploader(String baseUrl) {
        this.baseUrl = baseUrl.replaceAll("/$", "") + "/";
    }

    public void upload(BingoBoard board) throws IOException {
        createTiles(board, createBoard(board));
    }

    private int createBoard(BingoBoard board) throws IOException {
        Map<String, String> postData = new HashMap<>();
        postData.put("height", String.valueOf(board.getHeight()));
        postData.put("width", String.valueOf(board.getWidth()));
        postData.put("difficulty", String.valueOf(board.getDifficulty()));
        postData.put("title", board.getBoardMetadata().getTitle());
        postData.put("description", board.getBoardMetadata().getDescription());
        postData.put("game", board.getBoardMetadata().getGame());
        postData.put("authors", String.join(",", board.getBoardMetadata().getAuthors()));
        postData.put("version", board.getBoardMetadata().getVersion());
        postData.put("allow_multiple_claims", "false");
        JSONObject response = new JSONObject(apiCall("create-board.php", postData));
        System.out.println(response.toString(2));
        return response.getInt("message");
    }

    private void createTiles(BingoBoard board, int boardId) throws IOException {
        Map<String, String> postData = new HashMap<>();
        postData.put("boardId", String.valueOf(boardId));
        postData.put("tileJson", board.getBoardTilesJson().toString());
        JSONObject response = new JSONObject(apiCall("create-tile.php", postData));
        System.out.println(response.toString(2));
    }


    private String apiCall(String file, Map<String, String> postData) throws IOException {
        URL url = new URL(baseUrl + file);
        System.out.println("Performing request to " + url);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        // write post data
        StringJoiner postDataString = new StringJoiner("&");
        postData.forEach((key, value) -> postDataString.add(key + "=" + value));
        connection.getOutputStream().write(postDataString.toString().getBytes());
        connection.getOutputStream().flush();
        connection.getOutputStream().close();
        // read response
        StringBuilder response = new StringBuilder();
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            InputStream inputStream = connection.getInputStream();
            // input stream is an array of bytes
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                response.append(new String(buffer, 0, bytesRead));
            }
            inputStream.close();
        }
        return response.toString();
    }
}
