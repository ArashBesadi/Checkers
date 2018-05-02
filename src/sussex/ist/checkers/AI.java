package sussex.ist.checkers;


import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class is responsible for the intelligent checkers agent.
 */
public class AI {

    // several agent difficulty
    public static final int EASY = 1;
    public static final int MEDIUM = 2;
    public static final int HARD = 4;
    public static final int VERY_HARD = 10;

    private Utility utility;
    private MiniMax miniMax;
    private Board board;

    private String formerPositionKey;
    private int depth;
    public int recursionCounter;
    private boolean blackKing;

    public AI(Utility utility) {
        this.utility = utility;
        miniMax = new MiniMax(utility, this);
        depth = AI.MEDIUM;
    }

    /**
     * Moves the red and black pieces to its new position.
     *
     * @param movements   the movements which consists of the new, former and removed position keys
     * @param redPieces   the red pieces
     * @param blackPieces the black pieces
     * @param realMove    the realMove indicates whether or not the moves are simulations called while iterating thourgh the search tree
     * @param human       the human boolean indicates the current player
     */
    public void makeMove(String[] movements, Map<String, Piece> redPieces, Map<String, Piece> blackPieces, boolean realMove, boolean human) {

        // if human then swap the pieces
        if (human) {
            Map<String, Piece> tempBlack = blackPieces;
            blackPieces = redPieces;
            redPieces = tempBlack;
        }

        // get former position key
        String formerPositionKey;
        if (realMove) {
            formerPositionKey = this.formerPositionKey;
        } else {
            formerPositionKey = movements[1];
        }
        int[] formerXY = utility.getXY(formerPositionKey);
        int formerX = formerXY[0];
        int formerY = formerXY[1];

        // get new position key
        String newPositionKey = movements[0];
        int[] newXY = utility.getXY(newPositionKey);
        int newX = newXY[0];
        int newY = newXY[1];


        // put new position to the boarder representation
        blackPieces.put(newPositionKey, new Piece());
        // check whether piece can be promotoed to king
        if (newY == Utility.MIN_BOARDER || blackPieces.get(formerPositionKey).isKing()) {
            blackPieces.get(newPositionKey).setKing(true);
        }
        blackPieces.remove(formerPositionKey);

        JButton[][] buttonBoard = board.getButtonBoard();
        blackKing = blackPieces.get(newPositionKey).isKing();

        // if jump
        if (movements.length == 3) {
            if (realMove) {
                buttonBoard[formerY][formerX].setIcon(null);
                checkMultiStepMovement(redPieces, buttonBoard, newX, newY);
            } else {
                redPieces.remove(movements[2]);
            }
        } else {
            // simple move
            if (realMove) {
                buttonBoard[formerY][formerX].setIcon(null);
                buttonBoard[newY][newX].setIcon(new ImageIcon(board.getBlackPiece()));

                if (blackPieces.get(newPositionKey).isKing()) {
                    buttonBoard[newY][newX].setIcon(new ImageIcon(board.getBlackKingPiece()));
                }
            }
        }

        if (realMove) {
            board.setMovablePieces();
        }

    }

    /**
     * Checks whether jumps and multiple jumps are possible. Sets multiple timers in oder to achieve a fluent move flow.
     *
     * @param redPieces   the red pieces
     * @param buttonBoard the board which contains 64 buttons
     * @param newX        the jump x-position
     * @param newY        the jump y-positon
     */
    private void checkMultiStepMovement(Map<String, Piece> redPieces, JButton[][] buttonBoard, int newX, int newY) {

        int delay = 0;
        // a list of keys of pieces that can be removed
        Map<String, List<String[]>> removeKeys = utility.getJumpKey();
        for (String removeKey : removeKeys.keySet()) {
            List<String[]> redKeys = removeKeys.get(removeKey);

            for (int i = 0; i < redKeys.size(); i++) {

                redPieces.remove(redKeys.get(i)[0]);

                int finalI = i;
                // set a timer to achieve a pause
                Timer timer = new Timer(delay, ae -> {

                    // remove the red piece
                    int[] redXY = utility.getXY(redKeys.get(finalI)[0]);
                    buttonBoard[redXY[1]][redXY[0]].setIcon(null);

                    // set a temporary black piece
                    int[] blackKey = utility.getXY(redKeys.get(finalI)[1]);
                    buttonBoard[blackKey[1]][blackKey[0]].setIcon(new ImageIcon(board.getBlackPiece()));

                    // remove the previous black piece
                    if (finalI > 0) {
                        blackKey = utility.getXY(redKeys.get(finalI - 1)[1]);
                        board.getButtonBoard()[blackKey[1]][blackKey[0]].setIcon(null);
                    }
                });
                timer.setRepeats(false);
                timer.start();
                delay += 1000;

            }
        }
        // the timer for the last piece
        Timer timer = new Timer(delay, ae -> {
            buttonBoard[newY][newX].setIcon(new ImageIcon(board.getBlackPiece()));
            if (blackKing) {
                buttonBoard[newY][newX].setIcon(new ImageIcon(board.getBlackKingPiece()));
            }
        });
        timer.setRepeats(false);
        timer.start();
    }

    /**
     * Gets the possible simple and jump moves and then calls the minimax function. The best move is given the makeMove method.
     */
    public void moveAI() {

        Timer timer = new Timer(300, ae -> {
            Map<String, Piece> redPieces = board.getRedPieces();
            Map<String, Piece> blackPieces = board.getBlackPieces();

            // get the possible moves
            utility.getPossibleMoves(redPieces, blackPieces, false);
            List<String[]> simpleMoves = utility.getSimpleMoves();
            List<String[]> jumpMoves = utility.getJumpMoves();

            if (simpleMoves.isEmpty() && jumpMoves.isEmpty()) {
                board.getTextField().setText("Win!");
            } else {
                recursionCounter = 1;
                miniMax.miniMax(utility.deepCopyMap(redPieces), utility.deepCopyMap(blackPieces), depth, Integer.MIN_VALUE, Integer.MAX_VALUE, false);

                String[] bestMove = miniMax.getBestMove();
                formerPositionKey = bestMove[1];

                // if jump is possible check for multiple jumps
                if (bestMove.length == 3) {
                    jumpMoves = new ArrayList<>();
                    jumpMoves.add(bestMove);
                    utility.setJumpMoves(jumpMoves);
                    List<String[]> copiedJumpMoves = utility.deepCopyList(jumpMoves);
                    utility.setCopyJumpMoves(copiedJumpMoves);
                    utility.clearRemoveKeys();
                    utility.checkMultipleJump(utility.deepCopyMap(redPieces), utility.deepCopyMap(blackPieces), false);
                    bestMove = copiedJumpMoves.get(0);
                }
                makeMove(bestMove, redPieces, blackPieces, true, false);
                board.setMovablePieces();
            }
        });
        timer.setRepeats(false);
        timer.start();
    }

    public void setBoard(Board board) {
        this.board = board;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }
}
