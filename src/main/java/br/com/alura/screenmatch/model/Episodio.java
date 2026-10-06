package br.com.alura.screenmatch.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Episodio {
    private Integer temporada;
    private String titulo;
    private Integer numero;
    private Double avaliacao;
    private LocalDate dataLancamento;

    public Episodio(Integer temporada, DadosEpisodio dadosEpisodio) {
        this.temporada = temporada;
        this.titulo = dadosEpisodio.titulo();
        this.numero = dadosEpisodio.numero();
        this.avaliacao = dadosEpisodio.avaliacao().equals("N/A") ? 0.0 : Double.parseDouble(dadosEpisodio.avaliacao());
        this.dataLancamento = dadosEpisodio.dataLancamento().equals("N/A") ? null : LocalDate.parse(dadosEpisodio.dataLancamento());
    }

    public Integer getTemporada() {
        return temporada;
    }

    public String getTitulo() {
        return titulo;
    }

    public Integer getNumero() {
        return numero;
    }

    public Double getAvaliacao() {
        return avaliacao;
    }

    public LocalDate getDataLancamento() {
        return dataLancamento;
    }

    @Override
    public String toString() {
        return "Episodio{" +
                "temporada=" + getTemporada() +
                ", titulo='" + getTitulo() + '\'' +
                ", numero=" + getNumero() +
                ", avaliacao=" + getAvaliacao() +
                ", dataLancamento=" + (getDataLancamento() != null ?
                    getDataLancamento().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) : "N/A") +
                '}';
    }
}
