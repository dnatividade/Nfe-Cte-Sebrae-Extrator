// ///////////////////////////////////////////////////////////////////////////
// ExtractNfeZipGUI v.:0.2 (2024-10-28)
//
// Sistema para extrair os XML das Notas Fiscais Eletrônicas emitidas pelo
// antigo Emissor de NF-e gratuito do SEBRAE - NF-e 4.01
// Para usar, basta informar o caminho do diretorio database/NFE_400
// ///////////////////////////////////////////////////////////////////////////

// Autor: dnat
// CONNECTIVA REDES DE COMPUTADORES LTDA

//////////////////////////////////////////////////////////////////////////////
// *******************************
// *** Compilar e rodar .class ***
// *******************************
// Compilar: javac -cp "../derby-10_16_1_1/lib/derby.jar" ExtractNfeZipGUI.java
// Rodar:    java -cp ".:../derby-10_16_1_1/lib/derby.jar" ExtractNfeZipGUI <caminho_do_banco>
// ///////////////////////////////////////////////////////////////////////////
//
// ******************************
// *** Compilar e rodar .jar ***
// ******************************
// Compilar:  javac -cp "../derby-10_16_1_1/lib/derby.jar" ExtractNfeZipGUI.java
// Gerar JAR: jar cfm ExtractNfeZipGUI.jar MANIFEST.MF *.class
// Rodar:     java -jar ExtractNfeZipGUI.jar
//
// Conteudo do arquivo MANIFEST.MF ///////////////////////////////////////////
// Manifest-Version: 1.1
// Main-Class: ExtractNfeZipGUI
// Class-Path: ../derby-10_16_1_1/lib/derby.jar
// ///////////////////////////////////////////////////////////////////////////


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.file.*;
import java.sql.*;

public class ExtractNfeZipGUI extends JFrame {
    private JTextField dbPathField;
    private JButton selectDbButton, extractButton, extractCsvButton;  // Botões para extrair ZIP e CSV
    private JTextArea outputArea;
    private JFileChooser fileChooser;

    public ExtractNfeZipGUI() {
        super("Extrair NF-e do database do Emissor SEBRAE (NF-e 4.01)");

        // Layout principal
        setLayout(new BorderLayout());

        // Painel para a parte superior (campo de caminho do banco e botões)
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout());  // Alterado para FlowLayout

        dbPathField = new JTextField(30);
        selectDbButton = new JButton("[1] Selecionar Banco");

        // Botões para extrair arquivos XML e CSV
        extractButton = new JButton("[2] Baixar XML");
        extractCsvButton = new JButton("[3] Extrair informações NF-e emitidas (csv)");  // Novo

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
        String outputDir = "NFe_ZIP";

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
            String sql = "SELECT E.NR_DOCUMENTO, NF.SERIE, NF.NUMERO, NF.DOC_XML FROM NFE.NOTA_FISCAL NF "
                       + "JOIN NFE.EMITENTE E ON NF.ID_EMITENTE = E.ID_EMITENTE";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    String documentoEmit = rs.getString("NR_DOCUMENTO");
                    String serieDoc = rs.getString("SERIE");
                    String numeroDoc = rs.getString("NUMERO");
                    Blob doc_xmlBlob = rs.getBlob("DOC_XML");

                    // Obter os bytes do BLOB
                    byte[] blobBytes = doc_xmlBlob.getBytes(1, (int) doc_xmlBlob.length());

                    // Nome do arquivo a ser salvo
                    //String fileName = outputDir + File.separator + counter + "_" + documentoEmit + "_" + documentoDest + ".zip";
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
        String outputCsvFile = "notas_fiscais.csv";

        outputArea.append("Iniciando a extração dos dados para CSV...\n");

