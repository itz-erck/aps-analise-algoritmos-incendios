
package br.com.unip.incendios.service;

import br.com.unip.incendios.model.FocoIncendio;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class PrevisaoService {

    private static final int MESES_PREVISAO = 6;

    private static final DateTimeFormatter FORMATO_MES =
            DateTimeFormatter.ofPattern("MM/yyyy");

    public Map<String, Object> analisar(List<FocoIncendio> focos) {

        Map<String, Object> resultado = new LinkedHashMap<>();

        Map<YearMonth, Long> contagem = new TreeMap<>();

        for (FocoIncendio foco : focos) {
            if (foco.data() == null) {
                continue;
            }

            int ano = foco.data().getYear();

            if (ano != 2023 && ano != 2024) {
                continue;
            }

            YearMonth mes = YearMonth.from(foco.data());
            contagem.merge(mes, 1L, Long::sum);
        }

        List<Map<String, Object>> historico = new ArrayList<>();
        List<Map<String, Object>> previsoes = new ArrayList<>();

        resultado.put("historicoMensal", historico);
        resultado.put("previsoes", previsoes);
        resultado.put("temDados", !contagem.isEmpty());
        resultado.put("modeloDisponivel", false);
        resultado.put("totalHistorico", 0L);
        resultado.put("quantidadeMeses", 0);
        resultado.put("mesesTreino", 0);
        resultado.put("mesesTeste", 0);
        resultado.put("mae", null);
        resultado.put("rmse", null);

        if (contagem.isEmpty()) {
            resultado.put(
                    "mensagem",
                    "Importe os CSVs de 2023 e 2024 para analisar os dados."
            );
            return resultado;
        }

        // Completa os meses sem registros com quantidade zero.
        YearMonth primeiroMes = contagem.keySet().iterator().next();
        YearMonth ultimoMes = ((TreeMap<YearMonth, Long>) contagem).lastKey();

        List<Long> valores = new ArrayList<>();
        YearMonth mesAtual = primeiroMes;

        while (!mesAtual.isAfter(ultimoMes)) {
            long quantidade = contagem.getOrDefault(mesAtual, 0L);
            valores.add(quantidade);

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("mes", mesAtual.format(FORMATO_MES));
            item.put("ano", mesAtual.getYear());
            item.put("quantidade", quantidade);

            historico.add(item);
            mesAtual = mesAtual.plusMonths(1);
        }

        long totalHistorico = valores.stream()
                .mapToLong(Long::longValue)
                .sum();

        resultado.put("totalHistorico", totalHistorico);
        resultado.put("quantidadeMeses", valores.size());

        // Precisamos de pelo menos quatro meses para separar
        // treinamento e teste com uma quantidade mínima de dados.
        if (valores.size() < 4) {
            resultado.put(
                    "mensagem",
                    "São necessários pelo menos quatro meses de dados para avaliar o modelo."
            );
            return resultado;
        }

        int quantidadeTeste = Math.max(1, valores.size() / 4);
        int quantidadeTreino = valores.size() - quantidadeTeste;

        double[] xTreino = new double[quantidadeTreino];
        double[] yTreino = new double[quantidadeTreino];

        for (int i = 0; i < quantidadeTreino; i++) {
            xTreino[i] = i;
            yTreino[i] = valores.get(i);
        }

        // Treina usando apenas a parte inicial do histórico.
        Modelo modeloTeste = treinar(xTreino, yTreino);

        double somaErroAbsoluto = 0;
        double somaErroQuadratico = 0;

        for (int i = quantidadeTreino; i < valores.size(); i++) {
            double previsto = prever(modeloTeste, i);
            double real = valores.get(i);
            double erro = real - previsto;

            somaErroAbsoluto += Math.abs(erro);
            somaErroQuadratico += erro * erro;
        }

        double mae = somaErroAbsoluto / quantidadeTeste;
        double rmse = Math.sqrt(
                somaErroQuadratico / quantidadeTeste
        );

        resultado.put("mesesTreino", quantidadeTreino);
        resultado.put("mesesTeste", quantidadeTeste);
        resultado.put("mae", arredondar(mae));
        resultado.put("rmse", arredondar(rmse));
        resultado.put("modeloDisponivel", true);

        // Após avaliar o modelo, treina novamente usando
        // todo o histórico disponível para gerar as previsões.
        double[] xCompleto = new double[valores.size()];
        double[] yCompleto = new double[valores.size()];

        for (int i = 0; i < valores.size(); i++) {
            xCompleto[i] = i;
            yCompleto[i] = valores.get(i);
        }

        Modelo modeloFinal = treinar(xCompleto, yCompleto);

        YearMonth proximoMes = ultimoMes.plusMonths(1);

        for (int i = 0; i < MESES_PREVISAO; i++) {
            YearMonth mesPrevisto = proximoMes.plusMonths(i);

            double valorCalculado = prever(
                    modeloFinal,
                    valores.size() + i
            );

            long quantidadePrevista = Math.round(
                    Math.max(0, valorCalculado)
            );

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("mes", mesPrevisto.format(FORMATO_MES));
            item.put("ano", mesPrevisto.getYear());
            item.put("quantidade", quantidadePrevista);

            previsoes.add(item);
        }

        resultado.put(
                "mensagem",
                "Modelo avaliado em dados separados do treinamento. "
                        + "As previsões são estimativas e podem apresentar erros."
        );

        return resultado;
    }

    private Modelo treinar(double[] x, double[] y) {

        double mediaX = Arrays.stream(x).average().orElse(0);
        double mediaY = Arrays.stream(y).average().orElse(0);

        double numerador = 0;
        double denominador = 0;

        for (int i = 0; i < x.length; i++) {
            numerador += (x[i] - mediaX) * (y[i] - mediaY);
            denominador += (x[i] - mediaX) * (x[i] - mediaX);
        }

        double inclinacao = denominador == 0
                ? 0
                : numerador / denominador;

        double intercepto = mediaY - inclinacao * mediaX;

        return new Modelo(inclinacao, intercepto);
    }

    private double prever(Modelo modelo, double x) {
        return modelo.inclinacao() * x + modelo.intercepto();
    }

    private double arredondar(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }

    private record Modelo(double inclinacao, double intercepto) {
    }
}
