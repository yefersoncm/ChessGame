package org.example.chess;

import org.junit.jupiter.api.BeforeEach; // Setup before each test
import org.junit.jupiter.api.Test;      // Marks a test method
import static org.junit.jupiter.api.Assertions.*; // For assertions like assertEquals, assertTrue

public class BoardTest {

    private Board board; // Declare the board instance for tests

    @BeforeEach // This method runs before EACH test method
    void setUp() {
        // Arrange: Create a fresh board for each test to ensure isolation
        board = new Board();
        // If a test needs a blank board, you'd call board.clearBoard() here or within the specific test.
    }

    // Inside your BoardTest.java class, typically after the setUp() method.

    /**
     * Helper to set up a custom board for specific test scenarios.
     * Clears the board and places pieces according to notation.
     * Does NOT set turn or en passant target.
     * @param placements Varargs of piece placement strings (e.g., "Ke1", "pf7", "Rh8").
     */
    private void setCustomBoard(String... placements) {
        board.clearBoard(); // Clear existing setup and counts
        board.resetCastlingFlags(); // Also reset castling flags as board is cleared
        for (String p : placements) {
            board.placePiece(p); // Uses the placePiece method for setup
        }
    }

    // Example Test Method (for standard move)
    @Test
    void testInitialPawnMove() {
        // Arrange (setup specific for this test, overriding default setup if needed)
        // Board is already setup to standard initial position by @BeforeEach

        // Act (perform the action to be tested)
        // White Pawn from e2 (row 6, col 4) to e4 (row 4, col 4)
        Board.MoveResult result = board.move("e2e4");

        // Assert (verify the outcome)
        assertEquals(Board.MoveResult.VALID, result, "Pawn e2e4 should be a valid move.");
        assertNull(board.getPiece(6, 4), "e2 square should be empty after move.");
        assertNotNull(board.getPiece(4, 4), "e4 square should have a piece.");
        assertEquals(Piece.PieceType.PAWN, board.getPiece(4, 4).getType(), "Piece on e4 should be a pawn.");
        assertEquals(Piece.PieceColor.WHITE, board.getPiece(4, 4).getColor(), "Pawn on e4 should be white.");
        assertEquals(Piece.PieceColor.BLACK, board.getCurrentPlayerTurn(), "Turn should switch to Black.");
    }
    @Test
    void testStalemateScenario() {
        // Arrange: White King at a1, White Rook at c1, Black King at a3. Black to move. (Classic stalemate)
        setCustomBoard("Ka3", "Rb3", "ka1");
        board.setPlayerTurn(Piece.PieceColor.BLACK); // Black is NOT in check, but has no moves
        // Act: Try to find a legal move for Black
        String move = board.findRandomLegalMove();
        // Assert: No legal moves should be found, indicating stalemate (handled in Main based on isKingInCheck)
        assertNull(move, "Should find no legal moves for Black (stalemate).");
        assertFalse(board.isKingInCheck(Piece.PieceColor.BLACK), "Black King should NOT be in check.");
    }

    /**
     * Helper to set up a custom board for specific test scenarios.
     * Clears the board and places pieces according to notation.
     * Does NOT set turn or en passant target by default (must be done explicitly in test).
     * Also resets castling flags.
     * @param placements Varargs of piece placement strings (e.g., "Ke1", "pf7", "Rh8").
     */
    /* private void setCustomBoard(String... placements) {
        board.clearBoard(); // Clear existing setup and counts
        board.resetCastlingFlags(); // Reset castling flags as board is cleared
        for (String p : placements) {
            board.placePiece(p); // Uses the placePiece method for setup
        }
    }
*/
    // --- CASTLING TEST CASES ---

    @Test
    void testWhiteKingsideCastlingValid() {
        // Arrange: Clear board, place Kings and Rooks, ensure path is clear
        setCustomBoard("Ke1", "Rh1", "ke8"); // White K at e1, R at h1; Black K at e8
        board.setPlayerTurn(Piece.PieceColor.WHITE);

        // Act: Attempt to castle Kingside
        Board.MoveResult result = board.move("O-O"); // Kingside castling notation

        // Assert: Move should be valid, pieces should be in new positions, flags updated
        assertEquals(Board.MoveResult.VALID, result, "White Kingside castling should be valid.");
        assertNull(board.getPiece(7, 4), "e1 should be empty."); // Old King square
        assertNull(board.getPiece(7, 7), "h1 should be empty."); // Old Rook square
        assertNotNull(board.getPiece(7, 6), "g1 should have White King."); // New King square
        assertNotNull(board.getPiece(7, 5), "f1 should have White Rook."); // New Rook square

        assertEquals(Piece.PieceType.KING, board.getPiece(7, 6).getType());
        assertEquals(Piece.PieceColor.WHITE, board.getPiece(7, 6).getColor());
        assertEquals(Piece.PieceType.ROOK, board.getPiece(7, 5).getType());
        assertEquals(Piece.PieceColor.WHITE, board.getPiece(7, 5).getColor());

        // Verify castling flags are set
        assertTrue(board.whiteKingMoved, "White King moved flag should be true after castling.");
        assertTrue(board.whiteRookHMoved, "White Kingside Rook moved flag should be true after castling.");
        assertEquals(Piece.PieceColor.BLACK, board.getCurrentPlayerTurn(), "Turn should switch to Black.");
    }

