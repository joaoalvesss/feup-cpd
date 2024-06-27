# Avaliação de performance com single-core e multi-core

O projeto proposto serve para mostrar o efeito na performance do processador devido à hierarquia da memória quando é necessário obter acesso a grande quantidades de informação. Para além disto, o projeto também serve para mostrar que um processo pode ser realizado mais rapidamente usando vários threads do processador, mas nem sempre compensa devido aos recursos gastos em relação à diminuição do tempo proporcionada.

## Descrição do Problema

A nossa tarefa neste projeto é utilizar o produto de duas matrizes (que por natureza exige muitos recursos computacionais para realizar) para estudar os efeitos de aceder a grandes quantidades de entradas de dados na performance do processador.

### Explicação dos Algoritmos

<p> Para este trabalho, desenvolvemos 5 algoritmos diferentes para resolver o problema de multiplicação entre matrizes. Os 3 primeiros apresentados, que diferem na manipulação de memória, têm como objetivo medir a performance de um único thread do processador face a grandes grupos de dados. 
<p> Os 2 últimos algoritmos têm como objetivo medir a performance de vários threads do processador aquando da execução de tarefas para um mesmo processo em paralelo, sendo que este mesmo processo requer uma grande quantidade de dados. Estes 2 últimos têm apenas uma mudança que é onde é que os processos irão ser separados para serem executados em paralelo.


### **Multiplicação Simples**

<p> Para o primeiro algoritmo era-nos dado no enunciado código em C/C++, com complexidade temporal O(n^3), para multiplicar duas matrizes multiplicando sequencialmente cada linha da primeira matriz por cada coluna da segunda.

**Pseudo código:**

```c++
for(int i=0; i<m_ar; i++)
     for(int k=0; k<m_ar; k++)
          temp = 0;
          for(int j=0; j<m_br; j++)
               temp += pha[i*m_ar+k] * phb[k*m_br+j];
          phc[i*m_ar+j]=temp;
```

### **Multiplicação por Linhas**

<p> No segundo algoritmo desenvolvemos código (com a mesma complexidade temporal) que multiplica cada elemento da primeira matriz pela linha da segunda matriz que lhe corresponde e vai construindo a matriz resultante. Com este algoritmo o tempo de execução foi bastante mais reduzido do que quando comparado à execução do algoritmo anterior.

**Pseudo código:**

```c++
for(int i=0; i<m_ar; i++)
     for(int k=0; k<m_ar; k++)
          for(int j=0; j<m_br; j++) 
               phc[i*m_ar+j]+= pha[i*m_ar+k] * phb[k*m_br+j];
```

### **Multiplicação por Blocos**

<p> Finalmente o terceiro método consiste em dividir as matrizes em matrizes mais pequenas, realizar o cálculo nessas matrizes e depois somar tudo para obter o resultado final. Embora a complexidade temporal se mantenha em O(n<sup>3</sup>), a ideia é que existirá mais aproveitamento das caches, limitando o acesso a memórias mais lentas.

**Pseudo código:**

```c++
for(i = 0; i < m_ar; i += blockSize)
     for(j = 0; j < m_ar; j += blockSize)
          for(k = 0; k < m_br; k += blockSize)
               for (x = i; x < i + blockSize; x++) 
                    for (y = j; y < j + blockSize; y++)
                         for (z = k; z < k + blockSize; z++)
                              phc[x*m_ar+z] += pha[x*m_ar+y] * phb[y*m_br+z];
```

### **Multiplicação por Linhas em Paralelo**
     
Neste código, a tag ```#pragma omp parallel for``` é aplicada apenas ao loop mais externo (o loop que itera sobre i). Esta diretiva diz ao compilador para executar este ciclo em paralelo, distribuindo as iterações do ciclo i por diferentes threads. Os loops internos são executados totalmente por cada thread que executa uma parte do loop i. Esta abordagem é simples, mas pode nem sempre levar ao melhor desempenho, dependendo da carga de trabalho dentro do loop.

