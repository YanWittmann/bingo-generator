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
import java.util.Random;

public class BingoFrame extends JFrame {

    private final JPanel bingoGridPanel;
    private final JPanel toolBarPanel;
    private final JTextField seedField;
    private final JButton regenerateButton;

    public BingoFrame() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Bingo");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        bingoGridPanel = new JPanel();
        bingoGridPanel.setLayout(new GridLayout(5, 5));
        bingoGridPanel.add(new JLabel("Loading..."), 0, 0);
        add(bingoGridPanel, BorderLayout.NORTH);

        toolBarPanel = new JPanel();
        toolBarPanel.setLayout(new GridLayout(1, 2));
        add(toolBarPanel, BorderLayout.SOUTH);

        seedField = new JTextField();
        seedField.setText("");
        seedField.setToolTipText("Leave empty for random seed");
        toolBarPanel.add(seedField, 0, 0);

        regenerateButton = new JButton("Regenerate");
        regenerateButton.addActionListener(e -> {
            try {
                generateAndShow();
            } catch (FileNotFoundException e1) {
                e1.printStackTrace();
            }
        });
        toolBarPanel.add(regenerateButton, 0, 1);

        setVisible(true);
    }

    public void generateAndShow() throws FileNotFoundException {
        BingoConfiguration configuration = new BingoConfiguration(new File("bingo-core/src/test/resources/bingo/generate/outer_wilds.yaml"));
        BingoGenerator generator = new BingoGenerator(configuration);
        Random random;
        if (seedField.getText().isEmpty() || !seedField.getText().matches("[0-9]+")) {
            random = new Random();
        } else {
            random = new Random(Long.parseLong(seedField.getText()));
        }
        BingoBoard bingoBoard = generator.generateBingoBoard(random);
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
                if (bingoBoard.get(i, j).getTooltip() != null)
                    bingoTile.setToolTipText("<html>" + bingoBoard.get(i, j).getTooltip().replace("\n", "<br>") + "</html>");
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
    }
}
