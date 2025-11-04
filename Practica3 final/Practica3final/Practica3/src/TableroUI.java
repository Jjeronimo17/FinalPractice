import javax.swing.*;
import java.awt.*;

public class TableroUI extends JFrame {

    private final JLabel status = new JLabel("listo",  SwingConstants.LEFT);

    private final PanelTablero panel = new PanelTablero();
    private final JButton botonLimpiar = new JButton("Limpiar Tablero");
    private final JButton botonPosicion = new JButton("Posicion inicial");
    private final JButton botonVoltear = new JButton("Voltear");


    private final JTextField campoFen = new JTextField(35);
    private final JButton botonCargarFen = new JButton("Cargar FEN");

    public TableroUI() {
        super("Tablero de Ajedrez");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));
        getRootPane().setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        top.add(botonLimpiar);
        top.add(botonPosicion);
        top.add(botonVoltear);

 
        top.add(new JLabel("FEN:"));
        top.add(campoFen);
        top.add(botonCargarFen);

        add(top, BorderLayout.NORTH);

        add(panel, BorderLayout.CENTER);

        status.setBorder(BorderFactory.createEmptyBorder(4, 2, 2, 2));
        add(status, BorderLayout.SOUTH);

        botonLimpiar.addActionListener(e -> {
            panel.limpiarTablero();
            status.setText("Tablero Limpio");
        });

        botonPosicion.addActionListener(e -> {
            panel.posicionInicial();
            status.setText("Posicion Inicial Cargada");
        });

        botonVoltear.addActionListener(e -> {
            panel.voltearTablero();
            status.setText(panel.estaVolteado() ? "Orientacion: Negras Abajo" : "Orientacion Blancas Arriba");
        });

        // ---- NUEVO: acción para parsear y cargar la FEN ----
        botonCargarFen.addActionListener(e -> {
            String fen = campoFen.getText().trim();
            try {
                FenResult r = FenParser.parse(fen);
                setBoardFromFen(r.getBoard()); // usa el método nuevo de abajo
                status.setText(
                        (r.getSideToMove()=='w'?"Juegan Blancas":"Juegan Negras") +
                        " | EP: " + r.getEnPassant() +
                        " | ½m: " + r.getHalfmove() +
                        " | m: " + r.getFullmove()
                );
            } catch (FenException ex) {
                JOptionPane.showMessageDialog(this,
                        "FEN inválida: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                status.setText("FEN inválida");
            }
        });

        panel.posicionInicial();

        pack();
        setMinimumSize(new Dimension(760, 680)); // un poco más ancho para el campo FEN
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(TableroUI::new);
    }

    // ---- NUEVO: método puente para cargar matriz 8x8 desde el parser ----
    public void setBoardFromFen(char[][] board) {
        panel.ponerTablero(board);
    }

    static class PanelTablero extends JPanel {
        private static final Color LIGHT = new Color(240, 217, 181);
        private static final Color DARK = new Color(181,  136, 99);
        private static final Color PiezaBlanca = new Color(245, 245, 245);
        private static final Color PiezaNegra = new Color(30,  30,  30);
        private final char[][] finalTablero = new char[8][8];
        private final JLabel[][] celdas = new  JLabel[8][8];
        private boolean orientacion = false;   // false = Blancas abajo, true = Negras abajo
        private final Font fuentePiezas = fuente(40f);

        PanelTablero() {
            super(new GridLayout(8, 8, 0, 0));
            setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));

            modeloVacio();

            for(int i = 0; i < 8; i++) {
                for(int j = 0; j < 8; j++) {
                    JLabel celda = new JLabel("", SwingConstants.CENTER);
                    celda.setOpaque(true);
                    boolean contraste = ((i + j)% 2 == 0);
                    celda.setBackground(contraste ? LIGHT : DARK);
                    celda.setFont(fuentePiezas);
                    celda.setPreferredSize(new Dimension(64, 64));
                    celdas[i][j] = celda;
                    add(celda);
                }
            }
            pintarTablero();
        }
        void limpiarTablero() {
            modeloVacio();
            pintarTablero();
        }
        void posicionInicial() {
            modeloVacio();
            finalTablero[0] = new char[]{'r','n','b','q','k','b','n','r'}; // Negras arriba (fila 0 = rango 8)
            for(int a = 0; a < 8; a++) finalTablero[1][a] = 'p'; // peones negros
            for(int a = 0; a < 8; a++) finalTablero[6][a] = 'P'; // peones blancos
            finalTablero[7] = new char[]{'R','N','B','Q','K','B','N','R'}; // Blancas abajo (fila 7 = rango 1)
            pintarTablero();
        }
        void ponerTablero(char[][] tablero) {
            if(tablero == null || tablero.length != 8 || tablero[0].length != 8) {
                throw  new IllegalArgumentException("El tablero de ajedrez debe ser 8x8");
            }
            for(int r = 0; r < 8; r++) {
                for(int c = 0; c < 8; c++) {
                    finalTablero[r][c] = tablero[r][c];
                }
            }
            pintarTablero();
        }
        void voltearTablero() {
            orientacion = !orientacion;
            pintarTablero();
        }
        boolean estaVolteado() {
            return orientacion;
        }
        private void pintarTablero() {
            for(int i = 0; i < 8; i++) {
                for(int j = 0; j < 8; j++) {
                    int[] tableroReal = verModelo(i,j);
                    char p = finalTablero[tableroReal[0]][tableroReal[1]];
                    celdas[i][j].setText(toUnicode(p));
                    if(Character.isUpperCase(p)) {
                        celdas[i][j].setForeground(PiezaBlanca);
                    }else if (Character.isLowerCase(p)) {
                        celdas[i][j].setForeground(PiezaNegra);
                    }else {
                        celdas[i][j].setForeground(PiezaBlanca);
                    }
                }
            }
            revalidate();
            repaint();
        }
        private void modeloVacio() {
            for(int i = 0; i < 8; i++) {
                for(int j = 0; j < 8; j++) {
                    finalTablero[i][j] = '.';
                }
            }
        }
        private int[] verModelo(int i, int j) {
            if(!orientacion) {
                return new int[]{i, j};
            } else {
                return new int[]{7 - i, 7 - j};
            }
        }
        private static String toUnicode(char p) {
            switch(p) {
                case 'K': return "♔";
                case 'Q': return "♕";
                case 'R': return "♖";
                case 'B': return "♗";
                case 'N': return "♘";
                case 'P': return "♙";
                case 'k': return "♚";
                case 'q': return "♛";
                case 'r': return "♜";
                case 'b': return "♝";
                case 'n': return "♞";
                case 'p': return "♟";
                default: return "";
            }
        }
        private static Font fuente(float size) {
            String[] fuentes = { "Segoe UI Symbol" };
            for(String nombre : fuentes) {
                try {
                    Font f = new Font(nombre, Font.PLAIN, Math.round(size));
                    if(f.canDisplay('♔') && f.canDisplay('♟')) return f;
                }catch(Exception ignored) {}
            }
            return new JLabel().getFont().deriveFont(size);
        }
    }
}
