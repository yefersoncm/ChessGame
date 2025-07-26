package org.example.chess;

public class Main {
    public static void main(String[] args) {
        Board board = new Board(); // Create a new board

        System.out.println("--- Initial Board State ---");
        board.printBoard(); // Print the initial state

        // --- Example Moves ---

        // 1. Valid White Pawn move (e2 to e4)
        // (Row 6, Col 4 to Row 4, Col 4)
        System.out.println("\nAttempting White Pawn e2 to e4 (valid)...");
        board.movePiece(6, 4, 4, 4);
        board.printBoard();

        // 2. Invalid White move (attempt to move White again when it's Black's turn)
        System.out.println("\nAttempting White Pawn a2 to a3 (invalid - not White's turn)...");
        board.movePiece(6, 0, 5, 0); // a2 to a3
        board.printBoard();

        // 3. Valid Black Pawn move (d7 to d5)
        // (Row 1, Col 3 to Row 3, Col 3)
        System.out.println("\nAttempting Black Pawn d7 to d5 (valid)...");
        board.movePiece(1, 3, 3, 3);
        board.printBoard();

        // 4. Invalid Black move (attempt to move Black again when it's White's turn)
        System.out.println("\nAttempting Black Pawn f7 to f5 (invalid - not Black's turn)...");
        board.movePiece(1, 5, 3, 5); // f7 to f5
        board.printBoard();

        // 5. Valid White Knight move (g1 to f3)
        // (Row 7, Col 6 to Row 5, Col 5)
        System.out.println("\nAttempting White Knight g1 to f3 (valid)...");
        board.movePiece(7, 6, 5, 5); // White Knight from g1 to f3
        board.printBoard();

        // 6. Invalid move: No piece at specified start square (empty square)
        System.out.println("\nAttempting to move from empty square h5 (invalid)...");
        board.movePiece(3, 7, 2, 7); // h5 to h6
        board.printBoard();

        // 7. Invalid move: Attempt to move black piece when it's white's turn (after white knight moved)
        System.out.println("\nAttempting Black Pawn g7 to g6 (invalid - not Black's turn)...");
        board.printBoard();
        board.movePiece(1, 6, 2, 6); // g7 to g6


    }



}