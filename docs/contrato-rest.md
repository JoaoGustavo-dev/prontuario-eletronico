# Contrato REST

API do `prontuario-eletronico`. Base local: `http://localhost:8080`. Formato: JSON (UTF-8), exceto exportação (CSV) e importação (multipart).

## 1. Convenções

- Nomes de campos em camelCase.
- `LocalDate`: `aaaa-MM-dd`. `Instant`: ISO-8601 em UTC (ex.: `2026-11-01T08:00:00Z`).
- Autenticação: **HTTP Basic** em todas as chamadas, sem sessão (stateless).
- Credenciais vêm do `.env` (`AUTH_PROFISSIONAL_*` e `AUTH_LEITOR_*`). Nunca são versionadas.

## 2. Perfis e permissões

| Perfil | GET (qualquer rota) | POST (qualquer rota) |
|---|---|---|
| `PROFISSIONAL` | permitido | permitido |
| `LEITOR` | permitido | **403** |
| Sem credenciais ou inválidas | 401 | 401 |

O leitor, portanto, não altera registros (critério de aceite). Qualquer método diferente de GET exige `PROFISSIONAL`.

CORS: origens configuradas em `app.cors.origens`; métodos GET, POST e OPTIONS; cabeçalhos `Authorization` e `Content-Type`.

## 3. Formato de erro

```json
{
  "status": 400,
  "mensagem": "Dados inválidos.",
  "campos": { "cpf": "CPF deve conter 11 dígitos numéricos." }
}
```

`campos` traz o detalhe por campo apenas nos erros de validação do corpo (`@Valid`). Nos demais erros tratados pela API ele vai como `null`.

| Status | Quando |
|---|---|
| 400 | Validação do corpo, dados de domínio inválidos, arquivo CSV vazio ou com cabeçalho errado |
| 401 | Credenciais ausentes ou inválidas |
| 403 | Perfil sem permissão (leitor tentando escrever) |
| 404 | Paciente inexistente |
| 409 | CPF já cadastrado |
| 500 | Falha de criptografia (mensagem genérica; a causa fica só no log) |

Nem todo erro usa esse formato:

- **401 e 403** são devolvidos pelo Spring Security, fora do formato `ErroResposta`.
- **JSON malformado, parâmetro de caminho não numérico e campo `arquivo` ausente na importação** devolvem 400 no formato padrão do Spring, não no `ErroResposta`.

## 4. Endpoints

### 4.1 POST /pacientes (PROFISSIONAL)

Cadastra um paciente.

Corpo:

```json
{ "nome": "Maria Teste", "dataNascimento": "1980-05-10", "cpf": "88888888801" }
```

| Campo | Regra |
|---|---|
| `nome` | obrigatório, não vazio |
| `dataNascimento` | obrigatório, no passado |
| `cpf` | obrigatório, 11 dígitos numéricos |

Resposta **201**, cabeçalho `Location: /pacientes/{id}`:

```json
{ "id": 1, "nome": "Maria Teste", "dataNascimento": "1980-05-10", "cpf": "88888888801" }
```

Erros: 400 (validação), 409 (CPF duplicado).

### 4.2 GET /pacientes/{id}/historico (PROFISSIONAL ou LEITOR)

Devolve o paciente, seus registros clínicos e suas leituras de sinais (mais recentes primeiro), cada leitura com os alertas que gerou.

Resposta **200**:

```json
{
  "paciente": { "id": 1, "nome": "Maria Teste", "dataNascimento": "1980-05-10", "cpf": "88888888801" },
  "registros": [
    { "id": 3, "pacienteId": 1, "tipo": "EXAME", "descricao": "texto descritivo",
      "notaClinica": "nota já decifrada", "registradoEm": "2026-11-23T19:00:00Z" }
  ],
  "sinais": [
    { "id": 7, "pacienteId": 1, "pressaoSistolica": 190, "pressaoDiastolica": 100,
      "frequenciaCardiaca": 80, "instante": "2026-11-23T19:00:00Z",
      "alertas": [
        { "id": 5, "regra": "PRESSAO_CRITICA",
          "justificativa": "Pressão 190/100 mmHg atingiu o limite crítico (180/120 mmHg).",
          "geradoEm": "2026-11-23T19:00:01Z" }
      ] }
  ]
}
```

A nota clínica é decifrada na leitura. No banco ela fica cifrada (AES-256-GCM).

Erros: 404.

