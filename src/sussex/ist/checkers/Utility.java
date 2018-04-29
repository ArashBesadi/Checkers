package sussex.ist.checkers;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Utility {

    public static final int MAX_BOARDER = 7;
    public static final int MIN_BOARDER = 0;

    private List<String[]> simpleMoves;
    private List<String[]> jumpMoves;
    private List<String[]> copyJumpMoves;

    private Map<Integer, List<String[]>> tempRemoveKeys = new HashMap<>();
    private Map<String, List<String[]>> removeKeys = new HashMap<>();

    public void successor(String currentKey, Map<String, Piece> redPieces, Map<String, Piece> blackPieces, boolean human) {

        String[] xy = currentKey.split(":");
        int x = Integer.valueOf(xy[0]);
        int y = Integer.valueOf(xy[1]);

        Map<String, Piece> attackedPlayer;

        boolean king;
        if (human) {
            king = redPieces.get(currentKey).isKing();
            attackedPlayer = blackPieces;
        } else {
            king = blackPieces.get(currentKey).isKing();
            attackedPlayer = redPieces;
        }

        int maxY = y + 1;
        int minY = y - 1;
        int minX = x - 1;
        int maxX = x + 1;

        int attackMaxY = y + 2;
        int attackMinY = y - 2;
        int attackMaxX = x + 2;
        int attackMinX = x - 2;

        String key1 = createKey(maxX, minY);
        String key2 = createKey(minX, minY);
        String key3 = createKey(maxX, maxY);
        String key4 = createKey(minX, maxY);

        String attackKey1 = createKey(attackMaxX, attackMinY);
        String attackKey2 = createKey(attackMinX, attackMinY);
        String attackKey3 = createKey(attackMaxX, attackMaxY);
        String attackKey4 = createKey(attackMinX, attackMaxY);

        if (king || !human) {
            if (!redPieces.containsKey(attackKey1) && !blackPieces.containsKey(attackKey1)) {
                if (attackedPlayer.containsKey(key1)) {
                    if (attackMaxX <= MAX_BOARDER && attackMinY >= MIN_BOARDER) {
                        String[] possibleAttackMove = {attackKey1, currentKey, key1};
                        jumpMoves.add((possibleAttackMove));
                    }
                }
            }
            if (!redPieces.containsKey(attackKey2) && !blackPieces.containsKey(attackKey2)) {
                if (attackedPlayer.containsKey(key2)) {
                    if (attackMinX >= MIN_BOARDER && attackMinY >= MIN_BOARDER) {
                        String[] possibleAttackMove = {attackKey2, currentKey, key2};
                        jumpMoves.add(possibleAttackMove);
                    }
                }
            }
        }
        if (king || human) {
            if (!redPieces.containsKey(attackKey3) && !blackPieces.containsKey(attackKey3)) {
                if (attackedPlayer.containsKey(key3)) {
                    if (attackMaxX <= MAX_BOARDER && attackMaxY <= MAX_BOARDER) {
                        String[] possibleAttackMove = {attackKey3, currentKey, key3};
                        jumpMoves.add((possibleAttackMove));
                    }
                }
            }
            if (!redPieces.containsKey(attackKey4) && !blackPieces.containsKey(attackKey4)) {
                if (attackedPlayer.containsKey(key4)) {
                    if (attackMinX >= MIN_BOARDER && attackMaxY <= MAX_BOARDER) {
                        String[] possibleAttackMove = {attackKey4, currentKey, key4};
                        jumpMoves.add(possibleAttackMove);
                    }
                }
            }
        }


        if (jumpMoves.isEmpty()) {
            if (king || !human) {

                if (maxX <= MAX_BOARDER && minY >= MIN_BOARDER) {
                    if (!redPieces.containsKey(key1) && !blackPieces.containsKey(key1)) {
                        String[] possibleMove = {key1, currentKey};
                        simpleMoves.add(possibleMove);
                    }
                }
                if (minX >= MIN_BOARDER && minY >= MIN_BOARDER) {
                    if (!redPieces.containsKey(key2) && !blackPieces.containsKey(key2)) {
                        String[] possibleMove = {key2, currentKey};
                        simpleMoves.add(possibleMove);
                    }
                }

            }

            if (king || human) {
                if (maxX <= MAX_BOARDER && maxY <= MAX_BOARDER) {
                    if (!redPieces.containsKey(key3) && !blackPieces.containsKey(key3)) {
                        String[] possibleMove = {key3, currentKey};
                        simpleMoves.add(possibleMove);
                    }
                }
                if (minX >= MIN_BOARDER && maxY <= MAX_BOARDER) {
                    if (!redPieces.containsKey(key4) && !blackPieces.containsKey(key4)) {
                        String[] possibleMove = {key4, currentKey};
                        simpleMoves.add(possibleMove);
                    }
                }

            }
        }
    }

    public void checkMultipleJump(Map<String, Piece> redPieces, Map<String, Piece> blackPieces, boolean human) {

        if (!jumpMoves.isEmpty()) {

            for (int i = 0; i < copyJumpMoves.size(); i++) {

                String newKey = copyJumpMoves.get(i)[0];
                String formerKey = copyJumpMoves.get(i)[1];
                String removedPiece = copyJumpMoves.get(i)[2];

                if (!tempRemoveKeys.containsKey(i)) {
                    tempRemoveKeys.put(i, new ArrayList<>());
                }
                List<String[]> tempKeys = this.tempRemoveKeys.get(i);
                String[] keyCombination = {removedPiece,newKey};
                tempKeys.add(keyCombination);

                this.tempRemoveKeys.put(i, tempKeys);

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
                if (!jumpMoves.isEmpty()) {
                    copyJumpMoves.remove(i);
                    String[] key = jumpMoves.get(jumpMoves.size() - 1);

                    String[] comb = {key[2],key[0]};
                    tempKeys.add(comb);

                    copyJumpMoves.add(i, key);
                    clearMoves();
                    // maybe remove this
//                    removeKeys = new HashMap<>();
                    checkMultipleJump(redPieces, blackPieces, human);
                }
            }
            for (int i = 0; i < copyJumpMoves.size(); i++) {
                List<String[]> blackKeys = tempRemoveKeys.get(i);
                String key = copyJumpMoves.get(i)[0];
                removeKeys.put(key, blackKeys);
            }
            tempRemoveKeys = new HashMap<>();
        }

    }

    public void getPossibleMoves(Map<String, Piece> redPieces, Map<String, Piece> blackPieces,boolean human) {

        clearMoves();
        Map<String,Piece> pieces;
        if(human){
            pieces = redPieces;
        }else{
            pieces = blackPieces;
        }

        for (String key : pieces.keySet()) {
            successor(key, redPieces, blackPieces, human);
        }
    }

    public List<String[]> deepCopyList(List<String[]> originalList) {

        List<String[]> newList = new ArrayList<String[]>();
        for (String[] moves : originalList) {
            String[] copiedArray = new String[moves.length];
            System.arraycopy(moves, 0, copiedArray, 0, moves.length);
            newList.add(copiedArray);
        }
        return newList;

    }

    public Map<String, Piece> deepCopyMap(Map<String, Piece> redPieces) {

        Map<String, Piece> copyRedPiece = new HashMap<>();

        for (String key : redPieces.keySet()) {
            copyRedPiece.put(key, redPieces.get(key));
        }
        return copyRedPiece;
    }

    private String createKey(int x, int y) {
        return String.valueOf(x) + ":" + String.valueOf(y);
    }

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
