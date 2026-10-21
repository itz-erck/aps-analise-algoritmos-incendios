package br.com.unip.incendios.metrics;

public class MetricasOrdenacao {
    private long comparacoes;
    private long trocas;
    private long movimentacoes;

    public void comparar() { comparacoes++; }
    public void trocar() { trocas++; movimentacoes += 2; }
    public void mover(long quantidade) { movimentacoes += quantidade; }
    public long getComparacoes() { return comparacoes; }
    public long getTrocas() { return trocas; }
    public long getMovimentacoes() { return movimentacoes; }
}
