package de.yanwittmann.upload;

import de.yanwittmann.bingo.BingoBoard;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

class BingoDatabaseInterfaceTest {

    @Test
    public void uploadTest() throws IOException {
        BingoDatabaseInterface bingoDatabaseInterface = new BingoDatabaseInterface("http://yanwittmann.de/projects/bingo/backend/");
        BingoBoard bingoBoard = new BingoBoard(new JSONObject(String.join("", FileUtils.readLines(new File("../bingo-core/src/test/resources/bingo/load/outer_wilds.json"), StandardCharsets.UTF_8))));
        bingoDatabaseInterface.upload(bingoBoard);
    }

    @Test
    public void deleteTest() throws IOException {
        BingoDatabaseInterface bingoDatabaseInterface = new BingoDatabaseInterface("http://yanwittmann.de/projects/bingo/backend/");
        bingoDatabaseInterface.delete(4);
    }

}