# Central Inteligente de Processamento de Pedidos com IA

Sistema fullstack para recebimento e processamento de pedidos comerciais em linguagem natural, com extração automática de dados via Inteligência Artificial (OpenAI GPT-3.5) e fallback por regex quando a chave de API não está disponível.

---

## Descrição

A **Central Inteligente de Pedidos** permite que operadores submetam pedidos em texto livre (ex: _"Preciso de 10 caixas de leite integral para amanhã"_). O sistema interpreta automaticamente o texto, extrai cliente, data de entrega e lista de itens, e persiste o pedido estruturado no banco de dados. A interface React exibe o histórico de pedidos e permite o cadastro de novos.

---

## Tecnologias

| Camada     | Tecnologia                                      |
|------------|-------------------------------------------------|
| Backend    | Java 17 · Spring Boot 3.2 · Spring Data JPA · Lombok |
| Banco      | H2 (in-memory) — compatível com SQLite em produção |
| IA         | OpenAI API (`gpt-3.5-turbo`) com fallback por regex |
| Frontend   | React 19 · Vite · JavaScript (ESM)              |
| Build      | Maven 3.8+ (backend) · npm 9+ (frontend)        |

---

## Estrutura do Projeto (Monorepo)

```
central-inteligente-pedidos/
├── apps/
│   ├── backend-java/               # Spring Boot (Java 17, Maven)
│   │   ├── src/main/java/com/plathanus/backend/
│   │   │   ├── BackendApplication.java
│   │   │   ├── config/
│   │   │   │   └── GlobalExceptionHandler.java
│   │   │   ├── controller/
│   │   │   │   └── PedidoController.java
│   │   │   ├── dto/
│   │   │   │   ├── PedidoRequest.java
│   │   │   │   └── PedidoResponse.java
│   │   │   ├── entity/
│   │   │   │   ├── Item.java
│   │   │   │   └── Pedido.java
│   │   │   ├── repository/
│   │   │   │   └── PedidoRepository.java
│   │   │   └── service/
│   │   │       ├── AiParserService.java   # Integração com OpenAI + fallback
│   │   │       └── PedidoService.java
│   │   └── src/main/resources/
│   │       └── application.yml
│   └── frontend/                   # React + Vite
│       ├── src/
│       │   ├── components/
│       │   │   ├── PedidoForm.jsx
│       │   │   ├── PedidoList.jsx
│       │   │   └── PedidoCard.jsx
│       │   ├── services/
│       │   │   └── api.js
│       │   ├── App.jsx
│       │   └── main.jsx
│       └── .env.example
├── package.json                    # Scripts do monorepo (npm workspaces)
└── README.md
```

---

## Pré-requisitos

- Java 17+
- Maven 3.8+
- Node.js 18+
- npm 9+

---

## Como Rodar

### Backend (Spring Boot)

```bash
cd apps/backend-java
./mvnw spring-boot:run
```

> Ou, se o Maven estiver instalado globalmente: `mvn spring-boot:run`

O servidor estará disponível em: **http://localhost:8080**

O console do H2 (banco de dados em memória) estará em: **http://localhost:8080/h2-console**
- JDBC URL: `jdbc:h2:mem:pedidosdb`
- Usuário: `sa`
- Senha: *(vazio)*

#### Configurar a chave da OpenAI (opcional)

Exporte a variável de ambiente antes de iniciar o backend:

```bash
export OPENAI_API_KEY=sk-...
./mvnw spring-boot:run
```

Se `OPENAI_API_KEY` não estiver definida, o sistema usa automaticamente o **parser de fallback** por regex.

#### Compilar e testar o backend

```bash
cd apps/backend-java
mvn clean package   # gera o JAR
mvn test            # executa os testes
```

---

### Frontend (React + Vite)

```bash
cd apps/frontend
npm install
npm run dev
```

O aplicativo estará disponível em: **http://localhost:5173**

#### Variáveis de ambiente do frontend

Copie o arquivo de exemplo e ajuste se necessário:

```bash
cp apps/frontend/.env.example apps/frontend/.env
```

| Variável        | Padrão                    | Descrição                  |
|-----------------|---------------------------|----------------------------|
| `VITE_API_URL`  | `http://localhost:8080`   | URL base da API do backend |

#### Build de produção

```bash
cd apps/frontend
npm run build
```

---

### Rodar via scripts do monorepo

Na raiz do projeto:

```bash
npm install          # instala dependências do frontend
npm run frontend     # inicia o frontend (React + Vite)
npm run backend      # inicia o backend (Spring Boot)
```

---

## Endpoints

### `POST /pedido`

Cria um novo pedido a partir de texto livre em português.

**Request body:**
```json
{
  "texto": "Quero 10 caixas de leite integral e 5 fardos de água mineral para amanhã. Cliente: Supermercado Silva."
}
```

**Response `201 Created`:**
```json
{
  "id": 1,
  "cliente": "Supermercado Silva",
  "textoOriginal": "Quero 10 caixas de leite integral e 5 fardos de água mineral para amanhã. Cliente: Supermercado Silva.",
  "dataEntrega": "2026-05-02",
  "itens": [
    { "produto": "leite integral", "quantidade": 10, "unidade": "caixas" },
    { "produto": "água mineral", "quantidade": 5, "unidade": "fardos" }
  ],
  "createdAt": "2026-05-01T21:00:00"
}
```

---

### `GET /pedidos`

Lista todos os pedidos cadastrados.

**Response `200 OK`:** array de objetos `PedidoResponse` (mesmo formato do `POST`).

---

### `GET /pedido/{id}`

Busca um pedido pelo ID.

**Response `200 OK`:** objeto `PedidoResponse`.

