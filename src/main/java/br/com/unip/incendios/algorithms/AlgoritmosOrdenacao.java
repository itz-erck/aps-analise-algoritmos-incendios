package br.com.unip.incendios.algorithms;

import br.com.unip.incendios.metrics.MetricasOrdenacao;
import br.com.unip.incendios.model.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.text.Normalizer;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.Map;

public class AlgoritmosOrdenacao {
    public List<FocoIncendio> ordenar(List<FocoIncendio> entrada, AlgoritmoOrdenacao algoritmo,
                                      CriterioOrdenacao criterio, MetricasOrdenacao m) {
        List<FocoIncendio> dados = new ArrayList<>(entrada);
        Comparator<FocoIncendio> comparador = criarComparador(dados, criterio, m);
        switch (algoritmo) {
            case BUBBLE -> bubble(dados, comparador, m);
            case SELECTION -> selection(dados, comparador, m);
            case INSERTION -> insertion(dados, comparador, m);
            case MERGE -> dados = mergeSort(dados, comparador, m);
            case QUICK -> quickSort(dados, 0, dados.size() - 1, comparador, m);
        }
        return dados;
    }

    private Comparator<FocoIncendio> criarComparador(List<FocoIncendio> dados, CriterioOrdenacao criterio, MetricasOrdenacao m) {
        Map<FocoIncendio, String> biomasNormalizados = new IdentityHashMap<>();
        Map<FocoIncendio, String> municipiosNormalizados = new IdentityHashMap<>();
        for (FocoIncendio foco : dados) {
            biomasNormalizados.put(foco, normalizar(foco.bioma()));
            municipiosNormalizados.put(foco, normalizar(foco.municipio()));
        }
        java.util.function.Function<FocoIncendio, String> bioma = biomasNormalizados::get;
        java.util.function.Function<FocoIncendio, String> municipio = municipiosNormalizados::get;
        Comparator<FocoIncendio> primario = switch (criterio) {
            case DATA -> Comparator.comparing(FocoIncendio::data);
            case BIOMA -> Comparator.comparing(bioma);
            case MUNICIPIO -> Comparator.comparing(municipio);
        };
        Comparator<FocoIncendio> desempate = switch (criterio) {
            case DATA -> Comparator.comparing(bioma).thenComparing(municipio);
            case BIOMA -> Comparator.comparing(FocoIncendio::data).thenComparing(municipio);
            case MUNICIPIO -> Comparator.comparing(FocoIncendio::data).thenComparing(bioma);
        };
        Comparator<FocoIncendio> completo = primario.thenComparing(desempate);
        return (a, b) -> { m.comparar(); return completo.compare(a, b); };
    }


    private String normalizar(String s) {
        if (s == null) return "";
        String semAcentos = Normalizer.normalize(s.trim(), Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return semAcentos.toLowerCase(Locale.ROOT);
    }

    private void bubble(List<FocoIncendio> a, Comparator<FocoIncendio> c, MetricasOrdenacao m) {
        for (int i = 0; i < a.size() - 1; i++) {
            boolean trocou = false;
            for (int j = 0; j < a.size() - i - 1; j++) {
                if (c.compare(a.get(j), a.get(j + 1)) > 0) {
                    swap(a, j, j + 1, m); trocou = true;
                }
            }
            if (!trocou) break;
        }
    }
    private void selection(List<FocoIncendio> a, Comparator<FocoIncendio> c, MetricasOrdenacao m) {
        for (int i = 0; i < a.size() - 1; i++) {
            int min = i;
            for (int j = i + 1; j < a.size(); j++)
                if (c.compare(a.get(j), a.get(min)) < 0) min = j;
            if (min != i) swap(a, i, min, m);
        }
    }
    private void insertion(List<FocoIncendio> a, Comparator<FocoIncendio> c, MetricasOrdenacao m) {
        for (int i = 1; i < a.size(); i++) {
            FocoIncendio chave = a.get(i); m.mover(1);
            int j = i - 1;
            while (j >= 0 && c.compare(a.get(j), chave) > 0) {
                a.set(j + 1, a.get(j)); m.mover(1); j--;
            }
            a.set(j + 1, chave); m.mover(1);
        }
    }
    private List<FocoIncendio> mergeSort(List<FocoIncendio> a, Comparator<FocoIncendio> c, MetricasOrdenacao m) {
        if (a.size() <= 1) return a;
        int meio = a.size() / 2;
        List<FocoIncendio> esq = mergeSort(new ArrayList<>(a.subList(0, meio)), c, m);
        List<FocoIncendio> dir = mergeSort(new ArrayList<>(a.subList(meio, a.size())), c, m);
        List<FocoIncendio> out = new ArrayList<>(a.size());
        int i = 0, j = 0;
        while (i < esq.size() && j < dir.size()) {
            if (c.compare(esq.get(i), dir.get(j)) <= 0) out.add(esq.get(i++));
            else out.add(dir.get(j++));
            m.mover(1);
        }
        while (i < esq.size()) { out.add(esq.get(i++)); m.mover(1); }
        while (j < dir.size()) { out.add(dir.get(j++)); m.mover(1); }
        return out;
    }
    private void quickSort(List<FocoIncendio> a, int baixo, int alto, Comparator<FocoIncendio> c, MetricasOrdenacao m) {
        if (baixo >= alto) return;
        int p = partition(a, baixo, alto, c, m);
        quickSort(a, baixo, p - 1, c, m);
        quickSort(a, p + 1, alto, c, m);
    }
    private int partition(List<FocoIncendio> a, int baixo, int alto, Comparator<FocoIncendio> c, MetricasOrdenacao m) {
        FocoIncendio pivo = a.get(alto); int i = baixo - 1;
        for (int j = baixo; j < alto; j++) {
            if (c.compare(a.get(j), pivo) <= 0) { i++; if (i != j) swap(a, i, j, m); }
        }
        if (i + 1 != alto) swap(a, i + 1, alto, m);
        return i + 1;
    }
    private void swap(List<FocoIncendio> a, int i, int j, MetricasOrdenacao m) {
        FocoIncendio temp = a.get(i); a.set(i, a.get(j)); a.set(j, temp); m.trocar();
    }
}
