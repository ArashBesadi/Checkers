package sussex.ist.checkers;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

public class Human implements ActionListener {


    private Utility utility;
    private AI ai;
    private Board board;
    private JButton tempSelectedButton;
    private boolean wait = true;


    public Human(Utility utility, AI ai) {
        this.utility = utility;
        this.ai = ai;

        // And From your main() method or any other method
        java.util.Timer timer = new java.util.Timer();
        timer.schedule(new MoveAI(), 0, 100);
    }

    class MoveAI extends TimerTask {
        public void run() {
            if (!wait) {
                ai.moveAI();
                wait = true;
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        String currentPosition = e.getActionCommand();
        int[] xy = utility.getXY(currentPosition);

        board.setMovablePieces();
        JButton[] greenFields = board.getSimpleMoveFields();
        JButton[] redFields = board.getJumpFields();
        Map<String, Piece> blackPieces = board.getBlackPieces();
        Map<String, Piece> redPieces = board.getRedPieces();
        JButton[][] buttonBoard = board.getButtonBoard();

        if (checkGreenFieldSelected(greenFields, currentPosition))
            return;
        if (checkRedFieldSelected(redFields, currentPosition, blackPieces, buttonBoard)) {
            return;
        }

        if (redPieces.containsKey(currentPosition)) {

            tempSelectedButton = buttonBoard[xy[1]][xy[0]];

            boolean king = redPieces.get(currentPosition).isKing();
            if (king) {
                tempSelectedButton.setIcon(new ImageIcon(board.getYellowKingPiece()));

            } else {
                tempSelectedButton.setIcon(new ImageIcon(board.getYellowPiece()));
            }

            utility.clearMoves();
            utility.successor(currentPosition, redPieces, blackPieces, true);

            List<String[]> copiedAttackMoves = utility.deepCopyList(utility.getJumpMoves());
            utility.setCopyJumpMoves(copiedAttackMoves);
            utility.checkMultipleJump(utility.deepCopyMap(redPieces), utility.deepCopyMap(blackPieces), true);
            utility.setJumpMoves(copiedAttackMoves);

            List<String[]> jumpMoves = utility.getJumpMoves();

            if (!jumpMoves.isEmpty()) {

                for (int i = 0; i < jumpMoves.size(); i++) {

                    String attackPosition = jumpMoves.get(i)[0];
                    int[] attackXY = utility.getXY(attackPosition);

                    JButton redField = buttonBoard[attackXY[1]][attackXY[0]];
                    redField.setBackground(Color.RED);
                    redFields[i] = redField;
                }
            }
            utility.clearMoves();
            utility.successor(currentPosition, redPieces, blackPieces, true);
            List<String[]> simpleMoves = utility.getSimpleMoves();

            if (utility.getJumpMoves().isEmpty()) {
                if (!simpleMoves.isEmpty()) {
                    for (int i = 0; i < simpleMoves.size(); i++) {
                        String movePosition = simpleMoves.get(i)[0];
                        int[] moveXY = utility.getXY(movePosition);

                        JButton greenField = buttonBoard[moveXY[1]][moveXY[0]];
                        greenField.setBackground(board.getNeon_green());
                        greenFields[i] = greenField;
                    }
                }
            }
            board.resetGreenFields();
        }
    }

    private boolean checkRedFieldSelected(JButton[] redFields, String currentPosition, Map<String, Piece> blackPieces, JButton[][] buttonBoard) {
        for (JButton redField : redFields) {
            if (redField != null) {
                boolean fieldMatch = selectField(redField, currentPosition);
                if (fieldMatch) {
                    board.setJumpFields(new JButton[4]);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean checkGreenFieldSelected(JButton[] greenFields, String currentPosition) {

        for (JButton greenField : greenFields) {
            if (greenField != null) {
                boolean fieldMatch = selectField(greenField, currentPosition);
                if (fieldMatch) {
                    board.setSimpleMoveFields(new JButton[4]);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean selectField(JButton field, String newCoordinates) {

        String oldCoordinates = field.getActionCommand();
        boolean fieldMatch = oldCoordinates.equals(newCoordinates);


        if (fieldMatch) {

            tempSelectedButton.setIcon(null);

            List<String[]> blackKeys = utility.getRemoveKeys().get(newCoordinates);
            int delay = 0;
            if (blackKeys != null && !blackKeys.isEmpty()) {
                for (int i = 0; i < blackKeys.size(); i++) {

                    int finalI = i;
                    Timer timer = new Timer(delay, ae -> {

                        board.getBlackPieces().remove(blackKeys.get(finalI)[0]);
                        int[] blackXY = utility.getXY(blackKeys.get(finalI)[0]);
                        board.getButtonBoard()[blackXY[1]][blackXY[0]].setIcon(null);

                        if (finalI < blackKeys.size() - 1) {
                            int[] redXY = utility.getXY(blackKeys.get(finalI)[1]);
                            board.getButtonBoard()[redXY[1]][redXY[0]].setIcon(new ImageIcon(board.getRedPiece()));
                        }

                        if (finalI > 0) {
                            int[] redXY = utility.getXY(blackKeys.get(finalI - 1)[1]);
                            board.getButtonBoard()[redXY[1]][redXY[0]].setIcon(null);
                        }
                    });

                    timer.setRepeats(false);
                    timer.start();
                    delay += 500;

                }
                if (blackKeys.size() == 1) {
                    delay = 0;
                }
                System.out.println(delay);
                Timer timer = new Timer(delay - 500, ae -> {

                    setNewPosition(field, newCoordinates);
                    wait = false;

                });
                timer.setRepeats(false);
                timer.start();
            } else {
                setNewPosition(field, newCoordinates);
                wait = false;
            }
            utility.clearRemoveKeys();
        }

        return fieldMatch;
    }

    public void setBoard(Board board) {
        this.board = board;
    }

    private void setNewPosition(JButton field, String newCoordinates) {

        Map<String, Piece> redPieces = board.getRedPieces();
        String tempKey = tempSelectedButton.getActionCommand();
        boolean king = redPieces.get(tempKey).isKing();
        redPieces.remove(tempKey);

        redPieces.put(newCoordinates, new Piece());

        if (king) {
            field.setIcon(new ImageIcon(board.getRedKingPiece()));
            redPieces.get(newCoordinates).setKing(true);

        } else {
            field.setIcon(new ImageIcon(board.getRedPiece()));
            int y = Integer.valueOf(newCoordinates.split(":")[1]);
            if (y == Utility.MAX_BOARDER) {
                field.setIcon(new ImageIcon(board.getRedKingPiece()));
                redPieces.get(newCoordinates).setKing(true);
            }
        }
    }
}
