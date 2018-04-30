package sussex.ist.checkers;


/**
 * This class creates a checkers game based on the British rules.
 * The user can play against an intelligent agent with several difficulties.
 */
public class Main {

    public static void main(String[] args) {

        Utility utility = new Utility();
        AI ai = new AI(utility);
        Human human = new Human(utility, ai);

        Board board = new Board(human, ai);
        ai.setBoard(board);
        human.setBoard(board);
    }
}
