package org.example.chess;

import java.util.Scanner; // Import the Scanner class for user input

public class Main {
    public static void main(String[] args) {
        Board board = new Board(); // Create a new board
        Scanner scanner = new Scanner(System.in); // Create a Scanner object for reading input

        System.out.println("--- Chess Game Started ---");
        System.out.println("Enter moves in algebraic notation (e.g., 'e2e4'). Type 'exit' to quit.");

        // Main game loop
        while (true) { // Loop indefinitely until game ends or user exits
            board.printBoard(); // Clear console and print the current board state

            System.out.print(board.getCurrentPlayerTurn() + "'s turn. Enter your move: ");
            String moveInput = scanner.nextLine(); // Read user input

            if (moveInput.equalsIgnoreCase("exit") || moveInput.equalsIgnoreCase("quit")) {
                System.out.println("Exiting game. Goodbye!");
                break; // Exit the game loop
            }

            // Attempt to make the move. The board.move() method handles validation and turn switching.
            boolean moveSuccessful = board.move(moveInput);

            // If move was not successful, the Board.move() method already prints an error.
            // The loop will simply re-prompt the same player.
        }

        scanner.close(); // Close the scanner to release system resources
    }
}