import javax.swing.*;
import java.awt.*;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * AppCotacao — a INTERFACE GRÁFICA do app de cotações.
 *
 * Mostra três cartões (Dólar, Euro e Bitcoin) com o valor em reais e a
 * variação do dia. Um botão "Atualizar" busca os dados mais recentes.
 */
public class AppCotacao extends JFrame {

    // Paleta
    private static final Color FUNDO   = new Color(0xF4F6FA);
    private static final Color TOPO    = new Color(0x1F2A44); // azul-marinho
    private static final Color INK     = new Color(0x1F2430);
    private static final Color MUTED   = new Color(0x8A91A0);
    private static final Color SUBIU   = new Color(0x2E9E6B); // verde
    private static final Color CAIU    = new Color(0xC0544B); // vermelho

    // Formata números no padrão brasileiro (R$ 5,40  /  R$ 358.000,00).
    private static final Locale BR = Locale.forLanguageTag("pt-BR");
    private static final NumberFormat REAIS = NumberFormat.getCurrencyInstance(BR);

    private final CotacaoService servico = new CotacaoService();

    // Um cartão para cada moeda (as cores são os destaques de cada uma).
    private final CartaoMoeda cartaoDolar   = new CartaoMoeda("Dólar",   new Color(0x2E7D52));
    private final CartaoMoeda cartaoEuro    = new CartaoMoeda("Euro",    new Color(0x2D6CDF));
    private final CartaoMoeda cartaoBitcoin = new CartaoMoeda("Bitcoin", new Color(0xE8973A));

    private final JButton btAtualizar = new JButton("Atualizar");
    private final JLabel  status      = new JLabel("Carregando...", SwingConstants.CENTER);

    public AppCotacao() {
        setTitle("Cotação de Moedas");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(420, 560);
        setMinimumSize(new Dimension(380, 500));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(FUNDO);

        add(criarTopo(),   BorderLayout.NORTH);
        add(criarCentro(), BorderLayout.CENTER);
        add(criarRodape(), BorderLayout.SOUTH);

        atualizar(); // já busca as cotações ao abrir
    }

