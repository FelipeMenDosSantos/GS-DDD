import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import javax.swing.JOptionPane;
import com.google.gson.Gson;



public class App {

    /**
     * Classe principal responsável por interagir com o usuário, requisitar dados
     * da API e calcular o risco de incêndio.
     */
    public static void main(String[] args) {
        while (true) {
            String cidade = JOptionPane.showInputDialog("Digite o nome da cidade para monitorar:\n(Clique em Cancelar ou deixe em branco para sair)");

            if (cidade == null || cidade.trim().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Programa encerrado.");
                break;
            }

            ClimaResponse clima = ClimaService.getClima(cidade);

            if (clima != null) {
                StringBuilder mensagem = new StringBuilder();
                mensagem.append("=== Dados Climáticos de ").append(clima.name).append(" ===\n");
                mensagem.append("Temperatura: ").append(clima.main.temp).append(" °C\n");
                mensagem.append("Umidade: ").append(clima.main.humidity).append(" %\n");
                mensagem.append("Vento: ").append(clima.wind.speed).append(" m/s\n");

                AreaMonitorada area = new AreaMonitorada(clima.name, clima.main.temp, clima.main.humidity);
                boolean risco = area.calcularRisco();

                mensagem.append("Risco de Incêndio: ").append(risco ? "ALTO 🚨" : "Baixo ✅");

                JOptionPane.showMessageDialog(null, mensagem.toString());
            } else {
                JOptionPane.showMessageDialog(null, "Erro ao obter os dados climáticos.");
            }
        }
    }
}

class ClimaService {

    private static final String API_KEY = "3408f51d92849729cf6af7eee9e5f850";

    /**
     * Realiza uma requisição à API OpenWeatherMap para obter os dados climáticos da cidade.
     *
     * @param cidade Nome da cidade a ser consultada.
     * @return Objeto ClimaResponse com os dados climáticos, ou null se falhar.
     */
    public static ClimaResponse getClima(String cidade) {
        try {
            String cidadeCodificada = URLEncoder.encode(cidade, "UTF-8");
            String urlStr = "https://api.openweathermap.org/data/2.5/weather?q=" + cidadeCodificada + "&appid=" + API_KEY + "&units=metric";
            URL url = new URL(urlStr);
            HttpURLConnection conexao = (HttpURLConnection) url.openConnection();
            conexao.setRequestMethod("GET");

            BufferedReader leitor = new BufferedReader(new InputStreamReader(conexao.getInputStream()));
            StringBuilder resposta = new StringBuilder();
            String linha;
            while ((linha = leitor.readLine()) != null) {
                resposta.append(linha);
            }
            leitor.close();

            Gson gson = new Gson();
            return gson.fromJson(resposta.toString(), ClimaResponse.class);

        } catch (Exception e) {
            System.out.println("Erro ao acessar a API: " + e.getMessage());
            return null;
        }
    }
}

// Classes auxiliares para deserializar o JSON
class ClimaResponse {
    Main main;
    Wind wind;
    String name;
}

class Main {
    double temp;
    double humidity;
}

class Wind {
    double speed;
}

/**
 * Classe abstrata base que representa uma região que pode ser monitorada.
 */
abstract class Regiao {
    protected String nome;

    /**
     * Método abstrato que calcula o risco com base em critérios da subclasse.
     *
     * @return true se o risco for alto, false se for baixo.
     */
    public abstract boolean calcularRisco();
}

/**
 * Representa uma área monitorada com dados climáticos.
 * Estende a classe Regiao e implementa o cálculo de risco.
 */
class AreaMonitorada extends Regiao {
    private double temperatura;
    private double umidade;

    public AreaMonitorada(String nome, double temperatura, double umidade) {
        this.nome = nome;
        this.temperatura = temperatura;
        this.umidade = umidade;
    }

    /**
     * Sobrescreve o método da superclasse para calcular o risco de incêndio com base nos limites padrão.
     *
     * @return true se a temperatura for maior que 30°C e umidade menor que 40%.
     */
    @Override
    public boolean calcularRisco() {
        return temperatura > 30.0 && umidade < 40.0;
    }

    /**
     * Sobrecarga do método calcularRisco para permitir definir limites personalizados.
     *
     * @param limiteTemp Temperatura limite para risco alto.
     * @param limiteUmidade Umidade limite para risco alto.
     * @return true se os critérios personalizados forem atendidos.
     */
    public boolean calcularRisco(double limiteTemp, double limiteUmidade) {
        return temperatura > limiteTemp && umidade < limiteUmidade;
    }

    /**
     * Calcula uma pontuação de risco numérica com base em temperatura e umidade.
     *
     * @return Valor de 0 a 100 indicando o risco estimado.
     */
    public int calcularPontuacaoRisco() {
        int riscoTemp = (int) Math.min(100, temperatura * 2);
        int riscoUmidade = (int) Math.max(0, 100 - umidade);
        return (riscoTemp + riscoUmidade) / 2;
    }
}
