public final class FenResult {
    // Matriz 8x8: fila 0 = rango 8 en ajedrez, fila 7 = rango 1
    private final char[][] board;
    private final char sideToMove;   // 'w' o 'b'
    private final boolean K, Q, k, q; // derechos de enroque
    private final String enPassant;   // "-" o casilla tipo "e3"
    private final int halfmove;       // reloj de 50 jugadas
    private final int fullmove;       // número de jugada (>=1)

    public FenResult(char[][] board, char sideToMove,
                     boolean K, boolean Q, boolean k, boolean q,
                     String enPassant, int halfmove, int fullmove) {
        this.board = board;
        this.sideToMove = sideToMove;
        this.K = K; this.Q = Q; this.k = k; this.q = q;
        this.enPassant = enPassant;
        this.halfmove = halfmove;
        this.fullmove = fullmove;
    }

    public char[][] getBoard() { return board; }
    public char getSideToMove() { return sideToMove; }
    public boolean isCastleK() { return K; }
    public boolean isCastleQ() { return Q; }
    public boolean isCastlek() { return k; }
    public boolean isCastlekq() { return q; }
    public String getEnPassant() { return enPassant; }
    public int getHalfmove() { return halfmove; }
    public int getFullmove() { return fullmove; }
}
