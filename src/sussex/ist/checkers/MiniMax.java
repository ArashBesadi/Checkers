package sussex.ist.checkers;


import java.util.List;
import java.util.Map;

public class MiniMax {

    private Utility utility;
    private AI ai;
    private String[] bestMove;


    public MiniMax(Utility utility, AI ai) {
        this.utility = utility;
        this.ai = ai;
    }

    public int miniMax(Map<String, Piece> redPieces, Map<String, Piece> blackPieces, int depth, int alpha, int beta, boolean human) {

//        System.out.println(ai.recursionCounter++);

        utility.getPossibleMoves(redPieces, blackPieces,human);

        List<String[]> possibleAttackMoves = utility.getJumpMoves();
        List<String[]> possibleMoves = utility.getSimpleMoves();

        List<String[]> moves;
        moves = possibleMoves;

        if (!possibleAttackMoves.isEmpty()) {
            moves = possibleAttackMoves;
        }
        if (depth == 0 || moves.isEmpty()) {
            return heuristic(redPieces,blackPieces);
        }

        String[] tempBestMove = null;

        if (!human) {

            int score = Integer.MIN_VALUE;

            for (String[] move : moves) {

                Map<String, Piece> copyRedPieces = utility.deepCopyMap(redPieces);
                Map<String, Piece> copyBlackPieces = utility.deepCopyMap(blackPieces);

                ai.makeMove(move, copyRedPieces, copyBlackPieces, false,human);
                score = miniMax(copyRedPieces, copyBlackPieces, depth - 1, alpha, beta, true);
                if (alpha < score) {
                    alpha = score;
                    System.out.println(alpha);
                    tempBestMove = move;
                }
                if (beta <= alpha) {
                    break;
                }
            }
            bestMove = tempBestMove;
            return score;
        } else {

            int score = Integer.MAX_VALUE;
            for (String[] move : moves) {

                Map<String, Piece> copyRedPieces = utility.deepCopyMap(redPieces);
                Map<String, Piece> copyBlackPieces = utility.deepCopyMap(blackPieces);

                ai.makeMove(move, copyRedPieces, copyBlackPieces, false,human);
                score = miniMax(copyRedPieces, copyBlackPieces, depth - 1, alpha, beta, false);
                if (score < beta) {
                    beta = score;
                    tempBestMove = move;
                }
                if (beta <= alpha) {
                    break;
                }
            }
            bestMove = tempBestMove;
            return score;
        }
    }

    private int heuristic(Map<String, Piece> redPieces, Map<String, Piece> blackPieces){
        int diffBlackRed = blackPieces.size() - redPieces.size();
        int numberKings = 0;
        for(String black: blackPieces.keySet()){
            if(blackPieces.get(black).isKing()){
                numberKings++;
            }
        }
        return diffBlackRed + numberKings;
    }

    public String[] getBestMove() {
        return bestMove;
    }
}
