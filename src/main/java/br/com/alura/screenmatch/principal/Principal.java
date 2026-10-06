package br.com.alura.screenmatch.principal;

import br.com.alura.screenmatch.model.DadosSerie;
import br.com.alura.screenmatch.model.DadosTemporada;
import br.com.alura.screenmatch.model.Episodio;
import br.com.alura.screenmatch.service.ConsumoAPI;
import br.com.alura.screenmatch.service.ConverteDados;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Principal {

    private final Scanner leitura = new Scanner(System.in);
    private final ConsumoAPI consumoAPI = new ConsumoAPI();
    private final ConverteDados conversor = new ConverteDados();

    private static final String OMDB_API_URL = "http://www.omdbapi.com/";
    private static final Logger logger = Logger.getLogger(Principal.class.getName());

    public void exibeMenu(String omdbapiKey) {
        logger.info("Bem-vindo ao ScreenMatch!");
        logger.info("Digite o nome da série para buscar informações sobre ela:");
        String nomeSerie = leitura.nextLine();

        var json = consumoAPI.obterDados(OMDB_API_URL + "?t=" + nomeSerie.replace(" ", "+") + "&apikey=" + omdbapiKey);
        logger.info(json);

        DadosSerie serie = conversor.obterDados(json, DadosSerie.class);

        logger.info("Título: " + serie.title());
        logger.info("Total de temporadas: " + serie.totalSeasons());
        logger.info("Ano de lançamento: " + serie.year());
        logger.info("Escrito por: " + serie.writer());
        logger.info("Nota do IMDB: " + serie.imdbRating());
        logger.info("Sinopse: " + serie.plot());

        List<DadosTemporada> temporadas = new ArrayList<>();

        for (int i = 1; i <= serie.totalSeasons(); i++) {
            json = consumoAPI.obterDados(OMDB_API_URL + "?t=" + nomeSerie.replace(" ", "+") + "&Season=" + i + "&apikey=" + omdbapiKey);
            var temporada = conversor.obterDados(json, DadosTemporada.class);
            temporadas.add(temporada);
        }

        List<Episodio> episodios = temporadas.stream()
                .map(dadosTemporada -> dadosTemporada.episodios().stream()
                        .map(dadosEpisodio -> new Episodio(dadosTemporada.numero(), dadosEpisodio)).toList())
                .flatMap(List::stream).toList();

        var itemMenu = 0;
        do {
            logger.info("");
            logger.info("Menu:");
            logger.info("1 - Exibir episódios");
            logger.info("2 - Exibir episódios com melhor avaliação");
            logger.info("3 - Exibir episódios a partir de uma data");
            logger.info("4 - Exibir episódio por título");
            logger.info("5 - Media de avaliações por temporada");
            logger.info("6 - Exibir estatísticas da série");
            logger.info("0 - Sair");
            try {
                itemMenu = leitura.nextInt();
            } catch (InputMismatchException _) {
                logger.warning("Entrada inválida. Por favor, digite um número.");
                leitura.nextLine(); // Limpa o buffer do scanner
                continue; // Volta para o início do loop
            }
            leitura.nextLine(); // Limpa o buffer do scanner

            switch (itemMenu) {
                case 1 -> exibirEpisodios(episodios);
                case 2 -> exibirTop5EpisodiosPorAvaliacao(episodios);
                case 3 -> exibirEpisodiosAPartirDaDataInformada(episodios);
                case 4 -> exibirEpisodioPorTitulo(episodios);
                case 5 -> calcularMediaDasAvaliacoesDeCadaTemporada(temporadas);
                case 6 -> exibirEstatisticas(episodios);
                case 0 -> logger.info("Saindo...");
                default -> logger.warning("Opção inválida. Tente novamente.");
            }
        } while (itemMenu != 0);

    }

    private static void exibirEstatisticas(List<Episodio> episodios) {
        DoubleSummaryStatistics est = episodios.stream()
                .filter(e -> e.getAvaliacao() > 0.0)
                .collect(Collectors.summarizingDouble(Episodio::getAvaliacao));
        logger.info("");
        logger.info("Estatísticas da série:");
        logger.info("Média de avaliação: " + est.getAverage());
        logger.info("Maior avaliação: " + est.getMax());
        logger.info("Menor avaliação: " + est.getMin());
        logger.info("Total de episódios: " + est.getCount());
    }

    private static void calcularMediaDasAvaliacoesDeCadaTemporada(List<DadosTemporada> temporadas) {
        logger.info("");
        logger.info("Média de avaliações por temporada:");
        Map<Integer, Double> mediaAvaliacoesPorTemporada = new HashMap<>();
        for (DadosTemporada temporada : temporadas) {
            double media = temporada.episodios().stream()
                    .mapToDouble(dadosEpisodio -> {
                        try {
                            return Double.parseDouble(dadosEpisodio.avaliacao());
                        } catch (NumberFormatException _) {
                            return 0.0; // Caso a avaliação não seja um número válido
                        }
                    })
                    .average()
                    .orElse(0.0);
            mediaAvaliacoesPorTemporada.put(temporada.numero(), media);
        }
        mediaAvaliacoesPorTemporada.forEach((numero, media) ->
                logger.info("Temporada " + numero + ": " + String.format("%.2f", media)));
    }

    private void exibirEpisodioPorTitulo(List<Episodio> episodios) {
        logger.info("");
        logger.info("Digite o título do episódio que deseja buscar:");
        String titulo = leitura.nextLine();
        var resultadoBusca = episodios.stream()
                .filter(episodio -> episodio.getTitulo() != null && episodio.getTitulo().toUpperCase().contains(titulo.toUpperCase()))
                .findFirst();

        if (resultadoBusca.isPresent()) {
            logger.info(resultadoBusca.get().toString());
        } else {
            logger.warning("Episódio não encontrado.");
        }
    }

    private void exibirEpisodiosAPartirDaDataInformada(List<Episodio> episodios) {
        logger.info("");
        logger.info("Digite uma data (no formato YYYY-MM-DD) para exibir os episódios lançados a partir dela:");
        String dataInput = leitura.nextLine();
        episodios.stream()
                .filter(episodio -> episodio.getDataLancamento() != null && episodio.getDataLancamento().isAfter(java.time.LocalDate.parse(dataInput)))
                .sorted(Comparator.comparing(Episodio::getDataLancamento))
                .forEach(IO::println);
    }

    private static void exibirTop5EpisodiosPorAvaliacao(List<Episodio> episodios) {
        logger.info("");
        logger.info("Top 5 episódios com melhor avaliação:");
        episodios.stream()
                .sorted(Comparator.comparing(Episodio::getAvaliacao).reversed())
                .limit(5).forEach(episodio -> logger.info(episodio.toString()));
    }

    private static void exibirEpisodios(List<Episodio> episodios) {
        logger.info("");
        logger.info("Episódios da série:");
        episodios.forEach(episodio -> logger.info(episodio.toString()));
    }
}