**Pseudo código:**

 ```c++
#pragma omp parallel for 
for (int i=0; i<n; i++)      
     for (int k=0; k<n; k++)            
          for (int j=0; j<n; j++)            
               {             } 
```

Esta abordagem envolve a tag ```#pragma omp parallel``` aplicada ao bloco for mais externo e uma diretiva ```#pragma omp for``` aplicada apenas ao for loop mais interno. Este método tenta aproveitar o paralelismo em dois níveis: o paralelismo entre as linhas da matriz é explorado pelo loop externo, onde diferentes threads podem trabalhar em diferentes linhas simultaneamente; e o paralelismo dentro de uma linha específica da matriz é explorado pelo paralelismo no loop mais interno, permitindo que múltiplos threads colaborem no cálculo de uma única linha.

**Pseudo código:**
     
```c++
#pragma omp parallel
for (int i=0; i<n; i++)      
     for (int k=0; k<n; k++)
          #pragma omp for             
          for (int j=0; j<n; j++)            
               {             } 
```

## **Metricas de Performance**

Para avaliar a performance dos algoritmos, utilizamos a API de desempenho (PAPI) para medir a atividade da CPU e a utilização da memória cache com precisão, com intuito de avaliar o desempenho dos algoritmos em C/C++. Como têm um efeito na eficiência, as principais medidas foram os erros de cache nos níveis L1 e L2, o tempo de execução e as operações de ponto flutuante, para os casos de execução paralela. As medições foram efectuadas numa única máquina da FEUP com sistema Ubuntu 22.04, com um CPU i7 9700 e foram obtidas diretamente, sem quaisquer aproximações. A fiabilidade dos dados foi garantida executando os testes pelo menos 2 vezes para os casos de algoritmos em C/C++ e calculando a média dos resultados para reduzir as oscilações. Para evitar a sobreposição de alocação de memória, um novo processo foi iniciado para cada teste. Foi usada a *flag* de otimização -O2  para compilar os programas C/C++, tal como pedido. A avaliação de performance também contrastou esses resultados com os mesmos algoritmos implementados em Python.

## **Resultados e Análises**

### **Multiplicação Simples**

Observa-se que o código em python demora consideravelmente mais tempo que em c++, especialmente com tamanhos de matriz pequenos, onde fica duas ordens de grandeza acima do C++, devido à baixa proximidade que tem ao *hardware* em relação ao código de c++. Observa-se também que à medida que aumenta o tamanho da matriz, a diferença de tempo entre as duas linguagens diminui consideravelmente, sendo de apenas uma ordem de grandeza quando as matrizes são de 3000x3000, ainda que o código em python se mantenha consideravelmente mais lento que o código em c++. Deste modo, é impensável usar a implementação em python devido ao seu tempo de execução para tarefas relativamente pequenas. A única maneira de otimizar o código python seria usar a biblioteca do `numpy`, o que basicamente iria converter este código para c, otimizando-o assim.

![Simple_1](Simple_1.png)

### **Multiplicação por Linhas**

