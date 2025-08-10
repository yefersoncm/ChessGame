package org.example.chess; // Using the new package name

import java.util.Scanner;

public class Main {
    private static Scanner scanner = new Scanner(System.in);
    private static Board currentBoard;

    public static void main(String[] args) {
        mainMenu();
        scanner.close();
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
                scanner.next();
                System.out.print("Enter your choice: ");
            }
            choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    currentBoard = new Board(); // Fresh board for standard game
                    startHumanVsAIMatch(currentBoard);
                    break;
                case 2:
                    currentBoard = new Board(); // Fresh board for standard game
                    startHumanVsHumanMatch(currentBoard);
                    break;
                case 3:
                    // Training mode creates its own board instance which becomes currentBoard
                    currentBoard = new Board(); // Ensure a board instance exists before starting training setup
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

    public static void startHumanVsAIMatch(Board board) {
        Piece.PieceColor chosenAiColor = null;

        System.out.print("Who will be the AI? (WHITE/BLACK): ");
        String aiColorInput = scanner.nextLine().trim().toUpperCase();

        if (aiColorInput.equals("WHITE")) {
            chosenAiColor = Piece.PieceColor.WHITE;
        } else if (aiColorInput.equals("BLACK")) {
            chosenAiColor = Piece.PieceColor.BLACK;
        } else {
            System.out.println("Invalid input. Defaulting AI to BLACK.");
            chosenAiColor = Piece.PieceColor.BLACK;
        }

        Piece.PieceColor humanColor = (chosenAiColor == Piece.PieceColor.WHITE) ? Piece.PieceColor.BLACK : Piece.PieceColor.WHITE;
        Piece.PieceColor aiColor = chosenAiColor;

        System.out.println("--- Human (" + humanColor + ") vs. AI (" + aiColor + ") Match Started ---");
        System.out.println("Enter moves (e.g., 'e2e4' or 'Nf3'). Type 'exit' to quit.");

        runGameLoop(board, humanColor, aiColor);
    }

    public static void startHumanVsHumanMatch(Board board) {
        System.out.println("--- Human vs. Human Match Started ---");
        System.out.println("Enter moves (e.g., 'e2e4' or 'Nf3'). Type 'exit' to quit.");

        runGameLoop(board, Piece.PieceColor.WHITE, null);
    }

    public static void startTrainingMode() {
        Board trainingBoard = new Board(); // Create a specific board for training
        trainingBoard.clearBoard(); // Start with a blank board
        currentBoard = trainingBoard; // Assign to currentBoard for game loop after setup

        System.out.println("\n--- Training Mode: Custom Board Setup ---");
        System.out.println("Enter piece placements (e.g., 'Nf3' for White Knight at f3, 'kr1' for Black King at a1).");
        System.out.println("Type 'done' to finish placing pieces.");

        String input;
        do {
            currentBoard.printBoard(); // Use currentBoard for printing in setup
            System.out.print("Place piece (e.g., Nf3) or 'done': ");
            input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("done")) {
                if (currentBoard.findKing(Piece.PieceColor.WHITE) == null || currentBoard.findKing(Piece.PieceColor.BLACK) == null) {
                    System.out.println("Error: Both White and Black Kings must be on the board for a valid game. Please place them.");
                    continue;
                }
                break;
            }

            currentBoard.placePiece(input);

            System.out.print("Enter another piece? (y/n): ");
            String another = scanner.nextLine().trim().toLowerCase();
            if (!another.equals("y")) {
                break;
            }

        } while (true);

        Piece.PieceColor chosenStartingTurn = null;
        System.out.print("Who's turn is it to move first? (WHITE/BLACK): ");
        String turnInput = scanner.nextLine().trim().toUpperCase();
        if (turnInput.equals("WHITE")) {
            chosenStartingTurn = Piece.PieceColor.WHITE;
        } else if (turnInput.equals("BLACK")) {
            chosenStartingTurn = Piece.PieceColor.BLACK;
        } else {
            System.out.println("Invalid input. Defaulting turn to WHITE.");
            chosenStartingTurn = Piece.PieceColor.WHITE;
        }
        currentBoard.setPlayerTurn(chosenStartingTurn);

        System.out.print("Start this custom board as Human vs Human (HvH) or Human vs AI (HvAI)? ");
        String gameModeChoice = scanner.nextLine().trim().toUpperCase();

