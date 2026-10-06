package br.com.alura.screenmatch.principal;

import br.com.alura.screenmatch.model.DadosEpisodio;
import br.com.alura.screenmatch.model.DadosSerie;
import br.com.alura.screenmatch.model.DadosTemporada;
import br.com.alura.screenmatch.model.Episodio;
import br.com.alura.screenmatch.service.ConsumoAPI;
import br.com.alura.screenmatch.service.ConverteDados;

import java.util.*;

public class Principal {

    private Scanner leitura = new Scanner(System.in);
    private ConsumoAPI consumoAPI = new ConsumoAPI();
    private ConverteDados conversor = new ConverteDados();

    private final String OMDBAPI_URL = "http://www.omdbapi.com/";

    public void exibeMenu(String omdbapiKey) {
        IO.println("Bem-vindo ao ScreenMatch!");
        IO.println("Digite o nome da série para buscar informações sobre ela:");
        String nomeSerie = leitura.nextLine();

        var json = consumoAPI.obterDados(OMDBAPI_URL + "?t=" + nomeSerie.replace(" ", "+") + "&apikey=" + omdbapiKey);
        IO.println(json);

        DadosSerie serie = conversor.obterDados(json, DadosSerie.class);

        IO.println("Título: " + serie.title());
        IO.println("Total de temporadas: " + serie.totalSeasons());
        IO.println("Ano de lançamento: " + serie.year());
        IO.println("Escrito por: " + serie.writer());
        IO.println("Nota do IMDB: " + serie.imdbRating());
        IO.println("Sinopse: " + serie.plot());

        List<DadosTemporada> temporadas = new ArrayList<>();

        for (int i = 1; i <= serie.totalSeasons(); i++) {
            json = consumoAPI.obterDados(OMDBAPI_URL + "?t=" + nomeSerie.replace(" ", "+") + "&Season=" + i + "&apikey=" + omdbapiKey);
            var temporada = new ConverteDados().obterDados(json, DadosTemporada.class);
            temporadas.add(temporada);
        }

        List<Episodio> episodios = temporadas.stream()
                .map(dadosTemporada -> dadosTemporada.episodios().stream()
                        .map(dadosEpisodio -> new Episodio(dadosTemporada.numero(), dadosEpisodio)).toList())
                .flatMap(List::stream).toList();

        var itemMenu = 0;
        do {
            IO.println();
            IO.println("Menu:");
            IO.println("1 - Exibir episódios");
            IO.println("2 - Exibir episódios com melhor avaliação");
            IO.println("3 - Exibir episódios a partir de uma data");
            IO.println("4 - Exibir episódio por título");
            IO.println("0 - Sair");
            try {
                itemMenu = leitura.nextInt();
            } catch (InputMismatchException _) {
                IO.println("Entrada inválida. Por favor, digite um número.");
                leitura.nextLine(); // Limpa o buffer do scanner
                continue; // Volta para o início do loop
            }
            leitura.nextLine(); // Limpa o buffer do scanner

            switch (itemMenu) {
                case 1 -> exibirEpisodios(episodios);
                case 2 -> exibirTop5EpisodiosPorAvaliacao(episodios);
                case 3 -> exibirEpisodiosAPartirDaDataInformada(episodios);
                case 4 -> exibirEpisodioPorTitulo(episodios);
                case 0 -> IO.println("Saindo...");
                default -> IO.println("Opção inválida. Tente novamente.");
            }
        } while (itemMenu != 0);

    }

    private void exibirEpisodioPorTitulo(List<Episodio> episodios) {
        IO.println();
        IO.println("Digite o título do episódio que deseja buscar:");
        String titulo = leitura.nextLine();
        var resultadoBusca = episodios.stream()
                .filter(episodio -> episodio.getTitulo() != null && episodio.getTitulo().toUpperCase().contains(titulo.toUpperCase()))
                .findFirst();

        if (resultadoBusca.isPresent()) {
            IO.println(resultadoBusca.get());
        } else {
            IO.println("Episódio não encontrado.");
        }
    }

    private void exibirEpisodiosAPartirDaDataInformada(List<Episodio> episodios) {
        IO.println();
        IO.println("Digite uma data (no formato YYYY-MM-DD) para exibir os episódios lançados a partir dela:");
        String dataInput = leitura.nextLine();
        episodios.stream()
                .filter(episodio -> episodio.getDataLancamento() != null && episodio.getDataLancamento().isAfter(java.time.LocalDate.parse(dataInput)))
                .sorted(Comparator.comparing(Episodio::getDataLancamento))
                .forEach(IO::println);
    }

    private static void exibirTop5EpisodiosPorAvaliacao(List<Episodio> episodios) {
        IO.println();
        IO.println("Top 5 episódios com melhor avaliação:");
        episodios.stream()
                .sorted(Comparator.comparing(Episodio::getAvaliacao).reversed())
                .limit(5).forEach(IO::println);
    }

    private static void exibirEpisodios(List<Episodio> episodios) {
        IO.println();
        IO.println("Episódios da série:");
        episodios.forEach(IO::println);
    }
}
