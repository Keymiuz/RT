# PRD - Precision Physical Performance Tracker

Este documento consolida os requisitos de negócio e técnicos para a construção do Tracker de Performance Física de Precisão focado em corrida. O aplicativo é projetado para múltiplos perfis com sincronização offline-first, tendo como stack Angular no Frontend e Java Spring Boot no Backend.

---

## 1. Visão Geral do Produto

O aplicativo é um rastreador de performance de corrida focado em dados precisos. Ele permite alternar entre treinos na esteira e corrida de rua, calcula automaticamente métricas de pace e calorias com base no esforço real, e oferece suporte para que duas pessoas (ex: usuário e namorada) acompanhem suas metas individuais no mesmo dispositivo.

### Premissas Principais
- **Arquitetura:** Offline-First (Write-to-Local-First).
- **Backend Autoridade:** O backend Spring Boot é a autoridade final para cálculos de performance no momento da persistência.
- **Multiperfil:** Troca de perfil local simplificada no frontend com metas estritamente individuais.
- **Otimização de 5.5km:** Atalhos de UI e filtros de benchmarking específicos para o circuito diário padrão.

---

## 2. Pilares Técnicos

### Pilar 1: O Modelo de Dados

#### Perfil do Usuário (`User` / `Profile`)
- `id` (UUID): Identificador único do usuário.
- `name` (String): Nome de exibição.
- `weightKg` (Double): Peso corporal em kg (obrigatório para cálculo de calorias).
- `targetPace` (Double): Meta de pace em min/km (formato decimal).
- `updatedAt` (LocalDateTime): Timestamp da última atualização (usado no Last Write Wins).

#### Sessão de Corrida (`Session`)
- `id` (UUID): ID gerado na persistência ou sincronização.
- `clientSideUuid` (UUID): ID único gerado no frontend usado para idempotência.
- `profileId` (UUID): Relaciona a sessão a um perfil de usuário.
- `type` (Enum: `ESTEIRA`, `RUA`): Tipo do treino.
- `durationSeconds` (Long): Duração total em segundos.
- `distanceKm` (Double): Distância total em km.
- `speedKmh` (Double): Velocidade de input da máquina (apenas no modo `ESTEIRA`).
- `calculatedSpeedKmh` (Double): Velocidade efetiva real calculada no servidor ($\text{distancia} / \text{tempo}$).
- `paceMinKm` (Double): Ritmo de corrida médio ($\text{tempo em minutos} / \text{distancia}$).
- `burnedCalories` (Double): Calorias gastas estimadas através do MET.
- `isStandardCircuit` (Boolean): Flag que indica se o circuito é o padrão de 5.5km (`distanceKm == 5.5`).
- `createdAt` (LocalDateTime): Data e hora do treino.

---

### Pilar 2: A Fronteira da API (REST Spring Boot)

Os payloads de request e response do backend usarão tipos de dados estritamente tipados e imutáveis através de **Java Records**.

#### 1. Endpoint de Cálculo de Performance (Stateless)
* **Rota:** `POST /api/performance/calculate`
* **Descrição:** Calcula as métricas de treino sem persistência em banco.
* **Request DTO (`CalculationRequest`):**
```java
public record CalculationRequest(
    @NotNull String type, // "ESTEIRA" ou "RUA"
    @NotNull @Min(1) Long durationSeconds,
    @NotNull @DecimalMin("0.01") Double distanceKm,
    @NotNull @DecimalMin("1.0") Double weightKg,
    Double speedKmh // Opcional, necessário se for ESTEIRA
) {}
```
* **Response DTO (`CalculationResponse`):**
```java
public record CalculationResponse(
    Double paceMinKm,
    Double calculatedSpeedKmh,
    Double burnedCalories,
    Boolean isStandardCircuit
) {}
```

#### 2. Endpoint de Perfis (/api/profiles)
* **POST `/api/profiles`**: Criação ou atualização (Upsert). O backend compara o `updatedAt` enviado pelo cliente. Se o recebido for mais recente que o do banco, atualiza. Caso contrário, ignora (ou retorna o dado do banco).
* **GET `/api/profiles/{id}`**: Busca de um perfil pelo seu UUID.

#### 3. Endpoint de Sessões (/api/sessions)
* **POST `/api/sessions`**: Salva um novo treino.
  * **Header Obrigatório:** `X-Client-UUID` (para verificação de idempotência).
  * **Payload:** Contém os campos do `CalculationRequest` + `clientSideUuid` + `profileId`.
  * **Comportamento:** O interceptor `ClientSideUUIDInterceptor` verifica se o UUID do header já foi processado. Se não, o backend recalcula os campos usando a lógica interna (`PerformanceCalculator`), ignorando qualquer valor pré-calculado no frontend, e persiste o registro.
* **GET `/api/sessions?profileId={id}`**: Retorna todo o histórico de treinos do usuário.

---

### Pilar 3: Regras de Negócio e Casos Limite

#### 1. Regra de Validação Fail-Fast
- Qualquer input inválido (valores `<= 0` para tempo, distância e peso) gera um **HTTP 400 Bad Request** com o código estruturado `INVALID_INPUT_PARAMETERS`.
- Utilização de `@Valid` no Spring Boot.

