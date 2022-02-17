# Bingo Generator

A general-purpose text based bingo card generator.

## Usage

You have multiple ways of using the generator. You can either use the test classes to run the generator/uploader, or you
can use the maven plugins (which I recommend).

### Maven

First, [install maven](https://maven.apache.org/download.cgi) (if you haven't already).  
Then use the terminal to run the `install` command at the root of the project:

```bash
mvn install
```

### Test classes

[BingoGeneratorTest.java](bingo-core/src/test/java/de/yanwittmann/bingo/generator/BingoGeneratorTest.java)  
Use the test class to generate a bingo board. Here, you can specify the configuration file and additional parameters
like the number of rows and columns. You can also pass a `Random` instance to the generator to get a seeded bingo
board.  
This will generate a `.json` file that you can upload in the next step or use the
[BingoFrame.java](bingo-java-visualizer/src/main/java/de/yanwittmann/bingo/visualizer/BingoFrame.java)
to display it locally.

[BingoDatabaseInterfaceTest.java](bingo-web-interface/src/test/java/de/yanwittmann/upload/BingoDatabaseInterfaceTest.java)  
This test class allows you to upload, delete, modify or list bingo boards from/to the database. You will have to specify
an API URL for this, see the 'Configure database & Web interface' section below.

### Configure database & Web interface

On an SQL-based database that is available on the internet, create the two tables using
[init.sql](bingo-web-interface/src/main/website/backend/init.sql).

Then upload the [web files](bingo-web-interface/src/main/website) (or at least the `backend` first) to a php-capable
server. The URL to the `backend` directory is your API URL. In
[bingo.js](bingo-web-interface/src/main/website/bingo.js), set the `baseApiUrl` variable to the API URL.

You should now be able to use the scripts above to generate bingo boards and upload them to the database. Then you can
enter the board ID that has been created by the uploader to show the board in the web interface.
