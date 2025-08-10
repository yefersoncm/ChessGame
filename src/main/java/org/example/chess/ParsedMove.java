package org.example.chess;

/**
 * A class to encapsulate all details parsed from an algebraic move notation string.
 * This includes start/end coordinates, and special flags like promotion type or castling intent.
 */
public class ParsedMove {
    public int startRow;
    public int startCol;
    public int endRow;
    public int endCol;
    public Piece.PieceType promotionType = null; // Will be non-null if promotion piece is specified (e.g., "e8Q")
    public boolean isKingsideCastle = false;     // True if "O-O" was entered
    public boolean isQueensideCastle = false;    // True if "O-O-O" was entered

    // Constructor for standard moves (e.g., e2e4, Nf3)
    public ParsedMove(int startRow, int startCol, int endRow, int endCol) {
        this.startRow = startRow;
        this.startCol = startCol;
        this.endRow = endRow;
        this.endCol = endCol;
    }

    // Constructor for castling moves (only King's start/end is passed in for base)
    // The castling flags specify the exact type
    public ParsedMove(int startRow, int startCol, int endRow, int endCol, boolean isKingsideCastle, boolean isQueensideCastle) {
        this(startRow, startCol, endRow, endCol);
        this.isKingsideCastle = isKingsideCastle;
        this.isQueensideCastle = isQueensideCastle;
    }
}