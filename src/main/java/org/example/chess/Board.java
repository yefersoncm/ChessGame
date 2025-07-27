package org.example.chess; // Using the new package name

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Board {
    private Piece[][] squares;
    private Piece.PieceColor currentPlayerTurn;
    private Random random;

    private static final Pattern FULL_MOVE_NOTATION_PATTERN = Pattern.compile("^[a-h][1-8][a-h][1-8]$");
    // --- NEW PATTERNS for Disambiguated Moves ---
    // Piece + StartFile + EndFileRank (e.g., Nbd7)
    private static final Pattern DISAMBIGUATED_FILE_MOVE_PATTERN = Pattern.compile("^[NBRQK][a-h][a-h][1-8]$");
    // Piece + StartRank + EndFileRank (e.g., N1d7)
    private static final Pattern DISAMBIGUATED_RANK_MOVE_PATTERN = Pattern.compile("^[NBRQK][1-8][a-h][1-8]$");
    // --- END NEW PATTERNS ---
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

    /**
     * Parses various forms of algebraic notation into board array coordinates.
     * Order of parsing attempts is important:
     * 1. Full 'fileRankFileRank' (e.g., "e2e4")
     * 2. Disambiguated moves (e.g., "Nbd7", "N1d7")
     * 3. Shortened Piece Moves (e.g., "Nf3")
     * (Future: Pawn moves like "e4", captures with 'x', castling)
     *
     * @param notation The algebraic notation string.
     * @return An int array [startRow, startCol, endRow, endCol] or null if invalid or ambiguous.
     */
    private int[] parseAlgebraicNotation(String notation) {
        if (notation == null || notation.isEmpty()) {
            return null;
        }

        // 1. Try to parse as full 'fileRankFileRank' (e.g., "e2e4", "a1h8")
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

        // --- NEW: 2. Try to parse as Disambiguated Piece Move ---
        Matcher fileDisambiguatorMatcher = DISAMBIGUATED_FILE_MOVE_PATTERN.matcher(notation);
        Matcher rankDisambiguatorMatcher = DISAMBIGUATED_RANK_MOVE_PATTERN.matcher(notation);

        if (fileDisambiguatorMatcher.matches() || rankDisambiguatorMatcher.matches()) {
            char pieceChar = notation.charAt(0);
            char disambiguatorChar = notation.charAt(1); // 'b' for Nbd7, '1' for N1d7
            char endFileChar = notation.charAt(2);
            char endRankChar = notation.charAt(3);

            Piece.PieceType targetPieceType;
            switch (pieceChar) {
                case 'N': targetPieceType = Piece.PieceType.KNIGHT; break;
                case 'B': targetPieceType = Piece.PieceType.BISHOP; break;
                case 'R': targetPieceType = Piece.PieceType.ROOK; break;
                case 'Q': targetPieceType = Piece.PieceType.QUEEN; break;
                case 'K': targetPieceType = Piece.PieceType.KING; break;
                default: return null; // Should not happen with regex
            }

            int endCol = endFileChar - 'a';
            int endRow = 8 - Character.getNumericValue(endRankChar);

            List<int[]> candidateSources = new ArrayList<>();

            for (int sr = 0; sr < 8; sr++) {
                for (int sc = 0; sc < 8; sc++) {
                    Piece piece = squares[sr][sc];
                    if (piece != null && piece.getColor() == currentPlayerTurn && piece.getType() == targetPieceType) {
                        boolean disambiguatorMatches = false;
                        if (Character.isLetter(disambiguatorChar)) { // Disambiguation by file (e.g., 'b' in Nbd7)
                            if ((disambiguatorChar - 'a') == sc) {
                                disambiguatorMatches = true;
                            }
                        } else if (Character.isDigit(disambiguatorChar)) { // Disambiguation by rank (e.g., '1' in N1d7)
                            if ((8 - Character.getNumericValue(disambiguatorChar)) == sr) {
                                disambiguatorMatches = true;
                            }
                        }

                        if (disambiguatorMatches) {
                            if (isValidMoveAttempt(sr, sc, endRow, endCol)) {
                                candidateSources.add(new int[]{sr, sc});
                            }
                        }
                    }
                }
            }

            if (candidateSources.isEmpty()) {
                System.out.println("Invalid move: No " + targetPieceType + " from " + disambiguatorChar + " can legally move to " + endFileChar + endRankChar + ".");
                return null;
            } else if (candidateSources.size() > 1) {
                // This indicates a problem: even with disambiguation, it's ambiguous.
                // This shouldn't happen with correctly formed FIDE notation, but good to catch.
                System.out.println("Invalid move: Still ambiguous " + targetPieceType + " move to " + endFileChar + endRankChar + " even with disambiguator " + disambiguatorChar + ".");
                return null;
            } else {
                int[] source = candidateSources.get(0);
                return new int[]{source[0], source[1], endRow, endCol};
            }
        }
        // --- END NEW: Disambiguated Moves ---


        // 3. Try to parse as shortened Piece Move (e.g., "Nf3", "Bg5")
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
                default: return null; // Should not happen with regex
            }

            int endCol = endFileChar - 'a';
            int endRow = 8 - Character.getNumericValue(endRankChar);

            List<int[]> candidateSources = new ArrayList<>();

            for (int sr = 0; sr < 8; sr++) {
                for (int sc = 0; sc < 8; sc++) {
                    Piece piece = squares[sr][sc];
                    if (piece != null && piece.getColor() == currentPlayerTurn && piece.getType() == targetPieceType) {
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
                // --- MODIFIED ERROR MESSAGE for Ambiguity ---
                System.out.println("Invalid move: Ambiguous " + targetPieceType + " move to " + endFileChar + endRankChar + ". Please specify starting file or rank (e.g., 'Nbd7' or 'N1d7').");
                return null; // Ambiguous move
            } else {
                int[] source = candidateSources.get(0);
                return new int[]{source[0], source[1], endRow, endCol};
            }
        }

        // --- Future: Add other parsing formats here (e.g., pawn moves like "e4", captures with 'x', castling) ---

        System.out.println("Invalid move format: '" + notation + "'. Please use 'e2e4', 'Nf3', 'Nbd7', or 'N1d7' format.");
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

        if (!isValidMoveAttempt(startRow, startCol, endRow, endCol)) {
            // Error message already printed by isValidMoveAttempt
            return false;
        }

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

    private int[] findKing(Piece.PieceColor kingColor) {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece piece = squares[r][c];
                if (piece != null && piece.getType() == Piece.PieceType.KING && piece.getColor() == kingColor) {
                    return new int[]{r, c};
                }
            }
        }
        return null;
    }

    public boolean isKingInCheck(Piece.PieceColor kingColor) {
        int[] kingPos = findKing(kingColor);
        if (kingPos == null) {
            System.err.println("Error: King of color " + kingColor + " not found on board.");
            return false;
        }

        int kingRow = kingPos[0];
        int kingCol = kingPos[1];
        Piece.PieceColor opponentColor = (kingColor == Piece.PieceColor.WHITE) ? Piece.PieceColor.BLACK : Piece.PieceColor.WHITE;

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece opponentPiece = squares[r][c];
                if (opponentPiece != null && opponentPiece.getColor() == opponentColor) {
                    Piece tempKing = squares[kingRow][kingCol]; // Store King
                    squares[kingRow][kingCol] = null; // Temporarily remove King

                    boolean canAttackKing = isValidPieceMove(r, c, kingRow, kingCol);

                    squares[kingRow][kingCol] = tempKing; // Restore King

                    if (canAttackKing) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public String findRandomLegalMove() {
        List<String> legalMoves = new ArrayList<>();

        for (int startRow = 0; startRow < 8; startRow++) {
            for (int startCol = 0; startCol < 8; startCol++) {
                Piece piece = squares[startRow][startCol];

                if (piece != null && piece.getColor() == currentPlayerTurn) {
                    for (int endRow = 0; endRow < 8; endRow++) {
                        for (int endCol = 0; endCol < 8; endCol++) {
                            // isValidMoveAttempt now includes all validations (geometry, obstructions, self-check)
                            if (isValidMoveAttempt(startRow, startCol, endRow, endCol)) {
                                char startFile = (char) ('a' + startCol);
                                char startRank = (char) ('1' + (7 - startRow)); // CORRECTED RANK CONVERSION
                                char endFile = (char) ('a' + endCol);
                                char endRank = (char) ('1' + (7 - endRow));     // CORRECTED RANK CONVERSION
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

        if (!isValidPieceMove(startRow, startCol, endRow, endCol)) {
            return false;
        }

        // --- Self-Check Validation Logic (CRUCIAL) ---
        Piece originalStartPiece = squares[startRow][startCol];
        Piece originalEndPiece = squares[endRow][endCol];

        squares[endRow][endCol] = originalStartPiece; // Simulate move
        squares[startRow][startCol] = null;           // Clear old square

        boolean isKingInCheckAfterMove = isKingInCheck(currentPlayerTurn);

        squares[startRow][startCol] = originalStartPiece; // Undo move
        squares[endRow][endCol] = originalEndPiece;       // Restore captured piece or clear square

        if (isKingInCheckAfterMove) {
            // Uncomment for debugging if needed:
            // System.out.println("Debug: Move " + (char)('a'+startCol)+(char)('1'+(7-startRow)) + (char)('a'+endCol)+(char)('1'+(7-endRow)) + " results in self-check.");
            return false;
        }

        return true; // Move is fully valid
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
            System.out.print((char)('1' + (7 - r)) + "|"); // CORRECTED RANK CONVERSION
            for (int c = 0; c < 8; c++) {
                Piece piece = squares[r][c];
                System.out.print(" " + (piece != null ? piece.getAsciiChar() : " ") + " |");
            }
            System.out.println((char)('1' + (7 - r))); // CORRECTED RANK CONVERSION
            System.out.println(" +---+---+---+---+---+---+---+---+");
        }
        System.out.println("   A   B   C   D   E   F   G   H");
        System.out.println("Current Turn: " + currentPlayerTurn);
    }
}