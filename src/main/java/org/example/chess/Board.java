package org.example.chess;

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

    private int[] enPassantTargetSquare = null;

    // --- Castling Flags ---
    private boolean whiteKingMoved = false;
    private boolean blackKingMoved = false;
    private boolean whiteRookAMoved = false; // Queenside Rook (a1)
    private boolean whiteRookHMoved = false; // Kingside Rook (h1)
    private boolean blackRookAMoved = false; // Queenside Rook (a8)
    private boolean blackRookHMoved = false; // Kingside Rook (h8)
    // --- END Castling Flags ---

    private static final Pattern FULL_MOVE_NOTATION_PATTERN = Pattern.compile("^[a-h][1-8][a-h][1-8]$");
    private static final Pattern DISAMBIGUATED_FILE_MOVE_PATTERN = Pattern.compile("^[NBRQK][a-h][a-h][1-8]$");
    private static final Pattern DISAMBIGUATED_RANK_MOVE_PATTERN = Pattern.compile("^[NBRQK][1-8][a-h][1-8]$");
    private static final Pattern SHORTENED_PIECE_MOVE_PATTERN = Pattern.compile("^[NBRQK][a-h][1-8]$");

    private static final Pattern PIECE_PLACEMENT_PATTERN = Pattern.compile("^[NBRQKPRQnbrqkprq][a-h][1-8]$");

    private static final Pattern CASTLE_KINGSIDE_PATTERN = Pattern.compile("O-O|0-0");
    private static final Pattern CASTLE_QUEENSIDE_PATTERN = Pattern.compile("O-O-O|0-0-0");

    public enum MoveResult {
        VALID,
        INVALID,
        PROMOTION_PENDING
    }

    public Board() {
        squares = new Piece[8][8];
        random = new Random();
        initializePieceCounts();
        setupInitialBoard();
        currentPlayerTurn = Piece.PieceColor.WHITE;
        enPassantTargetSquare = null;

        // --- Initialize Castling Flags for New Game (important for starting board) ---
        whiteKingMoved = false;
        blackKingMoved = false;
        whiteRookAMoved = false;
        whiteRookHMoved = false;
        blackRookAMoved = false;
        blackRookHMoved = false;
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
        squares[7][2] = new  Piece(Piece.PieceType.BISHOP, Piece.PieceColor.WHITE); incrementPieceCount(Piece.PieceType.BISHOP, Piece.PieceColor.WHITE);
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
        enPassantTargetSquare = null;
        // --- Reset Castling Flags on Clear ---
        whiteKingMoved = false;
        blackKingMoved = false;
        whiteRookAMoved = false;
        whiteRookHMoved = false;
        blackRookAMoved = false;
        blackRookHMoved = false;
        // --- END Reset ---
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
    public Board.MoveResult move(String moveNotation) {
        // --- NEW: Check for Castling Notation First ---
        boolean isKingsideCastleNotation = CASTLE_KINGSIDE_PATTERN.matcher(moveNotation).matches();
        boolean isQueensideCastleNotation = CASTLE_QUEENSIDE_PATTERN.matcher(moveNotation).matches();

        if (isKingsideCastleNotation || isQueensideCastleNotation) {
            int kingStartRow = (currentPlayerTurn == Piece.PieceColor.WHITE) ? 7 : 0;
            int kingStartCol = 4; // e-file

            int kingEndCol = isKingsideCastleNotation ? 6 : 2; // g-file for Kingside, c-file for Queenside

            // Pass King's move only; movePiece will recognize it as castling and move Rook
            return movePiece(kingStartRow, kingStartCol, kingStartRow, kingEndCol);
        }
        // --- END NEW ---

        int[] coords = parseAlgebraicNotation(moveNotation);
        if (coords == null) {
            return Board.MoveResult.INVALID;
        }
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
     * @param startCol Starting column.
     * @param endRow Ending row.
     * @param endCol Ending column.
     * @return A MoveResult indicating if the move was valid, invalid, or if promotion is pending.
     */
    public Board.MoveResult movePiece(int startRow, int startCol, int endRow, int endCol) {
        // These checks are already done by isValidMoveAttempt, but keep for robustness if called directly
        if (startRow < 0 || startRow >= 8 || startCol < 0 || startCol >= 8 ||
                endRow < 0 || endRow >= 8 || endCol < 0 || endCol >= 8) {
            System.out.println("Invalid move: Internal coordinates out of board bounds.");
            return Board.MoveResult.INVALID;
        }

        Piece pieceToMove = squares[startRow][startCol];

        if (pieceToMove == null || pieceToMove.getColor() != currentPlayerTurn) {
            System.out.println("Invalid move: Piece at start square is not valid for current turn.");
            return Board.MoveResult.INVALID;
        }

        Piece pieceAtEnd = squares[endRow][endCol];
        // This check is now integrated more deeply into isValidMoveAttempt for pawn rules, but keeping it here for general clarity
        if (pieceAtEnd != null && pieceAtEnd.getColor() == currentPlayerTurn) {
            System.out.println("Invalid move: Cannot capture your own piece.");
            return Board.MoveResult.INVALID;
        }

        // --- All comprehensive checks are now inside isValidMoveAttempt ---
        if (!isValidMoveAttempt(startRow, startCol, endRow, endCol)) {
            // isValidMoveAttempt already prints specific error messages if it returns false
            return Board.MoveResult.INVALID;
        }

        // --- Determine if Castling Move (after isValidMoveAttempt confirms legality) ---
        boolean isCastlingMove = false;
        if (pieceToMove.getType() == Piece.PieceType.KING && Math.abs(startCol - endCol) == 2 && startRow == endRow) {
            isCastlingMove = true;
        }
        // --- END NEW ---

        boolean isEnPassantCapture = false;
        Piece capturedPawnByEnPassant = null;
        int capturedPawnByEnPassantRow = -1;
        int capturedPawnByEnPassantCol = -1;

        // 1. Determine if this specific move is an En Passant capture (before any piece movement)
        if (pieceToMove.getType() == Piece.PieceType.PAWN && Math.abs(startCol - endCol) == 1) { // Pawn making diagonal move
            if (squares[endRow][endCol] == null) { // Landing on empty square (MUST BE FOR EN PASSANT)
                if (enPassantTargetSquare != null && endRow == enPassantTargetSquare[0] && endCol == enPassantTargetSquare[1]) {
                    isEnPassantCapture = true;
                    capturedPawnByEnPassantRow = (pieceToMove.getColor() == Piece.PieceColor.WHITE) ? endRow + 1 : endRow - 1;
                    capturedPawnByEnPassantCol = endCol;
                    capturedPawnByEnPassant = squares[capturedPawnByEnPassantRow][capturedPawnByEnPassantCol];
                }
            }
        }

        // 2. Handle captured pieces (normal capture OR en passant capture)
        if (isEnPassantCapture) {
            if (capturedPawnByEnPassant != null && capturedPawnByEnPassant.getType() == Piece.PieceType.PAWN && capturedPawnByEnPassant.getColor() != pieceToMove.getColor()) {
                squares[capturedPawnByEnPassantRow][capturedPawnByEnPassantCol] = null; // Remove the captured pawn
                decrementPieceCount(capturedPawnByEnPassant.getType(), capturedPawnByEnPassant.getColor()); // Decrement its count
                System.out.println("En Passant capture!");
            } else {
                System.err.println("Error: En Passant confirmed but no valid pawn to capture at " + (char)('a'+capturedPawnByEnPassantCol) + (char)('1'+(7-capturedPawnByEnPassantRow)));
                return Board.MoveResult.INVALID;
            }
        } else if (pieceAtEnd != null) { // Not En Passant, but a normal capture
            decrementPieceCount(pieceAtEnd.getType(), pieceAtEnd.getColor());
        }

        // 3. Perform the basic move (pawn or other piece)
        squares[endRow][endCol] = pieceToMove;
        squares[startRow][startCol] = null;

        // --- NEW: Perform Rook move if Castling ---
        if (isCastlingMove) {
            int rookStartCol;
            int rookEndCol;
            if (endCol == 6) { // Kingside (O-O)
                rookStartCol = 7; // h-file
                rookEndCol = 5;   // f-file
            } else { // Queenside (O-O-O)
                rookStartCol = 0; // a-file
                rookEndCol = 3;   // d-file
            }
            // Move the Rook
            squares[endRow][rookEndCol] = squares[endRow][rookStartCol];
            squares[endRow][rookStartCol] = null;
            System.out.println("Castling performed!");
        }
        // --- END NEW ---

        // 4. Update hasMoved flags for King and Rooks (after their potential moves, including castling)
        if (pieceToMove.getType() == Piece.PieceType.KING) {
            if (pieceToMove.getColor() == Piece.PieceColor.WHITE) whiteKingMoved = true;
            else blackKingMoved = true;
        } else if (pieceToMove.getType() == Piece.PieceType.ROOK) {
            if (pieceToMove.getColor() == Piece.PieceColor.WHITE) {
                if (startCol == 0 && startRow == 7) whiteRookAMoved = true; // White a1 rook
                if (startCol == 7 && startRow == 7) whiteRookHMoved = true; // White h1 rook
            } else {
                if (startCol == 0 && startRow == 0) blackRookAMoved = true; // Black a8 rook
                if (startCol == 7 && startRow == 0) blackRookHMoved = true; // Black h8 rook
            }
        }


        // 5. Check for Pawn Promotion (after the pawn has officially moved)
        boolean isPromotion = false;
        if (pieceToMove.getType() == Piece.PieceType.PAWN) {
            if ((pieceToMove.getColor() == Piece.PieceColor.WHITE && endRow == 0) ||
                    (pieceToMove.getColor() == Piece.PieceColor.BLACK && endRow == 7)) {
                isPromotion = true;
            }
        }

        // 6. Update En Passant target for the *next* turn (based on *this* move)
        int rowDiffFromStart = Math.abs(startRow - endRow);
        if (pieceToMove.getType() == Piece.PieceType.PAWN && rowDiffFromStart == 2) { // Pawn moved exactly two squares
            if (pieceToMove.getColor() == Piece.PieceColor.WHITE) { // White pawn moved from rank 2 to 4
                enPassantTargetSquare = new int[]{endRow + 1, endCol}; // Target is square behind it (rank 3 for White)
            } else { // Black pawn moved from rank 7 to 5
                enPassantTargetSquare = new int[]{endRow - 1, endCol}; // Target is square behind it (rank 6 for Black)
            }
        } else {
            enPassantTargetSquare = null; // Clear if not a two-square pawn push
        }

        // 7. Return appropriate MoveResult. DO NOT switch turn yet if PROMOTION_PENDING.
        if (isPromotion) {
            System.out.println(pieceToMove.getColor() + " Pawn reached promotion square " + (char)('a'+endCol) + (char)('1'+(7-endRow)) + "!");
            return Board.MoveResult.PROMOTION_PENDING;
        } else {
            // If no promotion, switch turn as usual
            switchTurn();
            System.out.println("Move successful! Now it's " + currentPlayerTurn + "'s turn.");
            return Board.MoveResult.VALID;
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

        decrementPieceCount(Piece.PieceType.PAWN, promotingPawn.getColor());

        squares[promotionRow][promotionCol] = new Piece(chosenType, promotingPawn.getColor());

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
                // Straight move (forward 1 or 2 squares)
                if (colDiff == 0) {
                    if (piece.getColor() == Piece.PieceColor.WHITE && rowDir == -1) {
                        if (rowDiff == 1) return true;
                        if (rowDiff == 2 && startRow == 6 && squares[startRow-1][startCol] == null) return true; // Path check for 2-square move
                    } else if (piece.getColor() == Piece.PieceColor.BLACK && rowDir == 1) {
                        if (rowDiff == 1) return true;
                        if (rowDiff == 2 && startRow == 1 && squares[startRow+1][startCol] == null) return true; // Path check for 2-square move
                    }
                }
                // Diagonal attack (forward 1 square, column changes by 1)
                else if (colDiff == 1 && rowDiff == 1) {
                    if (piece.getColor() == Piece.PieceColor.WHITE && rowDir == -1) return true;
                    if (piece.getColor() == Piece.PieceColor.BLACK && rowDir == 1) return true;
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
                // Normal 1-square king move
                if (rowDiff <= 1 && colDiff <= 1 && (rowDiff > 0 || colDiff > 0)) {
                    return true;
                }
                // --- Castling geometric check ---
                // King moves 2 squares horizontally, on its home rank
                if (rowDiff == 0 && colDiff == 2) {
                    // Call isValidCastlingAttempt to check all specific castling rules.
                    return isValidCastlingAttempt(startRow, startCol, endRow, endCol);
                }
                // --- END Castling geometric check ---
                return false;

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
                        squares[kingRow][kingCol] = null; // Temporarily remove King for isValidPieceMove check
                        boolean canAttackKing = isValidPieceMove(r, c, kingRow, kingCol);
                        squares[kingRow][kingCol] = tempKing; // Restore King
                        if (canAttackKing) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Checks all the specific rules for castling beyond basic King movement geometry.
     * @param kingStartRow King's starting row (must be 0 or 7).
     * @param kingStartCol King's starting column (must be 4).
     * @param kingEndRow King's ending row (must be same as startRow).
     * @param kingEndCol King's ending column (2 for Queenside, 6 for Kingside).
     * @return true if castling is completely legal, false otherwise.
     */
    private boolean isValidCastlingAttempt(int kingStartRow, int kingStartCol, int kingEndRow, int kingEndCol) {
        // 1. King must be on its home rank
        if (kingStartRow != 0 && kingStartRow != 7) return false;
        // King must move exactly 2 squares horizontally on the same rank (already checked by caller in isValidPieceMove)
        if (Math.abs(kingEndCol - kingStartCol) != 2 || kingStartRow != kingEndRow) return false;

        // 2. Check if King has moved
        if (currentPlayerTurn == Piece.PieceColor.WHITE) {
            if (whiteKingMoved) { System.out.println("Invalid Castling: White King has moved."); return false; }
        } else { // Black
            if (blackKingMoved) { System.out.println("Invalid Castling: Black King has moved."); return false; }
        }

        // 3. Determine Rook's original position and check if it has moved
        int rookStartCol;
        int rookTargetCol; // The square the rook moves to
        boolean isKingside = (kingEndCol == 6); // g-file for King's end implies Kingside castling

        if (isKingside) {
            rookStartCol = 7; // h-file
            rookTargetCol = 5; // f-file
        } else { // Queenside (kingEndCol == 2 for c-file)
            rookStartCol = 0; // a-file
            rookTargetCol = 3; // d-file
        }
        int rookRow = kingStartRow; // Rook is on the same rank as King

        // Check if the relevant Rook exists on its original square and hasn't moved
        Piece rook = squares[rookRow][rookStartCol];
        if (rook == null || rook.getType() != Piece.PieceType.ROOK || rook.getColor() != currentPlayerTurn) {
            System.out.println("Invalid Castling: No " + currentPlayerTurn + " Rook at original " + (char)('a'+rookStartCol) + (char)('1'+(7-rookRow)) + ".");
            return false;
        }
        if (currentPlayerTurn == Piece.PieceColor.WHITE) {
            if (isKingside && whiteRookHMoved) { System.out.println("Invalid Castling: White Kingside Rook has moved."); return false; }
            if (!isKingside && whiteRookAMoved) { System.out.println("Invalid Castling: White Queenside Rook has moved."); return false; }
        } else { // Black
            if (isKingside && blackRookHMoved) { System.out.println("Invalid Castling: Black Kingside Rook has moved."); return false; }
            if (!isKingside && blackRookAMoved) { System.out.println("Invalid Castling: Black Queenside Rook has moved."); return false; }
        }

        // 4. Check if King is currently in check
        if (isKingInCheck(currentPlayerTurn)) {
            System.out.println("Invalid Castling: King is currently in check.");
            return false;
        }

        // 5. Check for obstructions between King and Rook
        // The path from King's initial square to Rook's initial square
        if (!isPathClear(kingStartRow, kingStartCol, rookRow, rookStartCol)) {
            System.out.println("Invalid Castling: Path between King and Rook is blocked.");
            return false;
        }

        // 6. Check if squares King passes through or lands on are attacked
        // King's original square (e.g., e1), intermediate square (f1 for kingside, d1 for queenside), and final square (g1 for kingside, c1 for queenside)
        int intermediateKingCol = kingStartCol + Integer.compare(kingEndCol, kingStartCol); // King's first step direction

        // Simulate King's movement step-by-step and check for check
        // We need to simulate on a temporary copy or use the make/undo strategy.
        // For isValidMoveAttempt, we use make/undo on the actual board. Let's do similar here.
        // The 'this.squares' will be the board from isValidMoveAttempt's simulation

        Piece originalKing = squares[kingStartRow][kingStartCol];
        Piece originalRook = squares[rookRow][rookStartCol];

        // --- Simulate King moving to intermediate square (1st step) ---
        squares[kingStartRow][kingStartCol] = null; // Temporarily remove King from start square
        squares[kingStartRow][intermediateKingCol] = originalKing; // Simulate King at intermediate square

        if (isKingInCheck(currentPlayerTurn)) {
            System.out.println("Invalid Castling: King passes through an attacked square (" + (char)('a'+intermediateKingCol) + (char)('1'+(7-kingStartRow)) + ").");
            // Restore board state before returning
            squares[kingStartRow][kingStartCol] = originalKing;
            squares[kingStartRow][intermediateKingCol] = null;
            return false;
        }

        // --- Simulate King moving to final square (2nd step) ---
        // King is already at intermediate. Now move it to final for next check.
        squares[kingStartRow][intermediateKingCol] = null; // Clear intermediate square
        squares[kingEndRow][kingEndCol] = originalKing;    // Simulate King at final square

        if (isKingInCheck(currentPlayerTurn)) {
            System.out.println("Invalid Castling: King lands in an attacked square (" + (char)('a'+kingEndCol) + (char)('1'+(7-kingEndRow)) + ").");
            // Restore board state before returning
            squares[kingStartRow][kingStartCol] = originalKing;
            squares[kingEndRow][kingEndCol] = null;
            return false;
        }

        // --- FINAL RESTORATION FOR VALIDATION ---
        // If it reaches here, the move is valid for castling.
        // Restore the board completely before exiting this validation method.
        squares[kingStartRow][kingStartCol] = originalKing;
        squares[kingEndRow][kingEndCol] = null; // Ensure final King square is clear as it was when method started (for simulation)
        squares[rookRow][rookStartCol] = originalRook; // Ensure original rook is there

        return true; // All castling conditions met
    }


    public String findRandomLegalMove() {
        List<String> legalMoves = new ArrayList<>();

        for (int startRow = 0; startRow < 8; startRow++) {
            for (int startCol = 0; startCol < 8; startCol++) {
                Piece piece = squares[startRow][startCol];

                if (piece != null && piece.getColor() == currentPlayerTurn) {
                    for (int endRow = 0; endRow < 8; endRow++) {
                        for (int endCol = 0; endCol < 8; endCol++) {
                            if (isValidMoveAttempt(startRow, startCol, endRow, endCol)) {
                                char startFile = (char) ('a' + startCol);
                                char startRank = (char) ('1' + (7 - startRow));
                                char endFile = (char) ('a' + endCol);
                                char endRank = (char) ('1' + (7 - endRow));

                                // --- NEW: Handle Castling Notation Generation for AI ---
                                if (piece.getType() == Piece.PieceType.KING && Math.abs(startCol - endCol) == 2 && startRow == endRow) {
                                    if (endCol == 6) legalMoves.add("O-O"); // Kingside
                                    else legalMoves.add("O-O-O"); // Queenside
                                } else {
                                    legalMoves.add("" + startFile + startRank + endFile + endRank);
                                }
                                // --- END NEW ---
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

    private boolean isValidMoveAttempt(int startRow, int startCol, int endRow, int endCol) {
        if (startRow < 0 || startRow >= 8 || startCol < 0 || startCol >= 8 ||
                endRow < 0 || endRow >= 8 || endCol < 0 || endCol >= 8) {
            return false;
        }

        Piece pieceToMove = squares[startRow][startCol];

        if (pieceToMove == null || pieceToMove.getColor() != currentPlayerTurn) {
            return false;
        }

        Piece targetPiece = squares[endRow][endCol];
        boolean isTargetEmpty = (targetPiece == null);
        boolean isTargetOccupiedByOpponent = (targetPiece != null && targetPiece.getColor() != currentPlayerTurn);
        boolean isTargetOccupiedByOwn = (targetPiece != null && targetPiece.getColor() == currentPlayerTurn);

        if (isTargetOccupiedByOwn && (startRow != endRow || startCol != endCol)) {
            return false;
        }

        // --- Geometric Move Check ---
        if (!isValidPieceMove(startRow, startCol, endRow, endCol)) {
            return false;
        }
        // --- END Geometric Move Check ---

        // --- Specific Occupation Rules (after geometry is confirmed) ---
        if (pieceToMove.getType() == Piece.PieceType.PAWN) {
            if (startCol == endCol) { // Straight pawn move: MUST be empty
                if (!isTargetEmpty) return false;
            } else { // Diagonal pawn move: MUST be capture OR En Passant
                boolean isEnPassantCandidate = (enPassantTargetSquare != null &&
                        endRow == enPassantTargetSquare[0] &&
                        endCol == enPassantTargetSquare[1]);

                if (isEnPassantCandidate) {
                    if (pieceToMove.getColor() == Piece.PieceColor.WHITE && startRow != 3) return false; // White on 5th rank
                    if (pieceToMove.getColor() == Piece.PieceColor.BLACK && startRow != 4) return false; // Black on 4th rank
                } else { // Normal diagonal pawn move, must be a capture
                    if (!isTargetOccupiedByOpponent) return false;
                }
            }
        } else { // For all non-pawn pieces
            // Occupation checks handled by `isTargetOccupiedByOwn` above.
            // If target is empty, valid. If target has opponent piece, valid capture.
        }
        // --- END Specific Occupation Rules ---


        // --- Self-Check Validation Logic (CRUCIAL) ---
        // This part also handles Castling's King-path-through-check validation by temporarily changing the board.

        // Save original board state for undoing temporary move
        Piece originalStartPiece = squares[startRow][startCol];
        Piece originalEndPiece = squares[endRow][endCol];
        Piece originalEnPassantCapturedPawn = null;

        // Perform move temporarily on board for simulation
        // For castling, this initial move is the King's 2-square step. isValidCastlingAttempt will do further simulations.
        squares[endRow][endCol] = originalStartPiece;
        squares[startRow][startCol] = null;

        // If the simulated move is an En Passant capture, remove the captured pawn temporarily
        if (pieceToMove.getType() == Piece.PieceType.PAWN && Math.abs(startCol - endCol) == 1 && isTargetEmpty && enPassantTargetSquare != null && endRow == enPassantTargetSquare[0] && endCol == enPassantTargetSquare[1]) {
            int capturedPawnRow = (originalStartPiece.getColor() == Piece.PieceColor.WHITE) ? endRow + 1 : endRow - 1;
            int capturedPawnCol = endCol;
            originalEnPassantCapturedPawn = squares[capturedPawnRow][capturedPawnCol];
            squares[capturedPawnRow][capturedPawnCol] = null;
        }

        boolean isKingInCheckAfterMove = isKingInCheck(currentPlayerTurn);

        // Undo the temporary move to restore the board state
        squares[startRow][startCol] = originalStartPiece;
        squares[endRow][endCol] = originalEndPiece;
        if (enPassantTargetSquare != null && originalEnPassantCapturedPawn != null) {
            int capturedPawnRow = (originalStartPiece.getColor() == Piece.PieceColor.WHITE) ? endRow + 1 : endRow - 1;
            int capturedPawnCol = endCol;
            squares[capturedPawnRow][capturedPawnCol] = originalEnPassantCapturedPawn;
        }

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