        if (gameModeChoice.equals("HVH")) {
            System.out.println("--- Starting Human vs. Human Match on Custom Board ---");
            runGameLoop(currentBoard, Piece.PieceColor.WHITE, null);
        } else if (gameModeChoice.equals("HVAI")) {
            Piece.PieceColor humanColor = Piece.PieceColor.WHITE;
            Piece.PieceColor aiColor = Piece.PieceColor.BLACK;

            Piece.PieceColor chosenAiColor = null;
            System.out.print("Who will be the AI in this custom match? (WHITE/BLACK): ");
            String customAiColorInput = scanner.nextLine().trim().toUpperCase();
            if (customAiColorInput.equals("WHITE")) {
                chosenAiColor = Piece.PieceColor.WHITE;
            } else if (customAiColorInput.equals("BLACK")) {
                chosenAiColor = Piece.PieceColor.BLACK;
            } else {
                System.out.println("Invalid input. Defaulting AI to BLACK.");
                chosenAiColor = Piece.PieceColor.BLACK;
            }
            aiColor = chosenAiColor;
            humanColor = (chosenAiColor == Piece.PieceColor.WHITE) ? Piece.PieceColor.BLACK : Piece.PieceColor.WHITE;

            System.out.println("--- Starting Human (" + humanColor + ") vs. AI (" + aiColor + ") Match on Custom Board ---");
            runGameLoop(currentBoard, humanColor, aiColor);
        } else {
            System.out.println("Invalid game mode choice. Returning to main menu.");
        }
    }

    public static void runGameLoop(Board board, Piece.PieceColor humanPlayerColor, Piece.PieceColor aiPlayerColor) {
        System.out.println("Type 'exit' to quit at any time during the match.");
        while (true) {
            board.printBoard();
            Piece.PieceColor currentPlayer = board.getCurrentPlayerTurn();
            String moveInput = "";
            Board.MoveResult moveResult;

            String tempFoundMove = null;
            if (currentPlayer == humanPlayerColor || (aiPlayerColor == null && currentPlayer != null)) {
                tempFoundMove = board.findRandomLegalMove();
            } else {
                tempFoundMove = board.findRandomLegalMove();
            }

            if (tempFoundMove == null) {
                board.printBoard();
                if (board.isKingInCheck(currentPlayer)) {
                    System.out.println("\n--- CHECKMATE! " + ( (currentPlayer == Piece.PieceColor.WHITE) ? "BLACK" : "WHITE" ) + " WINS! ---");
                } else {
                    System.out.println("\n--- STALEMATE! Game is a DRAW. ---");
                }
                break;
            }

            if (currentPlayer == humanPlayerColor || (aiPlayerColor == null && currentPlayer != null)) {
                System.out.print(currentPlayer + "'s turn. Enter your move: ");
                moveInput = scanner.nextLine();
                if (moveInput.equalsIgnoreCase("exit") || moveInput.equalsIgnoreCase("quit")) {
                    System.out.println("Exiting match. Returning to main menu.");
                    break;
                }
                moveResult = board.move(moveInput);
            } else {
                System.out.println(currentPlayer + "'s turn (AI). Thinking...");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.out.println("AI thinking interrupted.");
                }
                moveInput = tempFoundMove;
                System.out.println(currentPlayer + " AI chooses move: " + moveInput);
                moveResult = board.move(moveInput);
            }

            if (moveResult == Board.MoveResult.INVALID) {
            } else if (moveResult == Board.MoveResult.PROMOTION_PENDING) {
                System.out.println("Pawn promotion pending!");
                Piece.PieceColor promotingPawnColor = currentPlayer;
                Piece.PieceType chosenType;
                if (promotingPawnColor == humanPlayerColor || (aiPlayerColor == null && promotingPawnColor != null)) {
                    chosenType = promptForPromotionPiece(scanner);
                } else {
                    System.out.println(promotingPawnColor + " AI automatically promotes to QUEEN.");
                    chosenType = Piece.PieceType.QUEEN;
                }
                // --- CORRECTED CODE BLOCK ---
                ParsedMove parsedMove = board.parseAlgebraicNotation(moveInput);
                if (parsedMove != null) {
                    board.finalizePromotion(parsedMove.endRow, parsedMove.endCol, chosenType);
                } else {
                    System.err.println("Error: Could not re-parse promotion move for finalization.");
                    break;
                }
                // --- END CORRECTED CODE BLOCK ---
            } else if (moveResult == Board.MoveResult.VALID) {
            }

            if (moveResult != Board.MoveResult.INVALID) {
                Piece.PieceColor playerWhoseTurnJustStarted = board.getCurrentPlayerTurn();
                System.out.println("DEBUG: Checking if " + playerWhoseTurnJustStarted + "'s King is in check.");
                if (board.isKingInCheck(playerWhoseTurnJustStarted)) {
                    System.out.println("\n--- CHECK! " + playerWhoseTurnJustStarted + "'s King is in check! ---");
                }
            }
        }
    }

    private static Piece.PieceType promptForPromotionPiece(Scanner scanner) {
        System.out.println("Promote pawn to (Q)ueen, (R)ook, (B)ishop, or (N)knight?");
        String choice;
        while (true) {
            System.out.print("Enter your choice (Q/R/B/N): ");
            choice = scanner.nextLine().trim().toUpperCase();
            switch (choice) {
                case "Q": return Piece.PieceType.QUEEN;
                case "R": return Piece.PieceType.ROOK;
                case "B": return Piece.PieceType.BISHOP;
                case "N": return Piece.PieceType.KNIGHT;
                default:
                    System.out.println("Invalid choice. Please enter Q, R, B, or N.");
            }
        }
    }
}