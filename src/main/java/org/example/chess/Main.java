package org.example.chess; // Using the new package name

import java.util.Scanner;

public class Main {
    private static Scanner scanner = new Scanner(System.in); // Make scanner static and accessible

    public static void main(String[] args) {
        mainMenu();
        scanner.close(); // Close scanner when main menu loop ends
    }

    public static void mainMenu() {
        int choice;
        do {
            System.out.println("\n--- Chess Game Menu ---");
            System.out.println("1. Human vs AI Match");
            System.out.println("2. Human vs Human Match");
            System.out.println("3. Training (Custom Board Setup)");
            System.out.println("4. Exit");
            System.out.print("Enter your choice: ");

            while (!scanner.hasNextInt()) {
                System.out.println("Invalid input. Please enter a number.");
                scanner.next(); // Consume the invalid input
                System.out.print("Enter your choice: ");
            }
            choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline left-over

            switch (choice) {
                case 1:
                    startHumanVsAIMatch();
                    break;
                case 2:
                    startHumanVsHumanMatch();
                    break;
                case 3:
                    startTrainingMode();
                    break;
                case 4:
                    System.out.println("Exiting Chess Game. Goodbye!");
                    break;
                default:
                    System.out.println("Invalid choice. Please enter a number between 1 and 4.");
            }
        } while (choice != 4);
    }

    public static void startHumanVsAIMatch() {
        Board board = new Board(); // Start with a fresh, standard board

        Piece.PieceColor humanColor = Piece.PieceColor.WHITE; // Default human is White
        Piece.PieceColor aiColor = Piece.PieceColor.BLACK;    // Default AI is Black

        // Ask who AI will be
        System.out.print("Who will be the AI? (WHITE/BLACK): ");
        String aiColorInput = scanner.nextLine().trim().toUpperCase();
        if (aiColorInput.equals("WHITE")) {
            aiColor = Piece.PieceColor.WHITE;
            humanColor = Piece.PieceColor.BLACK;
        } else if (aiColorInput.equals("BLACK")) {
            // Defaults are fine
        } else {
            System.out.println("Invalid input. Defaulting AI to BLACK.");
        }

        System.out.println("--- Human (" + humanColor + ") vs. AI (" + aiColor + ") Match Started ---");
        System.out.println("Enter moves (e.g., 'e2e4' or 'Nf3'). Type 'exit' to quit.");

        runGameLoop(board, humanColor, aiColor);
    }

    public static void startHumanVsHumanMatch() {
        Board board = new Board(); // Start with a fresh, standard board
        System.out.println("--- Human vs. Human Match Started ---");
        System.out.println("Enter moves (e.g., 'e2e4' or 'Nf3'). Type 'exit' to quit.");

        // In HvH, both players are human. We can pass a null/dummy AI color.
        runGameLoop(board, Piece.PieceColor.WHITE, null); // White is always human; no AI color
    }

    public static void startTrainingMode() {
        Board trainingBoard = new Board(); // Create a specific board for training
        trainingBoard.clearBoard(); // Start with a blank board

        System.out.println("\n--- Training Mode: Custom Board Setup ---");
        System.out.println("Enter piece placements (e.g., 'Nf3' for White Knight at f3, 'kr1' for Black King at a1).");
        System.out.println("Type 'done' to finish placing pieces.");

        String input;
        do {
            trainingBoard.printBoard(); // Show current setup
            System.out.print("Place piece (e.g., Nf3) or 'done': ");
            input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("done")) {
                break;
            }

            trainingBoard.placePiece(input); // Attempt to place the piece

            // This loop ensures they press enter to continue or type 'n'
            System.out.print("Enter another piece? (y/n): ");
            String another = scanner.nextLine().trim().toLowerCase();
            if (!another.equals("y")) {
                break;
            }

        } while (true); // Loop until 'done' or 'n'

        // --- Set Initial Turn ---
        System.out.print("Who's turn is it to move first? (WHITE/BLACK): ");
        String turnInput = scanner.nextLine().trim().toUpperCase();
        if (turnInput.equals("WHITE")) {
            trainingBoard.setPlayerTurn(Piece.PieceColor.WHITE);
        } else if (turnInput.equals("BLACK")) {
            trainingBoard.setPlayerTurn(Piece.PieceColor.BLACK);
        } else {
            System.out.println("Invalid input. Defaulting turn to WHITE.");
            trainingBoard.setPlayerTurn(Piece.PieceColor.WHITE);
        }

        // --- Choose Game Mode After Setup ---
        System.out.print("Start this custom board as Human vs Human (HvH) or Human vs AI (HvAI)? ");
        String gameModeChoice = scanner.nextLine().trim().toUpperCase();

        if (gameModeChoice.equals("HVH")) {
            System.out.println("--- Starting Human vs. Human Match on Custom Board ---");
            runGameLoop(trainingBoard, Piece.PieceColor.WHITE, null); // Always prompt for both
        } else if (gameModeChoice.equals("HVAI")) {
            Piece.PieceColor humanColor = Piece.PieceColor.WHITE;
            Piece.PieceColor aiColor = Piece.PieceColor.BLACK;

            System.out.print("Who will be the AI in this custom match? (WHITE/BLACK): ");
            String customAiColorInput = scanner.nextLine().trim().toUpperCase();
            if (customAiColorInput.equals("WHITE")) {
                aiColor = Piece.PieceColor.WHITE;
                humanColor = Piece.PieceColor.BLACK;
            } else if (customAiColorInput.equals("BLACK")) {
                // Defaults are fine
            } else {
                System.out.println("Invalid input. Defaulting AI to BLACK.");
            }
            System.out.println("--- Starting Human (" + humanColor + ") vs. AI (" + aiColor + ") Match on Custom Board ---");
            runGameLoop(trainingBoard, humanColor, aiColor);
        } else {
            System.out.println("Invalid game mode choice. Returning to main menu.");
        }
    }


    /**
     * Central game loop logic, reusable for HvAI and HvH modes.
     * @param board The Board instance to use.
     * @param humanPlayerColor The color of the human player (or one of them for HvH).
     * @param aiPlayerColor The color of the AI player, or null if HvH.
     */
    public static void runGameLoop(Board board, Piece.PieceColor humanPlayerColor, Piece.PieceColor aiPlayerColor) {
        System.out.println("Type 'exit' to quit at any time during the match.");
        while (true) {
            board.printBoard(); // Display the current board state

            if (board.getCurrentPlayerTurn() == humanPlayerColor) {
                // Human's turn
                System.out.print(board.getCurrentPlayerTurn() + "'s turn. Enter your move: ");
                String moveInput = scanner.nextLine();

                if (moveInput.equalsIgnoreCase("exit") || moveInput.equalsIgnoreCase("quit")) {
                    System.out.println("Exiting match. Returning to main menu.");
                    break;
                }

                board.move(moveInput); // Attempt human's move
            } else {
                // AI's turn (or second human in HvH if aiPlayerColor is null)
                if (aiPlayerColor != null && board.getCurrentPlayerTurn() == aiPlayerColor) { // It's an AI turn
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
                } else { // It's a Human vs Human setup, so this is the second human player
                    System.out.print(board.getCurrentPlayerTurn() + "'s turn. Enter your move: ");
                    String moveInput = scanner.nextLine();

                    if (moveInput.equalsIgnoreCase("exit") || moveInput.equalsIgnoreCase("quit")) {
                        System.out.println("Exiting match. Returning to main menu.");
                        break;
                    }

                    board.move(moveInput);
                }
            }
        }
    }
}