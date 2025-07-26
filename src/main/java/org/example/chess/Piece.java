package org.example.chess;

public class Piece {

    // Nested public static enums for PieceType and PieceColor
    public enum PieceType {
        KING, QUEEN, ROOK, BISHOP, KNIGHT, PAWN
    }

    public enum PieceColor {
        WHITE, BLACK
    }

    private PieceType type;
    private PieceColor color;

    public Piece(PieceType type, PieceColor color) {
        this.type = type;
        this.color = color;
    }

    // Getters
    public PieceType getType() {
        return type;
    }

    public PieceColor getColor() {
        return color;
    }

    // Method to get the ASCII character for display
    public String getAsciiChar() {
        switch (type) {
            case KING:   return color == PieceColor.WHITE ? "K" : "k";
            case QUEEN:  return color == PieceColor.WHITE ? "Q" : "q";
            case ROOK:   return color == PieceColor.WHITE ? "R" : "r";
            case BISHOP: return color == PieceColor.WHITE ? "B" : "b"; // Corrected BISHOP
            case KNIGHT: return color == PieceColor.WHITE ? "N" : "n";
            case PAWN:   return color == PieceColor.WHITE ? "P" : "p";
            default:     return " "; // Should not happen for valid piece types
        }
    }
}