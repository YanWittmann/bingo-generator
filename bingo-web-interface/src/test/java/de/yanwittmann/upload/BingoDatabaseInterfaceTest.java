package de.yanwittmann.upload;

import de.yanwittmann.bingo.BingoBoard;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

class BingoDatabaseInterfaceTest {

    private final static int boardId = 1;
    private final static String baseApiUrl = "";

    @Test
    public void uploadTest() throws IOException {
        BingoDatabaseInterface bingoDatabaseInterface = new BingoDatabaseInterface("");
        BingoBoard bingoBoard = new BingoBoard(new JSONObject(String.join("", FileUtils.readLines(new File("../bingo-core/src/test/resources/bingo/load/outer_wilds.json"), StandardCharsets.UTF_8))));
        bingoDatabaseInterface.upload(bingoBoard, true);
    }

    @Test
    public void deleteTest() throws IOException {
        BingoDatabaseInterface bingoDatabaseInterface = new BingoDatabaseInterface("");
        bingoDatabaseInterface.delete(boardId);
    }

    @Test
    public void getBoardTilesTest() throws IOException {
        BingoDatabaseInterface bingoDatabaseInterface = new BingoDatabaseInterface("");
        bingoDatabaseInterface.getBoardTiles(boardId);
    }

    @Test
    public void getBoardClaims() throws IOException {
        BingoDatabaseInterface bingoDatabaseInterface = new BingoDatabaseInterface("");
        bingoDatabaseInterface.getBoardClaims(boardId);
    }

    @Test
    public void claimBoardTile() throws IOException {
        BingoDatabaseInterface bingoDatabaseInterface = new BingoDatabaseInterface("");
        bingoDatabaseInterface.claimBoardTile(boardId, 2, 2, '3');
    }

    @Test
    public void getBoards() throws IOException {
        BingoDatabaseInterface bingoDatabaseInterface = new BingoDatabaseInterface("");
        bingoDatabaseInterface.getBoards();
    }
}