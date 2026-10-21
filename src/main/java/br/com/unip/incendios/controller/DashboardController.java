
package br.com.unip.incendios.controller;

import br.com.unip.incendios.importacao.CsvImportService;
import br.com.unip.incendios.model.*;
import br.com.unip.incendios.service.FocoService;
import br.com.unip.incendios.service.PrevisaoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Controller
public class DashboardController {

    private final FocoService focoService;
    private final CsvImportService csvImportService;
    private final PrevisaoService previsaoService;

    public DashboardController(
            FocoService focoService,
            CsvImportService csvImportService,
            PrevisaoService previsaoService) {

        this.focoService = focoService;
        this.csvImportService = csvImportService;
        this.previsaoService = previsaoService;
    }

    @GetMapping("/")
    public String inicio(Model model) {
        List<FocoIncendio> dados = focoService.todos();

        long total2023 = dados.stream()
                .filter(f -> f.data() != null
                        && f.data().getYear() == 2023)
                .count();

        long total2024 = dados.stream()
                .filter(f -> f.data() != null
                        && f.data().getYear() == 2024)
                .count();

        long totalBiomas = dados.stream()
                .map(FocoIncendio::bioma)
                .filter(b -> b != null && !b.isBlank())
                .map(String::trim)
                .distinct()
                .count();

        long totalMunicipios = dados.stream()
                .map(FocoIncendio::municipio)
                .filter(m -> m != null && !m.isBlank())
                .map(String::trim)
                .distinct()
                .count();

        long maiorTotal = Math.max(total2023, total2024);

        int largura2023 = maiorTotal == 0
                ? 0
                : (int) Math.round(total2023 * 100.0 / maiorTotal);

        int largura2024 = maiorTotal == 0
                ? 0
                : (int) Math.round(total2024 * 100.0 / maiorTotal);

        model.addAttribute("total", dados.size());
        model.addAttribute("total2023", total2023);
        model.addAttribute("total2024", total2024);
        model.addAttribute("totalBiomas", totalBiomas);
        model.addAttribute("totalMunicipios", totalMunicipios);
        model.addAttribute("largura2023", largura2023);
        model.addAttribute("largura2024", largura2024);

        return "inicio";
    }

    @GetMapping("/ordenacao")
    public String ordenar(
            @RequestParam(defaultValue = "DATA")
            CriterioOrdenacao criterio,
            @RequestParam(defaultValue = "MERGE")
            AlgoritmoOrdenacao algoritmo,
            @RequestParam(defaultValue = "0")
            int pagina,
            Model model) {

        List<FocoIncendio> todos = focoService.todos();

        ResultadoOrdenacao resultado =
                focoService.ordenar(algoritmo, criterio, todos);

        int tamanhoPagina = 50;

        int totalPaginas = Math.max(
                1,
                (int) Math.ceil(
                        resultado.dados().size()
                                / (double) tamanhoPagina)
        );

        int paginaSegura = Math.min(
                Math.max(0, pagina),
                totalPaginas - 1
        );

        int inicio = paginaSegura * tamanhoPagina;

        int fim = Math.min(
                inicio + tamanhoPagina,
                resultado.dados().size()
        );

        model.addAttribute(
                "dados",
                resultado.dados().subList(inicio, fim)
        );
        model.addAttribute("total", todos.size());
        model.addAttribute("criterio", criterio);
        model.addAttribute("algoritmo", algoritmo);
        model.addAttribute("resultado", resultado);
        model.addAttribute("pagina", paginaSegura);
        model.addAttribute("totalPaginas", totalPaginas);
        model.addAttribute("algoritmos", AlgoritmoOrdenacao.values());
        model.addAttribute("criterios", CriterioOrdenacao.values());

        return "ordenacao";
    }

    @GetMapping("/importar")
    public String paginaImportar(Model model) {
        model.addAttribute("total", focoService.todos().size());
        return "importar";
    }

    @GetMapping("/ia/previsao")
    public String previsao(Model model) {
        model.addAllAttributes(
                previsaoService.analisar(focoService.todos())
        );

        return "ml";
    }

    @GetMapping("/ia/classificacao")
    public String classificacao() {
        return "dl";
    }

    @GetMapping("/sobre")
    public String sobre() {
        return "sobre";
    }

    @PostMapping("/importar")
    public String importar(
            @RequestParam("arquivos") MultipartFile[] arquivos,
            RedirectAttributes redirect) {

        try {
            if (arquivos == null || arquivos.length == 0) {
                throw new IllegalArgumentException(
                        "Selecione pelo menos um arquivo CSV.");
            }

            List<FocoIncendio> todosDados = new ArrayList<>();
            int arquivosImportados = 0;

            for (MultipartFile arquivo : arquivos) {
                if (arquivo == null || arquivo.isEmpty()) {
                    continue;
                }

                List<FocoIncendio> dadosArquivo =
                        csvImportService.importar(arquivo);

                todosDados.addAll(dadosArquivo);
                arquivosImportados++;
            }

            if (todosDados.isEmpty()) {
                throw new IllegalArgumentException(
                        "Selecione pelo menos um arquivo CSV válido.");
            }

            focoService.substituirDados(todosDados);

            redirect.addFlashAttribute(
                    "sucesso",
                    "Importação concluída: "
                            + arquivosImportados
                            + " arquivo(s), "
                            + todosDados.size()
                            + " registros."
            );

        } catch (Exception e) {
            redirect.addFlashAttribute("erro", e.getMessage());
        }

        return "redirect:/importar";
    }

    @GetMapping("/comparar")
    public String comparar(
            @RequestParam(defaultValue = "DATA")
            CriterioOrdenacao criterio,
            Model model) {

        model.addAttribute("criterio", criterio);
        model.addAttribute(
                "resultados",
                focoService.comparar(criterio)
        );

        return "comparacao";
    }
}
