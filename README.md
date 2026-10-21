# APS — Análise de Focos de Incêndio

Projeto acadêmico Java 21 + Spring Boot + Thymeleaf. A interface usa um menu lateral compartilhado para navegar entre Início, Importar CSV, Ordenação, Comparação, IA — Previsão (ML), IA — Classificação (DL) e Sobre.

## Executar

Na pasta do projeto, rode:

```bash
mvn spring-boot:run
```

Acesse `http://localhost:8080/`.

## Rotas

- `/` — início
- `/importar` — importação CSV
- `/ordenacao` — ordenar e inspecionar métricas
- `/comparar` — comparar algoritmos
- `/ia/previsao` — página inicial do módulo ML (ainda não implementado)
- `/ia/classificacao` — página inicial do módulo DL (ainda não implementado)
- `/sobre` — informações do projeto

Observação: os módulos de IA são espaços de navegação/estrutura, não modelos funcionais. O desempenho é uma medição inicial e deve ser validado com testes repetidos antes de ser apresentado como benchmark científico.
"# aps-analise-algoritmos-incendios" 
