package br.com.unip.incendios.service;

import br.com.unip.incendios.model.*;
import br.com.unip.incendios.metrics.MetricasOrdenacao;
import br.com.unip.incendios.algorithms.AlgoritmosOrdenacao;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class FocoService {
    private final List<FocoIncendio> focos = new CopyOnWriteArrayList<>();
    private final AlgoritmosOrdenacao algoritmos = new AlgoritmosOrdenacao();

    public synchronized void substituirDados(List<FocoIncendio> novos) {
        focos.clear(); focos.addAll(novos);
    }
    public List<FocoIncendio> todos() { return new ArrayList<>(focos); }
    public int quantidade() { return focos.size(); }
    public ResultadoOrdenacao ordenar(AlgoritmoOrdenacao algoritmo, CriterioOrdenacao criterio, List<FocoIncendio> origem) {
        MetricasOrdenacao m = new MetricasOrdenacao();
        long inicio = System.nanoTime();
        List<FocoIncendio> resultado = algoritmos.ordenar(origem, algoritmo, criterio, m);
        long duracao = System.nanoTime() - inicio;
        return new ResultadoOrdenacao(algoritmo.name(), criterio.name(), resultado,
                m.getComparacoes(), m.getTrocas(), m.getMovimentacoes(), duracao);
    }
    public Map<String, ResultadoOrdenacao> comparar(CriterioOrdenacao criterio) {
        List<FocoIncendio> origem = todos();
        Map<String, ResultadoOrdenacao> resultados = new LinkedHashMap<>();
        for (AlgoritmoOrdenacao a : AlgoritmoOrdenacao.values())
            resultados.put(a.name(), ordenar(a, criterio, origem)); 
        return resultados;
    }
}
