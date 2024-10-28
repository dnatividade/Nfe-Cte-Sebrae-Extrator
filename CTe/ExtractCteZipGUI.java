// ///////////////////////////////////////////////////////////////////////////
// ExtractCteZipGUI v.:0.1 (2024-10-28)
//
// Sistema para extrair os XML dos Conhecimentos de Transporte Eletrônicos emitidos pelo
// antigo Emissor de CT-e gratuito do SEBRAE - NF-e 3.00
// Para usar, basta informar o caminho do diretorio database/CTE_300
// ///////////////////////////////////////////////////////////////////////////

// Autor: dnat
// CONNECTIVA REDES DE COMPUTADORES LTDA

//////////////////////////////////////////////////////////////////////////////
// *******************************
// *** Compilar e rodar .class ***
// *******************************
// Compilar: javac -cp "../derby-10_16_1_1/lib/derby.jar" ExtractCteZipGUI.java
// Rodar:    java -cp ".:../derby-10_16_1_1/lib/derby.jar" ExtractCteZipGUI <caminho_do_banco>
// ///////////////////////////////////////////////////////////////////////////
//
// ******************************
// *** Compilar e rodar .jar ***
// ******************************
// Compilar:  javac -cp "../derby-10_16_1_1/lib/derby.jar" ExtractCteZipGUI.java
// Gerar JAR: jar cfm ExtractCteZipGUI.jar MANIFEST.MF *.class
// Rodar:     java -jar ExtractCteZipGUI.jar
//
// Conteudo do arquivo MANIFEST.MF ///////////////////////////////////////////
// Manifest-Version: 1.1
// Main-Class: ExtractCteZipGUI
// Class-Path: ../derby-10_16_1_1/lib/derby.jar
// ///////////////////////////////////////////////////////////////////////////


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.file.*;
import java.sql.*;

public class ExtractCteZipGUI extends JFrame {
    private JTextField dbPathField;
    private JButton selectDbButton, extractButton, extractCsvButton;  // Botões para extrair ZIP e CSV
    private JTextArea outputArea;
    private JFileChooser fileChooser;

    public ExtractCteZipGUI() {
        super("Extrair CT-e do database do Emissor SEBRAE (NF-e 3.00)");

        // Layout principal
        setLayout(new BorderLayout());

        // Painel para a parte superior (campo de caminho do banco e botões)
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout());  // Alterado para FlowLayout

        dbPathField = new JTextField(30);
        selectDbButton = new JButton("[1] Selecionar Banco");

        // Botões para extrair arquivos XML e CSV
        extractButton = new JButton("[2] Baixar XML");
        extractCsvButton = new JButton("[3] Extrair informações CT-e emitidas (csv)");  // Novo

        // Adicionar componentes ao painel de entrada
        topPanel.add(new JLabel("Caminho do Banco:"));
        topPanel.add(dbPathField);
        topPanel.add(selectDbButton);
        topPanel.add(extractButton);
        topPanel.add(extractCsvButton);  // Adicionando botão de CSV

        // Adiciona o painel superior ao topo da interface
        add(topPanel, BorderLayout.NORTH);

