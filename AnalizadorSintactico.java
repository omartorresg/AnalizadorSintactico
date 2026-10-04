import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnalizadorSintactico extends JFrame {

    private JTextArea txtInput;
    private JTextArea txtOutput;

    public AnalizadorSintactico() {
        // Configuración de la ventana principal Swing
        setTitle("Analizador Sintáctico - MiniLenguaje");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Panel de entrada (Izquierda)
        JPanel panelLeft = new JPanel(new BorderLayout());
        panelLeft.setBorder(BorderFactory.createTitledBorder("Entrada de Código (MiniLang)"));
        txtInput = new JTextArea("x = 10;\ny = (x + 5) * 2;\ntotal = x + y;", 20, 30);
        txtInput.setFont(new Font("Monospaced", Font.PLAIN, 13));
        panelLeft.add(new JScrollPane(txtInput), BorderLayout.CENTER);

        // Panel de salida (Derecha)
        JPanel panelRight = new JPanel(new BorderLayout());
        panelRight.setBorder(BorderFactory.createTitledBorder("Resultado del Análisis Sintáctico"));
        txtOutput = new JTextArea(20, 30);
        txtOutput.setFont(new Font("Monospaced", Font.PLAIN, 13));
        txtOutput.setEditable(false);
        panelRight.add(new JScrollPane(txtOutput), BorderLayout.CENTER);

        // Botón central de Análisis
        JButton btnAnalizar = new JButton("Analizar ➔");
        btnAnalizar.setFont(new Font("Arial", Font.BOLD, 14));
        btnAnalizar.setBackground(new Color(76, 175, 80));
        btnAnalizar.setForeground(Color.WHITE);
        btnAnalizar.addActionListener(e -> ejecutarAnalisis());

        JPanel panelCenter = new JPanel(new GridBagLayout());
        panelCenter.add(btnAnalizar);

        // Agregar componentes al Frame
        add(panelLeft, BorderLayout.WEST);
        add(panelCenter, BorderLayout.CENTER);
        add(panelRight, BorderLayout.EAST);
    }

    private void ejecutarAnalisis() {
        String codigo = txtInput.getText().trim();
        if (codigo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor ingresa código para analizar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            List<Token> tokens = Lexer.lex(codigo);
            Parser parser = new Parser(tokens);
            String log = parser.parse();
            txtOutput.setForeground(new Color(0, 128, 0)); // Verde
            txtOutput.setText("✅ ÉXITO:\nAnálisis Sintáctico Exitoso.\n\nReglas aplicadas:\n" + log);
        } catch (Exception ex) {
            txtOutput.setForeground(Color.RED);
            txtOutput.setText("❌ ERROR:\n" + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AnalizadorSintactico().setVisible(true));
    }
}

// ==========================================
// 1. ESTRUCTURA DE TOKENS Y LEXER
// ==========================================
enum TokenType {
    NUMBER, ID, ASSIGN, END, OP, LPAREN, RPAREN, EOF
}

class Token {
    TokenType type;
    String value;

    Token(TokenType type, String value) {
        this.type = type;
        this.value = value;
    }
}

class Lexer {
    public static List<Token> lex(String code) throws Exception {
        List<Token> tokens = new ArrayList<>();
        String regex = "(?<NUMBER>\\d+(\\.\\d*)?)|(?<ID>[a-zA-Z_]\\w*)|(?<ASSIGN>=)|(?<END>;)|(?<OP>[+\\-*/])|(?<LPAREN>\\()|(?<RPAREN>\\))|(?<SKIP>[ \\t\\n]+)|(?<MISMATCH>.)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(code);

        while (matcher.find()) {
            if (matcher.group("SKIP") != null) continue;
            if (matcher.group("MISMATCH") != null) throw new Exception("Carácter no válido: '" + matcher.group("MISMATCH") + "'");

            if (matcher.group("NUMBER") != null) tokens.add(new Token(TokenType.NUMBER, matcher.group("NUMBER")));
            else if (matcher.group("ID") != null) tokens.add(new Token(TokenType.ID, matcher.group("ID")));
            else if (matcher.group("ASSIGN") != null) tokens.add(new Token(TokenType.ASSIGN, matcher.group("ASSIGN")));
            else if (matcher.group("END") != null) tokens.add(new Token(TokenType.END, matcher.group("END")));
            else if (matcher.group("OP") != null) tokens.add(new Token(TokenType.OP, matcher.group("OP")));
            else if (matcher.group("LPAREN") != null) tokens.add(new Token(TokenType.LPAREN, matcher.group("LPAREN")));
            else if (matcher.group("RPAREN") != null) tokens.add(new Token(TokenType.RPAREN, matcher.group("RPAREN")));
        }
        tokens.add(new Token(TokenType.EOF, ""));
        return tokens;
    }
}

// ==========================================
// 2. PARSER (Descenso Recursivo)
// ==========================================
class Parser {
    private final List<Token> tokens;
    private int pos = 0;
    private Token currentToken;
    private final List<String> log = new ArrayList<>();

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        this.currentToken = tokens.get(0);
    }

    private void advance() {
        pos++;
        if (pos < tokens.size()) currentToken = tokens.get(pos);
    }

    private void match(TokenType type) throws Exception {
        if (currentToken.type == type) {
            advance();
        } else {
            throw new Exception("Se esperaba '" + type + "', pero se encontró '" + currentToken.type + "' (Valor: '" + currentToken.value + "')");
        }
    }

    public String parse() throws Exception {
        program();
        if (currentToken.type != TokenType.EOF) {
            throw new Exception("Error de sintaxis: Código sobrante al final.");
        }
        return String.join("\n", log);
    }

    private void program() throws Exception {
        while (currentToken.type != TokenType.EOF) {
            sentencia();
        }
    }

    private void sentencia() throws Exception {
        log.add("-> Analizando sentencia para variable: " + currentToken.value);
        match(TokenType.ID);
        match(TokenType.ASSIGN);
        expresion();
        match(TokenType.END);
    }

    private void expresion() throws Exception {
        termino();
        while (currentToken.type == TokenType.OP && (currentToken.value.equals("+") || currentToken.value.equals("-"))) {
            advance();
            termino();
        }
    }

    private void termino() throws Exception {
        factor();
        while (currentToken.type == TokenType.OP && (currentToken.value.equals("*") || currentToken.value.equals("/"))) {
            advance();
            factor();
        }
    }

    private void factor() throws Exception {
        if (currentToken.type == TokenType.NUMBER) match(TokenType.NUMBER);
        else if (currentToken.type == TokenType.ID) match(TokenType.ID);
        else if (currentToken.type == TokenType.LPAREN) {
            match(TokenType.LPAREN);
            expresion();
            match(TokenType.RPAREN);
        } else {
            throw new Exception("Error en factor: no se esperaba '" + currentToken.value + "'");
        }
    }
}