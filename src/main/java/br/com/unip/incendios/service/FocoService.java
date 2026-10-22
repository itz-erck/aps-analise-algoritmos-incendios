package br.com.unip.incendios.service;

import br.com.unip.incendios.model.*;
import br.com.unip.incendios.metrics.MetricasOrdenacao;
import br.com.unip.incendios.algorithms.AlgoritmosOrdenacao;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class FocoService {
    private volatile List<FocoIncendio> focos = List.of();
    private final Map<String, ResultadoOrdenacao> cacheOrdenacoes = new ConcurrentHashMap<>();
    private final AlgoritmosOrdenacao algoritmos = new AlgoritmosOrdenacao();

    public synchronized void substituirDados(List<FocoIncendio> novos) {
        focos = List.copyOf(novos);
        cacheOrdenacoes.clear();
    }
    public List<FocoIncendio> todos() { return focos; }
    public int quantidade() { return focos.size(); }
    public ResultadoOrdenacao ordenar(AlgoritmoOrdenacao algoritmo, CriterioOrdenacao criterio, List<FocoIncendio> origem) {
        return ordenar(algoritmo, criterio, origem, false);
    }
    public ResultadoOrdenacao ordenar(AlgoritmoOrdenacao algoritmo, CriterioOrdenacao criterio, List<FocoIncendio> origem, boolean recalcular) {
        String chave = algoritmo.name() + ':' + criterio.name();
        ResultadoOrdenacao emCache = cacheOrdenacoes.get(chave);
        if (!recalcular && emCache != null) return emCache;
        MetricasOrdenacao m = new MetricasOrdenacao();
        long inicio = System.nanoTime();
        List<FocoIncendio> resultado = algoritmos.ordenar(origem, algoritmo, criterio, m);
        long duracao = System.nanoTime() - inicio;
        ResultadoOrdenacao novo = new ResultadoOrdenacao(algoritmo.name(), criterio.name(), List.copyOf(resultado),
                m.getComparacoes(), m.getTrocas(), m.getMovimentacoes(), duracao);
        cacheOrdenacoes.put(chave, novo);
        return novo;
    }
    public Map<String, ResultadoOrdenacao> comparar(CriterioOrdenacao criterio) {
        return comparar(criterio, false);
    }
    public Map<String, ResultadoOrdenacao> comparar(CriterioOrdenacao criterio, boolean recalcular) {
        List<FocoIncendio> origem = todos();
        Map<String, ResultadoOrdenacao> resultados = new LinkedHashMap<>();
        for (AlgoritmoOrdenacao a : AlgoritmoOrdenacao.values())
            resultados.put(a.name(), ordenar(a, criterio, origem, recalcular));
        return resultados;
    }
    public boolean possuiComparacao(CriterioOrdenacao criterio) {
        for (AlgoritmoOrdenacao algoritmo : AlgoritmoOrdenacao.values()) {
            if (!cacheOrdenacoes.containsKey(algoritmo.name() + ':' + criterio.name())) return false;
        }
        return true;
    }
}
