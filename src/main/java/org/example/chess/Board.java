package org.example.chess; // Using the new package name

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

public class Board {
    private Piece[][] squares;
    private Piece.PieceColor currentPlayerTurn;
    private Random random;

    private static final Pattern FULL_MOVE_NOTATION_PATTERN = Pattern.compile("^[a-h][1-8][a-h][1-8]$");
    private static final Pattern SHORTENED_PIECE_MOVE_PATTERN = Pattern.compile("^[NBRQK][a-h][1-8]$");


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
        int[] coords = parseAlgebraicNotation(moveNotation);
        if (coords == null) {
            return false;
        }
        return movePiece(coords[0], coords[1], coords[2], coords[3]);
    }

    private int[] parseAlgebraicNotation(String notation) {
        if (notation == null || notation.isEmpty()) {
            return null;
        }

        if (FULL_MOVE_NOTATION_PATTERN.matcher(notation).matches()) {
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

        if (SHORTENED_PIECE_MOVE_PATTERN.matcher(notation).matches()) {
            char pieceChar = notation.charAt(0);
            char endFileChar = notation.charAt(1);
            char endRankChar = notation.charAt(2);

            Piece.PieceType targetPieceType;
            switch (pieceChar) {
                case 'N': targetPieceType = Piece.PieceType.KNIGHT; break;
                case 'B': targetPieceType = Piece.PieceType.BISHOP; break;
                case 'R': targetPieceType = Piece.PieceType.ROOK; break;
                case 'Q': targetPieceType = Piece.PieceType.QUEEN; break;
                case 'K': targetPieceType = Piece.PieceType.KING; break;
                default:
                    System.out.println("Invalid piece character in shortened move: " + pieceChar);
                    return null;
            }

            int endCol = endFileChar - 'a';
            int endRow = 8 - Character.getNumericValue(endRankChar);

            List<int[]> candidateSources = new ArrayList<>();

            for (int sr = 0; sr < 8; sr++) {
                for (int sc = 0; sc < 8; sc++) {
                    Piece piece = squares[sr][sc];
                    if (piece != null && piece.getColor() == currentPlayerTurn && piece.getType() == targetPieceType) {
                        // isValidMoveAttempt now correctly considers all rules, including self-check
                        if (isValidMoveAttempt(sr, sc, endRow, endCol)) {
                            candidateSources.add(new int[]{sr, sc});
                        }
                    }
                }
            }

            if (candidateSources.isEmpty()) {
                System.out.println("Invalid move: No " + targetPieceType + " can legally move to " + endFileChar + endRankChar + ".");
                return null;
            } else if (candidateSources.size() > 1) {
                System.out.println("Invalid move: Ambiguous " + targetPieceType + " move to " + endFileChar + endRankChar + ". Please specify starting file/rank (e.g., 'Nbd7' or 'N1d7').");
                return null;
            } else {
                int[] source = candidateSources.get(0);
                return new int[]{source[0], source[1], endRow, endCol};
            }
        }

        System.out.println("Invalid move format: '" + notation + "'. Please use 'e2e4' or 'Nf3' format.");
        return null;
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

        // --- All comprehensive checks are now inside isValidMoveAttempt ---
        if (!isValidMoveAttempt(startRow, startCol, endRow, endCol)) {
            // Error message already printed by isValidMoveAttempt
            return false;
        }

        // If isValidMoveAttempt returns true, the move is fully legal and can be executed
        // Note: isValidMoveAttempt already checks turn and piece ownership, but those are quick
        // front-line checks in movePiece too, so keeping them here for clarity.
        squares[endRow][endCol] = pieceToMove;
        squares[startRow][startCol] = null;

        switchTurn();
        System.out.println("Move successful! Now it's " + currentPlayerTurn + "'s turn.");
        return true;
    }

    private boolean isPathClear(int startRow, int startCol, int endRow, int endCol) {
        int rowDir = Integer.compare(endRow, startRow);
        int colDir = Integer.compare(endCol, startCol);

        int currentRow = startRow + rowDir;
        int currentCol = startCol + colDir;

        while (currentRow != endRow || currentCol != endCol) {
            if (squares[currentRow][currentCol] != null) {
                return false;
            }
            currentRow += rowDir;
            currentCol += colDir;
        }
        return true;
    }

    private boolean isValidPieceMove(int startRow, int startCol, int endRow, int endCol) {
        Piece piece = squares[startRow][startCol];
        if (piece == null) return false;

        int rowDiff = Math.abs(endRow - startRow);
        int colDiff = Math.abs(endCol - startCol);
        int rowDir = Integer.compare(endRow, startRow);
        int colDir = Integer.compare(endCol, startCol);

        switch (piece.getType()) {
            case PAWN:
                if (piece.getColor() == Piece.PieceColor.WHITE) {
                    if (startCol == endCol) { // Straight move
                        if (squares[endRow][endCol] != null) return false; // Cannot capture straight
                        if (rowDir == -1) { // Moving forward
                            if (rowDiff == 1) return true;
                            if (rowDiff == 2 && startRow == 6 && squares[startRow-1][startCol] == null) return true;
                        }
                    } else if (colDiff == 1) { // Diagonal move (capture)
                        if (rowDiff == 1 && rowDir == -1 && squares[endRow][endCol] != null) return true; // Must capture
                    }
                } else { // Black pawn
                    if (startCol == endCol) { // Straight move
                        if (squares[endRow][endCol] != null) return false; // Cannot capture straight
                        if (rowDir == 1) { // Moving forward
                            if (rowDiff == 1) return true;
                            if (rowDiff == 2 && startRow == 1 && squares[startRow+1][startCol] == null) return true;
                        }
                    } else if (colDiff == 1) { // Diagonal move (capture)
                        if (rowDiff == 1 && rowDir == 1 && squares[endRow][endCol] != null) return true; // Must capture
                    }
                }
                return false;

            case KNIGHT:
                return (rowDiff == 2 && colDiff == 1) || (rowDiff == 1 && colDiff == 2);

            case BISHOP:
                if (rowDiff == colDiff && rowDiff > 0) {
                    return isPathClear(startRow, startCol, endRow, endCol);
                }
                return false;

            case ROOK:
                if ((rowDiff > 0 && colDiff == 0) || (colDiff > 0 && rowDiff == 0)) {
                    return isPathClear(startRow, startCol, endRow, endCol);
                }
                return false;

            case QUEEN:
                if ((rowDiff > 0 && colDiff == 0) || (colDiff > 0 && rowDiff == 0) || (rowDiff == colDiff && rowDiff > 0)) {
                    return isPathClear(startRow, startCol, endRow, endCol);
                }
                return false;

            case KING:
                return rowDiff <= 1 && colDiff <= 1 && (rowDiff > 0 || colDiff > 0);

            default:
                return false;
        }
    }

    /**
     * Finds the King's position for a given color.
     * @param kingColor The color of the King to find.
     * @return An int array {row, col} of the King's position, or null if not found (should not happen in a valid game).
     */
    private int[] findKing(Piece.PieceColor kingColor) {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece piece = squares[r][c];
                if (piece != null && piece.getType() == Piece.PieceType.KING && piece.getColor() == kingColor) {
                    return new int[]{r, c};
                }
            }
        }
        return null; // King not found (this would indicate an error in game state or missing setup)
    }

    /**
     * Determines if the King of a given color is currently in check.
     * This method iterates through all opponent pieces and checks if any can attack the King's square.
     * @param kingColor The color of the King to check.
     * @return true if the King is in check, false otherwise.
     */
    public boolean isKingInCheck(Piece.PieceColor kingColor) {
        int[] kingPos = findKing(kingColor);
        if (kingPos == null) {
            // This case should ideally be handled at a higher level (game initialization check)
            // or by throwing a more specific exception in a production game.
            // For now, print error and assume no check if king is missing.
            System.err.println("Error: King of color " + kingColor + " not found on board. Cannot check for check.");
            return false;
        }

        int kingRow = kingPos[0];
        int kingCol = kingPos[1];
        Piece.PieceColor opponentColor = (kingColor == Piece.PieceColor.WHITE) ? Piece.PieceColor.BLACK : Piece.PieceColor.WHITE;

        // Iterate through ALL squares to find opponent's pieces
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece opponentPiece = squares[r][c];
                if (opponentPiece != null && opponentPiece.getColor() == opponentColor) {
                    // Temporarily remove the King from its square to check for attacks
                    // This is important because isValidPieceMove might treat a piece at endRow/endCol differently
                    // (e.g., pawn capture requires an opponent piece, but King is a friendly piece)
                    Piece tempKing = squares[kingRow][kingCol];
                    squares[kingRow][kingCol] = null; // Temporarily make King's square empty

                    // Check if the opponentPiece can geometrically attack the King's square
                    // We call isValidPieceMove, which checks geometric rules and path obstructions.
                    // The 'end' square for this check is the King's position.
                    boolean canAttackKing = isValidPieceMove(r, c, kingRow, kingCol);

                    squares[kingRow][kingCol] = tempKing; // Restore the King immediately

                    if (canAttackKing) {
                        return true; // Opponent piece attacks the King
                    }
                }
            }
        }
        return false; // King is not in check
    }

    public String findRandomLegalMove() {
        List<String> legalMoves = new ArrayList<>();

        for (int startRow = 0; startRow < 8; startRow++) {
            for (int startCol = 0; startCol < 8; startCol++) {
                Piece piece = squares[startRow][startCol];

                if (piece != null && piece.getColor() == currentPlayerTurn) {
                    for (int endRow = 0; endRow < 8; endRow++) {
                        for (int endCol = 0; endCol < 8; endCol++) {
                            // isValidMoveAttempt now includes the crucial self-check validation
                            // It will return true ONLY if the move is fully legal (geometry, no obstructions,
                            // no landing on own piece, AND no putting own King in check).
                            if (isValidMoveAttempt(startRow, startCol, endRow, endCol)) {
                                char startFile = (char) ('a' + startCol);
                                // --- FIX: Corrected Rank Conversion for Output ---
                                char startRank = (char) ('1' + (7 - startRow));
                                char endFile = (char) ('a' + endCol);
                                char endRank = (char) ('1' + (7 - endRow));
                                // --- END FIX ---
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
        System.out.println(currentPlayerTurn + " AI chooses move: " + chosenMove);
        return chosenMove;
    }

    /**
     * Helper method to test if a move is broadly valid without actually executing it on the main board.
     * This replicates initial checks from movePiece(), isValidPieceMove(), AND now checks for self-check.
     */
    private boolean isValidMoveAttempt(int startRow, int startCol, int endRow, int endCol) {
        // --- Initial general checks (copied from movePiece, as they are front-line checks) ---
        if (startRow < 0 || startRow >= 8 || startCol < 0 || startCol >= 8 ||
                endRow < 0 || endRow >= 8 || endCol < 0 || endCol >= 8) {
            return false;
        }

        Piece pieceToMove = squares[startRow][startCol];

        if (pieceToMove == null || pieceToMove.getColor() != currentPlayerTurn) {
            return false;
        }

        Piece pieceAtEnd = squares[endRow][endCol];
        if (pieceAtEnd != null && pieceAtEnd.getColor() == currentPlayerTurn) {
            return false;
        }
        // --- END Initial general checks ---

        // --- Geometric and Obstruction check ---
        if (!isValidPieceMove(startRow, startCol, endRow, endCol)) {
            return false;
        }
        // --- END Geometric and Obstruction check ---


        // --- NEW CRUCIAL STEP: Check for self-check after the potential move ---
        // 1. Temporarily make the move on the board
        Piece originalStartPiece = squares[startRow][startCol]; // Should be pieceToMove
        Piece originalEndPiece = squares[endRow][endCol];       // Could be null or opponent piece

        squares[endRow][endCol] = originalStartPiece; // Move piece
        squares[startRow][startCol] = null;           // Clear old square

        // 2. Check if the current player's King is in check AFTER this temporary move
        boolean isKingInCheckAfterMove = isKingInCheck(currentPlayerTurn);

        // 3. Undo the temporary move to restore the board state
        squares[startRow][startCol] = originalStartPiece; // Put piece back to start
        squares[endRow][endCol] = originalEndPiece;       // Put captured piece back or clear square

        // 4. If the King IS in check after the move, then this move is NOT valid
        if (isKingInCheckAfterMove) {
            // Uncomment next line for debugging if needed:
            // System.out.println("Debug: Move " + (char)('a'+startCol)+(8-startRow) + (char)('a'+endCol)+(8-endRow) + " results in self-check.");
            return false; // Move is illegal because it puts/leaves own King in check
        }

        return true; // Move is valid (passed all checks)
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
            // --- FIX: Corrected Rank Conversion for Printing ---
            System.out.print((char)('1' + (7 - r)) + "|"); // Example: r=0 -> '8', r=7 -> '1'
            // --- END FIX ---
            for (int c = 0; c < 8; c++) {
                Piece piece = squares[r][c];
                System.out.print(" " + (piece != null ? piece.getAsciiChar() : " ") + " |");
            }
            // --- FIX: Corrected Rank Conversion for Printing ---
            System.out.println((char)('1' + (7 - r)));
            // --- END FIX ---
            System.out.println(" +---+---+---+---+---+---+---+---+");
        }
        System.out.println("   A   B   C   D   E   F   G   H");
        System.out.println("Current Turn: " + currentPlayerTurn);
    }
}