**Response `404 Not Found`** se o pedido não existir.

---

## Uso de IA

### IA usada para parsing de pedidos

O `AiParserService` envia o texto do pedido à **OpenAI API** (`gpt-3.5-turbo`) com um prompt de sistema que instrui o modelo a retornar exclusivamente um JSON estruturado com `cliente`, `dataEntrega` e `itens`. A resposta é validada e mapeada para o domínio da aplicação.

Quando a chave de API não está configurada (ou a chamada falha), o sistema ativa automaticamente um **parser de fallback** baseado em expressões regulares, que detecta padrões como `"10 caixas de leite integral"` e referências temporais como `"amanhã"` e `"hoje"`.

### IA usada para auxiliar o desenvolvimento

O ChatGPT (GPT-4) foi utilizado durante o desenvolvimento para:
- Definição da arquitetura do monorepo
- Geração do esqueleto inicial de entidades e serviços Spring Boot
- Sugestão de componentes React
- Revisão de código e identificação de edge cases (ReDoS, validação de entrada)

### Prompts utilizados

#### Parsing de pedidos (enviado à API em cada requisição)

**System prompt:**
```
Você é um assistente que extrai informações estruturadas de textos de pedidos comerciais.
Retorne APENAS um JSON válido (sem markdown, sem explicações) com o seguinte formato:
{
  "cliente": "nome do cliente ou null se não informado",
  "dataEntrega": "data no formato YYYY-MM-DD ou null se não informada",
  "itens": [
    {"produto": "nome do produto", "quantidade": numero_inteiro, "unidade": "unidade de medida"}
  ]
}
Se a data de entrega for "amanhã", calcule com base na data de hoje.
Se não houver cliente mencionado, use null.
```

**User message (exemplo):**
```
Extraia as informações do seguinte pedido: Quero 10 caixas de leite integral e 5 fardos de água mineral para amanhã. Cliente: Supermercado Silva.
```

#### Prompts usados para auxiliar a arquitetura

```
Crie um projeto Spring Boot 3 com Java 17 em estrutura de monorepo.
Preciso de endpoints REST para criar e listar pedidos.
O pedido chega como texto livre e deve ser parseado por um serviço de IA.
Use H2 em memória, Spring Data JPA, Lombok e Bean Validation.
```

#### Prompts usados para o frontend

```
Crie um app React com Vite que consome uma API REST em http://localhost:8080.
Deve ter um formulário para digitar o texto do pedido, enviar via POST /pedido
e exibir a lista de pedidos retornada pelo GET /pedidos.
Use fetch nativo (sem axios). Mostre os itens do pedido em cada card.
```

### Validação das respostas da IA

- O modelo é configurado com `"response_format": {"type": "json_object"}` para forçar saída JSON.
- A resposta é desserializada via `ObjectMapper` do Jackson; qualquer erro de parsing lança exceção e ativa o fallback.
- Valores nulos ou ausentes (`cliente`, `dataEntrega`, `unidade`) são tratados defensivamente com valores padrão.
- Campos numéricos com valor ausente assumem quantidade `1`.

---

## Validação

| Tipo                   | Descrição                                                                                       |
|------------------------|-------------------------------------------------------------------------------------------------|
| **Testes manuais**     | Pedidos enviados via curl, Postman e pelo formulário React, validando respostas e persistência. |
| **Validação JSON**     | `@Valid` + Bean Validation no DTO de entrada; campo `texto` é obrigatório e não pode ser vazio. |
| **Fallback**           | Testado sem `OPENAI_API_KEY` configurada — parser regex extrai itens corretamente.              |
| **Revisão de código**  | Tratamento defensivo de nulos, input capeado em 2.000 caracteres para prevenir ReDoS.           |
| **Testes unitários**   | Estrutura de testes Spring Boot (`mvn test`) — base pronta para expansão com JUnit/Mockito.     |

---

## Decisões Técnicas

| Decisão                        | Justificativa                                                                                              |
|--------------------------------|------------------------------------------------------------------------------------------------------------|
| **Spring Boot**                | Ecossistema maduro, configuração mínima para REST + JPA, ampla documentação e suporte da comunidade.       |
| **Camada isolada de IA**       | `AiParserService` encapsula toda lógica de IA, facilitando troca de provedor ou modelo sem alterar o domínio. |
| **Monorepo**                   | Frontend e backend versionados juntos, facilitando revisões, deploys e onboarding de novos devs.           |
| **Fallback sem IA**            | Garante que o sistema funcione mesmo sem chave de API ou em ambiente offline, aumentando a resiliência.     |
| **H2 in-memory**               | Elimina dependência de banco externo em desenvolvimento; a camada JPA permite migrar para PostgreSQL/SQLite facilmente. |
| **`response_format: json_object`** | Instrui o modelo a retornar JSON puro, evitando markdown e texto livre que dificultariam o parse.      |

---

## Melhorias Futuras

- [ ] **Autenticação e autorização** — JWT ou OAuth2 para proteger os endpoints.
- [ ] **Testes automatizados** — suíte completa com JUnit 5, Mockito (backend) e Vitest/Testing Library (frontend).
- [ ] **Deploy em produção** — Dockerfile, CI/CD com GitHub Actions, deploy em Railway, Render ou AWS.
- [ ] **Persistência robusta** — migrar de H2 para PostgreSQL ou SQLite com Flyway para versionamento de schema.
- [ ] **NLP mais robusto** — uso de modelos mais avançados (GPT-4o, Claude) ou fine-tuning para domínio de pedidos.
- [ ] **Histórico e filtros** — paginação, filtro por cliente/data, e exportação em CSV/PDF.
- [ ] **Websockets / notificações** — atualização em tempo real da lista de pedidos no frontend.