    @Test
    void testWhiteQueensideCastlingValid() {
        // Arrange: Clear board, place Kings and Rooks, ensure path is clear
        setCustomBoard("Ke1", "Ra1", "ke8"); // White K at e1, R at a1; Black K at e8
        board.setPlayerTurn(Piece.PieceColor.WHITE);

        // Act: Attempt to castle Queenside
        Board.MoveResult result = board.move("O-O-O"); // Queenside castling notation

        // Assert
        assertEquals(Board.MoveResult.VALID, result, "White Queenside castling should be valid.");
        assertNull(board.getPiece(7, 4), "e1 should be empty.");
        assertNull(board.getPiece(7, 0), "a1 should be empty.");
        assertNotNull(board.getPiece(7, 2), "c1 should have White King.");
        assertNotNull(board.getPiece(7, 3), "d1 should have White Rook.");

        assertEquals(Piece.PieceType.KING, board.getPiece(7, 2).getType());
        assertEquals(Piece.PieceColor.WHITE, board.getPiece(7, 2).getColor());
        assertEquals(Piece.PieceType.ROOK, board.getPiece(7, 3).getType());
        assertEquals(Piece.PieceColor.WHITE, board.getPiece(7, 3).getColor());

        assertTrue(board.whiteKingMoved, "White King moved flag should be true after castling.");
        assertTrue(board.whiteRookAMoved, "White Queenside Rook moved flag should be true after castling.");
        assertEquals(Piece.PieceColor.BLACK, board.getCurrentPlayerTurn(), "Turn should switch to Black.");
    }

    @Test
    void testBlackKingsideCastlingValid() {
        // Arrange
        setCustomBoard("Ke1", "ke8", "rh8"); // White K at e1; Black K at e8, R at h8
        board.setPlayerTurn(Piece.PieceColor.BLACK);

        // Act
        Board.MoveResult result = board.move("O-O");

        // Assert
        assertEquals(Board.MoveResult.VALID, result, "Black Kingside castling should be valid.");
        assertNull(board.getPiece(0, 4), "e8 should be empty.");
        assertNull(board.getPiece(0, 7), "h8 should be empty.");
        assertNotNull(board.getPiece(0, 6), "g8 should have Black King.");
        assertNotNull(board.getPiece(0, 5), "f8 should have Black Rook.");

        assertEquals(Piece.PieceType.KING, board.getPiece(0, 6).getType());
        assertEquals(Piece.PieceColor.BLACK, board.getPiece(0, 6).getColor());
        assertEquals(Piece.PieceType.ROOK, board.getPiece(0, 5).getType());
        assertEquals(Piece.PieceColor.BLACK, board.getPiece(0, 5).getColor());

        assertTrue(board.blackKingMoved, "Black King moved flag should be true after castling.");
        assertTrue(board.blackRookHMoved, "Black Kingside Rook moved flag should be true after castling.");
        assertEquals(Piece.PieceColor.WHITE, board.getCurrentPlayerTurn(), "Turn should switch to White.");
    }

    @Test
    void testCastlingInvalidKingHasMoved() {
        // Arrange: Set up board, then move King to set flag, then try to castle
        setCustomBoard("Ke1", "Rh1", "ke8");
        board.setPlayerTurn(Piece.PieceColor.WHITE);
        board.move("e1f1"); // Move King, setting flag
        board.move("ke8e7"); // Black's dummy move

        // Act: Try to castle Kingside (should be invalid)
        Board.MoveResult result = board.move("O-O");

        // Assert
        assertEquals(Board.MoveResult.INVALID, result, "Castling should be invalid if King has moved.");
        // Verify board state is unchanged (or previous state if movePiece did partial changes)
        assertNotNull(board.getPiece(7, 5), "f1 should still have King (or whatever was there after e1f1).");
        assertNotNull(board.getPiece(7, 7), "h1 should still have Rook.");
        // Verify turn is NOT switched
        assertEquals(Piece.PieceColor.WHITE, board.getCurrentPlayerTurn(), "Turn should NOT switch on invalid move.");
    }

