# Otimizar desempenho das ordenações e requisições da aplicação

## Objetivo

Melhorar o desempenho da aplicação sem alterar seu funcionamento, os cinco algoritmos de ordenação ou as métricas exibidas na interface.

## Problemas identificados

### 1. Cópias desnecessárias da base

A lista de focos era copiada em praticamente toda requisição. Além disso, `CopyOnWriteArrayList` possui custo desnecessário neste caso, pois os dados são substituídos de uma só vez durante a importação e não registro por registro.

### 2. Ordenação repetida

Na rota `/ordenacao`, o algoritmo é executado novamente quando o usuário apenas troca de página. A rota `/comparar` também recalcula os cinco algoritmos sempre que é acessada, mesmo quando os dados e o critério não mudaram.

### 3. Normalização repetida de textos

Durante a ordenação por município ou bioma, a aplicação recalcula várias vezes a remoção de acentos e a conversão para letras minúsculas.

Em algoritmos O(n²), como Bubble Sort e Selection Sort, essa operação pode ocorrer milhões de vezes.

### 4. Estatísticas calculadas em várias passagens

A página inicial percorre a lista diversas vezes para obter os totais de 2023, 2024, biomas e municípios.

## Exemplo do impacto

Considerando uma base com 10.000 registros:

- Bubble Sort e Selection Sort podem realizar aproximadamente 50 milhões de comparações no pior caso.
- Se cada comparação normalizar duas strings, o custo de CPU e a criação de objetos aumentam consideravelmente.
- Com as chaves normalizadas previamente, as comparações reutilizam valores já preparados.
- Ao navegar da página 1 para a página 2, a aplicação pode reutilizar a ordenação pronta em vez de ordenar novamente todos os registros.

## Otimizações propostas

- Manter um snapshot imutável da lista importada.
- Remover o uso desnecessário de `CopyOnWriteArrayList`.
- Criar cache dos resultados por combinação de algoritmo e critério.
- Invalidar o cache sempre que uma nova importação substituir os dados.
- Pré-calcular as chaves normalizadas de município e bioma.
- Calcular os indicadores da página inicial em uma única passagem pela lista.
- Preservar os cinco algoritmos acadêmicos e suas métricas.

## Estimativa de melhoria

Os valores abaixo são estimativas e devem ser confirmados com benchmark usando os CSVs reais:

| Cenário | Melhoria provável |
|---|---:|
| Acesso repetido à mesma ordenação | 80% a 99% menos tempo |
| Navegação entre páginas | 80% a 99% menos tempo |
| Ordenação por município ou bioma | 20% a 60% menos tempo |
| Carregamento da página inicial | 2x a 4x menos trabalho de CPU |
| Comparação dos algoritmos | Maior ganho nos algoritmos O(n²) |

### Exemplo de requisição

Sem cache:

```text
GET /ordenacao?criterio=MUNICIPIO&algoritmo=MERGE&pagina=2
-> carrega os dados
-> copia a lista
-> normaliza os textos repetidamente
-> executa a ordenação
-> separa a página 2
```

Com as otimizações:

```text
GET /ordenacao?criterio=MUNICIPIO&algoritmo=MERGE&pagina=2
-> reutiliza a lista carregada
-> reutiliza a ordenação em cache
-> separa diretamente a página 2
```

## Critérios de aceite

- [ ] As rotas existentes continuam funcionando.
- [ ] Os cinco algoritmos permanecem disponíveis.
- [ ] Os registros ordenados continuam iguais aos resultados atuais.
- [ ] As métricas de comparações, trocas e movimentações continuam corretas.
- [ ] Uma nova importação invalida os resultados armazenados em cache.
- [ ] A troca de página não executa novamente uma ordenação já calculada.
- [ ] Criar benchmark com as bases de 2023 e 2024.
- [ ] Comparar o tempo médio antes e depois das alterações.
- [ ] Executar `mvn test` e validar os testes existentes.

## Observação

Os percentuais são estimativas. O ganho real depende do número de registros, do algoritmo utilizado, do hardware, da JVM e do tempo de renderização do Thymeleaf.

O cache deve ser invalidado sempre que os dados forem substituídos, para evitar que a aplicação exiba resultados de uma importação anterior.
