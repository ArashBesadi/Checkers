package sussex.ist.checkers;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

/**
 * This class is responsible for the user interactions.
 * It implements an ActionLister which listens to all 64 buttons of the board.
 * It also contains a time scheduler which checks whether the user has made a move which is needed for multiple jumps.
 */
public class Human implements ActionListener {


    private Utility utility;
    private AI ai;
    private Board board;
    private JButton currentlySelectedPiece;
    private boolean wait = true;
    private boolean invalidSimpleMove;
    private boolean invalidJump;

    public Human(Utility utility, AI ai) {
        this.utility = utility;
        this.ai = ai;

        java.util.Timer timer = new java.util.Timer();
        timer.schedule(new MoveAI(), 0, 500);
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

        board.setMovablePieces();

        // get the fields and pieces from the board
        JButton[] greenFields = board.getSimpleMoveFields();
        JButton[] redFields = board.getJumpFields();
        Map<String, Piece> blackPieces = board.getBlackPieces();
        Map<String, Piece> redPieces = board.getRedPieces();
        JButton[][] buttonBoard = board.getButtonBoard();

        String currentPosition = e.getActionCommand();
        int[] xy = utility.getXY(currentPosition);

        // check whether a green field has been selected previously
        if (checkSimpleMoveFieldSelected(greenFields, currentPosition)) {
            return;
        }

        // checks whether a red field has been selected previously
        if (checkJumpFieldSelected(redFields, currentPosition)) {
            return;
        }

        // reset colored fields
        board.setSimpleMoveFields(new JButton[4]);
        board.setJumpFields(new JButton[4]);

        checkInvalidMove(redPieces, currentPosition);


        if (redPieces.containsKey(currentPosition)) {

            // save the current selected piece
            currentlySelectedPiece = buttonBoard[xy[1]][xy[0]];

            // give the selected piece a yellow boarder
            boolean king = redPieces.get(currentPosition).isKing();
            if (king) {
                currentlySelectedPiece.setIcon(new ImageIcon(board.getYellowKingPiece()));

            } else {
                currentlySelectedPiece.setIcon(new ImageIcon(board.getYellowPiece()));
            }

            // check for multiple jumps
            utility.clearMoves();
            utility.successor(currentPosition, redPieces, blackPieces, true);
            List<String[]> copiedJumpMoves = utility.deepCopyList(utility.getJumpMoves());
            utility.setCopyJumpMoves(copiedJumpMoves);
            utility.checkMultipleJump(utility.deepCopyMap(redPieces), utility.deepCopyMap(blackPieces), true);
            utility.setJumpMoves(copiedJumpMoves);

            // apply jump
            List<String[]> jumpMoves = utility.getJumpMoves();
            if (!jumpMoves.isEmpty()) {

                for (int i = 0; i < jumpMoves.size(); i++) {

                    String jumpMove = jumpMoves.get(i)[0];
                    int[] jumpXY = utility.getXY(jumpMove);

                    JButton redField = buttonBoard[jumpXY[1]][jumpXY[0]];
                    redField.setBackground(Color.RED);
                    redFields[i] = redField;
                }
                board.setJumpFields(redFields);
            }

            // check for simple moves
            utility.clearMoves();
            utility.successor(currentPosition, redPieces, blackPieces, true);
            List<String[]> simpleMoves = utility.getSimpleMoves();

            // apply simple move
            if (utility.getJumpMoves().isEmpty()) {
                if (!simpleMoves.isEmpty()) {
                    for (int i = 0; i < simpleMoves.size(); i++) {
                        String movePosition = simpleMoves.get(i)[0];
                        int[] moveXY = utility.getXY(movePosition);

                        JButton greenField = buttonBoard[moveXY[1]][moveXY[0]];
                        greenField.setBackground(board.getNeon_green());
                        greenFields[i] = greenField;
                    }
                    board.setSimpleMoveFields(greenFields);
                }
            }
            board.resetGreenFields();
        }
    }


    /**
     * Checks whether a jump field is selected.
     * If a jump move is possible but the user clicks a different field, then the invalid move flag is set.
     *
     * @param jumpFields      the jumpField which are colored red
     * @param currentPosition the current position of the selected field
     * @return true if the user selected a jump field, false otherwise
     */
    private boolean checkJumpFieldSelected(JButton[] jumpFields, String currentPosition) {
        for (JButton jumpField : jumpFields) {
            if (jumpField != null) {
                boolean fieldMatch = selectField(jumpField, currentPosition);
                if (fieldMatch) {
                    invalidJump = false;
                    board.setJumpFields(new JButton[4]);
                    return true;
                } else {
                    invalidJump = true;
                }
            }
        }
        return false;
    }

