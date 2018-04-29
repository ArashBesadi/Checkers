package sussex.ist.checkers;


import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class AI {

    private Utility utility;
    private String formerKey;
    private Board board;
    private final MiniMax miniMax;
    public int recursionCounter;

    public AI(Utility utility) {
        this.utility = utility;
        miniMax = new MiniMax(utility, this);
    }

    public void makeMove(String[] movements, Map<String, Piece> redPieces, Map<String, Piece> blackPieces, boolean realMove) {

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

        if (realMove) {
            chessBoardSquares[formerY][formerX].setIcon(null);

            chessBoardSquares[newY][newX].setIcon(new ImageIcon(board.getBlackPiece()));

            if (blackPieces.get(newKey).isKing()) {
                chessBoardSquares[newY][newX].setIcon(new ImageIcon(board.getBlackKingPiece()));
            }
        }

        if (movements.length == 3) {
            if (realMove) {
                Map<String, List<String[]>> removeKeys = utility.getRemoveKeys();
                for (String removeKey : removeKeys.keySet()) {
                    List<String[]> redKeys = removeKeys.get(removeKey);
                    for (String[] redKey : redKeys) {
                        redPieces.remove(redKey[0]);
                        int[] redXY = utility.getXY(redKey[0]);
                        chessBoardSquares[redXY[1]][redXY[0]].setIcon(null);
                    }
                }
            } else {
                redPieces.remove(movements[2]);
            }
        }
        board.setMovablePieces();
    }


    public void moveAI() {

        Timer timer = new Timer(500, ae -> {

            Map<String, Piece> redPieces = board.getRedPieces();
            Map<String, Piece> blackPieces = board.getBlackPieces();

            utility.getPossibleMoves(redPieces, blackPieces);
            List<String[]> simpleMoves = utility.getSimpleMoves();
            List<String[]> jumpMoves = utility.getJumpMoves();

            if (simpleMoves.isEmpty() && jumpMoves.isEmpty()) {
                System.out.println("game over");
            } else {
                int depth = 2;
                recursionCounter = 1;
                miniMax.miniMax(utility.deepCopyMap(redPieces), utility.deepCopyMap(blackPieces), depth, Integer.MIN_VALUE, Integer.MAX_VALUE, true);


                String[] bestMove = miniMax.getBestMove();
                formerKey = bestMove[1];

                if (bestMove.length == 3) {
                    jumpMoves = new ArrayList<>();
                    jumpMoves.add(bestMove);
                    utility.setCopyJumpMoves(jumpMoves);
                    List<String[]> copiedAttackMoves = utility.deepCopyList(jumpMoves);
                    utility.setJumpMoves(copiedAttackMoves);
                    utility.clearRemoveKeys();
                    utility.checkMultipleJump(utility.deepCopyMap(redPieces), utility.deepCopyMap(blackPieces), false);
                    bestMove = copiedAttackMoves.get(0);
                }
                makeMove(bestMove, redPieces, blackPieces, true);
                board.setMovablePieces();

            }
        });
        timer.setRepeats(false);
        timer.start();
    }

    public void setBoard(Board board) {
        this.board = board;
    }
}
