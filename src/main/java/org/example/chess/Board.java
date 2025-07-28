package org.example.chess; // Using the new package name

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Board {
    private Piece[][] squares;
    private Piece.PieceColor currentPlayerTurn;
    private Random random;

    private Map<Piece.PieceColor, Map<Piece.PieceType, Integer>> pieceCounts;

    private static final Pattern FULL_MOVE_NOTATION_PATTERN = Pattern.compile("^[a-h][1-8][a-h][1-8]$");
    private static final Pattern DISAMBIGUATED_FILE_MOVE_PATTERN = Pattern.compile("^[NBRQK][a-h][a-h][1-8]$");
    private static final Pattern DISAMBIGUATED_RANK_MOVE_PATTERN = Pattern.compile("^[NBRQK][1-8][a-h][1-8]$");
    private static final Pattern SHORTENED_PIECE_MOVE_PATTERN = Pattern.compile("^[NBRQK][a-h][1-8]$");

    private static final Pattern PIECE_PLACEMENT_PATTERN = Pattern.compile("^[NBRQKPRQnbrqkprq][a-h][1-8]$");

    // --- NEW: Enum to represent the result of a move operation ---
    public enum MoveResult {
        VALID,              // Move was valid and executed, turn switched
        INVALID,            // Move was invalid, no changes, turn not switched
        PROMOTION_PENDING   // Pawn moved to last rank, promotion choice needed, turn not switched yet
    }

    public Board() {
        squares = new Piece[8][8];
        random = new Random();
        initializePieceCounts();
        setupInitialBoard();
        currentPlayerTurn = Piece.PieceColor.WHITE;
    }

    private void setupInitialBoard() {
        for (int i = 0; i < 8; i++) {
            squares[1][i] = new Piece(Piece.PieceType.PAWN, Piece.PieceColor.BLACK);
            incrementPieceCount(Piece.PieceType.PAWN, Piece.PieceColor.BLACK);
            squares[6][i] = new Piece(Piece.PieceType.PAWN, Piece.PieceColor.WHITE);
            incrementPieceCount(Piece.PieceType.PAWN, Piece.PieceColor.WHITE);
        }

        squares[0][0] = new Piece(Piece.PieceType.ROOK, Piece.PieceColor.BLACK); incrementPieceCount(Piece.PieceType.ROOK, Piece.PieceColor.BLACK);
        squares[0][1] = new Piece(Piece.PieceType.KNIGHT, Piece.PieceColor.BLACK); incrementPieceCount(Piece.PieceType.KNIGHT, Piece.PieceColor.BLACK);
        squares[0][2] = new Piece(Piece.PieceType.BISHOP, Piece.PieceColor.BLACK); incrementPieceCount(Piece.PieceType.BISHOP, Piece.PieceColor.BLACK);
        squares[0][3] = new Piece(Piece.PieceType.QUEEN, Piece.PieceColor.BLACK); incrementPieceCount(Piece.PieceType.QUEEN, Piece.PieceColor.BLACK);
        squares[0][4] = new Piece(Piece.PieceType.KING, Piece.PieceColor.BLACK); incrementPieceCount(Piece.PieceType.KING, Piece.PieceColor.BLACK);
        squares[0][5] = new Piece(Piece.PieceType.BISHOP, Piece.PieceColor.BLACK); incrementPieceCount(Piece.PieceType.BISHOP, Piece.PieceColor.BLACK);
        squares[0][6] = new Piece(Piece.PieceType.KNIGHT, Piece.PieceColor.BLACK); incrementPieceCount(Piece.PieceType.KNIGHT, Piece.PieceColor.BLACK);
        squares[0][7] = new Piece(Piece.PieceType.ROOK, Piece.PieceColor.BLACK); incrementPieceCount(Piece.PieceType.ROOK, Piece.PieceColor.BLACK);

        squares[7][0] = new Piece(Piece.PieceType.ROOK, Piece.PieceColor.WHITE); incrementPieceCount(Piece.PieceType.ROOK, Piece.PieceColor.WHITE);
        squares[7][1] = new Piece(Piece.PieceType.KNIGHT, Piece.PieceColor.WHITE); incrementPieceCount(Piece.PieceType.KNIGHT, Piece.PieceColor.WHITE);
        squares[7][2] = new Piece(Piece.PieceType.BISHOP, Piece.PieceColor.WHITE); incrementPieceCount(Piece.PieceType.BISHOP, Piece.PieceColor.WHITE);
        squares[7][3] = new Piece(Piece.PieceType.QUEEN, Piece.PieceColor.WHITE); incrementPieceCount(Piece.PieceType.QUEEN, Piece.PieceColor.WHITE);
        squares[7][4] = new Piece(Piece.PieceType.KING, Piece.PieceColor.WHITE); incrementPieceCount(Piece.PieceType.KING, Piece.PieceColor.WHITE);
        squares[7][5] = new Piece(Piece.PieceType.BISHOP, Piece.PieceColor.WHITE); incrementPieceCount(Piece.PieceType.BISHOP, Piece.PieceColor.WHITE);
        squares[7][6] = new Piece(Piece.PieceType.KNIGHT, Piece.PieceColor.WHITE); incrementPieceCount(Piece.PieceType.KNIGHT, Piece.PieceColor.WHITE);
        squares[7][7] = new Piece(Piece.PieceType.ROOK, Piece.PieceColor.WHITE); incrementPieceCount(Piece.PieceType.ROOK, Piece.PieceColor.WHITE);
    }

    private void initializePieceCounts() {
        pieceCounts = new HashMap<>();
        for (Piece.PieceColor color : Piece.PieceColor.values()) {
            pieceCounts.put(color, new HashMap<>());
            for (Piece.PieceType type : Piece.PieceType.values()) {
                pieceCounts.get(color).put(type, 0);
            }
        }
    }

    private void incrementPieceCount(Piece.PieceType type, Piece.PieceColor color) {
        pieceCounts.get(color).put(type, pieceCounts.get(color).get(type) + 1);
    }

    private void decrementPieceCount(Piece.PieceType type, Piece.PieceColor color) {
        pieceCounts.get(color).put(type, pieceCounts.get(color).get(type) - 1);
    }

    private boolean canPlacePiece(Piece.PieceType newPieceType, Piece.PieceColor newPieceColor, int targetRow, int targetCol) {
        Piece existingPiece = squares[targetRow][targetCol];

        if (existingPiece != null && existingPiece.getType() == newPieceType && existingPiece.getColor() == newPieceColor) {
            return true;
        }

        int currentCount = pieceCounts.get(newPieceColor).get(newPieceType);
        int maxLimit;
        switch (newPieceType) {
            case KING:   maxLimit = 1; break;
            case QUEEN:  maxLimit = 1; break;
            case ROOK:   maxLimit = 2; break;
            case KNIGHT: maxLimit = 2; break;
            case BISHOP: maxLimit = 2; break;
            case PAWN:   maxLimit = 8; break;
            default: return false;
        }

        if (currentCount >= maxLimit) {
            System.out.println("Cannot place more than " + maxLimit + " " + newPieceColor + " " + newPieceType + "s.");
            return false;
        }
        return true;
    }

    public void clearBoard() {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                squares[r][c] = null;
            }
        }
        initializePieceCounts();
        System.out.println("Board cleared to a blank state.");
    }

    public boolean placePiece(String placementNotation) {
        if (!PIECE_PLACEMENT_PATTERN.matcher(placementNotation).matches()) {
            System.out.println("Invalid placement format: '" + placementNotation + "'. Expected format like 'Nf3' (White Knight at f3) or 'kr1' (Black King at a1).");
            return false;
        }

        char pieceChar = placementNotation.charAt(0);
        char fileChar = placementNotation.charAt(1);
        char rankChar = placementNotation.charAt(2);

        Piece.PieceColor pieceColor = Character.isUpperCase(pieceChar) ? Piece.PieceColor.WHITE : Piece.PieceColor.BLACK;
        Piece.PieceType pieceType;

        switch (Character.toLowerCase(pieceChar)) {
            case 'p': pieceType = Piece.PieceType.PAWN; break;
            case 'n': pieceType = Piece.PieceType.KNIGHT; break;
            case 'b': pieceType = Piece.PieceType.BISHOP; break;
            case 'r': pieceType = Piece.PieceType.ROOK; break;
            case 'q': pieceType = Piece.PieceType.QUEEN; break;
            case 'k': pieceType = Piece.PieceType.KING; break;
            default: return false;
        }

        int col = fileChar - 'a';
        int row = 8 - Character.getNumericValue(rankChar);

        if (!canPlacePiece(pieceType, pieceColor, row, col)) {
            return false;
        }

        Piece oldPiece = squares[row][col];
        if (oldPiece != null && (oldPiece.getType() != pieceType || oldPiece.getColor() != pieceColor)) {
            decrementPieceCount(oldPiece.getType(), oldPiece.getColor());
        }

        squares[row][col] = new Piece(pieceType, pieceColor);
        if (oldPiece == null || (oldPiece.getType() != pieceType || oldPiece.getColor() != pieceColor)) {
            incrementPieceCount(pieceType, pieceColor);
        }

        System.out.println("Placed " + pieceColor + " " + pieceType + " at " + fileChar + rankChar + ".");
        return true;
    }

    public void setPlayerTurn(Piece.PieceColor color) {
        this.currentPlayerTurn = color;
    }

    public Piece getPiece(int row, int col) {
        if (row < 0 || row >= 8 || col < 0 || col >= 8) {
            System.err.println("Error: Board coordinates out of bounds [" + row + "," + col + "]");
            return null;
        }
        return squares[row][col];
    }

    /**
     * Attempts to execute a move based on algebraic notation.
     * @param moveNotation The algebraic notation string.
     * @return A MoveResult indicating success, failure, or pending promotion.
     */
    public MoveResult move(String moveNotation) {
        int[] coords = parseAlgebraicNotation(moveNotation);
        if (coords == null) {
            return MoveResult.INVALID;
        }
        // This movePiece is where the main move logic and promotion detection happens
        return movePiece(coords[0], coords[1], coords[2], coords[3]);
    }

    public int[] parseAlgebraicNotation(String notation) {
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

        Matcher fileDisambiguatorMatcher = DISAMBIGUATED_FILE_MOVE_PATTERN.matcher(notation);
        Matcher rankDisambiguatorMatcher = DISAMBIGUATED_RANK_MOVE_PATTERN.matcher(notation);

        if (fileDisambiguatorMatcher.matches() || rankDisambiguatorMatcher.matches()) {
            char pieceChar = notation.charAt(0);
            char disambiguatorChar = notation.charAt(1);
            char endFileChar = notation.charAt(2);
            char endRankChar = notation.charAt(3);

            Piece.PieceType targetPieceType;
            switch (pieceChar) {
                case 'N': targetPieceType = Piece.PieceType.KNIGHT; break;
                case 'B': targetPieceType = Piece.PieceType.BISHOP; break;
                case 'R': targetPieceType = Piece.PieceType.ROOK; break;
                case 'Q': targetPieceType = Piece.PieceType.QUEEN; break;
                case 'K': targetPieceType = Piece.PieceType.KING; break;
                default: return null;
            }

            int endCol = endFileChar - 'a';
            int endRow = 8 - Character.getNumericValue(endRankChar);

            List<int[]> candidateSources = new ArrayList<>();

            for (int sr = 0; sr < 8; sr++) {
                for (int sc = 0; sc < 8; sc++) {
                    Piece piece = squares[sr][sc];
                    if (piece != null && piece.getColor() == currentPlayerTurn && piece.getType() == targetPieceType) {
                        boolean disambiguatorMatches = false;
                        if (Character.isLetter(disambiguatorChar)) { // Disambiguation by file
                            if ((disambiguatorChar - 'a') == sc) {
                                disambiguatorMatches = true;
                            }
                        } else if (Character.isDigit(disambiguatorChar)) { // Disambiguation by rank
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
                System.out.println("Invalid move: Still ambiguous " + targetPieceType + " move to " + endFileChar + endRankChar + " even with disambiguator " + disambiguatorChar + ".");
                return null;
            } else {
                int[] source = candidateSources.get(0);
                return new int[]{source[0], source[1], endRow, endCol};
            }
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
                default: return null;
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
                System.out.println("Invalid move: Ambiguous " + targetPieceType + " move to " + endFileChar + endRankChar + ". Please specify starting file or rank (e.g., 'Nbd7' or 'N1d7').");
                return null;
            } else {
                int[] source = candidateSources.get(0);
                return new int[]{source[0], source[1], endRow, endCol};
            }
        }

        System.out.println("Invalid move format: '" + notation + "'. Please use 'e2e4', 'Nf3', 'Nbd7', or 'N1d7' format.");
        return null;
    }

    /**
     * Executes a move on the board after coordinates have been determined and passed preliminary validations.
     * This method handles the actual board update, piece capture, promotion detection, and turn switching.
     * @param startRow Starting row.
     * @param intstartCol Starting column. // Corrected parameter name
     * @param endRow Ending row.
     * @param endCol Ending column.
     * @return A MoveResult indicating if the move was valid, invalid, or if promotion is pending.
     */
    public MoveResult movePiece(int startRow, int intstartCol, int endRow, int endCol) { // Note: Renamed startCol to intstartCol to avoid clash with local var
        // These checks are already done by isValidMoveAttempt, but keep for robustness if called directly
        if (startRow < 0 || startRow >= 8 || intstartCol < 0 || intstartCol >= 8 ||
                endRow < 0 || endRow >= 8 || endCol < 0 || endCol >= 8) {
            System.out.println("Invalid move: Internal coordinates out of board bounds.");
            return MoveResult.INVALID;
        }

        Piece pieceToMove = squares[startRow][intstartCol];

        if (pieceToMove == null || pieceToMove.getColor() != currentPlayerTurn) {
            System.out.println("Invalid move: Piece at start square is not valid for current turn."); // More general msg
            return MoveResult.INVALID;
        }

        Piece pieceAtEnd = squares[endRow][endCol];
        if (pieceAtEnd != null && pieceAtEnd.getColor() == currentPlayerTurn) {
            System.out.println("Invalid move: Cannot capture your own piece.");
            return MoveResult.INVALID;
        }

        // --- All comprehensive checks are now inside isValidMoveAttempt ---
        if (!isValidMoveAttempt(startRow, intstartCol, endRow, endCol)) {
            // isValidMoveAttempt already prints specific error messages if it returns false
            return MoveResult.INVALID;
        }

        // --- If all validations pass, perform the move and handle promotion ---
        // 1. Decrement count of captured piece, if any
        if (pieceAtEnd != null) {
            decrementPieceCount(pieceAtEnd.getType(), pieceAtEnd.getColor());
        }

        // 2. Perform the basic move (pawn or other piece)
        squares[endRow][endCol] = pieceToMove;
        squares[startRow][intstartCol] = null;

        // 3. Check for Promotion
        boolean isPromotion = false;
        if (pieceToMove.getType() == Piece.PieceType.PAWN) {
            if ((pieceToMove.getColor() == Piece.PieceColor.WHITE && endRow == 0) ||
                    (pieceToMove.getColor() == Piece.PieceColor.BLACK && endRow == 7)) {
                isPromotion = true;
            }
        }

        // 4. If promotion is pending, signal it to Main. DO NOT switch turn yet.
        if (isPromotion) {
            System.out.println(pieceToMove.getColor() + " Pawn reached promotion square " + (char)('a'+endCol) + (char)('1'+(7-endRow)) + "!");
            // Store the coordinates of the promoting pawn for Main to finalize
            // Not strictly needed here, as Main will know the end square from input.
            return MoveResult.PROMOTION_PENDING;
        } else {
            // If no promotion, switch turn as usual
            switchTurn();
            System.out.println("Move successful! Now it's " + currentPlayerTurn + "'s turn.");
            return MoveResult.VALID;
        }
    }

    /**
     * Finalizes a pawn promotion by replacing the pawn with the chosen piece.
     * This method is called by Main after the player chooses a promotion piece.
     * @param promotionRow The row where the pawn promoted.
     * @param promotionCol The column where the pawn promoted.
     * @param chosenType The Piece.PieceType to promote to.
     */
    public void finalizePromotion(int promotionRow, int promotionCol, Piece.PieceType chosenType) {
        Piece promotingPawn = squares[promotionRow][promotionCol];
        if (promotingPawn == null || promotingPawn.getType() != Piece.PieceType.PAWN) {
            System.err.println("Error: No pawn found at promotion square or not a pawn.");
            return;
        }

        // Decrement the pawn count
        decrementPieceCount(Piece.PieceType.PAWN, promotingPawn.getColor());

        // Place the new promoted piece
        squares[promotionRow][promotionCol] = new Piece(chosenType, promotingPawn.getColor());

        // Increment the count of the new piece type
        incrementPieceCount(chosenType, promotingPawn.getColor());

        System.out.println(promotingPawn.getColor() + " Pawn promoted to " + chosenType + "!");

        // After promotion is finalized, switch the turn
        switchTurn();
        System.out.println("Turn switched to " + currentPlayerTurn + ".");
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
                        if (squares[endRow][endCol] != null) return false;
                        if (rowDir == -1) {
                            if (rowDiff == 1) return true;
                            if (rowDiff == 2 && startRow == 6 && squares[startRow-1][startCol] == null) return true;
                        }
                    } else if (colDiff == 1) { // Diagonal move (capture)
                        if (rowDiff == 1 && rowDir == -1 && squares[endRow][endCol] != null) return true;
                    }
                } else { // Black pawn
                    if (startCol == endCol) { // Straight move
                        if (squares[endRow][endCol] != null) return false;
                        if (rowDir == 1) {
                            if (rowDiff == 1) return true;
                            if (rowDiff == 2 && startRow == 1 && squares[startRow+1][startCol] == null) return true;
                        }
                    } else if (colDiff == 1) { // Diagonal move (capture)
                        if (rowDiff == 1 && rowDir == 1 && squares[endRow][endCol] != null) return true;
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

    public int[] findKing(Piece.PieceColor kingColor) {
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
            System.err.println("Error: King of color " + kingColor + " not found on board. Cannot check for check.");
            return false;
        }

        int kingRow = kingPos[0];
        int kingCol = kingPos[1];
        Piece.PieceColor opponentColor = (kingColor == Piece.PieceColor.WHITE) ? Piece.PieceColor.BLACK : Piece.PieceColor.WHITE;

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece opponentPiece = squares[r][c];
                if (opponentPiece != null && opponentPiece.getColor() == opponentColor) {
                    if (opponentPiece.getType() == Piece.PieceType.PAWN) {
                        int pawnRowDir = (opponentPiece.getColor() == Piece.PieceColor.WHITE) ? -1 : 1;
                        int rowDiff = kingRow - r;
                        int colDiff = kingCol - c;
                        if (Math.abs(colDiff) == 1 && rowDiff == pawnRowDir) {
                            return true;
                        }
                    } else {
                        Piece tempKing = squares[kingRow][kingCol];
                        squares[kingRow][kingCol] = null;
                        boolean canAttackKing = isValidPieceMove(r, c, kingRow, kingCol);
                        squares[kingRow][kingCol] = tempKing;
                        if (canAttackKing) {
                            return true;
                        }
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
                            // Call isValidMoveAttempt to get full validation, including self-check
                            // For move generation, we only care if it's "VALID" (not "PROMOTION_PENDING" here)
                            // We need to temporarily move and check for promotion for isValidMoveAttempt
                            // No, isValidMoveAttempt should just say if the move is legal; promotion is an *effect*
                            // The current isValidMoveAttempt correctly returns true for a promotion move that is legal.

                            // To filter out non-moves and promotions for AI generation
                            // We don't need the AI to choose "e8Q" at this stage; just "e8"
                            // If the move leads to PROMOTION_PENDING, it's still a "legal" move to be generated.
                            if (isValidMoveAttempt(startRow, startCol, endRow, endCol)) {
                                char startFile = (char) ('a' + startCol);
                                char startRank = (char) ('1' + (7 - startRow));
                                char endFile = (char) ('a' + endCol);
                                char endRank = (char) ('1' + (7 - endRow));
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

        // --- Self-Check Validation Logic ---
        Piece originalStartPiece = squares[startRow][startCol];
        Piece originalEndPiece = squares[endRow][endCol];

        squares[endRow][endCol] = originalStartPiece;
        squares[startRow][startCol] = null;

        boolean isKingInCheckAfterMove = isKingInCheck(currentPlayerTurn);

        squares[startRow][startCol] = originalStartPiece;
        squares[endRow][endCol] = originalEndPiece;

        if (isKingInCheckAfterMove) {
            return false;
        }

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
            System.out.print((char)('1' + (7 - r)) + "|");
            for (int c = 0; c < 8; c++) {
                Piece piece = squares[r][c];
                System.out.print(" " + (piece != null ? piece.getAsciiChar() : " ") + " |");
            }
            System.out.println((char)('1' + (7 - r)));
            System.out.println(" +---+---+---+---+---+---+---+---+");
        }
        System.out.println("   A   B   C   D   E   F   G   H");
        System.out.println("Current Turn: " + currentPlayerTurn);
    }
}