Observamos que as duas linguagens diferem bastante no tempo de execucão, sendo que em Python este é exponencialmente maior. Ao comparar os dois algoritmos (Multiplicação Simples e Multiplicação por Linhas), observamos que em C++ este último é mais eficiente, o que é mais evidente quanto maior o tamanho da matriz (sendo que a 3000x3000, o primeiro algoritmo demora cerca de 10 vezes mais). No entanto, esta relação não se verifica em Python, na qual os dois algoritmos tem performances semelhantes (sendo o segundo ligeiramente menos eficiente). 
Esta diferença de execução temporal marcada entre as duas linguagens deve-se a estas seguirem paradigmas diferentes, sendo que C++ é compilada e python é interpretada. Isto leva a que o que python seja uma linguagem de nível mais alto, diminuindo assim a proximidade com o hardware, o que leva, consequentemente, a que as operações e acessos envolvendo todo o tipo de memórias sejam consideravelmente mais lentas quando comparado com uma linguagem como C++.
A eficiência superior do código que faz operações de multiplicação linha por linha, comparativamente ao código de multiplicação simples, pode ser atribuída à redução significativa nas falhas de cache durante a execução. Essa melhoria ocorre porque, na multiplicação simples, que processa os elementos linha por coluna, quase todas as operações resultam em falhas de cache. Isso deve-se ao facto de que, após uma falha de cache, apenas um pequeno bloco de dados (aproximadamente equivalente a uma linha da matriz e o número utilizado na multiplicação corrente) é puxado da memória principal. Consequentemente, na operação subsequente, o algoritmo de multiplicação simples precisa de pedir o próximo número da coluna da memória, visto que ele não está disponível na cache. Por outro lado, ao executar as multiplicações linha por linha, o algoritmo de multiplicação por linhas aproveita eficientemente o bloco de dados já presente na cache, resultado da falha anterior. Essa abordagem minimiza as operações de acesso à memória, resultando numa boa redução do tempo de execução em comparação com o algoritmo de multiplicação simples, que não otimiza o uso do cache de forma tão eficaz.

![Simple_Line](Simple_Line.png)
![py_c++_time](py_c++_time.png)


### **Multiplicação por Blocos**

Ao comparar o tempo demorado pelo algoritmo anterior com várias instâncias da Multiplicação por Blocos (blocos de 128, 256 e 512 respetivamente), observa-se que tendencialmente este último é mais rápido, embora não por uma diferença muito significativa. Também se nota uma tendência de mais rapidez com blocos mais pequenos, no entanto mais uma vez a inconsistência dos dados obtidos ao nivel dos tempos não nos permita tirar as conclusões que deveriam ser oberváveis. Isto deve-se, muito provavelmente, a uma implementação mal otimizada da ideia do algoritmo da multiplicação por blocos.

Ao analisar os dados de falhas de cache dos dois métodos, notamos que no que toca à cache L1, a Multiplicação por Linhas excede largamente o número de falhas de cache dos três métodos por blocos. No entanto, nas falhas de cache em L2, constatamos o inverso: a multiplicação por linhas tem menos falhas de cache que a multiplicação por blocos. Isto acontece porque quando existe uma falha de um valor em cache, o método dos blocos vai buscar uma grande quantidade de dados sequencialmente à memória, dados estes que irão ser usados em operações num futuro não muito distante ao momento em que a execuação se encontra naquele momento. Deste modo, o número de vezes que a execução do código tem que recorrer à memória principal é reduzido, o que faria com o tempo de execução fosse inferior do que quando comparado ao método de multiplicação por linhas (o que infelizmente não se constatou nos dados que obtemos).

Com o aumentar do tamanho dos blocos, a média das falhas de cache diminuiu um pouco, o que mostra que se cada vez que a execução programa precisar de ir à memória principal por falta de dados, para os colocar em cache, compensa ir buscar mais informação de uma vez só, pois essa informação que o bloco contêm irá, provavelmente, ser usada quase de seguida para efetuar mais operações. Com a diminuição das idas à memória para buscar dados, seria de esperar que o tempo de execução também seguisse a mesma tendência porque as operações mais demoradas, que são as idas à memória, foram reduzidas mas, infelizmente, não pudemos concluir isso através da nossa recolha de dados provalvelmente, devido à má otimização na implementação do algoritmo.

![time2-3](time2-3.png)

![cache2-3](cache2-3.png)


## **Multiplicação por Linhas em Paralelo**

Na análise dos dados obtidos a partir de 3 medições para cada caso, identificamos diferenças significativas entre os dois algoritmos de multiplicação de matrizes em paralelo.

