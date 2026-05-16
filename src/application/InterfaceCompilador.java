package application;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import model.entities.Lexer;
import model.entities.Parser;

public class InterfaceCompilador extends JFrame {

    private JTextPane txtEditor;
    private JTextArea txtLinhas;
    private JTextArea txtConsole;
    private File currentFile = null;
    
    // Flags para evitar loops de recursão infinita nos listeners
    private boolean isHighlighting = false; 
    private boolean isUpdatingCaret = false;

    // Lista de palavras-chave para colorir (Baseado no seu Lexer)
    private static final Set<String> KEYWORDS = new HashSet<>(List.of(
        "break", "default", "func", "interface", "select", "case", "defer", "go", 
        "map", "struct", "chan", "else", "goto", "package", "switch", "const", 
        "fallthrough", "if", "range", "type", "continue", "for", "import", "return", "var",
        "true", "false", "nil"
    ));

    public InterfaceCompilador() {
        setTitle("JGO - Go Compiler");
        setSize(950, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // 1. Configuração do Editor com Coloração e Realce
        txtEditor = new JTextPane();
        txtEditor.setFont(new Font("Consolas", Font.PLAIN, 14));
        txtEditor.setEditorKit(new StyledEditorKit());

        // 2. Configuração da Barra Lateral de Linhas
        txtLinhas = new JTextArea("1");
        txtLinhas.setFont(new Font("Consolas", Font.PLAIN, 14));
        txtLinhas.setBackground(new Color(245, 245, 245));
        txtLinhas.setForeground(Color.GRAY);
        txtLinhas.setEditable(false);
        txtLinhas.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.LIGHT_GRAY));

        // Painel que junta a numeração e o editor
        JPanel painelEditorCompleto = new JPanel(new BorderLayout());
        painelEditorCompleto.add(txtLinhas, BorderLayout.WEST);
        painelEditorCompleto.add(txtEditor, BorderLayout.CENTER);
        JScrollPane scrollEditor = new JScrollPane(painelEditorCompleto);

        // 3. Configuração da Consola de Saída
        txtConsole = new JTextArea();
        txtConsole.setFont(new Font("Consolas", Font.PLAIN, 12));
        txtConsole.setBackground(new Color(30, 30, 30));
        txtConsole.setForeground(Color.GREEN);
        txtConsole.setEditable(false);
        JScrollPane scrollConsole = new JScrollPane(txtConsole);

