//A classe Moeda é o "molde" de uma cotação.

public class Moeda {

    private final String nome;
    private final double valor;
    private final double variacao;
    private final double alta;
    private final double baixa;
    private final String atualizadoEm;

    public Moeda(String nome, double valor, double variacao,
                 double alta, double baixa, String atualizadoEm) {
        this.nome = nome;
        this.valor = valor;
        this.variacao = variacao;
        this.alta = alta;
        this.baixa = baixa;
        this.atualizadoEm = atualizadoEm;
    }

    // --- GETTERS (só leitura, pois a classe é imutável) ---
    public String getNome()         { return nome; }
    public double getValor()        { return valor; }
    public double getVariacao()     { return variacao; }
    public double getAlta()         { return alta; }
    public double getBaixa()        { return baixa; }
    public String getAtualizadoEm() { return atualizadoEm; }

    /** Retorna true se a moeda subiu ou ficou estável no dia. */
    public boolean subiu() {
        return variacao >= 0;
    }
}
