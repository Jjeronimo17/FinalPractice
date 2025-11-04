import java.util.HashSet;
import java.util.Set;

public final class FenParser {

    private FenParser() {}

    /** Parsea y valida una FEN (6 campos). Lanza FenException con mensaje claro si hay error. */
    public static FenResult parse(String fen) {
        if (fen == null || fen.isEmpty())
            throw new FenException("Cadena vacía.");

        String[] parts = fen.trim().split("\\s+");
        if (parts.length != 6)
            throw new FenException("La FEN debe tener 6 campos separados por espacio.");

        char[][] board = parseBoard(parts[0]);         // campo 1: piezas
        char stm = parseSideToMove(parts[1]);          // campo 2: turno
        boolean[] castles = parseCastling(parts[2]);   // campo 3: enroques (KQkq)
        String ep = parseEnPassant(parts[3]);          // campo 4: en passant
        int half = parseNonNegativeInt(parts[4], "halfmove clock");
        int full = parsePositiveInt(parts[5], "fullmove counter");

        return new FenResult(board, stm, castles[0], castles[1], castles[2], castles[3],
                ep, half, full);
    }

    // ---------- Campo 1: pieza/colocación ----------
    private static char[][] parseBoard(String field) {
        String[] ranks = field.split("/");
        if (ranks.length != 8)
            throw new FenException("Debe haber 8 filas separadas por '/'.");
        char[][] b = new char[8][8]; // [0]=rank8 ... [7]=rank1

        for (int r = 0; r < 8; r++) {
            String row = ranks[r];
            int col = 0;
            for (int i = 0; i < row.length(); i++) {
                char c = row.charAt(i);

                if (c >= '1' && c <= '8') {
                    int blanks = c - '0';
                    col += blanks;
                    if (col > 8)
                        throw new FenException("La fila " + (8 - r) + " excede 8 columnas.");
                    continue;
                }

                if (!isPieceChar(c))
                    throw new FenException("Pieza inválida '" + c + "' en fila " + (8 - r) + ".");

                if (col >= 8)
                    throw new FenException("La fila " + (8 - r) + " tiene más de 8 columnas.");
                b[r][col++] = c;
            }
            if (col != 8)
                throw new FenException("La fila " + (8 - r) + " no completa 8 columnas.");
        }
        return b;
    }

    private static boolean isPieceChar(char c) {
        switch (c) {
            case 'P','N','B','R','Q','K','p','n','b','r','q','k': return true;
            default: return false;
        }
    }

    // ---------- Campo 2: lado al movimiento ----------
    private static char parseSideToMove(String s) {
        if (s.length() == 1 && (s.charAt(0) == 'w' || s.charAt(0) == 'b')) return s.charAt(0);
        throw new FenException("Campo 'side to move' debe ser 'w' o 'b'.");
    }

    // ---------- Campo 3: enroque ----------
    // Devuelve boolean[]{K,Q,k,q}
    private static boolean[] parseCastling(String s) {
        boolean K=false,Q=false,k=false,q=false;
        if (s.equals("-")) return new boolean[]{false,false,false,false};
        Set<Character> seen = new HashSet<>();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if ("KQkq".indexOf(c) == -1)
                throw new FenException("Carácter de enroque inválido: '" + c + "'.");
            if (!seen.add(c))
                throw new FenException("Enroque repetido: '" + c + "'.");
            if (c=='K') K=true; else if (c=='Q') Q=true; else if (c=='k') k=true; else q=true;
        }
        return new boolean[]{K,Q,k,q};
    }

    // ---------- Campo 4: en passant ----------
    private static String parseEnPassant(String s) {
        if (s.equals("-")) return s;
        if (s.length()!=2) throw new FenException("En passant debe ser '-' o casilla como 'e3'/'c6'.");
        char file = s.charAt(0), rank = s.charAt(1);
        if (file<'a' || file>'h') throw new FenException("Archivo inválido en en passant: '"+file+"'.");
        if (!(rank=='3' || rank=='6')) throw new FenException("Rango inválido en en passant: '"+rank+"'.");
        return s;
    }

    // ---------- Campos 5 y 6: enteros ----------
    private static int parseNonNegativeInt(String s, String name) {
        try {
            int v = Integer.parseInt(s);
            if (v < 0) throw new FenException("El "+name+" no puede ser negativo.");
            return v;
        } catch (NumberFormatException e) {
            throw new FenException("El "+name+" debe ser un entero no negativo.");
        }
    }

    private static int parsePositiveInt(String s, String name) {
        try {
            int v = Integer.parseInt(s);
            if (v < 1) throw new FenException("El "+name+" debe ser ≥ 1.");
            return v;
        } catch (NumberFormatException e) {
            throw new FenException("El "+name+" debe ser un entero positivo.");
        }
    }
}
