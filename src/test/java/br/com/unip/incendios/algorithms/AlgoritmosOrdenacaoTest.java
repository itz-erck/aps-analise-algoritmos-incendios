package br.com.unip.incendios.algorithms;

import br.com.unip.incendios.metrics.MetricasOrdenacao;
import br.com.unip.incendios.model.*;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

// testes dos algoritmos   

class AlgoritmosOrdenacaoTest {
    private FocoIncendio foco(String id, String municipio, String bioma, int dia) {
        return new FocoIncendio(id,id,-10,-60,LocalDate.of(2024,1,dia),"Brasil","RO",municipio,bioma);
    }
    @Test void todosAlgoritmosOrdenamPorMunicipio() {
        List<FocoIncendio> entrada = List.of(foco("1","Vilhena","Cerrado",3), foco("2","Ariquemes","Amazonia",2), foco("3","Cacoal","Amazonia",1));
        AlgoritmosOrdenacao a = new AlgoritmosOrdenacao();
        for (AlgoritmoOrdenacao algoritmo : AlgoritmoOrdenacao.values()) {
            List<FocoIncendio> ordenados = a.ordenar(entrada, algoritmo, CriterioOrdenacao.MUNICIPIO, new MetricasOrdenacao());
            assertEquals(List.of("Ariquemes","Cacoal","Vilhena"), ordenados.stream().map(FocoIncendio::municipio).toList());
        }
        assertEquals("Vilhena", entrada.get(0).municipio(), "A lista original não deve ser alterada");
    }
    @Test void ordenaPorData() {
        List<FocoIncendio> entrada = List.of(foco("1","A","Amazonia",3), foco("2","B","Amazonia",1));
        List<FocoIncendio> r = new AlgoritmosOrdenacao().ordenar(entrada, AlgoritmoOrdenacao.MERGE, CriterioOrdenacao.DATA, new MetricasOrdenacao());
        assertEquals(1, r.get(0).data().getDayOfMonth());
    }
}
