package org.example.chess;


import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Board board = new Board();
        Scanner scanner = new Scanner(System.in);

        // Decide which player is human and which is AI
        Piece.PieceColor humanPlayerColor = Piece.PieceColor.WHITE; // Human plays White
        Piece.PieceColor aiPlayerColor = Piece.PieceColor.BLACK;    // AI plays Black

        System.out.println("--- Chess Game Started: Human (" + humanPlayerColor + ") vs. AI (" + aiPlayerColor + ") ---");
        System.out.println("Enter moves in algebraic notation (e.g., 'e2e4'). Type 'exit' to quit.");

        while (true) {
            board.printBoard(); // Display the current board state

            if (board.getCurrentPlayerTurn() == humanPlayerColor) {
                // Human's turn
                System.out.print(humanPlayerColor + "'s turn. Enter your move: ");
                String moveInput = scanner.nextLine();

                if (moveInput.equalsIgnoreCase("exit") || moveInput.equalsIgnoreCase("quit")) {
                    System.out.println("Exiting game. Goodbye!");
                    break;
                }

                board.move(moveInput); // Attempt human's move
            } else {
                // AI's turn
                System.out.println(aiPlayerColor + "'s turn (AI). Thinking...");
                try {
                    Thread.sleep(1000); // Simulate "thinking" time for the AI
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.out.println("AI thinking interrupted.");
                }

                String aiMove = board.findRandomLegalMove(); // AI finds a random move
                if (aiMove != null) {
                    board.move(aiMove); // Execute AI's move
                } else {
                    // No legal moves for AI (e.g., checkmate/stalemate, though not fully detected yet)
                    System.out.println("AI has no legal moves. Game over (for now).");
                    break;
                }
            }
        }

        scanner.close();
    }
}