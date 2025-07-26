package org.example.chess;

import java.util.regex.Pattern; // Import for using regular expressions

public class Board {
    private Piece[][] squares;
    private Piece.PieceColor currentPlayerTurn;

    // --- NEW: Declare the Pattern for move notation validation ---
    private static final Pattern MOVE_NOTATION_PATTERN = Pattern.compile("^[a-h][1-8][a-h][1-8]$");

    public Board() {
        squares = new Piece[8][8];
        setupInitialBoard();
        currentPlayerTurn = Piece.PieceColor.WHITE;
    }

    private void setupInitialBoard() {
        // Set up pawns
        for (int i = 0; i < 8; i++) {
            squares[1][i] = new Piece(Piece.PieceType.PAWN, Piece.PieceColor.BLACK); // Rank 7 (index 1)
            squares[6][i] = new Piece(Piece.PieceType.PAWN, Piece.PieceColor.WHITE); // Rank 2 (index 6)
        }

        // Set up black major/minor pieces (Rank 8, index 0)
        squares[0][0] = new Piece(Piece.PieceType.ROOK, Piece.PieceColor.BLACK);
        squares[0][1] = new Piece(Piece.PieceType.KNIGHT, Piece.PieceColor.BLACK);
        squares[0][2] = new Piece(Piece.PieceType.BISHOP, Piece.PieceColor.BLACK);
        squares[0][3] = new Piece(Piece.PieceType.QUEEN, Piece.PieceColor.BLACK);
        squares[0][4] = new Piece(Piece.PieceType.KING, Piece.PieceColor.BLACK);
        squares[0][5] = new Piece(Piece.PieceType.BISHOP, Piece.PieceColor.BLACK);
        squares[0][6] = new Piece(Piece.PieceType.KNIGHT, Piece.PieceColor.BLACK);
        squares[0][7] = new Piece(Piece.PieceType.ROOK, Piece.PieceColor.BLACK);

        // Set up white major/minor pieces (Rank 1, index 7)
        squares[7][0] = new Piece(Piece.PieceType.ROOK, Piece.PieceColor.WHITE);
        squares[7][1] = new Piece(Piece.PieceType.KNIGHT, Piece.PieceColor.WHITE);
        squares[7][2] = new Piece(Piece.PieceType.BISHOP, Piece.PieceColor.WHITE);
        squares[7][3] = new Piece(Piece.PieceType.QUEEN, Piece.PieceColor.WHITE);
        squares[7][4] = new Piece(Piece.PieceType.KING, Piece.PieceColor.WHITE);
        squares[7][5] = new Piece(Piece.PieceType.BISHOP, Piece.PieceColor.WHITE);
        squares[7][6] = new Piece(Piece.PieceType.KNIGHT, Piece.PieceColor.WHITE);
        squares[7][7] = new Piece(Piece.PieceType.ROOK, Piece.PieceColor.WHITE);
    }

    public Piece getPiece(int row, int col) {
        if (row < 0 || row >= 8 || col < 0 || col >= 8) {
            System.err.println("Error: Board coordinates out of bounds [" + row + "," + col + "]");
            return null;
        }
        return squares[row][col];
    }

    public boolean move(String moveNotation) {
        int[] coords = parseNotationToCoordinates(moveNotation);
        if (coords == null) {
            // Error message already printed by parseNotationToCoordinates
            return false;
        }
        return movePiece(coords[0], coords[1], coords[2], coords[3]);
    }

    /**
     * Parses a chess notation string (e.g., "e2e4") into board array coordinates.
     * Includes regex validation for the string format.
     *
     * @param notation The algebraic notation string (e.g., "e2e4").
     * @return An int array [startRow, startCol, endRow, endCol] or null if invalid format.
     */
    private int[] parseNotationToCoordinates(String notation) {
        if (notation == null) {
            return null;
        }

        // --- NEW: Use regex to validate the format ---
        if (!MOVE_NOTATION_PATTERN.matcher(notation).matches()) {
            System.out.println("Invalid move format: '" + notation + "'. Expected format like 'e2e4'.");
            return null;
        }
        // --- END NEW ---

        // The rest of the parsing logic, now that the format is guaranteed
        char startFileChar = notation.charAt(0);
        char startRankChar = notation.charAt(1);
        char endFileChar = notation.charAt(2);
        char endRankChar = notation.charAt(3);

        int startCol = startFileChar - 'a'; // 'a' -> 0, 'b' -> 1, ..., 'h' -> 7
        int startRow = 8 - Character.getNumericValue(startRankChar); // '1' -> 7, '2' -> 6, ..., '8' -> 0

        int endCol = endFileChar - 'a';
        int endRow = 8 - Character.getNumericValue(endRankChar);

        return new int[]{startRow, startCol, endRow, endCol};
    }

    public boolean movePiece(int startRow, int startCol, int endRow, int endCol) {
        // Bounds check (already done in parseNotation, but good to keep for direct calls)
        if (startRow < 0 || startRow >= 8 || startCol < 0 || startCol >= 8 ||
                endRow < 0 || endRow >= 8 || endCol < 0 || endCol >= 8) {
            System.out.println("Invalid move: Internal coordinates out of board bounds.");
            return false;
        }

        Piece pieceToMove = squares[startRow][startCol];

        if (pieceToMove == null) {
            System.out.println("Invalid move: No piece found at [" + (char)('A' + startCol) + (8 - startRow) + "].");
            return false;
        }

        if (pieceToMove.getColor() != currentPlayerTurn) {
            System.out.println("Invalid move: It's " + currentPlayerTurn + "'s turn. You can only move your own pieces.");
            return false;
        }

        Piece pieceAtEnd = squares[endRow][endCol];
        if (pieceAtEnd != null && pieceAtEnd.getColor() == currentPlayerTurn) {
            System.out.println("Invalid move: Cannot capture your own piece at [" + (char)('A' + endCol) + (8 - endRow) + "].");
            return false;
        }

        // --- Future: Add piece-specific move rules here (e.g., how a pawn moves) ---

        squares[endRow][endCol] = pieceToMove;
        squares[startRow][startCol] = null;

        switchTurn();
        System.out.println("Move successful! Now it's " + currentPlayerTurn + "'s turn.");
        return true;
    }

    private void switchTurn() {
        if (currentPlayerTurn == Piece.PieceColor.WHITE) {
            currentPlayerTurn = Piece.PieceColor.BLACK;
        } else {
            currentPlayerTurn = Piece.PieceColor.WHITE;
        }
    }

    public Piece.PieceColor getCurrentPlayerTurn() {
        return currentPlayerTurn;
    }

    private void clearConsole() {
        try {
            final String os = System.getProperty("os.name");
            if (os.contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (final Exception e) {
            for (int i = 0; i < 50; ++i) System.out.println();
            System.out.println("Warning: Console clear failed. Printing newlines instead.");
        }
    }

    public void printBoard() {
        clearConsole();

        System.out.println("   A   B   C   D   E   F   G   H");
        System.out.println(" +---+---+---+---+---+---+---+---+");

        for (int r = 0; r < 8; r++) {
            System.out.print((8 - r) + "|");
            for (int c = 0; c < 8; c++) {
                Piece piece = squares[r][c];
                System.out.print(" " + (piece != null ? piece.getAsciiChar() : " ") + " |");
            }
            System.out.println((8 - r));
            System.out.println(" +---+---+---+---+---+---+---+---+");
        }
        System.out.println("   A   B   C   D   E   F   G   H");
        System.out.println("Current Turn: " + currentPlayerTurn);
    }
}