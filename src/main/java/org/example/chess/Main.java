package org.example.chess;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Board board = new Board();
        Scanner scanner = new Scanner(System.in);

        Piece.PieceColor humanPlayerColor = Piece.PieceColor.WHITE;
        Piece.PieceColor aiPlayerColor = Piece.PieceColor.BLACK;

        System.out.println("--- Chess Game Started: Human (" + humanPlayerColor + ") vs. AI (" + aiPlayerColor + ") ---");
        System.out.println("Enter moves in algebraic notation (e.g., 'e2e4' or 'Nf3'). Type 'exit' to quit.");

        while (true) {
            board.printBoard();

            if (board.getCurrentPlayerTurn() == humanPlayerColor) {
                System.out.print(humanPlayerColor + "'s turn. Enter your move: ");
                String moveInput = scanner.nextLine();

                if (moveInput.equalsIgnoreCase("exit") || moveInput.equalsIgnoreCase("quit")) {
                    System.out.println("Exiting game. Goodbye!");
                    break;
                }

                board.move(moveInput);
            } else {
                System.out.println(aiPlayerColor + "'s turn (AI). Thinking...");
                try {
                    Thread.sleep(1000); // Simulate "thinking" time
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.out.println("AI thinking interrupted.");
                }

                String aiMove = board.findRandomLegalMove();
                if (aiMove != null) {
                    board.move(aiMove);
                } else {
                    System.out.println("AI has no legal moves. Game over (for now).");
                    break;
                }
            }
        }

        scanner.close();
    }
}