    /** Faixa azul-marinho no topo com o título. */
    private JComponent criarTopo() {
        JPanel topo = new JPanel(new BorderLayout());
        topo.setBackground(TOPO);
        topo.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));

        JLabel titulo = new JLabel("Cotação de Moedas");
        titulo.setForeground(Color.WHITE);
        titulo.setFont(new Font("SansSerif", Font.BOLD, 20));

        JLabel sub = new JLabel("Valores em tempo real, em reais");
        sub.setForeground(new Color(0xB9C2D6));
        sub.setFont(new Font("SansSerif", Font.PLAIN, 12));

        JPanel textos = new JPanel(new GridLayout(2, 1));
        textos.setOpaque(false);
        textos.add(titulo);
        textos.add(sub);
        topo.add(textos, BorderLayout.WEST);
        return topo;
    }

    /** Centro: os três cartões empilhados. */
    private JComponent criarCentro() {
        JPanel centro = new JPanel();
        centro.setBackground(FUNDO);
        centro.setLayout(new BoxLayout(centro, BoxLayout.Y_AXIS));
        centro.setBorder(BorderFactory.createEmptyBorder(18, 18, 8, 18));

        centro.add(cartaoDolar);
        centro.add(Box.createVerticalStrut(12));
        centro.add(cartaoEuro);
        centro.add(Box.createVerticalStrut(12));
        centro.add(cartaoBitcoin);
        centro.add(Box.createVerticalGlue());
        return centro;
    }

    /** Rodapé: botão Atualizar + texto de status. */
    private JComponent criarRodape() {
        JPanel rodape = new JPanel(new BorderLayout(0, 8));
        rodape.setBackground(FUNDO);
        rodape.setBorder(BorderFactory.createEmptyBorder(4, 18, 16, 18));

        btAtualizar.setFont(new Font("SansSerif", Font.BOLD, 14));
        btAtualizar.setBackground(TOPO);
        btAtualizar.setForeground(Color.WHITE);
        btAtualizar.setFocusPainted(false);
        btAtualizar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btAtualizar.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        btAtualizar.addActionListener(e -> atualizar());

        status.setFont(new Font("SansSerif", Font.PLAIN, 12));
        status.setForeground(MUTED);

        rodape.add(btAtualizar, BorderLayout.NORTH);
        rodape.add(status, BorderLayout.SOUTH);
        return rodape;
    }

    private void atualizar() {
        btAtualizar.setEnabled(false);
        status.setForeground(MUTED);
        status.setText("Atualizando...");

        new SwingWorker<List<Moeda>, Void>() {
            @Override
            protected List<Moeda> doInBackground() throws Exception {
                return servico.buscarCotacoes();
            }

            @Override
            protected void done() {
                try {
                    mostrar(get());
                } catch (Exception e) {
                    status.setForeground(CAIU);
                    status.setText("Não foi possível atualizar. Verifique a internet.");
                }
                btAtualizar.setEnabled(true);
            }
        }.execute();
    }

    void mostrar(List<Moeda> moedas) {
        cartaoDolar.preencher(moedas.get(0));
        cartaoEuro.preencher(moedas.get(1));
        cartaoBitcoin.preencher(moedas.get(2));
        status.setForeground(MUTED);
        status.setText("Atualizado em " + moedas.get(0).getAtualizadoEm());
    }

    // ============================================================
    //  CARTÃO DE UMA MOEDA (um pequeno componente reutilizável)
    // ============================================================
    private static class CartaoMoeda extends JPanel {
        private final JLabel lblValor     = new JLabel("—");
        private final JLabel lblVariacao  = new JLabel(" ");
        private final JLabel lblAltaBaixa = new JLabel(" ");

        CartaoMoeda(String nome, Color cor) {
            setLayout(new BorderLayout());
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createLineBorder(new Color(0xE3E7EF)));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

            // Faixa colorida na lateral esquerda.
            JPanel faixa = new JPanel();
            faixa.setBackground(cor);
            faixa.setPreferredSize(new Dimension(7, 0));
            add(faixa, BorderLayout.WEST);

            // Miolo: nome + valor + alta/baixa.
            JPanel miolo = new JPanel();
            miolo.setOpaque(false);
            miolo.setLayout(new BoxLayout(miolo, BoxLayout.Y_AXIS));
            miolo.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

            JLabel lblNome = new JLabel(nome);
            lblNome.setFont(new Font("SansSerif", Font.BOLD, 15));
            lblNome.setForeground(cor);

            lblValor.setFont(new Font("SansSerif", Font.BOLD, 26));
            lblValor.setForeground(INK);

            lblAltaBaixa.setFont(new Font("SansSerif", Font.PLAIN, 11));
            lblAltaBaixa.setForeground(MUTED);

            miolo.add(lblNome);
            miolo.add(Box.createVerticalStrut(4));
            miolo.add(lblValor);
            miolo.add(Box.createVerticalStrut(4));
            miolo.add(lblAltaBaixa);
            add(miolo, BorderLayout.CENTER);

            // Direita: a variação do dia (fica verde ou vermelha).
            lblVariacao.setFont(new Font("SansSerif", Font.BOLD, 15));
            lblVariacao.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 18));
            add(lblVariacao, BorderLayout.EAST);
        }

        void preencher(Moeda m) {
            lblValor.setText(REAIS.format(m.getValor()));

            // Variação: seta para cima/baixo + sinal + cor.
            String seta = m.subiu() ? "▲" : "▼"; // ▲ ou ▼
            String texto = String.format("%+.2f%%", m.getVariacao()).replace(".", ",");
            lblVariacao.setText(seta + " " + texto);
            lblVariacao.setForeground(m.subiu() ? SUBIU : CAIU);

            lblAltaBaixa.setText("Alta " + REAIS.format(m.getAlta())
                    + "  ·  Baixa " + REAIS.format(m.getBaixa()));
        }
    }

    /** Ponto de partida do programa. */
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignorado) {}
        SwingUtilities.invokeLater(() -> new AppCotacao().setVisible(true));
    }
}
