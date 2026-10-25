// Ferramentas nativas do Java para fazer requisições HTTP (não precisa de biblioteca externa!).
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * CotacaoService — a "camada de serviço" do app.
 * API usada: https://docs.awesomeapi.com.br/api-de-moedas (não exige cadastro)
 */
public class CotacaoService {

    // Pede Dólar (USD), Euro (EUR) e Bitcoin (BTC), todos convertidos para Real (BRL).
    private static final String URL =
            "https://economia.awesomeapi.com.br/last/USD-BRL,EUR-BRL,BTC-BRL";

    // O "cliente" HTTP que faz as requisições (reutilizado em todas as chamadas).
    private final HttpClient cliente = HttpClient.newHttpClient();

    public List<Moeda> buscarCotacoes() throws Exception {
        HttpRequest requisicao = HttpRequest.newBuilder()
                .uri(URI.create(URL))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        HttpResponse<String> resposta =
                cliente.send(requisicao, HttpResponse.BodyHandlers.ofString());

        if (resposta.statusCode() != 200) {
            throw new RuntimeException("A API respondeu com status " + resposta.statusCode());
        }

        // Transforma o texto JSON recebido em objetos Moeda.
        return interpretar(resposta.body());
    }

    List<Moeda> interpretar(String json) {
        List<Moeda> lista = new ArrayList<>();
        lista.add(lerMoeda(json, "USDBRL", "Dólar"));
        lista.add(lerMoeda(json, "EURBRL", "Euro"));
        lista.add(lerMoeda(json, "BTCBRL", "Bitcoin"));
        return lista;
    }

    /** Isola o trecho de uma moeda no JSON e lê os campos que interessam. */
    private Moeda lerMoeda(String json, String chave, String apelido) {
        // Pega só o "pedaço" daquela moeda, que fica entre { e } logo após a chave.
        String bloco = entre(json, "\"" + chave + "\":{", "}");

        double valor    = Double.parseDouble(campo(bloco, "bid"));
        double variacao = Double.parseDouble(campo(bloco, "pctChange"));
        double alta     = Double.parseDouble(campo(bloco, "high"));
        double baixa    = Double.parseDouble(campo(bloco, "low"));
        String data     = campo(bloco, "create_date");

        return new Moeda(apelido, valor, variacao, alta, baixa, data);
    }

    /** Devolve o texto que está ENTRE dois marcadores. */
    private String entre(String texto, String ini, String fim) {
        int a = texto.indexOf(ini);
        if (a < 0) throw new RuntimeException("Não encontrei no JSON: " + ini);
        a += ini.length();
        int b = texto.indexOf(fim, a);
        return texto.substring(a, b);
    }

    private String campo(String bloco, String nome) {
        String marca = "\"" + nome + "\":\"";
        int a = bloco.indexOf(marca) + marca.length();
        int b = bloco.indexOf("\"", a);
        return bloco.substring(a, b);
    }
}