        try (Connection conn = DriverManager.getConnection(jdbcUrl);
             FileWriter fileWriter = new FileWriter(outputCsvFile);
             PrintWriter printWriter = new PrintWriter(fileWriter)) {

            // Consulta SQL
            String sql = "SELECT NF.ID_NOTA_FISCAL, NF.NUMERO, NF.SERIE, NF.MODELO, NF.SITUACAO, "
                       + "NF.MES, NF.ANO, NF.TIPO_EMISSAO, NF.DATA_EMISSAO, "
                       + "NF.DATA_AUTORIZACAO, NF.CODIGO_NUMERICO_CHAVE_ACESSO, "
                       + "NF.DIGITO_CHAVE_ACESSO, NF.AUTORIZACAO_EXPORTADA_XML, "
                       + "NF.DOCUMENTO_DEST, NF.UF_DEST, NF.NUMERO_RECIBO, "
                       + "NF.DANFE_IMPRESSO, NF.DATA_SISTEMA, NF.NUMERO_PROTOCOLO, "
                       + "NF.DATA_PROTOCOLO, NF.CODIGO_UF_EMITENTE, NF.ID_EMITENTE, "
                       + "NF.ID_LOTE, NF.CODIGO_ERRO, NF.MENSAGEM_ERRO, NF.VERSAO, "
                       + "E.X_NOME, E.NR_DOCUMENTO "
                       + "FROM NFE.NOTA_FISCAL NF "
                       + "JOIN NFE.EMITENTE E ON NF.ID_EMITENTE = E.ID_EMITENTE";

            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {

                // Escrever cabeçalho do CSV
                printWriter.println("ID_NOTA_FISCAL,NUMERO,SERIE,MODELO,SITUACAO,MES,ANO,TIPO_EMISSAO,"            //1 - 8
                                   + "DATA_EMISSAO,DATA_AUTORIZACAO,CODIGO_NUMERICO_CHAVE_ACESSO,"                 //9 - 11
                                   + "DIGITO_CHAVE_ACESSO,AUTORIZACAO_EXPORTADA_XML,"                              //12  13
                                   + "ID_EMITENTE,NR_DOCUMENTO_EMITENTE,X_NOME_EMITENTE,CODIGO_UF_EMITENTE,"       //14 - 17
                                   + "DOCUMENTO_DEST,UF_DEST,NUMERO_RECIBO,DANFE_IMPRESSO,DATA_SISTEMA,"           //18 - 22
                                   + "NUMERO_PROTOCOLO,DATA_PROTOCOLO,ID_LOTE,CODIGO_ERRO,MENSAGEM_ERRO,VERSAO");  //23 - 28

                // Escrever os dados
                while (rs.next()) {
                    printWriter.println(rs.getInt("ID_NOTA_FISCAL") + "," +
                                       rs.getString("NUMERO") + "," +
                                       rs.getString("SERIE") + "," +
                                       rs.getString("MODELO") + "," +
                                       rs.getString("SITUACAO") + "," +
                                       rs.getInt("MES") + "," +
                                       rs.getInt("ANO") + "," +
                                       rs.getString("TIPO_EMISSAO") + "," +
                                       rs.getDate("DATA_EMISSAO") + "," +
                                       rs.getDate("DATA_AUTORIZACAO") + "," +
                                       rs.getString("CODIGO_NUMERICO_CHAVE_ACESSO") + "," +
                                       rs.getString("DIGITO_CHAVE_ACESSO") + "," +
                                       rs.getString("AUTORIZACAO_EXPORTADA_XML") + "," +

                                       rs.getInt("ID_EMITENTE") + "," +
                                       rs.getString("NR_DOCUMENTO") + "," +
                                       rs.getString("X_NOME") + "," +
                                       rs.getInt("CODIGO_UF_EMITENTE") + "," +
                                       
                                       rs.getString("DOCUMENTO_DEST") + "," +
                                       rs.getString("UF_DEST") + "," +
                                       rs.getString("NUMERO_RECIBO") + "," +
                                       rs.getString("DANFE_IMPRESSO") + "," +
                                       rs.getDate("DATA_SISTEMA") + "," +
                                       rs.getString("NUMERO_PROTOCOLO") + "," +
                                       rs.getDate("DATA_PROTOCOLO") + "," +
                                       rs.getInt("ID_LOTE") + "," +
                                       rs.getString("CODIGO_ERRO") + "," +
                                       rs.getString("MENSAGEM_ERRO") + "," +
                                       rs.getString("VERSAO"));
                }

                outputArea.append("Dados extraídos com sucesso para " + outputCsvFile + "\n");
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            outputArea.append("Erro ao processar a extração de dados: " + e.getMessage() + "\n");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ExtractNfeZipGUI());
    }
}