O primeiro algoritmo, caracterizado pela utilização de 1 única tag pragma, demonstrou ser superior em diversos aspectos, incluindo tempos de execução, número de operações de ponto flutuante por segundo (flops) e ganho de desempenho (speedup), em comparação ao segundo algoritmo, que se distinguia pela aplicação de 2 tags pragma. No entanto, observou-se que, apesar de sua superioridade evidente, o primeiro algoritmo começou a apresentar uma utilização menos eficiente dos recursos disponíveis conforme o tamanho das matrizes aumentava.

Comparando com o algoritmo de multiplicação de matrizes sem paralelização, o primeiro algoritmo paralelizado justifica amplamente o uso de maiores recursos computacionais, oferecendo uma solução significativamente mais rápida para o problema. Por outro lado, o segundo algoritmo paralelizado não demonstra uma justificativa clara para o uso intensivo de recursos computacionais, visto que seus resultados não superam substancialmente a versão que funciona com um único thread.

Em termos de desempenho, a escolha da melhor abordagem depende um pouco da arquitetura do sistema e do tamanho da matriz a ser processada. Se o sistema tiver uma grande quantidade de memória cache e for capaz de lidar eficientemente com acessos à memória não sequenciais, o primeiro algoritmo paralelo é preferível. No entanto, se o sistema tiver uma quantidade limitada de memória cache ou se o tamanho da matriz for muito grande, a segunda abordagem pode ser mais eficiente, pois pode resultar em menos falhas de cache. Ainda assim, para matrizes demasiado grandes a implementação do algoritmo devia ser diferente das 2 opções apresentadas, de forma a que fosse possível fazer um melhor uso das memórias cache e assim reduzir ainda mais o tempo de execução.

Além disso, enfrentamos um problema inicialmente com o segundo algoritmo: embora executasse normalmente, não realizava as multiplicações matriciais esperadas, resultando numa execução infinita. A causa do problema foi identificada como a omissão da declaração `int` nos ciclos `for`, o que impedia que as variáveis dos ciclos fossem tratadas como locais. Assim, quando as tarefas eram distribuídas paralelamente entre os diversos processos, a ausência de variáveis locais significava que o fragmento de código executado em cada processo não tinha acesso às variáveis necessárias para realizar as multiplicações matriciais, levando assim a que a execução do código não acabasse. Este problema que encontramos destaca a importância de uma verificação cuidadosa das implementações em ambientes de computação paralela, onde detalhes simples, como a declaração de variáveis, podem ter impactos significativos na execução dos algoritmos.

Inicialmente, a expectativa era de que a versão do algoritmo com duas tags `#pragma` superasse a versão com uma única tag em termos de desempenho, devido à sua capacidade de distribuir um maior número de tarefas simultaneamente. No entanto, esta hipótese não se confirmou nas medições de flops e de speedup, o que resultou em uma eficiência consideravelmente reduzida. Uma possível explicação para isto pode ser a excessiva paralelização. Isso pode ter levado a uma situação em que tarefas foram atribuídas a threads que já se encontravam sobrecarregadas com outras tarefas. Curiosamente, mesmo que essas threads não necessitassem realizar novos acessos à memória - graças à disponibilidade dos dados necessários em cache - isso não se traduziu em uma vantagem significativa. De fato, observou-se que a versão com duas diretivas `#pragma` apresentou um número menor de falhas de cache em comparação à versão com uma única diretiva. Este resultado sugere uma complexa interação entre paralelização, gestão de tarefas e eficiência no acesso à memória, reforçando a necessidade de haver equilíbrio na distribuição de tarefas entre as threads disponíveis para otimizar o desempenho do algoritmo.

<p align="center">
  <img src="paralelo1.png" alt="s1" width="30%" />
  <img src="paralelo2.png" alt="s2" width="30%" />
  <img src="Mflops.png" alt="s3" width="30%" />
</p>


## Conclusões

