
package br.com.unip.incendios.importacao;

import br.com.unip.incendios.model.FocoIncendio;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
public class CsvImportService {

    public List<FocoIncendio> importar(MultipartFile arquivo) throws Exception {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new IllegalArgumentException("Selecione um arquivo CSV.");
        }

        List<FocoIncendio> focos = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(
                        arquivo.getInputStream(),
                        StandardCharsets.UTF_8))) {

            String cabecalho = br.readLine();

            if (cabecalho == null) {
                throw new IllegalArgumentException("O arquivo está vazio.");
            }

            String[] colunas = cabecalho
                    .replace("\uFEFF", "")
                    .split(",", -1);

            Map<String, Integer> idx = new HashMap<>();

            for (int i = 0; i < colunas.length; i++) {
                idx.put(colunas[i].trim().toLowerCase(Locale.ROOT), i);
            }

            String[] obrigatorias = {
                "id_bdq", "foco_id", "lat", "lon",
                "data_pas", "pais", "estado", "municipio", "bioma"
            };

            for (String coluna : obrigatorias) {
                if (!idx.containsKey(coluna)) {
                    throw new IllegalArgumentException(
                            "Coluna obrigatória ausente: " + coluna);
                }
            }

            String linha;
            int numero = 1;

            while ((linha = br.readLine()) != null) {
                numero++;

                if (linha.isBlank()) {
                    continue;
                }

                String[] v = linha.split(",", -1);

                try {
                    String dataTexto = valor(v, idx, "data_pas");
                    LocalDate data = parseData(dataTexto);

                    FocoIncendio foco = new FocoIncendio(
                            valor(v, idx, "id_bdq"),
                            valor(v, idx, "foco_id"),
                            Double.parseDouble(valor(v, idx, "lat")),
                            Double.parseDouble(valor(v, idx, "lon")),
                            data,
                            valor(v, idx, "pais"),
                            valor(v, idx, "estado"),
                            valor(v, idx, "municipio"),
                            valor(v, idx, "bioma")
                    );

                    focos.add(foco);

                } catch (Exception e) {
                    throw new IllegalArgumentException(
                            "Erro na linha " + numero + ": " + e.getMessage(),
                            e);
                }
            }
        }

        if (focos.isEmpty()) {
            throw new IllegalArgumentException(
                    "Nenhum registro válido foi encontrado.");
        }

        return focos;
    }

    private String valor(
            String[] v,
            Map<String, Integer> idx,
            String nome) {

        int i = idx.get(nome);

        if (i >= v.length) {
            throw new IllegalArgumentException(
                    "Quantidade de campos insuficiente.");
        }

        return v[i].trim();
    }

    private LocalDate parseData(String texto) {
        texto = texto.trim();

        // Formato: 2024-01-08
        try {
            return LocalDate.parse(
                    texto,
                    DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException ignored) {
        }

        // Formato: 2024-01-08 18:00:00
        try {
            return LocalDateTime.parse(
                    texto,
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            ).toLocalDate();
        } catch (DateTimeParseException ignored) {
        }

        // Formato ISO com T: 2024-01-08T18:00:00
        try {
            return LocalDateTime.parse(
                    texto,
                    DateTimeFormatter.ISO_LOCAL_DATE_TIME
            ).toLocalDate();
        } catch (DateTimeParseException ignored) {
        }

        throw new IllegalArgumentException(
                "data_pas inválida: " + texto
                + ". Formatos aceitos: yyyy-MM-dd ou yyyy-MM-dd HH:mm:ss.");
    }
}
