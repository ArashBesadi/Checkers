package sussex.ist.checkers;


import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class AI {

    public static final int EASY = 1;
    public static final int MEDIUM = 2;
    public static final int HARD = 4;
    public static final int VERY_HARD = 6;
    public int depth;

    private Utility utility;
    private String formerKey;
    private Board board;
    private final MiniMax miniMax;
    public int recursionCounter;

    public AI(Utility utility) {
        this.utility = utility;
        miniMax = new MiniMax(utility, this);
        depth = AI.MEDIUM;
    }

    public void makeMove(String[] movements, Map<String, Piece> redPieces, Map<String, Piece> blackPieces, boolean realMove, boolean human) {

        if (human) {
            Map<String, Piece> tempBlack = blackPieces;
            blackPieces = redPieces;
            redPieces = tempBlack;
        }

        String newKey = movements[0];
        String formerKey;

        if (realMove) {
            formerKey = this.formerKey;
        } else {
            formerKey = movements[1];
        }

        int[] newXY = utility.getXY(newKey);
        int newX = newXY[0];
        int newY = newXY[1];

        int[] formerXY = utility.getXY(formerKey);
        int formerX = formerXY[0];
        int formerY = formerXY[1];

        blackPieces.put(newKey, new Piece());

        if (newY == Utility.MIN_BOARDER || blackPieces.get(formerKey).isKing()) {
            blackPieces.get(newKey).setKing(true);
        }

        blackPieces.remove(formerKey);

        JButton[][] chessBoardSquares = board.getButtonBoard();
        chessBoardSquares[formerY][formerX].setIcon(null);

        boolean blackKing = blackPieces.get(newKey).isKing();


        if (movements.length == 3) {
            if (realMove) {
                int delay = 0;
                Map<String, List<String[]>> removeKeys = utility.getRemoveKeys();
                for (String removeKey : removeKeys.keySet()) {
                    List<String[]> redKeys = removeKeys.get(removeKey);

                    for (int i = 0; i < redKeys.size(); i++) {

                        redPieces.remove(redKeys.get(i)[0]);

                        int finalI = i;
                        Timer timer = new Timer(delay, ae -> {

                            int[] redXY = utility.getXY(redKeys.get(finalI)[0]);
                            chessBoardSquares[redXY[1]][redXY[0]].setIcon(null);

                            int[] blackKey = utility.getXY(redKeys.get(finalI)[1]);
                            chessBoardSquares[blackKey[1]][blackKey[0]].setIcon(new ImageIcon(board.getBlackPiece()));

                            if (finalI > 0) {
                                blackKey = utility.getXY(redKeys.get(finalI - 1)[1]);
                                board.getButtonBoard()[blackKey[1]][blackKey[0]].setIcon(null);
                            }
                        });
                        timer.setRepeats(false);
                        timer.start();
                        delay += 500;

                    }
                }
                Timer timer = new Timer(delay, ae -> {
                    chessBoardSquares[newY][newX].setIcon(new ImageIcon(board.getBlackPiece()));
                    if (blackKing) {
                        chessBoardSquares[newY][newX].setIcon(new ImageIcon(board.getBlackKingPiece()));
                    }
                });
                timer.setRepeats(false);
                timer.start();

            } else {
                redPieces.remove(movements[2]);
            }
        } else {
            if (realMove) {
                chessBoardSquares[newY][newX].setIcon(new ImageIcon(board.getBlackPiece()));

                if (blackPieces.get(newKey).isKing()) {
                    chessBoardSquares[newY][newX].setIcon(new ImageIcon(board.getBlackKingPiece()));
                }
            }
        }
        board.setMovablePieces();

    }


    public void moveAI() {

        Map<String, Piece> redPieces = board.getRedPieces();
        Map<String, Piece> blackPieces = board.getBlackPieces();

        utility.getPossibleMoves(redPieces, blackPieces, false);
        List<String[]> simpleMoves = utility.getSimpleMoves();
        List<String[]> jumpMoves = utility.getJumpMoves();

        if (simpleMoves.isEmpty() && jumpMoves.isEmpty()) {
            System.out.println("win");
        } else {
            recursionCounter = 1;
            miniMax.miniMax(utility.deepCopyMap(redPieces), utility.deepCopyMap(blackPieces), depth, Integer.MIN_VALUE, Integer.MAX_VALUE, false);


            String[] bestMove = miniMax.getBestMove();
            formerKey = bestMove[1];

            if (bestMove.length == 3) {
                jumpMoves = new ArrayList<>();
                jumpMoves.add(bestMove);
                utility.setJumpMoves(jumpMoves);
                List<String[]> copiedAttackMoves = utility.deepCopyList(jumpMoves);
                utility.setCopyJumpMoves(copiedAttackMoves);
                utility.clearRemoveKeys();
                utility.checkMultipleJump(utility.deepCopyMap(redPieces), utility.deepCopyMap(blackPieces), false);
                bestMove = copiedAttackMoves.get(0);


            }
            makeMove(bestMove, redPieces, blackPieces, true, false);
            board.setMovablePieces();

        }
    }

    public void setBoard(Board board) {
        this.board = board;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }
}
