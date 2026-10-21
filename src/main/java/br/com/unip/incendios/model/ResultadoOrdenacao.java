package br.com.unip.incendios.model;

import java.util.List;

public record ResultadoOrdenacao(
        String algoritmo,
        String criterio,
        List<FocoIncendio> dados,
        long comparacoes,
        long trocas,
        long movimentacoes,
        long tempoNanos
) {
    public double tempoMillis() { return tempoNanos / 1_000_000.0; }
}