        // Área de log para exibir os resultados da extração
        outputArea = new JTextArea(10, 50);
        outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputArea);

        // Adiciona o log ao centro da interface
        add(scrollPane, BorderLayout.CENTER);

        // Inicializar o seletor de arquivos
        fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        // Ação para selecionar o banco de dados
        selectDbButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
                int returnValue = fileChooser.showOpenDialog(null);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File selectedDir = fileChooser.getSelectedFile();
                    dbPathField.setText(selectedDir.getAbsolutePath());
                }
            }
        });

        // Ação para extrair os arquivos XML (zipados)
        extractButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String dbPath = dbPathField.getText();
                if (dbPath.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Por favor, selecione o caminho do banco de dados.");
                } else {
                    extractZipFiles(dbPath);
                }
            }
        });

        // Ação para extrair os dados para CSV
        extractCsvButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String dbPath = dbPathField.getText();
                if (dbPath.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Por favor, selecione o caminho do banco de dados.");
                } else {
                    extractDataToCsv(dbPath);
                }
            }
        });

        // Configurações da janela
        setSize(1200, 540);  // Janela aberta com o tamanho 1200x540
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);  // Centraliza a janela na tela
        setVisible(true);
    }

    private void extractZipFiles(String dbPath) {
        String jdbcUrl = "jdbc:derby:" + dbPath + ";create=false";
        String outputDir = "CTe_ZIP";

        // Criar o diretório de saída se não existir
        File directory = new File(outputDir);
        if (!directory.exists()) {
            directory.mkdir();
        }

        // Inicializar o contador
        int counter = 1;
        outputArea.append("Iniciando a extração dos arquivos XML...\n");

        try (Connection conn = DriverManager.getConnection(jdbcUrl)) {
            // Consulta SQL para obter DOCUMENTO_DEST e DOC_XML (BLOB)
            String sql = "SELECT E.CNPJ, CT.SERIE, CT.NUMERO, CT.DOC_XML FROM CTE.CONHECIMENTO_TRANSPORTE CT "
                       + "JOIN CTE.EMITENTE E ON CT.ID_EMITENTE = E.ID_EMITENTE";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    String documentoEmit = rs.getString("CNPJ");
                    String serieDoc = rs.getString("SERIE");
                    String numeroDoc = rs.getString("NUMERO");
                    Blob doc_xmlBlob = rs.getBlob("DOC_XML");

                    // Obter os bytes do BLOB
                    byte[] blobBytes = doc_xmlBlob.getBytes(1, (int) doc_xmlBlob.length());

                    // Nome do arquivo a ser salvo
                    String fileName = outputDir + File.separator + counter + "_" + documentoEmit + "_" + serieDoc + "-" + numeroDoc + ".zip";

                    // Escrever o arquivo ZIP
                    Files.write(Paths.get(fileName), blobBytes);

                    outputArea.append("Arquivo " + fileName + " criado com sucesso!\n");

                    // Incrementar o contador
                    counter++;
                }

                outputArea.append((counter - 1) + " arquivos recuperados para o diretório: " + outputDir + "\n");
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            outputArea.append("Erro ao processar o banco de dados: " + e.getMessage() + "\n");
        }
    }

    // Método para extrair dados para CSV
    private void extractDataToCsv(String dbPath) {
        String jdbcUrl = "jdbc:derby:" + dbPath + ";create=false";
        String outputCsvFile = "conhecimentos_transporte.csv";

        outputArea.append("Iniciando a extração dos dados para CSV...\n");

        try (Connection conn = DriverManager.getConnection(jdbcUrl);
             FileWriter fileWriter = new FileWriter(outputCsvFile);
             PrintWriter printWriter = new PrintWriter(fileWriter)) {

            // Consulta SQL
            String sql = "SELECT CT.ID_CONHECIMENTO_TRANSPORTE, CT.NUMERO, CT.SERIE, CT.MODELO, CT.SITUACAO, "
                       + "CT.MES, CT.ANO, CT.TIPO_SERVICO, CT.TIPO_EMISSAO, CT.UF_INICIO, CT.UF_TERMINO, "
                       + "CT.DATA_HORA_EMISSAO, CT.DATA_AUTORIZACAO, CT.CODIGO_NUMERICO_CHAVE_ACESSO, "
                       + "CT.DIGITO_CHAVE_ACESSO, CT.AUTORIZACAO_EXPORTADA_XML, CT.NUMERO_RECIBO, "
                       + "CT.DACTE_IMPRESSO, CT.DATA_SISTEMA, CT.PROTOCOLO, CT.NUMERO_PROTOCOLO, "
                       + "CT.DATA_PROTOCOLO, CT.UF_EMITENTE, CT.ID_EMITENTE, CT.ID_LOTE, CT.CODIGO_ERRO, "
                       + "CT.MENSAGEM_ERRO, CT.DOCUMENTO_TOMADOR, CT.DOCUMENTO_REMETENTE, "
                       + "CT.DOCUMENTO_EXPEDIDOR, CT.DOCUMENTO_RECEBEDOR, CT.VERSAO, CT.DOCUMENTO_OUTRO, "
                       + "E.X_NOME, E.CNPJ "
                       + "FROM CTE.CONHECIMENTO_TRANSPORTE CT "
                       + "JOIN CTE.EMITENTE E ON CT.ID_EMITENTE = E.ID_EMITENTE";

            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {

                // Escrever cabeçalho do CSV
                printWriter.println("ID_CONHECIMENTO_TRANSPORTE,NUMERO,SERIE,MODELO,SITUACAO,"                //1 - 5
                                   + "MES,ANO,TIPO_SERVICO,TIPO_EMISSAO,UF_INICIO,UF_TERMINO,"                //6 - 11
                                   + "DATA_HORA_EMISSAO,DATA_AUTORIZACAO,CODIGO_NUMERICO_CHAVE_ACESSO,"       //12 - 14
                                   + "DIGITO_CHAVE_ACESSO,AUTORIZACAO_EXPORTADA_XML,NUMERO_RECIBO,"           //15 - 17
                                   + "DACTE_IMPRESSO,DATA_SISTEMA,NUMERO_PROTOCOLO,DATA_PROTOCOLO,"           //18 - 21
                                   + "UF_EMITENTE,ID_EMITENTE,CNPJ_EMITENTE,X_NOME_EMITENTE,"                 //22 - 25
                                   + "ID_LOTE,CODIGO_ERRO,MENSAGEM_ERRO,"                                     //26 - 28
                                   + "DOCUMENTO_TOMADOR,DOCUMENTO_REMETENTE,DOCUMENTO_EXPEDIDOR,"             //29 - 31
                                   + "DOCUMENTO_RECEBEDOR,VERSAO,DOCUMENTO_OUTRO");                           //32 - 34

                // Escrever os dados
                while (rs.next()) {
                    printWriter.println(rs.getInt("ID_CONHECIMENTO_TRANSPORTE") + "," +
                                       rs.getString("NUMERO") + "," +
                                       rs.getString("SERIE") + "," +
                                       rs.getString("MODELO") + "," +
                                       rs.getString("SITUACAO") + "," +
                                       rs.getInt("MES") + "," +
                                       rs.getInt("ANO") + "," +
                                       rs.getString("TIPO_SERVICO") + "," +
                                       rs.getString("TIPO_EMISSAO") + "," +
                                       rs.getString("UF_INICIO") + "," +
                                       rs.getString("UF_TERMINO") + "," +
                                       rs.getTimestamp("DATA_HORA_EMISSAO") + "," +
                                       rs.getTimestamp("DATA_AUTORIZACAO") + "," +
                                       rs.getString("CODIGO_NUMERICO_CHAVE_ACESSO") + "," +
                                       rs.getString("DIGITO_CHAVE_ACESSO") + "," +
                                       rs.getString("AUTORIZACAO_EXPORTADA_XML") + "," +
                                       rs.getString("NUMERO_RECIBO") + "," +
                                       rs.getString("DACTE_IMPRESSO") + "," +
                                       rs.getTimestamp("DATA_SISTEMA") + "," +
                                       rs.getString("NUMERO_PROTOCOLO") + "," +
                                       rs.getTimestamp("DATA_PROTOCOLO") + "," +
                                       rs.getString("UF_EMITENTE") + "," +
                                       rs.getInt("ID_EMITENTE") + "," +
                                       rs.getString("CNPJ") + "," +
                                       rs.getString("X_NOME") + "," +
                                       rs.getInt("ID_LOTE") + "," +
                                       rs.getString("CODIGO_ERRO") + "," +
                                       rs.getString("MENSAGEM_ERRO") + "," +
                                       rs.getString("DOCUMENTO_TOMADOR") + "," +
                                       rs.getString("DOCUMENTO_REMETENTE") + "," +
                                       rs.getString("DOCUMENTO_EXPEDIDOR") + "," +
                                       rs.getString("DOCUMENTO_RECEBEDOR") + "," +
                                       rs.getString("VERSAO") + "," +
                                       rs.getString("DOCUMENTO_OUTRO"));
                }
                outputArea.append("Dados extraídos com sucesso para " + outputCsvFile + "\n");
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            outputArea.append("Erro ao processar a extração de dados: " + e.getMessage() + "\n");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ExtractCteZipGUI());
    }
}