    /**
     * Checks whether a simple move field is selected.
     * If a simple move is possible but the user clicks a different field, then the invalid move flag is set.
     *
     * @param greenFields
     * @param currentPosition
     * @return
     */
    private boolean checkSimpleMoveFieldSelected(JButton[] greenFields, String currentPosition) {

        for (JButton greenField : greenFields) {
            if (greenField != null) {
                boolean fieldMatch = selectField(greenField, currentPosition);
                if (fieldMatch) {
                    board.setSimpleMoveFields(new JButton[4]);
                    invalidSimpleMove = false;
                    return true;
                } else {
                    invalidSimpleMove = true;
                }
            }
        }
        return false;
    }

    /**
     * Checks whether the current position matches a possible move.
     * If so then multiple jump moves are simulated if available.
     *
     * @param possibleFields  fields that are possible to select
     * @param currentPosition the position of the selected field
     * @return ture, if the user selected a field that is a valid move, false otherwise
     */
    private boolean selectField(JButton possibleFields, String currentPosition) {

        String possiblePossition = possibleFields.getActionCommand();
        boolean fieldMatch = possiblePossition.equals(currentPosition);

        if (fieldMatch) {

            currentlySelectedPiece.setIcon(null);

            List<String[]> blackKeys = utility.getRemoveKeys().get(currentPosition);
            int delay = 0;
            if (blackKeys != null && !blackKeys.isEmpty()) {
                for (int i = 0; i < blackKeys.size(); i++) {

                    // add a timer in order to illustrate multiple jumps by triggering them
                    // after a delay which is increased each iteration
                    int finalI = i;
                    Timer timer = new Timer(delay, ae -> {

                        board.getBlackPieces().remove(blackKeys.get(finalI)[0]);
                        int[] blackXY = utility.getXY(blackKeys.get(finalI)[0]);
                        board.getButtonBoard()[blackXY[1]][blackXY[0]].setIcon(null);

                        // add a temporal red piece to a intermediate step
                        if (finalI < blackKeys.size() - 1) {
                            int[] redXY = utility.getXY(blackKeys.get(finalI)[1]);
                            board.getButtonBoard()[redXY[1]][redXY[0]].setIcon(new ImageIcon(board.getRedPiece()));
                        }

                        // remove the temporal red piece after the next iteration
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
                // set a timer for the last move
                Timer timer = new Timer(delay - 500, ae -> {
                    setNewPosition(possibleFields, currentPosition);
                    wait = false;
                });
                timer.setRepeats(false);
                timer.start();
            } else {
                setNewPosition(possibleFields, currentPosition);
                wait = false;
            }
            utility.clearRemoveKeys();
        }

        return fieldMatch;
    }

    /**
     * Moves the current piece to the selected field and updates the GUI by removing the former pieces.
     *
     * @param currentField    fields that are possible to select
     * @param currentPosition the position of the selected field
     */
    private void setNewPosition(JButton currentField, String currentPosition) {

        Map<String, Piece> redPieces = board.getRedPieces();
        String tempKey = currentlySelectedPiece.getActionCommand();
        boolean king = redPieces.get(tempKey).isKing();
        redPieces.remove(tempKey);

        redPieces.put(currentPosition, new Piece());

        if (king) {
            currentField.setIcon(new ImageIcon(board.getRedKingPiece()));
            redPieces.get(currentPosition).setKing(true);

        } else {
            currentField.setIcon(new ImageIcon(board.getRedPiece()));
            int y = Integer.valueOf(currentPosition.split(":")[1]);
            if (y == Utility.MAX_BOARDER) {
                currentField.setIcon(new ImageIcon(board.getRedKingPiece()));
                redPieces.get(currentPosition).setKing(true);
            }
        }
    }

    /**
     * Checks whether to set the invalid flag move.
     *
     * @param redPieces       red pieces
     * @param currentPosition the selected position
     */
    private void checkInvalidMove(Map<String, Piece> redPieces, String currentPosition) {

        if (!redPieces.containsKey(currentPosition)) {
            if (invalidSimpleMove) {
                board.getTextField().setText("Invalid move! Please choose a field that is highlighted green. See the help menu for the rules.");
            }
        }
        if (!redPieces.containsKey(currentPosition)) {
            if (invalidJump) {
                board.getTextField().setText("Invalid jump! Please choose a field that is highlighted red. See the help menu for the rules.");
            }
        }
        if (!invalidSimpleMove && !invalidJump) {
            board.getTextField().setText("Feedback: ");
        }

        invalidSimpleMove = false;
        invalidJump = false;
    }

    public void setBoard(Board board) {
        this.board = board;
    }
}
