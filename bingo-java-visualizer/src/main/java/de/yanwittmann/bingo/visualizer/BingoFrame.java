package de.yanwittmann.bingo.visualizer;

import de.yanwittmann.bingo.BingoBoard;
import de.yanwittmann.bingo.generator.BingoConfiguration;
import de.yanwittmann.bingo.generator.BingoGenerator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;

public class BingoFrame extends JFrame {

    private final Panel bingoGridPanel;
    private final JButton regenerateButton;

    public BingoFrame() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Bingo");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        bingoGridPanel = new Panel();
        bingoGridPanel.setLayout(new GridLayout(5, 5));
        bingoGridPanel.add(new JLabel("Loading..."), 0, 0);
        add(bingoGridPanel, BorderLayout.NORTH);

        regenerateButton = new JButton("Regenerate");
        regenerateButton.addActionListener(e -> {
            try {
                generateAndShow();
            } catch (FileNotFoundException e1) {
                e1.printStackTrace();
            }
        });
        add(regenerateButton, BorderLayout.SOUTH);

        setVisible(true);
    }

    public void generateAndShow() throws FileNotFoundException {
        BingoConfiguration configuration = new BingoConfiguration(new File("bingo-core/src/test/resources/bingo/generate/outer_wilds.yaml"));
        BingoGenerator generator = new BingoGenerator(configuration);
        BingoBoard bingoBoard = generator.generateBingoBoard();
        showBoard(bingoBoard);
    }

    public void showBoard(BingoBoard bingoBoard) {
        bingoGridPanel.removeAll();
        bingoGridPanel.setLayout(new GridLayout(bingoBoard.getWidth(), bingoBoard.getHeight()));
        EmptyBorder eBorder = new EmptyBorder(10, 10, 10, 10);
        LineBorder lBorder = new LineBorder(new Color(100, 100, 100));
        for (int i = 0; i < bingoBoard.getWidth(); i++) {
            for (int j = 0; j < bingoBoard.getHeight(); j++) {
                JLabel bingoTile = new JLabel("<html><center>" + bingoBoard.get(i, j).getText() + "</center></html>");
                bingoTile.setHorizontalAlignment(SwingConstants.CENTER);
                bingoTile.setVerticalAlignment(SwingConstants.CENTER);
                bingoTile.setBackground(new Color(255, 217, 217));
                bingoTile.setOpaque(true);
                bingoTile.setVisible(true);
                bingoTile.setBorder(BorderFactory.createCompoundBorder(lBorder, eBorder));
                bingoGridPanel.add(bingoTile, i, j);
            }
        }
        bingoGridPanel.revalidate();
    }

    public static void main(String[] args) throws FileNotFoundException {
        new BingoFrame().generateAndShow();
        System.out.println("Done");
    }
}
