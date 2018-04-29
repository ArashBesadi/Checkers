package sussex.ist.checkers;


public class Main {

    public static void main(String[] args) {

        Utility utility = new Utility();
        AI ai = new AI(utility);
        Human human = new Human(utility,ai);

        Board board = new Board(human,ai);
        ai.setBoard(board);
        human.setBoard(board);
    }
}
