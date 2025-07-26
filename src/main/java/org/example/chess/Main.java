package org.example.chess;


public class Main {
    public static void main(String[] args) {
        Board board = new Board(); // Create a new board

        System.out.println("--- Initial Board State ---");
        board.printBoard(); // Print the initial state

        // --- Example Moves using Chess Notation ---

        // 1. Valid White Pawn move (e2 to e4)
        System.out.println("\nAttempting White Pawn e2 to e4 (valid)...");
        board.move("e2e4"); // Use the new move method
        board.printBoard();

        // 2. Invalid White move (attempt to move White again when it's Black's turn)
        System.out.println("\nAttempting White Pawn a2 to a3 (invalid - not White's turn)...");
        board.move("a2a3"); // Use the new move method
        board.printBoard();

        // 3. Valid Black Pawn move (d7 to d5)
        System.out.println("\nAttempting Black Pawn d7 to d5 (valid)...");
        board.move("d7d5"); // Use the new move method
        board.printBoard();

        // 4. Invalid Black move (attempt to move Black again when it's White's turn)
        System.out.println("\nAttempting Black Pawn f7 to f5 (invalid - not Black's turn)...");
        board.move("f7f5"); // Use the new move method
        board.printBoard();

        // 5. Valid White Knight move (g1 to f3)
        System.out.println("\nAttempting White Knight g1 to f3 (valid)...");
        board.move("g1f3"); // Use the new move method
        board.printBoard();

        // 6. Invalid move: No piece at specified start square (empty square)
        System.out.println("\nAttempting to move from empty square h5 (invalid)...");
        board.move("h5h6"); // Use the new move method
        board.printBoard();

        // 7. Invalid move: Attempt to move black piece when it's white's turn (after white knight moved)
        System.out.println("\nAttempting Black Pawn g7 to g6 (invalid - not Black's turn)...");
        board.move("g7g6"); // Use the new move method
        board.printBoard();

        // 8. Invalid move format
        System.out.println("\nAttempting move with invalid format 'e24'...");
        board.move("e24");
        board.printBoard();

        System.out.println("\nGame over (demo ends).");
    }
}