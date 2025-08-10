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

    private boolean whiteKingMoved = false;
    private boolean blackKingMoved = false;
    private boolean whiteRookAMoved = false;
    private boolean whiteRookHMoved = false;
    private boolean blackRookAMoved = false;
    private boolean blackRookHMoved = false;

    private static final Pattern FULL_MOVE_NOTATION_PATTERN = Pattern.compile("^[a-h][1-8][a-h][1-8]$");
    private static final Pattern DISAMBIGUATED_FILE_MOVE_PATTERN = Pattern.compile("^[NBRQK][a-h][a-h][1-8]$");
    private static final Pattern DISAMBIGUATED_RANK_MOVE_PATTERN = Pattern.compile("^[NBRQK][1-8][a-h][1-8]$");
    private static final Pattern SHORTENED_PIECE_MOVE_PATTERN = Pattern.compile("^[NBRQK][a-h][1-8]$");

    private static final Pattern PIECE_PLACEMENT_PATTERN = Pattern.compile("^[NBRQKPRQnbrqkprq][a-h][1-8]$");
    // Inside your Board class, near the other Pattern declarations
    private static final Pattern PAWN_PUSH_NOTATION_PATTERN = Pattern.compile("^[a-h][1-8]$");
    private static final Pattern PAWN_CAPTURE_NOTATION_PATTERN = Pattern.compile("^[a-h]x[a-h][1-8]$");
    private static final Pattern CASTLE_KINGSIDE_PATTERN = Pattern.compile("O-O|0-0");
    private static final Pattern CASTLE_QUEENSIDE_PATTERN = Pattern.compile("O-O-O|0-0-0");
    private static final Pattern PROMOTION_NOTATION_PATTERN = Pattern.compile("^[a-h][1-8][a-h][1-8][NBRQ]$");

    public enum MoveResult {
        VALID,
        INVALID,
        PROMOTION_PENDING
    }

    // --- NEW: Piece Values for AI Evaluation ---
    private static final Map<Piece.PieceType, Integer> PIECE_VALUES;
    static {
        PIECE_VALUES = new HashMap<>();
        PIECE_VALUES.put(Piece.PieceType.PAWN, 1);
        PIECE_VALUES.put(Piece.PieceType.KNIGHT, 3);
        PIECE_VALUES.put(Piece.PieceType.BISHOP, 3);
        PIECE_VALUES.put(Piece.PieceType.ROOK, 5);
        PIECE_VALUES.put(Piece.PieceType.QUEEN, 9);
        PIECE_VALUES.put(Piece.PieceType.KING, 100); // King's value is high but not infinite, as it can be captured
    }

    public Board() {
        squares = new Piece[8][8];
        random = new Random();
        initializePieceCounts();
        setupInitialBoard();
        currentPlayerTurn = Piece.PieceColor.WHITE;
        enPassantTargetSquare = null;

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
        enPassantTargetSquare = null;

        whiteKingMoved = false;
        blackKingMoved = false;
        whiteRookAMoved = false;
        whiteRookHMoved = false;
        blackRookAMoved = false;
        blackRookHMoved = false;
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
        ParsedMove parsedMoveDetails = parseAlgebraicNotationInternal(moveNotation);
        if (parsedMoveDetails == null) {
            return Board.MoveResult.INVALID;
        }
        return movePiece(parsedMoveDetails);
    }

    public ParsedMove parseAlgebraicNotation(String notation) {
        return parseAlgebraicNotationInternal(notation);
    }

    private ParsedMove parseAlgebraicNotationInternal(String notation) {
        if (notation == null || notation.isEmpty()) {
            return null;
        }

        boolean isKingsideCastleNotation = CASTLE_KINGSIDE_PATTERN.matcher(notation).matches();
        boolean isQueensideCastleNotation = CASTLE_QUEENSIDE_PATTERN.matcher(notation).matches();

        if (isKingsideCastleNotation || isQueensideCastleNotation) {
            int kingStartRow = (currentPlayerTurn == Piece.PieceColor.WHITE) ? 7 : 0;
            int kingStartCol = 4; // e-file
            int kingEndCol = isKingsideCastleNotation ? 6 : 2;

            return new ParsedMove(kingStartRow, kingStartCol, kingStartRow, kingEndCol, isKingsideCastleNotation, isQueensideCastleNotation);
        }

        if (PROMOTION_NOTATION_PATTERN.matcher(notation).matches()) {
            char startFileChar = notation.charAt(0);
            char startRankChar = notation.charAt(1);
            char endFileChar = notation.charAt(2);
            char endRankChar = notation.charAt(3);
            char promotedPieceChar = notation.charAt(4);

            int startCol = startFileChar - 'a';
            int startRow = 8 - Character.getNumericValue(startRankChar);
            int endCol = endFileChar - 'a';
            int endRow = 8 - Character.getNumericValue(endRankChar);

            Piece pieceAtStart = squares[startRow][startCol];
            if (pieceAtStart == null || pieceAtStart.getType() != Piece.PieceType.PAWN || pieceAtStart.getColor() != currentPlayerTurn) {
                System.out.println("Invalid promotion move: No pawn of your color at starting square " + startFileChar + startRankChar + ".");
                return null;
            }
            if (!((pieceAtStart.getColor() == Piece.PieceColor.WHITE && endRow == 0) || (pieceAtStart.getColor() == Piece.PieceColor.BLACK && endRow == 7))) {
                System.out.println("Invalid promotion move: Pawn must reach 1st or 8th rank for promotion.");
                return null;
            }

            Piece.PieceType promotionType;
            switch (promotedPieceChar) {
                case 'Q': promotionType = Piece.PieceType.QUEEN; break;
                case 'R': promotionType = Piece.PieceType.ROOK; break;
                case 'B': promotionType = Piece.PieceType.BISHOP; break;
                case 'N': promotionType = Piece.PieceType.KNIGHT; break;
                default: return null;
            }

            ParsedMove parsedMove = new ParsedMove(startRow, startCol, endRow, endCol);
            parsedMove.promotionType = promotionType;
            return parsedMove;
        }

        if (PAWN_CAPTURE_NOTATION_PATTERN.matcher(notation).matches()) {
            char startFileChar = notation.charAt(0);
            char endFileChar = notation.charAt(2);
            char endRankChar = notation.charAt(3);

            int startCol = startFileChar - 'a';
            int endCol = endFileChar - 'a';
            int endRow = 8 - Character.getNumericValue(endRankChar);

            int startRow;
            if (currentPlayerTurn == Piece.PieceColor.WHITE) {
                startRow = endRow + 1;
            } else {
                startRow = endRow - 1;
            }

            Piece pieceAtStart = squares[startRow][startCol];
            if (pieceAtStart == null || pieceAtStart.getType() != Piece.PieceType.PAWN || pieceAtStart.getColor() != currentPlayerTurn) {
                System.out.println("Invalid pawn capture: No pawn found at " + startFileChar + (8-startRow) + " to make this capture.");
                return null;
            }

            return new ParsedMove(startRow, startCol, endRow, endCol);
        }

        if (PAWN_PUSH_NOTATION_PATTERN.matcher(notation).matches()) {
            char endFileChar = notation.charAt(0);
            char endRankChar = notation.charAt(1);

            int endCol = endFileChar - 'a';
            int endRow = 8 - Character.getNumericValue(endRankChar);

            List<int[]> candidateSources = new ArrayList<>();
            int pawnDirection = (currentPlayerTurn == Piece.PieceColor.WHITE) ? -1 : 1;

            int candidateRow1 = endRow - pawnDirection;
            if (candidateRow1 >= 0 && candidateRow1 < 8) {
                Piece pieceAtSource = squares[candidateRow1][endCol];
                if (pieceAtSource != null && pieceAtSource.getType() == Piece.PieceType.PAWN && pieceAtSource.getColor() == currentPlayerTurn) {
                    if (isValidMoveAttempt(new ParsedMove(candidateRow1, endCol, endRow, endCol))) {
                        candidateSources.add(new int[]{candidateRow1, endCol});
                    }
                }
            }

            int candidateRow2 = endRow - (pawnDirection * 2);
            if ((currentPlayerTurn == Piece.PieceColor.WHITE && candidateRow2 == 6) || (currentPlayerTurn == Piece.PieceColor.BLACK && candidateRow2 == 1)) {
                Piece pieceAtSource = squares[candidateRow2][endCol];
                if (pieceAtSource != null && pieceAtSource.getType() == Piece.PieceType.PAWN && pieceAtSource.getColor() == currentPlayerTurn) {
                    if (isValidMoveAttempt(new ParsedMove(candidateRow2, endCol, endRow, endCol))) {
                        candidateSources.add(new int[]{candidateRow2, endCol});
                    }
                }
            }

            if (candidateSources.size() == 1) {
                int[] source = candidateSources.get(0);
                return new ParsedMove(source[0], source[1], endRow, endCol);
            } else if (candidateSources.isEmpty()) {
                System.out.println("Invalid pawn push: No pawn can move to " + notation + ".");
                return null;
            } else {
                System.out.println("Invalid pawn push: Ambiguous move to " + notation + ". Multiple pawns can move here.");
                return null;
            }
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
            return new ParsedMove(startRow, startCol, endRow, endCol);
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
                            ParsedMove tempParsedMove = new ParsedMove(sr, sc, endRow, endCol);
                            if (isValidMoveAttempt(tempParsedMove)) {
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
                return new ParsedMove(source[0], source[1], endRow, endCol);
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
                        ParsedMove tempParsedMove = new ParsedMove(sr, sc, endRow, endCol);
                        if (isValidMoveAttempt(tempParsedMove)) {
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
                return new ParsedMove(source[0], source[1], endRow, endCol);
            }
        }

        System.out.println("Invalid move format: '" + notation + "'. Please use 'e2e4', 'Nf3', 'Nbd7', or 'N1d7' format.");
        return null;
    }

    /**
     * Executes a move on the board after coordinates have been determined and passed preliminary validations.
     * @param parsedMove The ParsedMove object containing move details.
     * @return A MoveResult indicating if the move was valid, invalid, or if promotion is pending.
     */
    public Board.MoveResult movePiece(ParsedMove parsedMove) {
        int startRow = parsedMove.startRow;
        int startCol = parsedMove.startCol;
        int endRow = parsedMove.endRow;
        int endCol = parsedMove.endCol;

        Piece pieceToMove = squares[startRow][startCol];
        Piece pieceAtEnd = squares[endRow][endCol];

        if (pieceToMove == null || pieceToMove.getColor() != currentPlayerTurn) {
            System.out.println("Invalid move: Piece at start square is not valid for current turn.");
            return Board.MoveResult.INVALID;
        }

        if (!isValidMoveAttempt(parsedMove)) {
            return Board.MoveResult.INVALID;
        }

        boolean isEnPassantCapture = false;
        Piece capturedPawnByEnPassant = null;
        int capturedPawnByEnPassantRow = -1;
        int capturedPawnByEnPassantCol = -1;

        if (pieceToMove.getType() == Piece.PieceType.PAWN && Math.abs(startCol - endCol) == 1 && squares[endRow][endCol] == null) {
            if (enPassantTargetSquare != null && endRow == enPassantTargetSquare[0] && endCol == enPassantTargetSquare[1]) {
                isEnPassantCapture = true;
                capturedPawnByEnPassantRow = (pieceToMove.getColor() == Piece.PieceColor.WHITE) ? endRow + 1 : endRow - 1;
                capturedPawnByEnPassantCol = endCol;
                capturedPawnByEnPassant = squares[capturedPawnByEnPassantRow][capturedPawnByEnPassantCol];
            }
        }

        if (isEnPassantCapture) {
            if (capturedPawnByEnPassant != null && capturedPawnByEnPassant.getType() == Piece.PieceType.PAWN && capturedPawnByEnPassant.getColor() != pieceToMove.getColor()) {
                squares[capturedPawnByEnPassantRow][capturedPawnByEnPassantCol] = null;
                decrementPieceCount(capturedPawnByEnPassant.getType(), capturedPawnByEnPassant.getColor());
                System.out.println("En Passant capture!");
            } else {
                System.err.println("Error: En Passant confirmed but no valid pawn to capture at " + (char)('a'+capturedPawnByEnPassantCol) + (char)('1'+(7-capturedPawnByEnPassantRow)));
                return Board.MoveResult.INVALID;
            }
        } else if (pieceAtEnd != null) {
            decrementPieceCount(pieceAtEnd.getType(), pieceAtEnd.getColor());
        }

        squares[endRow][endCol] = pieceToMove;
        squares[startRow][startCol] = null;

        if (parsedMove.isKingsideCastle || parsedMove.isQueensideCastle) {
            int rookStartCol = parsedMove.isKingsideCastle ? 7 : 0;
            int rookEndCol = parsedMove.isKingsideCastle ? 5 : 3;
            squares[endRow][rookEndCol] = squares[endRow][rookStartCol];
            squares[endRow][rookStartCol] = null;
            System.out.println("Castling performed!");
        }

        if (pieceToMove.getType() == Piece.PieceType.KING) {
            if (pieceToMove.getColor() == Piece.PieceColor.WHITE) whiteKingMoved = true;
            else blackKingMoved = true;
        } else if (pieceToMove.getType() == Piece.PieceType.ROOK) {
            if (pieceToMove.getColor() == Piece.PieceColor.WHITE) {
                if (startCol == 0 && startRow == 7) whiteRookAMoved = true;
                if (startCol == 7 && startRow == 7) whiteRookHMoved = true;
            } else {
                if (startCol == 0 && startRow == 0) blackRookAMoved = true;
                if (startCol == 7 && startRow == 0) blackRookHMoved = true;
            }
        }

        boolean isPromotionMove = (pieceToMove.getType() == Piece.PieceType.PAWN &&
                ((pieceToMove.getColor() == Piece.PieceColor.WHITE && endRow == 0) ||
                        (pieceToMove.getColor() == Piece.PieceColor.BLACK && endRow == 7)));

        if (isPromotionMove) {
            if (parsedMove.promotionType != null) {
                finalizePromotion(endRow, endCol, parsedMove.promotionType);
                return Board.MoveResult.VALID;
            } else {
                System.out.println(pieceToMove.getColor() + " Pawn reached promotion square " + (char)('a'+endCol) + (char)('1'+(7-endRow)) + "!");
                return Board.MoveResult.PROMOTION_PENDING;
            }
        } else {
            switchTurn();
            System.out.println("Move successful! Now it's " + currentPlayerTurn + "'s turn.");
            return Board.MoveResult.VALID;
        }
    }

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

    private boolean isValidPieceMove(ParsedMove parsedMove) {
        int startRow = parsedMove.startRow;
        int startCol = parsedMove.startCol;
        int endRow = parsedMove.endRow;
        int endCol = parsedMove.endCol;
        Piece piece = squares[startRow][startCol];
        if (piece == null) return false;

        int rowDiff = Math.abs(endRow - startRow);
        int colDiff = Math.abs(endCol - startCol);
        int rowDir = Integer.compare(endRow, startRow);
        int colDir = Integer.compare(endCol, startCol);

        switch (piece.getType()) {
            case PAWN:
                if (colDiff == 0) {
                    if (piece.getColor() == Piece.PieceColor.WHITE && rowDir == -1) {
                        if (rowDiff == 1) return true;
                        if (rowDiff == 2 && startRow == 6 && squares[startRow-1][startCol] == null) return true;
                    } else if (piece.getColor() == Piece.PieceColor.BLACK && rowDir == 1) {
                        if (rowDiff == 1) return true;
                        if (rowDiff == 2 && startRow == 1 && squares[startRow+1][startCol] == null) return true;
                    }
                }
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
                if (rowDiff <= 1 && colDiff <= 1 && (rowDiff > 0 || colDiff > 0)) {
                    return true;
                }
                if (rowDiff == 0 && colDiff == 2) {
                    return isValidCastlingAttempt(startRow, startCol, endRow, endCol);
                }
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
                        squares[kingRow][kingCol] = null;
                        boolean canAttackKing = isValidPieceMove(new ParsedMove(r, c, kingRow, kingCol));
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

    private boolean isValidCastlingAttempt(int kingStartRow, int kingStartCol, int kingEndRow, int kingEndCol) {
        if (kingStartCol != 4 || (kingStartRow != 0 && kingStartRow != 7)) {
            System.out.println("Invalid Castling: King not in original e-file or home rank.");
            return false;
        }
        if (Math.abs(kingEndCol - kingStartCol) != 2 || kingStartRow != kingEndRow) return false;

        if (currentPlayerTurn == Piece.PieceColor.WHITE) {
            if (whiteKingMoved) { System.out.println("Invalid Castling: White King has moved."); return false; }
        } else {
            if (blackKingMoved) { System.out.println("Invalid Castling: Black King has moved."); return false; }
        }

        int rookStartCol;
        boolean isKingside = (kingEndCol == 6);

        if (isKingside) {
            rookStartCol = 7;
        } else {
            rookStartCol = 0;
        }
        int rookRow = kingStartRow;

        Piece rook = squares[rookRow][rookStartCol];
        if (rook == null || rook.getType() != Piece.PieceType.ROOK || rook.getColor() != currentPlayerTurn) {
            System.out.println("Invalid Castling: No " + currentPlayerTurn + " Rook at original " + (char)('a'+rookStartCol) + (char)('1'+(7-rookRow)) + ".");
            return false;
        }
        if (currentPlayerTurn == Piece.PieceColor.WHITE) {
            if (isKingside && whiteRookHMoved) { System.out.println("Invalid Castling: White Kingside Rook has moved."); return false; }
            if (!isKingside && whiteRookAMoved) { System.out.println("Invalid Castling: White Queenside Rook has moved."); return false; }
        } else {
            if (isKingside && blackRookHMoved) { System.out.println("Invalid Castling: Black Kingside Rook has moved."); return false; }
            if (!isKingside && blackRookAMoved) { System.out.println("Invalid Castling: Black Queenside Rook has moved."); return false; }
        }

        if (isKingInCheck(currentPlayerTurn)) {
            System.out.println("Invalid Castling: King is currently in check.");
            return false;
        }

        if (!isPathClear(kingStartRow, kingStartCol, rookRow, rookStartCol)) {
            System.out.println("Invalid Castling: Path between King and Rook is blocked.");
            return false;
        }

        int intermediateKingCol = kingStartCol + Integer.compare(kingEndCol, kingStartCol);

        Piece originalKingPiece = squares[kingStartRow][kingStartCol];
        Piece originalRookPiece = squares[rookRow][rookStartCol];
        Piece pieceAtIntermediateKingSquare = squares[kingStartRow][intermediateKingCol];
        Piece pieceAtFinalKingSquare = squares[kingEndRow][kingEndCol];

        squares[kingStartRow][kingStartCol] = null;
        squares[kingStartRow][intermediateKingCol] = originalKingPiece;

        if (isKingInCheck(currentPlayerTurn)) {
            System.out.println("Invalid Castling: King passes through an attacked square (" + (char)('a'+intermediateKingCol) + (char)('1'+(7-kingStartRow)) + ").");
            squares[kingStartRow][kingStartCol] = originalKingPiece;
            squares[kingStartRow][intermediateKingCol] = pieceAtIntermediateKingSquare;
            return false;
        }

        squares[kingStartRow][intermediateKingCol] = pieceAtIntermediateKingSquare;
        squares[kingEndRow][kingEndCol] = originalKingPiece;

        if (isKingInCheck(currentPlayerTurn)) {
            System.out.println("Invalid Castling: King lands in an attacked square (" + (char)('a'+kingEndCol) + (char)('1'+(7-kingEndRow)) + ").");
            squares[kingStartRow][kingStartCol] = originalKingPiece;
            squares[kingEndRow][kingEndCol] = pieceAtFinalKingSquare;
            return false;
        }

        squares[kingStartRow][kingStartCol] = originalKingPiece;
        squares[kingEndRow][kingEndCol] = pieceAtFinalKingSquare;
        squares[kingStartRow][intermediateKingCol] = pieceAtIntermediateKingSquare;
        squares[rookRow][rookStartCol] = originalRookPiece;

        return true;
    }


    public String findRandomLegalMove() {
        List<String> legalMoves = new ArrayList<>();

        for (int startRow = 0; startRow < 8; startRow++) {
            for (int startCol = 0; startCol < 8; startCol++) {
                Piece piece = squares[startRow][startCol];

                if (piece != null && piece.getColor() == currentPlayerTurn) {
                    for (int endRow = 0; endRow < 8; endRow++) {
                        for (int endCol = 0; endCol < 8; endCol++) {
                            ParsedMove tempMove = new ParsedMove(startRow, startCol, endRow, endCol);
                            if (piece.getType() == Piece.PieceType.KING && startRow == endRow && Math.abs(startCol - endCol) == 2) {
                                tempMove.isKingsideCastle = (endCol == 6);
                                tempMove.isQueensideCastle = (endCol == 2);
                            }

                            if (isValidMoveAttempt(tempMove)) {
                                char startFile = (char) ('a' + startCol);
                                char startRank = (char) ('1' + (7 - startRow));
                                char endFile = (char) ('a' + endCol);
                                char endRank = (char) ('1' + (7 - endRow));

                                if (tempMove.isKingsideCastle) { legalMoves.add("O-O"); }
                                else if (tempMove.isQueensideCastle) { legalMoves.add("O-O-O"); }
                                else {
                                    legalMoves.add("" + startFile + startRank + endFile + endRank);
                                }
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

    private boolean isValidMoveAttempt(ParsedMove parsedMove) {
        int startRow = parsedMove.startRow;
        int startCol = parsedMove.startCol;
        int endRow = parsedMove.endRow;
        int endCol = parsedMove.endCol;

        Piece pieceToMove = squares[startRow][startCol];
        Piece targetPiece = squares[endRow][endCol];

        if (startRow < 0 || startRow >= 8 || startCol < 0 || startCol >= 8 ||
                endRow < 0 || endRow >= 8 || endCol < 0 || endCol >= 8) {
            return false;
        }

        if (pieceToMove == null || pieceToMove.getColor() != currentPlayerTurn) {
            return false;
        }

        boolean isTargetEmpty = (targetPiece == null);
        boolean isTargetOccupiedByOpponent = (targetPiece != null && targetPiece.getColor() != currentPlayerTurn);
        boolean isTargetOccupiedByOwn = (targetPiece != null && targetPiece.getColor() == currentPlayerTurn);

        if (isTargetOccupiedByOwn && (startRow != endRow || startCol != endCol)) {
            return false;
        }

        if (!isValidPieceMove(parsedMove)) {
            return false;
        }

        if (!parsedMove.isKingsideCastle && !parsedMove.isQueensideCastle) {
            if (pieceToMove.getType() == Piece.PieceType.PAWN) {
                if (startCol == endCol) {
                    if (!isTargetEmpty) return false;
                } else {
                    boolean isEnPassantCandidate = (enPassantTargetSquare != null &&
                            endRow == enPassantTargetSquare[0] &&
                            endCol == enPassantTargetSquare[1]);

                    if (isEnPassantCandidate) {
                        if (pieceToMove.getColor() == Piece.PieceColor.WHITE && startRow != 3) return false;
                        if (pieceToMove.getColor() == Piece.PieceColor.BLACK && startRow != 4) return false;
                    } else {
                        if (!isTargetOccupiedByOpponent) return false;
                    }
                }
            }
        }

        Piece originalStartPiece = squares[startRow][startCol];
        Piece originalEndPiece = squares[endRow][endCol];
        Piece originalEnPassantCapturedPawn = null;
        Piece originalRookPiece = null;
        int rookOriginalCol = -1;
        int rookSimulatedEndCol = -1;

        squares[endRow][endCol] = originalStartPiece;
        squares[startRow][startCol] = null;

        if (pieceToMove.getType() == Piece.PieceType.PAWN && Math.abs(startCol - endCol) == 1 && isTargetEmpty && enPassantTargetSquare != null && endRow == enPassantTargetSquare[0] && endCol == enPassantTargetSquare[1]) {
            int capturedPawnRow = (originalStartPiece.getColor() == Piece.PieceColor.WHITE) ? endRow + 1 : endRow - 1;
            int capturedPawnCol = endCol;
            originalEnPassantCapturedPawn = squares[capturedPawnRow][capturedPawnCol];
            squares[capturedPawnRow][capturedPawnCol] = null;
        }

        if (parsedMove.isKingsideCastle || parsedMove.isQueensideCastle) {
            rookOriginalCol = parsedMove.isKingsideCastle ? 7 : 0;
            rookSimulatedEndCol = parsedMove.isKingsideCastle ? 5 : 3;
            originalRookPiece = squares[startRow][rookOriginalCol];
            squares[endRow][rookSimulatedEndCol] = originalRookPiece;
            squares[startRow][rookOriginalCol] = null;
        }

        boolean isKingInCheckAfterMove = isKingInCheck(currentPlayerTurn);

        squares[startRow][startCol] = originalStartPiece;
        squares[endRow][endCol] = originalEndPiece;
        if (originalEnPassantCapturedPawn != null) {
            int capturedPawnRow = (originalStartPiece.getColor() == Piece.PieceColor.WHITE) ? endRow + 1 : endRow - 1;
            int capturedPawnCol = endCol;
            squares[capturedPawnRow][capturedPawnCol] = originalEnPassantCapturedPawn;
        }
        if (parsedMove.isKingsideCastle || parsedMove.isQueensideCastle) {
            squares[startRow][rookOriginalCol] = originalRookPiece;
            squares[endRow][rookSimulatedEndCol] = null;
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

    /**
     * Resets all castling flags to false. Useful for test setup.
     */
    public void resetCastlingFlags() {
        whiteKingMoved = false;
        blackKingMoved = false;
        whiteRookAMoved = false;
        whiteRookHMoved = false;
        blackRookAMoved = false;
        blackRookHMoved = false;
    }
}