    @Test
    void testCastlingInvalidRookHasMoved() {
        // Arrange: Move Rook to set flag
        setCustomBoard("Ke1", "Rh1", "ke8");
        board.setPlayerTurn(Piece.PieceColor.WHITE);
        board.move("h1g1"); // Move Rook, setting flag
        board.move("ke8e7"); // Black's dummy move

        // Act
        Board.MoveResult result = board.move("O-O");

        // Assert
        assertEquals(Board.MoveResult.INVALID, result, "Castling should be invalid if Rook has moved.");
        assertNotNull(board.getPiece(7, 4), "e1 should still have King.");
        assertNotNull(board.getPiece(7, 6), "g1 should still have Rook (or whatever was there after h1g1).");
        assertEquals(Piece.PieceColor.WHITE, board.getCurrentPlayerTurn());
    }

    @Test
    void testCastlingInvalidPathBlocked() {
        // Arrange: Place a piece between King and Rook
        setCustomBoard("Ke1", "Rh1", "Bf1", "ke8"); // White K, R, and Bishop at f1 blocking
        board.setPlayerTurn(Piece.PieceColor.WHITE);

        // Act
        Board.MoveResult result = board.move("O-O");

        // Assert
        assertEquals(Board.MoveResult.INVALID, result, "Castling should be invalid if path is blocked.");
        assertNotNull(board.getPiece(7, 4), "e1 should still have King.");
        assertNotNull(board.getPiece(7, 5), "f1 should still have Bishop.");
        assertNotNull(board.getPiece(7, 7), "h1 should still have Rook.");
        assertEquals(Piece.PieceColor.WHITE, board.getCurrentPlayerTurn());
    }

    @Test
    void testCastlingInvalidKingInCheck() {
        // Arrange: Place opponent piece to check King
        setCustomBoard("Ke1", "Rh1", "ra8", "ke8"); // White K, R; Black Rook on a8 checking e1
        board.setPlayerTurn(Piece.PieceColor.WHITE);
        assertTrue(board.isKingInCheck(Piece.PieceColor.WHITE), "White King should be in check initially.");

        // Act
        Board.MoveResult result = board.move("O-O");

        // Assert
        assertEquals(Board.MoveResult.INVALID, result, "Castling should be invalid if King is in check.");
        assertNotNull(board.getPiece(7, 4), "e1 should still have King.");
        assertEquals(Piece.PieceColor.WHITE, board.getCurrentPlayerTurn());
    }

    @Test
    void testCastlingInvalidKingPassesThroughCheck() {
        // Arrange: Place opponent piece to attack f1 (the intermediate square for Kingside)
        setCustomBoard("Ke1", "Rh1", "ra6", "ke8"); // White K, R; Black Rook on a6 attacks f1
        board.setPlayerTurn(Piece.PieceColor.WHITE);
        assertFalse(board.isKingInCheck(Piece.PieceColor.WHITE), "White King should NOT be in check initially.");

        // Act
        Board.MoveResult result = board.move("O-O");

        // Assert
        assertEquals(Board.MoveResult.INVALID, result, "Castling should be invalid if King passes through attacked square.");
        assertNotNull(board.getPiece(7, 4), "e1 should still have King.");
        assertEquals(Piece.PieceColor.WHITE, board.getCurrentPlayerTurn());
    }

    @Test
    void testCastlingInvalidKingLandsInCheck() {
        // Arrange: Place opponent piece to attack g1 (the landing square for Kingside)
        setCustomBoard("Ke1", "Rh1", "rb1", "ke8"); // White K, R; Black Rook on b1 attacks g1
        board.setPlayerTurn(Piece.PieceColor.WHITE);
        assertFalse(board.isKingInCheck(Piece.PieceColor.WHITE), "White King should NOT be in check initially.");

        // Act
        Board.MoveResult result = board.move("O-O");

        // Assert
        assertEquals(Board.MoveResult.INVALID, result, "Castling should be invalid if King lands in attacked square.");
        assertNotNull(board.getPiece(7, 4), "e1 should still have King.");
        assertEquals(Piece.PieceColor.WHITE, board.getCurrentPlayerTurn());
    }
    // Other @Test methods for different scenarios...
}