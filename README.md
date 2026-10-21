# APS — Análise de Focos de Incêndio

Projeto acadêmico desenvolvido em **Java 21, Spring Boot, Thymeleaf, HTML e CSS** para aplicar e comparar algoritmos de ordenação utilizando dados de focos de incêndio registrados em Rondônia, nos anos de 2023 e 2024.

A aplicação possui uma interface web com menu lateral compartilhado, permitindo navegar entre as funcionalidades do sistema.

## Funcionalidades

- **Início:** apresentação do projeto e acesso às funcionalidades.
- **Importação CSV:** carregamento dos dados de focos de incêndio de 2023 e 2024.
- **Ordenação:** organização e visualização dos registros por critérios selecionados.
- **Comparação de algoritmos:** análise do desempenho dos algoritmos de ordenação implementados.
- **Métricas de desempenho:** medição de tempo de execução, comparações, trocas e movimentações de dados.
- **Previsão com Machine Learning (ML):** módulo de previsão da quantidade de focos de incêndio.
- **Sobre:** informações gerais sobre o projeto.

## Algoritmos de ordenação

O projeto implementa e compara cinco algoritmos:

- Bubble Sort
- Selection Sort
- Insertion Sort
- Merge Sort
- Quick Sort

A comparação considera métricas de execução e a complexidade teórica de cada algoritmo, permitindo observar diferenças de desempenho na organização dos dados.

## Dados utilizados

Foram anexados ao repositório os arquivos CSV com os dados dos focos de incêndio referentes aos anos de **2023 e 2024**, utilizados na análise desenvolvida para o estado de Rondônia.

Os arquivos permitem importar os registros para a aplicação e realizar testes com os algoritmos implementados.

## Tecnologias utilizadas

- Java 21
- Spring Boot
- Maven
- Thymeleaf
- HTML5 e CSS3
- CSV para armazenamento e importação dos dados
- Machine Learning para previsão

## Como executar

1. Instale o JDK 21 e o Maven.
2. Clone este repositório e entre na pasta do projeto.
3. Execute o comando:

   ```bash
   mvn spring-boot:run
   ```

4. Acesse a aplicação pelo navegador:

   `http://localhost:8080/`

## Rotas da aplicação

| Rota | Funcionalidade |
|---|---|
| `/` | Página inicial |
| `/importar` | Importação de arquivos CSV |
| `/ordenacao` | Ordenação e métricas |
| `/comparar` | Comparação dos algoritmos |
| `/ia/previsao` | Módulo de previsão com ML |
| `/sobre` | Informações do projeto |

## Objetivo acadêmico

A APS tem como objetivo aplicar os conceitos de Estrutura de Dados e análise de algoritmos em um problema real, utilizando dados de focos de incêndio para avaliar estratégias de ordenação e comparar seus custos computacionais.
