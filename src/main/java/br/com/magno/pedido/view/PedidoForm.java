package br.com.magno.pedido.view;

import br.com.magno.pedido.entidade.Pedido;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.UUID;

public class PedidoForm extends JFrame {
	
    private static final long serialVersionUID = 503339225455509815L;
    
	private JTextField txtProduto = new JTextField(15);
    private JTextField txtQuantidade = new JTextField(5);
    private JLabel lblStatus = new JLabel("Status: Aguardando envio...");
    private JButton btnEnviar = new JButton("Enviar Pedido");
    private JButton btnSair = new JButton("Sair");
    
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule()) 
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public PedidoForm() {
        super("Sistema de Pedidos - Cliente");
        setupLayout();
        btnEnviar.addActionListener(e -> processarEnvio());
        btnSair.addActionListener(e -> {
        	Object[] options = {"Sim", "Cancelar"};
            
            int confirm = JOptionPane.showOptionDialog(
                    this,
                    "Deseja realmente fechar o sistema?",
                    "Confirmação de Saída",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,     
                    options,  
                    options[0] 
            );
                
            if (confirm == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });
        
        ((AbstractDocument) txtQuantidade.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                if (string.matches("\\d+")) { 
                    super.insertString(fb, offset, string, attr);
                }
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                if (text.matches("\\d+")) {
                    super.replace(fb, offset, length, text, attrs);
                }
            }
        });
        
        
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void setupLayout() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; 
        add(new JLabel("Produto:"), gbc);
        gbc.gridx = 1; 
        add(txtProduto, gbc);

        gbc.gridx = 0; gbc.gridy = 1; 
        add(new JLabel("Quantidade:"), gbc);
        gbc.gridx = 1; 
        add(txtQuantidade, gbc);

        gbc.gridx = 0; 
        gbc.gridy = 2; 
        gbc.gridwidth = 2; 
        add(btnEnviar, gbc);
        
        gbc.gridx = 0; 
        gbc.gridy = 3; 
        gbc.gridwidth = 2; 
        add(btnSair, gbc);
        
        gbc.gridx = 0; 
        gbc.gridy = 4; 
        add(lblStatus, gbc);
    }

    private void processarEnvio() {
        lblStatus.setText("Status: Validando...");

        String produto = txtProduto.getText().trim();
        String qtdTexto = txtQuantidade.getText().trim();
        
        if (produto.isEmpty() || qtdTexto.isEmpty()) {
            exibirErro("Todos os campos devem ser preenchidos!");
            return;
        }
        
        if (produto.isEmpty()) {
            exibirErro("O nome do produto deve ser preenchido!");
            txtProduto.requestFocus();
            return;
        }

        int quantidade = Integer.parseInt(qtdTexto);

        if (quantidade <= 0) {
            exibirErro("A quantidade deve ser maior que zero!");
            return;
        }
      
        enviarParaServidor(produto, quantidade);
    }

    private void exibirErro(String mensagem) {
        lblStatus.setText("Status: Erro de validação.");
        lblStatus.setForeground(Color.RED);
        JOptionPane.showMessageDialog(this, mensagem, "Erro de Preenchimento", JOptionPane.WARNING_MESSAGE);
    }
    
    private void enviarParaServidor(String produto, int quantidade) {
        try {
            lblStatus.setForeground(Color.BLACK);
            UUID id = UUID.randomUUID();
            Pedido pedido = new Pedido();
            pedido.setId(id);
            pedido.setProduto(txtProduto.getText());
            pedido.setQuantidade(Integer.parseInt(txtQuantidade.getText()));
            pedido.setDataCriacao(LocalDateTime.now());

            String json = objectMapper.writeValueAsString(pedido);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/pedidos"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            btnEnviar.setEnabled(false);
            lblStatus.setText("Status: Enviando pedido...");

            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 202) {
                        iniciarPollingStatus(id);
                    } else {
                        SwingUtilities.invokeLater(() -> {
                            exibirErro("Erro no servidor: " + response.body());
                            btnEnviar.setEnabled(true);
                        });
                    }
                })
                .exceptionally(ex -> {
                    SwingUtilities.invokeLater(() -> {
                        exibirErro("Servidor offline! Certifique-se que o Backend está rodando.");
                        btnEnviar.setEnabled(true);
                    });
                    return null;
                });

        } catch (Exception ex) {
            exibirErro("Erro inesperado: " + ex.getMessage());
        }
    }    
    
    private void iniciarPollingStatus(UUID id) {
        SwingWorker<Void, String> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                String status = "";
                while (!status.equals("SUCESSO") && !status.equals("FALHA")) {
                    Thread.sleep(2_000);
                    
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create("http://localhost:8080/api/pedidos/status/" + id))
                            .GET()
                            .build();

                    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                    status = response.body();
                    
                    publish(status); 
                }
                return null;
            }

            @Override
            protected void process(java.util.List<String> chunks) {
                String ultimoStatus = chunks.get(chunks.size() - 1);
                lblStatus.setText("Status: " + ultimoStatus);
            }

            @Override
            protected void done() {
                btnEnviar.setEnabled(true);
                try {
                    get();
                } catch (Exception e) {
                    lblStatus.setText("Erro ao consultar status.");
                }
            }
        };

        worker.execute();
    }
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        EventQueue.invokeLater(() -> {
            try {
                PedidoForm frame = new PedidoForm();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}