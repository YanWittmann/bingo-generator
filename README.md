# Bingo Generator

A general-purpose text based bingo card generator.

## Usage

You have multiple ways of using the generator. You can either use the test classes to run the generator/uploader, or you
can use the maven plugins (which I recommend).

### Maven

First, install maven (if you haven't already).  
Then use the terminal to run these commands at the root of the project:

```bash
cd bingo-core
mvn install
cd ..
cd bingo-web-interface
mvn install
cd ..
```

## Generate bingo boards

Once you wrote your configuration, you can generate bingo boards in two ways:

1. [BingoGeneratorTest.java](bingo-core/src/test/java/de/yanwittmann/bingo/generator/BingoGeneratorTest.java)  
   Use the test class to generate a bingo board. Here, you can specify the configuration file and additional parameters
   like the number of rows and columns. You can also pass a `Random` instance to the generator to get a seeded bingo
   board.