Este projeto abordou o impacto da hierarquia de memória na eficiência do processador, centrando-se na multiplicação de matrizes. Analisámos o desempenho de um único thread utilizando diferentes linguagens de programação e algoritmos, incluindo uma comparação entre a multiplicação básica, a multiplicação de elementos em linha e estratégias orientadas para blocos. Além disso, explorámos melhorias de desempenho multi-core através da paralelização com o OpenMP. As nossas conclusões sublinharam o papel crucial dos padrões de acesso à memória e da localidade dos dados na otimização do desempenho, especialmente para grandes conjuntos de dados. A abordagem orientada por blocos melhorou significativamente a eficiência ao reduzir as falhas de cache, enquanto as estratégias de paralelização ofereceram aumentos de velocidade consideráveis, destacando o potencial da otimização personalizada no aproveitamento de arquitecturas multi-core para tarefas computacionais.


## Anexos

### **Multiplicação Simples**

**Performance de execução de C++:**

| Tamanho    | Média Tempo    | Média L1 DCM      | Média L2 DCM      |
|------|---------|-------------|-------------|
| 600  | 0.191s  | 244.736.726 | 39.999.744  |
| 1000 | 1.198s  | 1.215.307.603| 311.245.709|
| 1400 | 3.587s  | 3.485.857.191| 1.545.743.700|
| 1800 | 18.339s | 9.076.319.032| 8.109.335.579|
| 2200 | 39.136s | 17.662.238.423|22.408.602.254|
| 2600 | 70.318s | 30.904.922.089|51.515.883.858|
| 3000 | 118.795s| 50.292.461.091|96.402.821.770|

**Performance de execução de Python:** 

| Tamanho    | Tempo      |
|------|-----------|
| 600  | 17.556s   |
| 1000 | 83.324s   |
| 1400 | 239.460s  |
| 1800 | 514.599s  |
| 2200 | 943.233s  |
| 2600 | 1583.318s |
| 3000 | 2442.989s |

### **Multiplicação por Linhas**

**Performance de execução de C++:**

| Tamanho | Média Tempo    | Média L1 DCM         | Média L2 DCM |
|------|---------|-------------|-------------|
| 600  | 0.100s  | 27.110.106  | 58.222.182  |
| 1000 | 0.490s  | 125.648.779 | 265.912.826 |
| 1400 | 1.568s  | 346.005.847 | 706.831.952 |
| 1800 | 3.404s  | 745.525.426 | 1.430.285.904|
| 2200 | 6.289s  | 2.074.596.414| 2.552.856.674|
| 2600 | 10.459s | 4.412.650.420| 4.140.165.592|
| 3000 | 16.056s | 6.780.010.541| 6.412.639.185|
| 4096 | 41.419s | 17.535.438.490|15.921.657.681|
| 6144 | 138.775s| 59.164.301.740|54.623.085.749|
| 8192 | 338.681s| 140.275.619.456|131.629.533.874|
| 10240| 664.349s| 273.678.267.951|276.741.874.746|

**Performance de execução de Python:** 

| Tamanho | Tempo    |
|---------|----------|
| 600     | 23.565s  |
| 1000    | 109.081s |
| 1400    | 299.459s |
| 1800    | 634.459s |
| 2200    | 1160.432 |
| 2600    | 1914.375 |
| 3000    | 2952.307 |

### **Multiplicação por Blocos**

**Performance de execução de C++:**