#### 2. Inconsistência de Velocidade na Esteira
- Velocidade efetiva calculada:
  $$\text{velocidadeEfetiva} = \frac{\text{distanceKm}}{\text{durationSeconds} / 3600.0}$$
- A tolerância aceita entre a velocidade informada da máquina (`speedKmh`) e a `velocidadeEfetiva` é de no máximo **15%**.
- Se a diferença for $\le 15\%$, o backend usa a `velocidadeEfetiva` para computar a performance.
- Se a diferença for $> 15\%$, lança `InconsistentDataException` resultando em **HTTP 422 Unprocessable Entity**.

#### 3. Cálculo Contínuo do MET (Fórmula de Regressão)
$$\text{velocidade\_m\_min} = \text{velocidadeEfetivaKmh} \times 16.6667$$
$$\text{MET} = 3.5 + (0.2 \times \text{velocidade\_m\_min})$$
$$\text{Calorias} = \frac{\text{MET} \times 3.5 \times \text{pesoKg}}{200.0} \times \left(\frac{\text{durationSeconds}}{60.0}\right)$$

#### 4. Fluxo de Sincronização Frontend (Angular)
1. **Write-to-Local-First:** Todo treino é salvo imediatamente no `IndexedDB` local através da biblioteca `Dexie.js`.
2. **Tentativa de Push:** O Angular tenta dar POST no `/api/sessions`.
3. **Fallback:** Se falhar por conexão (Timeout/5xx/etc.), a sessão local é marcada como `SYNC_PENDING` com `retry_count`.
4. **Sync Background:** O `SyncService` monitora o `navigator.onLine` e executa um polling a cada 5 minutos para processar os pendentes do IndexedDB.
5. **Idempotência:** O cabeçalho `X-Client-UUID` garante que envios repetidos devido a falhas de rede anteriores não criem duplicidades no banco de dados do backend.

---

## 4. Cronograma de Implementação (Tarefas < 300 LOC)

### Back-End (Java Spring Boot)

* **Tarefa 1: Estrutura Base, DTOs e PerformanceCalculator** (~150 LOC)
  - Criar `CalculationRequest` e `CalculationResponse` como Java Records.
  - Implementar a interface `PerformanceCalculator` e a classe `PerformanceCalculatorImpl` contendo a fórmula matemática de regressão contínua do MET, pace e a regra de flag para circuitos de 5.5km.
  - Testes unitários para validar o cálculo de calorias e pace de forma determinística.

* **Tarefa 2: Entidades e Repositórios de Banco de Dados** (~180 LOC)
  - Criar as entidades JPA `User` e `Session`.
  - Configurar banco H2/PostgreSQL e criar os repositórios JPA (`UserRepository` e `SessionRepository`).
  - Incluir validações de chaves e campos obrigatórios.

* **Tarefa 3: Serviços de Domínio e Validações de Negócio** (~200 LOC)
  - Criar o `ProfileService` (com lógica de Upsert comparando `updatedAt` para Last Write Wins).
  - Criar o `SessionService` que faz o recálculo via `PerformanceCalculator` antes de persistir, e valida a consistência de velocidade de esteira (limite de 15% ou lança `InconsistentDataException`).
  - Implementar as classes de exceção e um `@ControllerAdvice` para capturar os erros e responder adequadamente HTTP 400 (`INVALID_INPUT_PARAMETERS`) e HTTP 422 (`INCONSISTENT_TREADMILL_DATA`).

* **Tarefa 4: Controllers de API e Interceptor de Idempotência** (~200 LOC)
  - Criar `PerformanceController`, `ProfileController` e `SessionController`.
  - Implementar o `ClientSideUUIDInterceptor` (ou HandlerInterceptor) que intercepta o cabeçalho `X-Client-UUID` do POST de sessões e impede inserções duplicadas, retornando 200 OK para requisições já processadas.

---

### Front-End (Angular + Dexie.js)

* **Tarefa 5: Configuração do Banco de Dados Local (IndexedDB)** (~120 LOC)
  - Configurar o `Dexie.js` em um arquivo de configuração de banco de dados do navegador.
  - Criar as tabelas locais `profiles` e `sessions` espelhando os modelos do backend, adicionando campos de sincronização local (`syncStatus`, `retryCount`).

* **Tarefa 6: Serviços Angular de Comunicação (API e Sync)** (~220 LOC)
  - Criar os serviços HTTP injetáveis para comunicação com o backend (`ProfileApiService`, `SessionApiService`, `PerformanceApiService`).
  - Implementar o `SyncService` que executa a varredura do banco IndexedDB buscando registros `SYNC_PENDING`, reenvia as sessões usando o UUID local e trata o avanço/resete de tentativas.

* **Tarefa 7: Telas de Gerenciamento de Perfil e Toggle de Usuário** (~250 LOC)
  - Interface do usuário para cadastrar/editar os dois perfis locais (peso, nome, meta).
  - Toggle de alteração rápida de usuário ativo, modificando o estado no frontend e atualizando as visualizações.

* **Tarefa 8: Telas de Registro de Treino (Esteira/Rua) e Benchmarking de 5.5km** (~280 LOC)
  - Implementação do cronômetro ativo para o modo Rua.
  - Formulário para inserção de dados de máquina para o modo Esteira.
  - Botão de atalho rápido "Quick-Log 5.5km".
  - Componente de histórico e gráficos simples filtrando pelo benchmark `is_standard_circuit`.