### 4.3 POST /pacientes/{id}/registros (PROFISSIONAL)

Cria um exame ou uma receita.

Corpo:

```json
{ "tipo": "EXAME", "notaClinica": "texto livre", "tipoExame": "Hemograma", "resultado": "Normal" }
```

```json
{ "tipo": "RECEITA", "notaClinica": "texto livre", "medicamento": "Losartana", "posologia": "1 comprimido por dia" }
```

| Campo | Regra |
|---|---|
| `tipo` | obrigatório: `EXAME` ou `RECEITA` (aceita minúsculas e espaços nas pontas). Outro valor: 400 com a mensagem "Tipo de registro inválido. Use EXAME ou RECEITA." |
| `notaClinica` | opcional; cifrada antes de gravar |
| `tipoExame` | obrigatório quando `tipo` é `EXAME`; vazio gera 400 "Tipo do exame é obrigatório." |
| `resultado` | opcional, usado quando `tipo` é `EXAME` |
| `medicamento` | obrigatório quando `tipo` é `RECEITA`; vazio gera 400 "Medicamento é obrigatório." |
| `posologia` | opcional, usado quando `tipo` é `RECEITA` |

Resposta **201**: objeto de registro (mesmo formato dos itens de `registros` do histórico).

Erros: 400, 404.

### 4.4 POST /pacientes/{id}/sinais (PROFISSIONAL)

Registra uma leitura de sinais vitais e aplica as regras de alerta.

Corpo:

```json
{ "pressaoSistolica": 190, "pressaoDiastolica": 100, "frequenciaCardiaca": 80, "instante": "2026-11-23T19:00:00Z" }
```

| Campo | Unidade | Regra |
|---|---|---|
| `pressaoSistolica` | mmHg | obrigatório; entre 40 e 300 e maior que a diastólica |
| `pressaoDiastolica` | mmHg | obrigatório; entre 20 e 200 |
| `frequenciaCardiaca` | bpm | obrigatório; entre 20 e 300 |
| `instante` | UTC | opcional; se omitido, a API usa o momento do registro |

Valores fora dessas faixas geram 400 com mensagem de domínio ("Pressão arterial fora da faixa plausível (mmHg)." ou "Frequência cardíaca fora da faixa plausível (bpm)."). Campo ausente gera 400 de validação, com `campos`.

Resposta **201**: leitura com a lista `alertas` gerados (vazia se nenhuma regra disparou).

Erros: 400, 404.

### 4.5 GET /alertas (PROFISSIONAL ou LEITOR)

Lista os alertas gerados (sem paginação).

Resposta **200**:

```json
[
  { "id": 5, "regra": "PRESSAO_CRITICA",
    "justificativa": "Pressão 190/100 mmHg atingiu o limite crítico (180/120 mmHg).",
    "geradoEm": "2026-11-23T19:00:01Z", "pacienteId": 1, "pacienteNome": "Maria Teste" }
]
```

`geradoEm` é o instante em que o servidor criou o alerta, e é sobre ele que se mede o critério de 5 s.

### 4.6 GET /exportacao/pacientes.csv e GET /exportacao/sinais.csv (PROFISSIONAL ou LEITOR)

Devolvem CSV (`text/csv; charset=UTF-8`, separador `;`) com `Content-Disposition: attachment`.

- `pacientes.csv`: `cpf;nome;data_nascimento`
- `sinais.csv`: `cpf;pressao_sistolica_mmhg;pressao_diastolica_mmhg;frequencia_cardiaca_bpm;instante_utc`

### 4.7 POST /importacao/pacientes e POST /importacao/sinais (PROFISSIONAL)

Corpo `multipart/form-data` com o campo **`arquivo`** (CSV UTF-8, mesmo formato da exportação). Limite padrão de upload do Spring: 1 MB.

Resposta **200** (importação parcial):

```json
{ "linhasLidas": 20, "importadas": 18,
  "erros": [ { "linha": 4, "motivo": "CPF já cadastrado." } ] }
```

`linha` é o número da linha no arquivo, contando o cabeçalho como linha 1. Linhas de sinais importadas passam pelas regras e geram alertas.

Erros: 400 para arquivo vazio ou cabeçalho diferente do esperado (nenhuma linha é importada).

## 5. Fora do contrato (melhorias futuras)

Listagem de pacientes (`GET /pacientes`), paginação de `/alertas`, endpoint de identidade do usuário, JWT, WebSocket ou SSE no lugar do polling.