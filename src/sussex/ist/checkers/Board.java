package sussex.ist.checkers;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Board {

    private Human human;
    private Utility utility;

    private Map<String, Piece> redPieces = new HashMap<>();
    private Map<String, Piece> blackPieces = new HashMap<>();

    private BufferedImage redPiece;
    private BufferedImage redKingPiece;
    private BufferedImage blackPiece;
    private BufferedImage blackKingPiece;
    private BufferedImage whitePiece;
    private BufferedImage whiteKingPiece;
    private BufferedImage yellowPiece;
    private BufferedImage yellowKingPiece;

    private JButton[][] buttonBoard = new JButton[8][8];
    private JButton[] simpleMoveFields = new JButton[4];
    private JButton[] jumpFields = new JButton[4];

    private Color dark_brown = new Color(156, 101, 43);
    private Color light_brown = new Color(211, 168, 107);
    private Color neon_green = new Color(124, 250, 80);

    public Board(Human human) {
        this.human = human;
        this.utility = new Utility();
        initGUI();
    }

    public final void initGUI() {

        JPanel panel = new JPanel(new GridLayout(0, 8));
        panel.setBorder(new EmptyBorder(5, 5, 5, 5));

        JPanel boardConstrain = new JPanel(new GridBagLayout());
        boardConstrain.add(panel);

        JFrame frame = new JFrame("Checkers");
        frame.setResizable(false);
        frame.setAlwaysOnTop(true);

        JMenuBar menuBar = new JMenuBar();
        JMenu newGame = new JMenu("New Game");
        JMenu help = new JMenu("Help");
        JMenuItem newRules = new JMenuItem("Rules");
        help.add(newRules);
        newRules.addActionListener(e -> {
            if (Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().browse(new URI("http://www.indepthinfo.com/checkers/play.shtml"));
                } catch (IOException | URISyntaxException e1) {
                    e1.printStackTrace();
                }
            }
        });

        menuBar.add(newGame);
        menuBar.add(help);

        frame.setJMenuBar(menuBar);
        frame.add(boardConstrain);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        loadPictures();

        for (int y = buttonBoard.length - 1; y >= 0; y--) {
            for (int x = 0; x < buttonBoard[y].length; x++) {


                JButton b = new JButton();
                b.setOpaque(true);
                b.setActionCommand(String.valueOf(x) + ":" + String.valueOf(y));
                b.setBorderPainted(false);
                b.addActionListener(human);
                buttonBoard[y][x] = b;
                panel.add(buttonBoard[y][x]);


                if ((x % 2 == 1 && y % 2 == 1) ||    (x % 2 == 0 && y % 2 == 0)) {
                    b.setBackground(light_brown);

                } else {
                    b.setBackground(dark_brown);
                    if (y < 3) {
//                        redPieces.put(String.valueOf(x) + ":" + String.valueOf(y), new Piece());
//                        buttonBoard[y][x].setIcon(new ImageIcon(redPiece));
                    }
                    if (y > 4) {
//                        blackPieces.put(String.valueOf(x) + ":" + String.valueOf(y), new Piece());
//                        buttonBoard[y][x].setIcon(new ImageIcon(blackPiece));
                    }
                }
            }
        }

        Scenario scenario = new Scenario(this);
//        scenario.multipleJumpsAI();
        scenario.multipleJumpsHuman();
//        scenario.generalCheck();

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        setMovablePieces();
    }

    private void loadPictures() {
        try {
            whitePiece = ImageIO.read(new File("images/whitePiece.png"));
            redPiece = ImageIO.read(new File("images/redPiece.png"));
            redKingPiece = ImageIO.read(new File("images/redKing.png"));
            blackPiece = ImageIO.read(new File("images/blackPiece.png"));
            blackKingPiece = ImageIO.read(new File("images/blackKingPiece.png"));
            whiteKingPiece = ImageIO.read(new File("images/whiteKing.png"));
            yellowPiece = ImageIO.read(new File("images/yellowPiece.png"));
            yellowKingPiece = ImageIO.read(new File("images/yellowKingPiece.png"));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setMovablePieces() {

        utility.clearMoves();

        for (String redKey : redPieces.keySet()) {

            utility.successor(redKey, redPieces, blackPieces, true);
        }
        if (!utility.getJumpMoves().isEmpty()) {
            setWhitePieces(utility.getJumpMoves());
        } else {
            setWhitePieces(utility.getSimpleMoves());
        }

    }

    private void setWhitePieces(List<String[]> moves) {

        clearBoard();

        for (String[] move : moves) {
            String key = move[1];
            int[] xy = utility.getXY(key);
            boolean king = redPieces.get(key).isKing();
            if (king) {
                buttonBoard[xy[1]][xy[0]].setIcon(new ImageIcon(whiteKingPiece));

            } else {
                buttonBoard[xy[1]][xy[0]].setIcon(new ImageIcon(whitePiece));
            }
        }

    }

    private void clearBoard() {
        for (int y = buttonBoard.length - 1; y >= 0; y--) {
            for (int x = 0; x < buttonBoard[y].length; x++) {
                if (!((x % 2 == 1 && y % 2 == 1) || (x % 2 == 0 && y % 2 == 0))) {
                    buttonBoard[y][x].setBackground(dark_brown);

                    String key = x + ":" + y;
                    if (redPieces.containsKey(key)) {

                        if (redPieces.get(key).isKing()) {
                            buttonBoard[y][x].setIcon(new ImageIcon(redKingPiece));

                        } else {
                            buttonBoard[y][x].setIcon(new ImageIcon(redPiece));
                        }
                    }
                }
            }
        }
    }

    public void resetGreenFields() {

        utility.clearMoves();

        for (String redKey : redPieces.keySet()) {
            utility.successor(redKey, redPieces, blackPieces, true);
        }
        if (!utility.getJumpMoves().isEmpty()) {
            for (int i = 0; i < simpleMoveFields.length; i++) {
                if (simpleMoveFields[i] != null) {
                    simpleMoveFields[i].setBackground(dark_brown);
                    simpleMoveFields[i] = null;
                }
            }
        }


    }

    public JButton[] getSimpleMoveFields() {
        return simpleMoveFields;
    }

    public JButton[] getJumpFields() {
        return jumpFields;
    }

    public void setSimpleMoveFields(JButton[] simpleMoveFields) {
        this.simpleMoveFields = simpleMoveFields;
    }

    public void setJumpFields(JButton[] jumpFields) {
        this.jumpFields = jumpFields;
    }

    public Map<String, Piece> getRedPieces() {
        return redPieces;
    }

    public Map<String, Piece> getBlackPieces() {
        return blackPieces;
    }

    public JButton[][] getButtonBoard() {
        return buttonBoard;
    }

    public BufferedImage getRedPiece() {
        return redPiece;
    }

    public BufferedImage getRedKingPiece() {
        return redKingPiece;
    }

    public BufferedImage getBlackPiece() {
        return blackPiece;
    }

    public BufferedImage getBlackKingPiece() {
        return blackKingPiece;
    }

    public BufferedImage getYellowPiece() {
        return yellowPiece;
    }

    public BufferedImage getYellowKingPiece() {
        return yellowKingPiece;
    }

    public Color getNeon_green() {
        return neon_green;
    }
}