package sussex.ist.checkers;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class contains some methods that both the user and the AI share such as the successor method.
 */
public class Utility {

    public static final int MAX_BOARDER = 7;
    public static final int MIN_BOARDER = 0;

    private List<String[]> simpleMoves;
    private List<String[]> jumpMoves;
    private List<String[]> copyJumpMoves;

    private Map<Integer, List<String[]>> tempRemoveKeys = new HashMap<>();
    private Map<String, List<String[]>> removeKeys = new HashMap<>();

    /**
     * Returns possible simple moves and jump moves given a current position (key).
     *
     * @param selectedPosition the key to the selected position
     * @param redPieces        the red pieces
     * @param blackPieces      the black pieces
     * @param human            the current player (the user or the AI)
     */
    public void successor(String selectedPosition, Map<String, Piece> redPieces, Map<String, Piece> blackPieces, boolean human) {

        String[] xy = selectedPosition.split(":");
        int x = Integer.valueOf(xy[0]);
        int y = Integer.valueOf(xy[1]);

        Map<String, Piece> currentPlayer;

        // set the current player (either red or black)
        boolean king;
        if (human) {
            king = redPieces.get(selectedPosition).isKing();
            currentPlayer = blackPieces;
        } else {
            king = blackPieces.get(selectedPosition).isKing();
            currentPlayer = redPieces;
        }

        // simple move states
        int maxY = y + 1;
        int minY = y - 1;
        int minX = x - 1;
        int maxX = x + 1;

        // jump move states
        int jumpMaxY = y + 2;
        int jumpMinY = y - 2;
        int jumpMaxX = x + 2;
        int jumpMinX = x - 2;

        String simpleMoveKey1 = createKey(maxX, minY);
        String simpleMoveKey2 = createKey(minX, minY);
        String simpleMoveKey3 = createKey(maxX, maxY);
        String simpleMoveKey4 = createKey(minX, maxY);

        String jumpKey1 = createKey(jumpMaxX, jumpMinY);
        String jumpKey2 = createKey(jumpMinX, jumpMinY);
        String jumpKey3 = createKey(jumpMaxX, jumpMaxY);
        String jumpKey4 = createKey(jumpMinX, jumpMaxY);

        // check valid jump moves and add it into a jump list
        if (king || !human) {
            if (!redPieces.containsKey(jumpKey1) && !blackPieces.containsKey(jumpKey1)) {
                if (currentPlayer.containsKey(simpleMoveKey1)) {
                    if (jumpMaxX <= MAX_BOARDER && jumpMinY >= MIN_BOARDER) {
                        String[] jumpMoveKeys = {jumpKey1, selectedPosition, simpleMoveKey1};
                        jumpMoves.add((jumpMoveKeys));
                    }
                }
            }
            if (!redPieces.containsKey(jumpKey2) && !blackPieces.containsKey(jumpKey2)) {
                if (currentPlayer.containsKey(simpleMoveKey2)) {
                    if (jumpMinX >= MIN_BOARDER && jumpMinY >= MIN_BOARDER) {
                        String[] jumpMoveKeys = {jumpKey2, selectedPosition, simpleMoveKey2};
                        jumpMoves.add(jumpMoveKeys);
                    }
                }
            }
        }
        if (king || human) {
            if (!redPieces.containsKey(jumpKey3) && !blackPieces.containsKey(jumpKey3)) {
                if (currentPlayer.containsKey(simpleMoveKey3)) {
                    if (jumpMaxX <= MAX_BOARDER && jumpMaxY <= MAX_BOARDER) {
                        String[] jumpMoveKeys = {jumpKey3, selectedPosition, simpleMoveKey3};
                        jumpMoves.add((jumpMoveKeys));
                    }
                }
            }
            if (!redPieces.containsKey(jumpKey4) && !blackPieces.containsKey(jumpKey4)) {
                if (currentPlayer.containsKey(simpleMoveKey4)) {
                    if (jumpMinX >= MIN_BOARDER && jumpMaxY <= MAX_BOARDER) {
                        String[] jumpMoveKeys = {jumpKey4, selectedPosition, simpleMoveKey4};
                        jumpMoves.add(jumpMoveKeys);
                    }
                }
            }
        }

        // check valid simple moves and add it into a simple move list
        if (jumpMoves.isEmpty()) {
            if (king || !human) {

                if (maxX <= MAX_BOARDER && minY >= MIN_BOARDER) {
                    if (!redPieces.containsKey(simpleMoveKey1) && !blackPieces.containsKey(simpleMoveKey1)) {
                        String[] simpleMove = {simpleMoveKey1, selectedPosition};
                        simpleMoves.add(simpleMove);
                    }
                }
                if (minX >= MIN_BOARDER && minY >= MIN_BOARDER) {
                    if (!redPieces.containsKey(simpleMoveKey2) && !blackPieces.containsKey(simpleMoveKey2)) {
                        String[] simpleMove = {simpleMoveKey2, selectedPosition};
                        simpleMoves.add(simpleMove);
                    }
                }

            }
            if (king || human) {
                if (maxX <= MAX_BOARDER && maxY <= MAX_BOARDER) {
                    if (!redPieces.containsKey(simpleMoveKey3) && !blackPieces.containsKey(simpleMoveKey3)) {
                        String[] simpleMove = {simpleMoveKey3, selectedPosition};
                        simpleMoves.add(simpleMove);
                    }
                }
                if (minX >= MIN_BOARDER && maxY <= MAX_BOARDER) {
                    if (!redPieces.containsKey(simpleMoveKey4) && !blackPieces.containsKey(simpleMoveKey4)) {
                        String[] simpleMove = {simpleMoveKey4, selectedPosition};
                        simpleMoves.add(simpleMove);
                    }
                }

            }
        }
    }