        // Divisão do ecrã (Editor em cima, Consola em baixo)
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollEditor, scrollConsole);
        splitPane.setDividerLocation(420);
        add(splitPane, BorderLayout.CENTER);

        // 4. Criação dos Botões (Menu Superior)
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnAbrir = new JButton("Abrir Ficheiro");
        JButton btnSalvar = new JButton("Salvar");
        JButton btnCompilar = new JButton("Analisar");

        painelBotoes.add(btnAbrir);
        painelBotoes.add(btnSalvar);
        painelBotoes.add(btnCompilar);
        add(painelBotoes, BorderLayout.NORTH);

        // 5. Escuta de eventos do documento (Digitação e Edição)
        txtEditor.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { atualizarInterface(); }
            @Override public void removeUpdate(DocumentEvent e) { atualizarInterface(); }
            @Override public void changedUpdate(DocumentEvent e) {}
        });

        // 6. Escuta de eventos do cursor (Destaque de linha atual)
        txtEditor.addCaretListener(new CaretListener() {
            @Override
            public void caretUpdate(CaretEvent e) {
                if (isUpdatingCaret || isHighlighting) return;
                SwingUtilities.invokeLater(() -> realcarLinhaAtual());
            }
        });

        // 7. Ações dos Botões
        btnAbrir.addActionListener(e -> abrirFicheiro());
        btnSalvar.addActionListener(e -> salvarFicheiro());
        btnCompilar.addActionListener(e -> executarCompilador());

        // Redireciona e trata acentuação UTF-8
        redirecionarConsole();
    }

    private void atualizarInterface() {
        SwingUtilities.invokeLater(() -> {
            atualizarNumeroLinhas();
            aplicarHighlight();
        });
    }

    private void atualizarNumeroLinhas() {
        String texto = txtEditor.getText();
        int totalLinhas = texto.split("\\r?\\n", -1).length;
        
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= totalLinhas; i++) {
            sb.append(i).append("\n");
        }
        txtLinhas.setText(sb.toString());
    }

    private void aplicarHighlight() {
        if (isHighlighting) return;
        isHighlighting = true;

        StyledDocument doc = txtEditor.getStyledDocument();
        String texto = txtEditor.getText();

        Style estiloPadrao = txtEditor.addStyle("Padrao", null);
        StyleConstants.setForeground(estiloPadrao, Color.BLACK);
        StyleConstants.setBold(estiloPadrao, false);

        Style estiloKeyword = txtEditor.addStyle("Keyword", null);
        StyleConstants.setForeground(estiloKeyword, new Color(0, 0, 180));
        StyleConstants.setBold(estiloKeyword, true);

        Style estiloString = txtEditor.addStyle("String", null);
        StyleConstants.setForeground(estiloString, new Color(160, 30, 30));

        // 1. Reseta o texto inteiro para a cor e formatação padrão
        doc.setCharacterAttributes(0, texto.length(), estiloPadrao, true);

        // 2. Colorir Palavras-Chave
        Pattern patternPalavras = Pattern.compile("\\b[a-zA-Z_][a-zA-Z0-9_]*\\b");
        Matcher matcherPalavras = patternPalavras.matcher(texto);
        while (matcherPalavras.find()) {
            String palavra = matcherPalavras.group();
            if (KEYWORDS.contains(palavra)) {
                doc.setCharacterAttributes(matcherPalavras.start(), palavra.length(), estiloKeyword, false);
            }
        }

        // 3. Colorir Strings Literals ("...")
        Pattern patternStrings = Pattern.compile("\"[^\"]*\"");
        Matcher matcherStrings = patternStrings.matcher(texto);
        while (matcherStrings.find()) {
            doc.setCharacterAttributes(matcherStrings.start(), matcherStrings.group().length(), estiloString, false);
        }

        isHighlighting = false;
        realcarLinhaAtual(); // Re-aplica o fundo cinza por cima da nova digitação
    }

    private void realcarLinhaAtual() {
        if (isUpdatingCaret) return;
        isUpdatingCaret = true;

        StyledDocument doc = txtEditor.getStyledDocument();
        String texto = txtEditor.getText();
        int caretPos = txtEditor.getCaretPosition();

        // Estilos de Fundo
        SimpleAttributeSet fundoPadrao = new SimpleAttributeSet();
        StyleConstants.setBackground(fundoPadrao, Color.WHITE);

        SimpleAttributeSet fundoLinhaAtiva = new SimpleAttributeSet();
        StyleConstants.setBackground(fundoLinhaAtiva, new Color(234, 234, 234)); // Cinza claro tipo #ddd suave

        // 1. Limpa o fundo de todo o texto para Branco
        doc.setCharacterAttributes(0, texto.length(), fundoPadrao, false);

        // 2. Localiza os limites da linha onde está o cursor
        try {
            int lineStart = Utilities.getRowStart(txtEditor, caretPos);
            int lineEnd = Utilities.getRowEnd(txtEditor, caretPos);
            int length = lineEnd - lineStart;

            if (length > 0) {
                // Pinta o fundo da linha selecionada sem remover a cor das palavras
                doc.setCharacterAttributes(lineStart, length, fundoLinhaAtiva, false);
            }
        } catch (BadLocationException e) {
            // Ignora falhas de limite
        }

        isUpdatingCaret = false;
    }

    private void abrirFicheiro() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            currentFile = fileChooser.getSelectedFile();
            try {
                String content = Files.readString(currentFile.toPath(), StandardCharsets.UTF_8);
                txtEditor.setText(content);
                txtConsole.setText("Ficheiro carregado: " + currentFile.getName() + "\n");
                atualizarInterface();
            } catch (IOException ex) {
                txtConsole.setText("Erro ao ler ficheiro: " + ex.getMessage());
            }
        }
    }

    private void salvarFicheiro() {
        if (currentFile == null) {
            JFileChooser fileChooser = new JFileChooser();
            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                currentFile = fileChooser.getSelectedFile();
            } else {
                return;
            }
        }
        try {
            Files.writeString(currentFile.toPath(), txtEditor.getText(), StandardCharsets.UTF_8);
            txtConsole.append("Ficheiro guardado com sucesso!\n");
        } catch (IOException ex) {
            txtConsole.append("Erro ao salvar ficheiro: " + ex.getMessage() + "\n");
        }
    }

    private void executarCompilador() {
        if (txtEditor.getText().trim().isEmpty()) {
            txtConsole.setText("O editor está vazio. Escreva algum código Go primeiro.");
            return;
        }

        txtConsole.setText(""); 
        
        try {
            List<String> sourceCode = List.of(txtEditor.getText().split("\\r?\\n"));

            System.out.println("### INICIANDO ANÁLISE LÉXICA ###");
            Lexer lexer = new Lexer(sourceCode);
            lexer.analex();

            System.out.println("\n### INICIANDO ANÁLISE SINTÁTICA ###");
            Parser parser = new Parser(lexer.getTokens());
            parser.parse();

        } catch (Exception ex) {
            System.err.println("Erro crítico na execução: " + ex.getMessage());
        }
    }

    private void redirecionarConsole() {
        // ByteArrayOutputStream acumula bytes para que caracteres multi-byte (acentos em UTF-8) não quebrem
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        
        OutputStream out = new OutputStream() {
            @Override
            public void write(int b) {
                buffer.write(b);
                if (b == '\n') { // Envia ao JTextArea linha por linha decodificada corretamente
                    txtConsole.append(buffer.toString(StandardCharsets.UTF_8));
                    buffer.reset();
                    txtConsole.setCaretPosition(txtConsole.getDocument().getLength());
                }
            }
        };
        System.setOut(new PrintStream(out, true, StandardCharsets.UTF_8));
        System.setErr(new PrintStream(out, true, StandardCharsets.UTF_8));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new InterfaceCompilador().setVisible(true));
    }
}