| Tamanho da Matriz   | Tamanho do Bloco | Média Tempo    | Média L1 DCM       | Média L2 DCM       |
|------|-----|---------|--------------|--------------|
| 4096 | 128 | 33.019s | 9.523.025.183| 33.266.708.687|
| 4096 | 256 | 27.396s | 9.108.233.115| 23.434.556.887|
| 4096 | 512 | 36.618s | 8.805.250.341| 19.541.892.856|
| 6144 | 128 | 109.632s| 32.633.672.553|111.654.587.868|
| 6144 | 256 | 94.360s | 30.612.006.100| 79.733.199.242|
| 6144 | 512 | 91.241s | 29.684.919.828| 68.045.175.847|
| 8192 | 128 | 271.923s| 74.142.385.559|261.527.950.412|
| 8192 | 256 | 421.233s| 71.980.446.810|167.231.243.044|
| 8192 | 512 | 353.636s| 70.763.318.666|149.009.188.872|
| 10240| 128 | 498.051s|151.095.947.827|522.515.172.707|
| 10240| 256 | 524.209s|142.411.251.441|367.465.312.747|
| 10240| 512 | 530.571s|137.105.919.242|309.608.889.254|

### **Multiplicação por Linhas em Paralelo (1)**

| Tamanho | Média Tempo | Média L1 DCM | Média L2 DCM | MFlops | Speedup | Eficiência |
|------|---------|-------------|-------------|------|------|-------|
| 600  | 0.015s | 3394996 | 7175277 | 28.800.000.000 | 6.66 | 0.823 |
| 1000 | 0.073s | 15723518 | 33008590 | 27.397.260.273 | 6.71 | 0.839 |
| 1400 | 0.236s | 43512844 | 88065607 | 23.254.237.288 | 6.64 | 0.830 | 
| 1800 | 0.533s | 93706898 | 186818343 | 21.883.677.298 | 6.39 | 0.799 |
| 2200 | 0.993s | 258920916 | 331658960 | 21.446.122.860 | 6.33 | 0.791 |
| 2600 | 1.668s | 549919452 | 551200051 | 21.074.340.527 | 6.27 | 0.784 |
| 3000 | 2.550s | 845490868 | 860381209 | 21.176.470.588 | 6.29 | 0.786 |
| 4096 | 6.929s | 2207650428 | 2152899600 | 19.835.323.058 | 5.98 | 0.748 |
| 6144 | 29.964s | 7451840398 | 7037319146 | 15.480.458.816 | 4.63 | 0.579 |
| 8192 | 78.395s | 17643728531 | 16456833256 | 14.025.277.476 | 4.32 | 0.540 | 
| 10240| 163.757s | 34461767121 | 32404246080 | 13.113.843.365 | 4.05 | 0.506 |

### **Multiplicação por Linhas em Paralelo (2)**

| Tamanho | Média Tempo | Média L1 DCM | Média L2 DCM | MFlops | Speedup | Eficiência |
|------|---------|-------------|-------------|------|------|-------|
| 600  | 0.156s | 8000064 | 32524521 | 2.769.230.769 | 1.56 | 0.195 |
| 1000 | 0.497s | 29761432 | 112621605 | 3.944.773.175 | 0.99 | 0.124 |
| 1400 | 1.450s | 69980545 | 226658387 | 3.784.827.586 | 1.08 | 0.135 |
| 1800 | 3.120s | 136022272 | 367822089 | 3.738.461.538 | 1.09 | 0.136 |
| 2200 | 5.733s | 233359101 | 576535908 | 3.714.634.571 | 1.09 | 0.136 |
| 2600 | 9.236s | 367428502 | 857736760 | 3.805.976.613 | 1.13 | 0.141 |
| 3000 | 14.513s | 547079767 | 1215354486 | 3.720.802.039 | 1.11 | 0.139 |
| 4096 | 33.281s | 1222832364 | 2325997360 | 4.129.652.158 | 1.24 | 0.155 |
| 6144 | 109.289s | 4107338308 | 7846597030 | 4.244.310.662 | 1.27 | 0.159 |
| 8192 | 257.524s | 9262658392 | 14931840772 | 4.269.550.130 | 1.32 | 0.165 |
| 10240| 494.037s | 18273020445 | 29078244477 | 4.346.807.320 | 1.35 | 0.169 |


MFLOPS = 2n^3 / TEMPO
SPEEDUP = Tsenquencial / Tparalelo
EFFICIENCY = SPEEDUP / THREADS