    /**
     * Checks whether multiple jumps are possible and save them into a HashMap removeKeys.
     *
     * @param redPieces   the red pieces
     * @param blackPieces the black pieces
     * @param human       the current player
     */
    public void checkMultipleJump(Map<String, Piece> redPieces, Map<String, Piece> blackPieces, boolean human) {

        if (!jumpMoves.isEmpty()) {

            for (int i = 0; i < copyJumpMoves.size(); i++) {

                // get the new, former and removed positions
                String newKey = copyJumpMoves.get(i)[0];
                String formerKey = copyJumpMoves.get(i)[1];
                String removeKey = copyJumpMoves.get(i)[2];

                // save the keys for later multi-step visualisation
                if (!tempRemoveKeys.containsKey(i)) {
                    tempRemoveKeys.put(i, new ArrayList<>());
                }
                List<String[]> tempKeys = this.tempRemoveKeys.get(i);
                String[] keyCombination = {removeKey, newKey};
                tempKeys.add(keyCombination);

                this.tempRemoveKeys.put(i, tempKeys);

                // add the position to the board representation
                Piece piece = new Piece();
                if (human) {
                    piece.setKing(redPieces.get(formerKey).isKing());
                    redPieces.put(newKey, piece);

                } else {
                    piece.setKing(blackPieces.get(formerKey).isKing());
                    blackPieces.put(newKey, piece);
                }

                jumpMoves = new ArrayList<>();
                successor(newKey, redPieces, blackPieces, human);

                // if following jump is possible
                if (!jumpMoves.isEmpty()) {
                    copyJumpMoves.remove(i);
                    String[] key = jumpMoves.get(jumpMoves.size() - 1);

                    String[] comb = {key[2], key[0]};
                    tempKeys.add(comb);

                    copyJumpMoves.add(i, key);
                    clearMoves();
                    checkMultipleJump(redPieces, blackPieces, human);
                }
            }
            // save all the temporal keys for visualisation in removeKeys
            for (int i = 0; i < copyJumpMoves.size(); i++) {
                List<String[]> blackKeys = tempRemoveKeys.get(i);
                String key = copyJumpMoves.get(i)[0];
                removeKeys.put(key, blackKeys);
            }
            tempRemoveKeys = new HashMap<>();
        }

    }

    /**
     * Returns all possible moves for a current player.
     *
     * @param redPieces   the red pieces
     * @param blackPieces the black pieces
     * @param human       the current player
     */
    public void getPossibleMoves(Map<String, Piece> redPieces, Map<String, Piece> blackPieces, boolean human) {

        clearMoves();
        Map<String, Piece> pieces;
        if (human) {
            pieces = redPieces;
        } else {
            pieces = blackPieces;
        }

        for (String currentPlayerKeys : pieces.keySet()) {
            successor(currentPlayerKeys, redPieces, blackPieces, human);
        }
    }

    /**
     * Returns a deep (not shallow) copy of a list.
     *
     * @param originalList the list to copy
     * @return a copy of the origin list
     */
    public List<String[]> deepCopyList(List<String[]> originalList) {

        List<String[]> newList = new ArrayList<String[]>();
        for (String[] moves : originalList) {
            String[] copiedArray = new String[moves.length];
            System.arraycopy(moves, 0, copiedArray, 0, moves.length);
            newList.add(copiedArray);
        }
        return newList;

    }

    /**
     * Returns a deep (not shallow) copy of a map.
     *
     * @param originalMap the map to be copied
     * @return a copy of the origin map
     */
    public Map<String, Piece> deepCopyMap(Map<String, Piece> originalMap) {

        Map<String, Piece> copyRedPiece = new HashMap<>();

        for (String key : originalMap.keySet()) {
            copyRedPiece.put(key, originalMap.get(key));
        }
        return copyRedPiece;
    }


    /**
     * Splits the key into an int array holding the x and y positions.
     *
     * @param key the key to split
     * @return an array holding the x and y positions
     */
    public int[] getXY(String key) {
        String[] keyXY = key.split(":");
        int x = Integer.valueOf(keyXY[0]);
        int y = Integer.valueOf(keyXY[1]);
        int xy[] = new int[2];
        xy[0] = x;
        xy[1] = y;
        return xy;
    }

    public List<String[]> getSimpleMoves() {
        return simpleMoves;
    }

    public void setJumpMoves(List<String[]> jumpMoves) {
        this.jumpMoves = jumpMoves;
    }

    public List<String[]> getJumpMoves() {
        return jumpMoves;
    }

    public void clearMoves() {
        simpleMoves = new ArrayList<>();
        jumpMoves = new ArrayList<>();
    }

    private String createKey(int x, int y) {
        return String.valueOf(x) + ":" + String.valueOf(y);
    }

    public Map<String, List<String[]>> getRemoveKeys() {
        return removeKeys;
    }

    public void clearRemoveKeys() {
        removeKeys = new HashMap<>();
    }

    public void setCopyJumpMoves(List<String[]> copyJumpMoves) {
        this.copyJumpMoves = copyJumpMoves;
    }
}
