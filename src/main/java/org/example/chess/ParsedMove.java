package org.example.chess;

public class ParsedMove {
    public int startRow;
    public int startCol;
    public int endRow;
    public int endCol;
    public Piece.PieceType promotionType = null;
    public boolean isKingsideCastle = false;
    public boolean isQueensideCastle = false;

    public ParsedMove(int startRow, int startCol, int endRow, int endCol) {
        this.startRow = startRow;
        this.startCol = startCol;
        this.endRow = endRow;
        this.endCol = endCol;
    }

    public ParsedMove(int startRow, int startCol, int endRow, int endCol, boolean isKingsideCastle, boolean isQueensideCastle) {
        this(startRow, startCol, endRow, endCol);
        this.isKingsideCastle = isKingsideCastle;
        this.isQueensideCastle = isQueensideCastle;
    }
}