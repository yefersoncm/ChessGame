package org.example.chess;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

public class Board {
    private Piece[][] squares;
    private Piece.PieceColor currentPlayerTurn;
    private Random random;

    private static final Pattern MOVE_NOTATION_PATTERN = Pattern.compile("^[a-h][1-8][a-h][1-8]$");

    public Board() {
        squares = new Piece[8][8];
        setupInitialBoard();
        currentPlayerTurn = Piece.PieceColor.WHITE;
        random = new Random();
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
            return false;
        }
        return movePiece(coords[0], coords[1], coords[2], coords[3]);
    }

    private int[] parseNotationToCoordinates(String notation) {
        if (notation == null) {
            return null;
        }

        if (!MOVE_NOTATION_PATTERN.matcher(notation).matches()) {
            System.out.println("Invalid move format: '" + notation + "'. Expected format like 'e2e4'.");
            return null;
        }

        char startFileChar = notation.charAt(0);
        char startRankChar = notation.charAt(1);
        char endFileChar = notation.charAt(2);
        char endRankChar = notation.charAt(3);

        int startCol = startFileChar - 'a';
        int startRow = 8 - Character.getNumericValue(startRankChar);

        int endCol = endFileChar - 'a';
        int endRow = 8 - Character.getNumericValue(endRankChar);

        return new int[]{startRow, startCol, endRow, endCol};
    }

    public boolean movePiece(int startRow, int startCol, int endRow, int endCol) {
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

        // --- Now calling isValidPieceMove which includes obstruction checks ---
        if (!isValidPieceMove(startRow, startCol, endRow, endCol)) {
            System.out.println("Invalid move: This piece cannot move to that square according to its rules or path is blocked.");
            return false;
        }

        squares[endRow][endCol] = pieceToMove;
        squares[startRow][startCol] = null;

        switchTurn();
        System.out.println("Move successful! Now it's " + currentPlayerTurn + "'s turn.");
        return true;
    }

    /**
     * Checks if the path between a sliding piece's start and end squares is clear.
     * This method assumes the move is geometrically valid for a sliding piece.
     * @param startRow Starting row.
     * @param startCol Starting column.
     * @param endRow Ending row.
     * @param endCol Ending column.
     * @return true if the path is clear, false if an obstruction is found.
     */
    private boolean isPathClear(int startRow, int startCol, int endRow, int endCol) {
        int rowDir = Integer.compare(endRow, startRow); // -1, 0, or 1
        int colDir = Integer.compare(endCol, startCol); // -1, 0, or 1

        int currentRow = startRow + rowDir;
        int currentCol = startCol + colDir;

        // Iterate through all intermediate squares until just before the end square
        while (currentRow != endRow || currentCol != endCol) {
            if (squares[currentRow][currentCol] != null) {
                return false; // Obstruction found
            }
            currentRow += rowDir;
            currentCol += colDir;
        }
        return true; // Path is clear
    }

    /**
     * Checks if a move is geometrically valid for a specific piece type,
     * including path obstructions for sliding pieces.
     * Does NOT check for putting own king in check.
     */
    private boolean isValidPieceMove(int startRow, int startCol, int endRow, int endCol) {
        Piece piece = squares[startRow][startCol];
        if (piece == null) return false; // Should not happen if called correctly

        int rowDiff = Math.abs(endRow - startRow);
        int colDiff = Math.abs(endCol - startCol);
        int rowDir = Integer.compare(endRow, startRow);
        int colDir = Integer.compare(endCol, startCol);

        switch (piece.getType()) {
            case PAWN:
                // Pawns move differently based on color and capture
                if (piece.getColor() == Piece.PieceColor.WHITE) {
                    if (startCol == endCol) { // Straight move (no capture)
                        if (squares[endRow][endCol] != null) return false; // Cannot capture straight
                        if (rowDir == -1) { // Moving forward
                            if (rowDiff == 1) return true; // 1 square forward
                            if (rowDiff == 2 && startRow == 6 && squares[startRow-1][startCol] == null) return true; // 2 squares on first move
                        }
                    } else if (colDiff == 1) { // Diagonal move (capture)
                        if (rowDiff == 1 && rowDir == -1 && squares[endRow][endCol] != null) return true; // Diagonal capture (must capture)
                    }
                } else { // Black pawn
                    if (startCol == endCol) { // Straight move (no capture)
                        if (squares[endRow][endCol] != null) return false; // Cannot capture straight
                        if (rowDir == 1) { // Moving forward
                            if (rowDiff == 1) return true; // 1 square forward
                            if (rowDiff == 2 && startRow == 1 && squares[startRow+1][startCol] == null) return true; // 2 squares on first move
                        }
                    } else if (colDiff == 1) { // Diagonal move (capture)
                        if (rowDiff == 1 && rowDir == 1 && squares[endRow][endCol] != null) return true; // Diagonal capture (must capture)
                    }
                }
                return false; // All other pawn moves are invalid

            case KNIGHT:
                // L-shape: 2 squares in one direction (row or col), 1 square in perpendicular direction
                return (rowDiff == 2 && colDiff == 1) || (rowDiff == 1 && colDiff == 2);

            case BISHOP:
                // Diagonal: abs(rowDiff) == abs(colDiff)
                if (rowDiff == colDiff && rowDiff > 0) {
                    return isPathClear(startRow, startCol, endRow, endCol); // Check for obstructions
                }
                return false;

            case ROOK:
                // Straight: either rowDiff is 0 (horizontal) or colDiff is 0 (vertical)
                if ((rowDiff > 0 && colDiff == 0) || (colDiff > 0 && rowDiff == 0)) {
                    return isPathClear(startRow, startCol, endRow, endCol); // Check for obstructions
                }
                return false;

            case QUEEN:
                // Combines Bishop and Rook moves
                if ((rowDiff > 0 && colDiff == 0) || (colDiff > 0 && rowDiff == 0) || (rowDiff == colDiff && rowDiff > 0)) {
                    return isPathClear(startRow, startCol, endRow, endCol); // Check for obstructions
                }
                return false;

            case KING:
                // 1 square in any direction (no obstructions to check for single step)
                return rowDiff <= 1 && colDiff <= 1 && (rowDiff > 0 || colDiff > 0);

            default:
                return false;
        }
    }

    public String findRandomLegalMove() {
        List<String> legalMoves = new ArrayList<>();

        for (int startRow = 0; startRow < 8; startRow++) {
            for (int startCol = 0; startCol < 8; startCol++) {
                Piece piece = squares[startRow][startCol];

                if (piece != null && piece.getColor() == currentPlayerTurn) {
                    for (int endRow = 0; endRow < 8; endRow++) {
                        for (int endCol = 0; endCol < 8; endCol++) {
                            // Check if the move is valid according to all rules implemented so far
                            // This includes: general bounds, turn, not capturing own piece, and piece-specific geometry/obstructions
                            // We temporarily 'test' the move using the same validation logic as movePiece()
                            if (isValidMoveAttempt(startRow, startCol, endRow, endCol)) {
                                // If it passes basic validations, add it
                                char startFile = (char) ('a' + startCol);
                                char startRank = (char) ('1' + (8 - startRow));
                                char endFile = (char) ('a' + endCol);
                                char endRank = (char) ('1' + (8 - endRow));
                                legalMoves.add("" + startFile + startRank + endFile + endRank);
                            }
                        }
                    }
                }
            }
        }

        if (legalMoves.isEmpty()) {
            System.out.println("No legal moves found for " + currentPlayerTurn + ".");
            return null;
        }

        String chosenMove = legalMoves.get(random.nextInt(legalMoves.size()));
        return chosenMove;
    }

    /**
     * Helper method to test if a move is broadly valid without actually executing it.
     * This replicates initial checks from movePiece() and isValidPieceMove() for move generation.
     * Important: Does NOT check for putting own king in check (that's complex and future scope).
     */
    private boolean isValidMoveAttempt(int startRow, int startCol, int endRow, int endCol) {
        // Basic bounds check
        if (startRow < 0 || startRow >= 8 || startCol < 0 || startCol >= 8 ||
                endRow < 0 || endRow >= 8 || endCol < 0 || endCol >= 8) {
            return false;
        }

        Piece pieceToMove = squares[startRow][startCol];

        // Must be a piece at start, and must be the current player's piece
        if (pieceToMove == null || pieceToMove.getColor() != currentPlayerTurn) {
            return false;
        }

        // Cannot land on own piece
        Piece pieceAtEnd = squares[endRow][endCol];
        if (pieceAtEnd != null && pieceAtEnd.getColor() == currentPlayerTurn) {
            return false;
        }

        // Check piece-specific movement rules and obstructions
        return isValidPieceMove(startRow, startCol, endRow, endCol);
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